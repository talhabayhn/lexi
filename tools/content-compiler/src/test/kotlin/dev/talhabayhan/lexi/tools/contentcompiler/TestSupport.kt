package dev.talhabayhan.lexi.tools.contentcompiler

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.name
import kotlin.io.path.writeText
import kotlin.test.assertTrue
import kotlin.test.fail

const val CARD_HEADER = "id,answer,letter,rule,pos,clue_tr,example_en,example_tr,difficulty,tags,category"

/** Copies `src/test/resources/fixtures/<name>` (synthetic content, never real pack content) into a temp dir. */
fun copyFixture(name: String): Path {
    val source = Paths.get(checkNotNull(CsvParserTest::class.java.getResource("/fixtures/$name")).toURI())
    val target = createTempDirectory("lexi-$name-")
    Files.walk(source).use { paths ->
        paths.forEach { path ->
            val destination = target.resolve(source.relativize(path).toString())
            if (Files.isDirectory(path)) destination.createDirectories() else Files.copy(path, destination)
        }
    }
    return target
}

fun Path.writeCards(relative: String, vararg rows: String): Path {
    val file = resolve(relative)
    file.parent.createDirectories()
    file.writeText((listOf(CARD_HEADER) + rows).joinToString("\n", postfix = "\n"))
    return file
}

/** A valid card row; override single columns by name. */
fun cardRow(
    id: String = "a1-zebra",
    answer: String = "ZEBRA",
    letter: String = "Z",
    rule: String = "STARTS",
    pos: String = "noun",
    clueTr: String = "çizgili hayvan",
    exampleEn: String = "The zebra has stripes.",
    exampleTr: String = "Zebranın çizgileri var.",
    difficulty: String = "1",
    tags: String = "",
    category: String = "food",
): String = listOf(id, answer, letter, rule, pos, clueTr, exampleEn, exampleTr, difficulty, tags, category)
    .joinToString(",") { if (it.contains(',') || it.contains('"')) "\"" + it.replace("\"", "\"\"") + "\"" else it }

fun ValidationResult.assertNoErrors() {
    if (errors.isNotEmpty()) fail("expected no errors, got:\n" + errors.joinToString("\n"))
}

fun ValidationResult.assertError(fileName: String, line: Int?, fragment: String) =
    assertDiagnostic(errors, fileName, line, fragment)

fun ValidationResult.assertWarning(fileName: String, line: Int?, fragment: String) =
    assertDiagnostic(warnings, fileName, line, fragment)

private fun assertDiagnostic(list: List<Diagnostic>, fileName: String, line: Int?, fragment: String) {
    assertTrue(
        list.any { it.file?.name == fileName && it.line == line && fragment in it.message },
        "expected '$fragment' at $fileName:$line, got:\n" + list.joinToString("\n"),
    )
}
