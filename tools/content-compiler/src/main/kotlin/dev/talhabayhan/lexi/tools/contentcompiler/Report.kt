package dev.talhabayhan.lexi.tools.contentcompiler

import dev.talhabayhan.lexi.core.domain.model.Level

/** Human-readable summary: cards per level × letter, per category × level, exam packs (`CONTENT.md` §3). */
object Report {
    fun render(result: ValidationResult, packsDir: String?): String = buildString {
        val content = result.content
        val baseCards = content.cards.filterNot { it.fromPack }
        val packExtras = content.cards.count { it.fromPack }
        appendLine(
            "Content: contentVersion ${content.contentVersion}, ${baseCards.size} cards" +
                if (packExtras > 0) " + $packExtras exam-pack cards" else "",
        )
        appendLine()

        val letters = 'A'..'Z'
        appendLine("Cards per level and letter (base content; '!' = below ${ContentCompiler.MIN_CARDS_PER_LETTER}):")
        appendLine("Level  Total " + letters.joinToString("") { it.toString().padStart(3) } + "  Status")
        for (level in Level.entries) {
            val levelCards = baseCards.filter { it.level == level }
            val shipped = level in content.shippedLevels
            if (levelCards.isEmpty() && !shipped) continue
            val counts = letters.joinToString("") { letter ->
                val count = levelCards.count { it.letter == letter }
                val cell = if (count < ContentCompiler.MIN_CARDS_PER_LETTER) "$count!" else "$count"
                cell.padStart(3)
            }
            val status = when {
                shipped -> "shipped"
                level in content.smokeLevels -> "SMOKE (dev data, not for release)"
                else -> "not shipped"
            }
            appendLine(level.name.padEnd(6) + levelCards.size.toString().padStart(6) + " " + counts + "  " + status)
        }
        appendLine()

        appendLine("Cards per category and level:")
        val levelsWithCards = Level.entries.filter { level -> baseCards.any { it.level == level } }
        appendLine("Category".padEnd(18) + levelsWithCards.joinToString("") { it.name.padStart(6) })
        for (category in content.categories) {
            val counts = levelsWithCards.map { level ->
                baseCards.count { it.level == level && it.categoryId == category.id }
            }
            if (counts.all { it == 0 }) continue
            appendLine(category.id.padEnd(18) + counts.joinToString("") { it.toString().padStart(6) })
        }
        appendLine()

        if (packsDir == null) {
            appendLine("Exam packs: none (no --packs directory given)")
        } else {
            appendLine("Exam packs from $packsDir: ${content.packs.size}")
            for (pack in content.packs) {
                val members = content.packCards.filter { it.packId == pack.id }
                val units = members.mapNotNull { it.unit }.distinct().size
                appendLine(
                    "  ${pack.id.padEnd(20)} ${members.size} cards, $units units${if (pack.isPro) ", Pro" else ""}",
                )
            }
        }
        appendLine()
        append("${result.errors.size} error(s), ${result.warnings.size} warning(s)")
    }
}
