package dev.talhabayhan.lexi.tools.contentcompiler

import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.Level
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

/**
 * `content/` (+ optional external exam-pack directory) → validated [CompiledContent] → `content.db`.
 * Layout and rules: `docs/CONTENT.md` §1–§3 and §7.
 */
object ContentCompiler {
    /** ⚙ A shipped level needs at least this many cards for every letter (`CONTENT.md` §3). */
    const val MIN_CARDS_PER_LETTER = 3

    /** ⚙ Below this a shipped level gets a coverage warning. */
    const val RECOMMENDED_CARDS_PER_LETTER = 5

    private val CATEGORY_COLUMNS = listOf("id", "name_tr", "name_en", "levels", "sort", "is_pro")
    private val PACK_COLUMNS = listOf("id", "name", "description", "is_pro", "sort")
    private val PACK_META_COLUMNS = listOf(
        "id",
        "title_tr",
        "title_en",
        "description_tr",
        "target_level_range",
        "card_target",
        "free_preview_letters",
    )
    private val UNIT_COLUMNS = listOf("card_id", "unit")

    fun parseLevelRange(range: String): Pair<Level, Level>? {
        val parts = range.split('-')
        if (parts.size != 2) return null
        val start = Level.entries.firstOrNull { it.name == parts[0] } ?: return null
        val end = Level.entries.firstOrNull { it.name == parts[1] } ?: return null
        if (start.ordinal > end.ordinal) return null
        return start to end
    }

    /** Default location for the temporary DB file, relative to the working directory (repo root via Gradle). */
    val DEFAULT_TEMP_DIR: Path = Path("build/content-compiler")

    fun compile(
        contentDir: Path,
        packsDir: Path?,
        output: Path,
        builtAt: String?,
        tempDir: Path = DEFAULT_TEMP_DIR,
    ): CompileResult {
        val validation = validate(contentDir, packsDir)
        if (validation.errors.isNotEmpty()) return CompileResult(validation, written = false)
        ContentDbWriter.write(validation.content, output, builtAt, tempDir)
        return CompileResult(validation, written = true)
    }

    fun validate(contentDir: Path, packsDir: Path?): ValidationResult {
        val diagnostics = Diagnostics()
        val metaFile = contentDir.resolve("meta.json")
        val meta = readMeta(metaFile, diagnostics)
        val categories = readCategories(contentDir.resolve("categories.csv"), diagnostics)
        val categoryById = categories.associateBy { it.id }
        val blocklistFile = contentDir.resolve("blocklist.txt")
        val blocklist = if (blocklistFile.isRegularFile()) {
            Blocklist.load(blocklistFile, diagnostics)
        } else {
            diagnostics.warning(blocklistFile, null, "no blocklist; profanity check skipped")
            Blocklist.EMPTY
        }

        val seenIds = mutableSetOf<String>()
        fun readCards(cardsDir: Path, fromPack: Boolean): List<CardEntry> = cardFiles(cardsDir, diagnostics)
            .flatMap { (level, file) ->
                val rows = CsvFiles.readTable(file, CardRules.COLUMNS, diagnostics).orEmpty()
                rows.mapNotNull { row ->
                    seenIds += row["id"]
                    CardRules.parse(row, level, fromPack, categoryById, blocklist, diagnostics)
                }
            }

        val cardsDir = contentDir.resolve("cards")
        if (!cardsDir.isDirectory()) diagnostics.error(cardsDir, null, "cards directory is missing")
        val baseCards = if (cardsDir.isDirectory()) readCards(cardsDir, fromPack = false) else emptyList()

        var packs = emptyList<PackEntry>()
        var packCardRows = emptyList<CsvRow>()
        var packCards = emptyList<CardEntry>()
        if (packsDir != null) {
            if (!packsDir.isDirectory()) {
                diagnostics.error(packsDir, null, "exam pack directory does not exist")
            } else {
                packs = readPacks(packsDir.resolve("packs.csv"), packsDir.resolve("packs-meta.csv"), diagnostics)
                val extraCards = packsDir.resolve("cards")
                if (extraCards.isDirectory()) packCards = readCards(extraCards, fromPack = true)
                packCardRows = readUnitRows(packsDir, packs, diagnostics)
            }
        }

        val cards = baseCards + packCards
        val cardsById = cards.associateBy { it.id }
        checkDuplicates(cards, diagnostics)
        checkExampleLeaks(cards, diagnostics)
        val packMembership = checkPackMembership(packs, packCardRows, packCards, seenIds, diagnostics)
        checkCoverage(baseCards, meta.shippedLevels, meta.smokeLevels, metaFile, diagnostics)
        val effectiveShippedPacks = meta.shippedPacks
            ?: (if (meta.shippedLevels.isEmpty()) emptySet() else packs.map { it.id }.toSet())
        checkPackCoverage(packs, packMembership, cardsById, effectiveShippedPacks, packsDir, diagnostics)
        checkPackDuplicateAnswers(packs, packMembership, cardsById, diagnostics)

        val content = CompiledContent(
            contentVersion = meta.contentVersion,
            shippedLevels = meta.shippedLevels,
            smokeLevels = meta.smokeLevels,
            categories = categories.sortedWith(compareBy({ it.sort }, { it.id })),
            cards = cards.sortedWith(compareBy({ it.level }, { it.id })),
            packs = packs.sortedWith(compareBy({ it.sort }, { it.id })),
            packCards = packMembership,
        )
        return ValidationResult(content, diagnostics.all)
    }

    private data class Meta(
        val contentVersion: Int,
        val shippedLevels: Set<Level>,
        val smokeLevels: Set<Level>,
        val shippedPacks: Set<String>?,
    )

    private fun readMeta(file: Path, diagnostics: Diagnostics): Meta {
        if (!file.isRegularFile()) {
            diagnostics.error(file, null, "meta.json is missing")
            return Meta(0, emptySet(), emptySet(), null)
        }
        val text = CsvFiles.readUtf8(file, diagnostics) ?: return Meta(0, emptySet(), emptySet(), null)
        val root = runCatching { Json.parseToJsonElement(text).jsonObject }.getOrElse {
            diagnostics.error(file, null, "not a JSON object: ${it.message?.lineSequence()?.first()}")
            return Meta(0, emptySet(), emptySet(), null)
        }
        val version = (root["contentVersion"] as? JsonPrimitive)?.takeIf { !it.isString }?.intOrNull
        if (version == null || version < 1) diagnostics.error(file, null, "contentVersion must be a positive integer")

        val shipped = readLevelList(root, "shippedLevels", file, diagnostics)
        val smoke = readLevelList(root, "smokeLevels", file, diagnostics)
        val shippedPacks = readPackList(root, "shippedPacks", file, diagnostics)
        (shipped intersect smoke).forEach {
            diagnostics.error(file, null, "${it.name} cannot be both in shippedLevels and smokeLevels")
        }
        return Meta(version ?: 0, shipped, smoke, shippedPacks)
    }

    private fun readPackList(root: JsonObject, key: String, file: Path, diagnostics: Diagnostics): Set<String>? =
        when (val list = root[key]) {
            null -> null

            is JsonArray -> {
                val packs = mutableSetOf<String>()
                list.forEach { element ->
                    val name = (element as? JsonPrimitive)?.takeIf { it.isString }?.content
                    if (name == null) {
                        diagnostics.error(file, null, "$key: expected pack id string, got $element")
                    } else {
                        packs += name
                    }
                }
                packs
            }

            else -> {
                diagnostics.error(file, null, "$key must be an array of pack id strings")
                emptySet()
            }
        }

    private fun readLevelList(root: JsonObject, key: String, file: Path, diagnostics: Diagnostics): Set<Level> {
        val levels = mutableSetOf<Level>()
        when (val list = root[key]) {
            null -> Unit

            is JsonArray -> list.forEach { element ->
                val name = (element as? JsonPrimitive)?.takeIf { it.isString }?.content
                val level = Level.entries.firstOrNull { it.name == name }
                if (level == null) {
                    diagnostics.error(file, null, "$key: unknown level $element (use ${Level.entries.joinToString()})")
                } else {
                    levels += level
                }
            }

            else -> diagnostics.error(file, null, "$key must be an array of level names")
        }
        return levels
    }

    private fun readCategories(file: Path, diagnostics: Diagnostics): List<CategoryEntry> {
        if (!file.isRegularFile()) {
            diagnostics.error(file, null, "categories.csv is missing")
            return emptyList()
        }
        val rows = CsvFiles.readTable(file, CATEGORY_COLUMNS, diagnostics) ?: return emptyList()
        val seen = mutableSetOf<String>()
        val categories = rows.mapNotNull { row ->
            var ok = true
            fun error(message: String) {
                ok = false
                diagnostics.error(file, row.line, message)
            }
            val id = row["id"]
            if (!CardRules.SLUG.matches(id)) error("category id '$id' must be a lowercase slug")
            if (!seen.add(id)) error("duplicate category id '$id'")
            if (row["name_tr"].isEmpty()) error("name_tr must not be empty")
            if (row["name_en"].isEmpty()) error("name_en must not be empty")
            val levels = row["levels"].split('|').mapNotNull { name ->
                Level.entries.firstOrNull { it.name == name }.also {
                    if (it ==
                        null
                    ) {
                        error("level '$name' is not a known level")
                    }
                }
            }
            val sort = row["sort"].toIntOrNull()
            if (sort == null) error("sort '${row["sort"]}' must be an integer")
            val isPro = parseFlag(row["is_pro"])
            if (isPro == null) error("is_pro '${row["is_pro"]}' must be 0 or 1")
            if (!ok || sort == null || isPro == null) return@mapNotNull null
            CategoryEntry(id, row["name_tr"], row["name_en"], levels, sort, isPro)
        }
        if (seen.none { it == CategoryId.MIXED.value }) {
            diagnostics.error(file, null, "the virtual 'mixed' category is missing")
        }
        return categories
    }

    /** (level, CSV file) pairs from `cards/<LEVEL>/`; anything else under `cards/` is reported. */
    private fun cardFiles(cardsDir: Path, diagnostics: Diagnostics): List<Pair<Level, Path>> =
        cardsDir.listDirectoryEntries().sorted().flatMap { entry ->
            when {
                entry.isDirectory() -> {
                    val level = Level.entries.firstOrNull { it.name == entry.name }
                    val files = entry.listDirectoryEntries("*.csv").sorted()
                    if (level == null) {
                        files.forEach {
                            diagnostics.error(
                                it,
                                null,
                                "unknown level folder '${entry.name}' (use ${Level.entries.joinToString()})",
                            )
                        }
                        emptyList()
                    } else {
                        files.map { level to it }
                    }
                }

                entry.extension == "csv" -> {
                    diagnostics.error(entry, null, "card files belong in a level folder, e.g. cards/A1/")
                    emptyList()
                }

                else -> emptyList()
            }
        }

    private fun readPacks(packsFile: Path, metaFile: Path, diagnostics: Diagnostics): List<PackEntry> {
        val packsExist = packsFile.isRegularFile()
        val metaExists = metaFile.isRegularFile()
        if (!packsExist) diagnostics.error(packsFile, null, "packs.csv is missing in the exam pack directory")
        if (!metaExists) diagnostics.error(metaFile, null, "packs-meta.csv is missing in the exam pack directory")
        if (!packsExist || !metaExists) return emptyList()

        val packRows = CsvFiles.readTable(packsFile, PACK_COLUMNS, diagnostics) ?: return emptyList()
        val metaRows = CsvFiles.readTable(metaFile, PACK_META_COLUMNS, diagnostics) ?: return emptyList()

        val seenPackIds = mutableSetOf<String>()
        val packsById = packRows.mapNotNull { row ->
            var ok = true
            fun error(message: String) {
                ok = false
                diagnostics.error(packsFile, row.line, message)
            }
            val id = row["id"]
            if (!CardRules.SLUG.matches(id)) error("pack id '$id' must be a lowercase slug")
            if (!seenPackIds.add(id)) error("duplicate pack id '$id'")
            if (row["name"].isEmpty()) error("name must not be empty")
            val isPro = parseFlag(row["is_pro"])
            if (isPro == null) error("is_pro '${row["is_pro"]}' must be 0 or 1")
            val sort = row["sort"].toIntOrNull()
            if (sort == null) error("sort '${row["sort"]}' must be an integer")
            if (!ok || isPro == null || sort == null) return@mapNotNull null
            id to (row to Source(packsFile, row.line))
        }.toMap()

        val seenMetaIds = mutableSetOf<String>()
        val metaById = metaRows.mapNotNull { row ->
            var ok = true
            fun error(message: String) {
                ok = false
                diagnostics.error(metaFile, row.line, message)
            }
            val id = row["id"]
            if (!CardRules.SLUG.matches(id)) error("pack id '$id' must be a lowercase slug")
            if (!seenMetaIds.add(id)) error("duplicate pack id '$id'")
            if (row["title_tr"].isEmpty()) error("title_tr must not be empty")
            if (row["title_en"].isEmpty()) error("title_en must not be empty")
            val range = parseLevelRange(row["target_level_range"])
            if (range == null) {
                error("target_level_range '${row["target_level_range"]}' must be <LEVEL>-<LEVEL> (e.g. A2-B1)")
            }
            val cardTarget = row["card_target"].toIntOrNull()
            if (cardTarget == null || cardTarget <= 0) {
                error("card_target '${row["card_target"]}' must be a positive integer")
            }
            val preview = row["free_preview_letters"]
            if (preview.isNotEmpty()) {
                if (preview.any { it !in 'A'..'Z' }) {
                    error("free_preview_letters '$preview' must be uppercase A-Z letters")
                }
                if (preview.toSet().size != preview.length) {
                    error("free_preview_letters '$preview' must not contain duplicate letters")
                }
            }
            if (!ok || range == null || cardTarget == null) return@mapNotNull null
            id to row
        }.toMap()

        val allPackIds = packsById.keys
        val allMetaIds = metaById.keys
        (allPackIds - allMetaIds).forEach { id ->
            diagnostics.error(metaFile, null, "pack '$id' is missing from packs-meta.csv")
        }
        (allMetaIds - allPackIds).forEach { id ->
            val metaRow = metaById.getValue(id)
            diagnostics.error(metaFile, metaRow.line, "pack '$id' in packs-meta.csv is not declared in packs.csv")
        }

        return packsById.mapNotNull { (id, pair) ->
            val (packRow, source) = pair
            val metaRow = metaById[id] ?: return@mapNotNull null
            PackEntry(
                id = id,
                name = packRow["name"],
                description = packRow["description"],
                isPro = parseFlag(packRow["is_pro"])!!,
                sort = packRow["sort"].toInt(),
                titleTr = metaRow["title_tr"],
                titleEn = metaRow["title_en"],
                descriptionTr = metaRow["description_tr"],
                targetLevelRange = metaRow["target_level_range"],
                cardTarget = metaRow["card_target"].toInt(),
                freePreviewLetters = metaRow["free_preview_letters"],
                source = source,
            )
        }
    }

    /** `units/<pack-id>.csv` rows, tagged with the pack id taken from the file name. */
    private fun readUnitRows(packsDir: Path, packs: List<PackEntry>, diagnostics: Diagnostics): List<CsvRow> {
        val unitsDir = packsDir.resolve("units")
        val files = if (unitsDir.isDirectory()) unitsDir.listDirectoryEntries("*.csv").sorted() else emptyList()
        val packIds = packs.map { it.id }.toSet()
        val withUnits = files.map { it.name.removeSuffix(".csv") }.toSet()
        packs.filter { it.id !in withUnits }.forEach {
            diagnostics.warning(
                it.source.file,
                it.source.line,
                "pack '${it.id}' has no units file (units/${it.id}.csv)",
            )
        }
        return files.flatMap { file ->
            val packId = file.name.removeSuffix(".csv")
            if (packId !in packIds) {
                diagnostics.error(file, null, "pack '$packId' is not declared in packs.csv")
                emptyList()
            } else {
                CsvFiles.readTable(file, UNIT_COLUMNS, diagnostics).orEmpty()
            }
        }
    }

    private fun checkPackMembership(
        packs: List<PackEntry>,
        unitRows: List<CsvRow>,
        packCards: List<CardEntry>,
        knownIds: Set<String>,
        diagnostics: Diagnostics,
    ): List<PackCardEntry> {
        if (packs.isEmpty() && unitRows.isEmpty()) return emptyList()
        val seen = mutableSetOf<Pair<String, String>>()
        val entries = unitRows.mapNotNull { row ->
            val packId = row.file.name.removeSuffix(".csv")
            val cardId = row["card_id"]
            when {
                cardId !in knownIds -> diagnostics.error(row.file, row.line, "unknown card '$cardId'")

                !seen.add(packId to cardId) ->
                    diagnostics.error(row.file, row.line, "duplicate card '$cardId' in pack '$packId'")

                else -> return@mapNotNull PackCardEntry(packId, cardId, row["unit"].ifEmpty { null })
            }
            null
        }
        val used = entries.map { it.cardId }.toSet()
        packCards.filter { it.id !in used }.forEach {
            diagnostics.warning(it.source.file, it.source.line, "pack card '${it.id}' is not used by any pack")
        }
        return entries.sortedWith(compareBy({ it.packId }, { it.cardId }))
    }

    private fun checkDuplicates(cards: List<CardEntry>, diagnostics: Diagnostics) {
        fun report(groups: Map<*, List<CardEntry>>, message: (CardEntry) -> String) {
            groups.values.filter { it.size > 1 }.forEach { group ->
                group.forEach { card ->
                    val others = (group - card).joinToString { "${it.source.file.name}:${it.source.line}" }
                    diagnostics.error(card.source.file, card.source.line, "${message(card)} (also at $others)")
                }
            }
        }
        report(cards.groupBy { it.id }) { "duplicate id '${it.id}'" }
        report(cards.groupBy { it.level to it.answer }) { "duplicate answer '${it.answer}' in ${it.level.name}" }
    }

    /**
     * `CONTENT.md` §2/§3: `example_en` must not contain another answer of the same level (base and pack cards
     * alike) as whole word(s), or the Example joker leaks it. A card's own answer is ignored.
     */
    private fun checkExampleLeaks(cards: List<CardEntry>, diagnostics: Diagnostics) {
        cards.groupBy { it.level }.values.forEach { levelCards ->
            for (card in levelCards) {
                for (other in levelCards) {
                    if (other === card || other.answer == card.answer) continue
                    if (TextMatch.containsWords(card.exampleEn, other.answer)) {
                        diagnostics.error(
                            card.source.file,
                            card.source.line,
                            "example_en of '${card.id}' contains the answer '${other.answer}' of '${other.id}'",
                        )
                    }
                }
            }
        }
    }

    /** `CONTENT.md` §3: every letter of a shipped level has ≥ [MIN_CARDS_PER_LETTER] cards; base content only. */
    private fun checkCoverage(
        cards: List<CardEntry>,
        shipped: Set<Level>,
        smoke: Set<Level>,
        metaFile: Path,
        diagnostics: Diagnostics,
    ) {
        val byLevel = cards.groupBy { it.level }
        for (level in Level.entries) {
            val levelCards = byLevel[level].orEmpty()
            if (levelCards.isEmpty() && level !in shipped) continue
            if (level in smoke) continue // dev-only smoke set: thin coverage is expected
            val counts = ('A'..'Z').associateWith { letter -> levelCards.count { it.letter == letter } }
            if (level in shipped) {
                counts.forEach { (letter, count) ->
                    if (count < MIN_CARDS_PER_LETTER) {
                        diagnostics.error(
                            metaFile,
                            null,
                            "${level.name} letter $letter has $count card(s), minimum $MIN_CARDS_PER_LETTER for a shipped level",
                        )
                    } else if (count < RECOMMENDED_CARDS_PER_LETTER) {
                        diagnostics.warning(
                            metaFile,
                            null,
                            "${level.name} letter $letter has $count card(s), recommended $RECOMMENDED_CARDS_PER_LETTER",
                        )
                    }
                }
            } else {
                val thin = counts.filterValues { it < MIN_CARDS_PER_LETTER }.keys
                if (thin.isNotEmpty()) {
                    diagnostics.warning(
                        metaFile,
                        null,
                        "${level.name} (not shipped yet): ${thin.size} letter(s) below $MIN_CARDS_PER_LETTER cards: " +
                            thin.joinToString(""),
                    )
                }
            }
        }
    }

    private fun checkPackCoverage(
        packs: List<PackEntry>,
        packCards: List<PackCardEntry>,
        cardsById: Map<String, CardEntry>,
        shippedPacks: Set<String>,
        packsDir: Path?,
        diagnostics: Diagnostics,
    ) {
        val byPack = packCards.groupBy { it.packId }
        for (pack in packs) {
            val members = byPack[pack.id].orEmpty().mapNotNull { cardsById[it.cardId] }
            val unitFile = packsDir?.resolve("units")?.resolve("${pack.id}.csv")?.takeIf { it.isRegularFile() }
                ?: pack.source.file
            val counts = ('A'..'Z').associateWith { letter -> members.count { it.letter == letter } }
            if (pack.id in shippedPacks) {
                counts.forEach { (letter, count) ->
                    if (count < MIN_CARDS_PER_LETTER) {
                        diagnostics.error(
                            unitFile,
                            null,
                            "pack '${pack.id}' letter $letter has $count card(s), minimum $MIN_CARDS_PER_LETTER",
                        )
                    } else if (count < RECOMMENDED_CARDS_PER_LETTER) {
                        diagnostics.warning(
                            unitFile,
                            null,
                            "pack '${pack.id}' letter $letter has $count card(s), recommended $RECOMMENDED_CARDS_PER_LETTER",
                        )
                    }
                }
            } else {
                val thin = counts.filterValues { it < MIN_CARDS_PER_LETTER }.keys
                if (thin.isNotEmpty()) {
                    diagnostics.warning(
                        unitFile,
                        null,
                        "pack '${pack.id}' (not shipped yet): ${thin.size} letter(s) below " +
                            "$MIN_CARDS_PER_LETTER cards: " + thin.joinToString(""),
                    )
                }
            }
        }
    }

    private fun checkPackDuplicateAnswers(
        packs: List<PackEntry>,
        packCards: List<PackCardEntry>,
        cardsById: Map<String, CardEntry>,
        diagnostics: Diagnostics,
    ) {
        val byPack = packCards.groupBy { it.packId }
        for (pack in packs) {
            val members = byPack[pack.id].orEmpty().mapNotNull { cardsById[it.cardId] }
            val groups = members.groupBy { it.answer }.filterValues { it.size > 1 }
            groups.values.forEach { duplicates ->
                duplicates.forEach { card ->
                    val others = (duplicates - card).joinToString { "${it.source.file.name}:${it.source.line}" }
                    diagnostics.error(
                        card.source.file,
                        card.source.line,
                        "duplicate answer '${card.answer}' in pack '${pack.id}' (also at $others)",
                    )
                }
            }
        }
    }

    private fun parseFlag(value: String): Boolean? = when (value) {
        "0" -> false
        "1" -> true
        else -> null
    }
}
