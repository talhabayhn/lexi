package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.theme.LexiColors
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import kotlin.math.max

/** Geometry of the Canvas-drawn category and exam-pack art (`flow.html` SVG, `viewBox="0 0 163 95"`). */
public object CategoryArtDefaults {
    /** 163: art box width (inside the card's 3dp frame); also the drawing's viewBox width. */
    public const val WIDTH: Float = 163f

    /** 95: art box height; also the drawing's viewBox height. */
    public const val HEIGHT: Float = 95f

    /** 3: ink outline width in viewBox units (scales with the art). */
    public const val STROKE: Float = 3f

    /** 0.45: opacity of a locked exam art, drawn over white. */
    public const val LOCKED_ALPHA: Float = 0.45f
}

/**
 * The 20 category illustrations. First draft, needs design sign-off (video-flow-notes.md section 7 item 4).
 * Eleven are the exact `flow.html` SVGs; the rest are traced from `art_v3.png`.
 */
public enum class CategoryArtKind {
    Mixed,
    DailyLife,
    FoodDrink,
    Home,
    FamilyPeople,
    ColorsNumbers,
    BodyHealth,
    SchoolWork,
    Animals,
    CityTransport,
    TimeWeather,
    Travel,
    Sports,
    Music,
    FilmTv,
    Business,
    Technology,
    FalseFriends,
    Idioms,
    PhrasalVerbs,
    ClothesShopping,
    Hobbies,
    Verbs,
    Adjectives,
    Economy,
    Feelings,
    SocietyLaw,
    Ideas,
    ;

    public companion object {
        /** Maps a `content/categories.csv` id to its art; null for an unknown id (the caller falls back to Mixed). */
        public fun forId(id: String): CategoryArtKind? = when (id) {
            "mixed" -> Mixed
            "daily-life" -> DailyLife
            "food-drink" -> FoodDrink
            "home" -> Home
            "family-people" -> FamilyPeople
            "colors-numbers" -> ColorsNumbers
            "body-health" -> BodyHealth
            "school-work" -> SchoolWork
            "animals" -> Animals
            "city-transport" -> CityTransport
            "time-weather" -> TimeWeather
            "travel" -> Travel
            "sports" -> Sports
            "music" -> Music
            "film-tv" -> FilmTv
            "business" -> Business
            "technology" -> Technology
            "false-friends" -> FalseFriends
            "idioms" -> Idioms
            "phrasal-verbs" -> PhrasalVerbs
            "clothes-shopping" -> ClothesShopping
            "hobbies" -> Hobbies
            "verbs" -> Verbs
            "adjectives" -> Adjectives
            "economy" -> Economy
            "feelings" -> Feelings
            "society-law" -> SocietyLaw
            "ideas" -> Ideas
            else -> null
        }
    }
}

/**
 * The exam-pack illustrations. First draft, needs design sign-off (video-flow-notes.md section 7 item 4).
 * Traced from `cats_v3.png`; IELTS is cut off in the draft, so its art (speech bubble + headphones) is
 * needs design.
 */
public enum class ExamArtKind {
    Lgs,
    YdsYokdil,
    YksDil,
    Toefl,
    Ielts,
    ;

    public companion object {
        /** Maps an exam-pack id (`lgs*`, `yds-yokdil`, `yks-dil`, `toefl`, `ielts`) to its art; null if unknown. */
        public fun forId(id: String): ExamArtKind? = when {
            id.startsWith("lgs") -> Lgs
            id == "yds-yokdil" -> YdsYokdil
            id == "yks-dil" -> YksDil
            id == "toefl" -> Toefl
            id == "ielts" -> Ielts
            else -> null
        }
    }
}

/**
 * Category illustration: background field plus drawing, filling its bounds (cover scaling of the 163 x 95
 * design box). Decorative: carries no semantics, the card around it holds the label.
 * First draft, needs design sign-off (video-flow-notes.md section 7 item 4).
 */
@Composable
public fun CategoryArt(kind: CategoryArtKind, modifier: Modifier = Modifier) {
    val colors = LexiTheme.colors
    val art = categoryDrawing(kind, colors)
    ArtCanvas(art.background, art.draw, modifier)
}

/**
 * Exam-pack illustration; [locked] draws the whole art dimmed ([CategoryArtDefaults.LOCKED_ALPHA]) over white.
 * Decorative, no semantics. First draft, needs design sign-off (video-flow-notes.md section 7 item 4).
 */
@Composable
public fun ExamArt(kind: ExamArtKind, modifier: Modifier = Modifier, locked: Boolean = false) {
    val colors = LexiTheme.colors
    val art = examDrawing(kind, colors)
    val base = if (locked) {
        modifier.background(colors.white).graphicsLayer { alpha = CategoryArtDefaults.LOCKED_ALPHA }
    } else {
        modifier
    }
    ArtCanvas(art.background, art.draw, base)
}

private class Drawing(val background: Color, val draw: Sketch.() -> Unit)

@Composable
private fun ArtCanvas(background: Color, draw: Sketch.() -> Unit, modifier: Modifier) {
    val measurer = rememberTextMeasurer()
    val textStyle = LexiTheme.typography.button24
    val drawing = remember(background, draw, measurer, textStyle) {
        Modifier.clipToBounds().drawWithCache {
            val sketch = Sketch(this, measurer, textStyle)
            sketch.draw()
            val ops = sketch.ops
            onDrawBehind {
                drawRect(background)
                val scale = max(size.width / CategoryArtDefaults.WIDTH, size.height / CategoryArtDefaults.HEIGHT)
                val dx = (size.width - CategoryArtDefaults.WIDTH * scale) / 2f
                val dy = (size.height - CategoryArtDefaults.HEIGHT * scale) / 2f
                withTransform({
                    translate(dx, dy)
                    scale(scale, scale, Offset.Zero)
                }) { ops.forEach { it() } }
            }
        }
    }
    Spacer(modifier.then(drawing).clearAndSetSemantics {})
}

/** Collects draw operations in viewBox units; built once per size inside the draw cache. */
private class Sketch(
    private val density: Density,
    private val measurer: TextMeasurer,
    private val textStyle: TextStyle,
) {
    val ops = ArrayList<DrawScope.() -> Unit>()
    var ink: Color = Color.Unspecified

    private fun shape(
        fill: Color?,
        stroke: Color?,
        width: Float,
        cap: StrokeCap,
        join: StrokeJoin,
        draw: DrawScope.(Color, DrawStyle) -> Unit,
    ) {
        val outline = Stroke(width, cap = cap, join = join)
        ops += {
            if (fill != null) draw(fill, Fill)
            if (stroke != null) draw(stroke, outline)
        }
    }

    /** Rectangle; [line] adds the 3-unit ink outline. */
    fun rect(x: Float, y: Float, w: Float, h: Float, fill: Color?, line: Boolean = false) =
        shape(fill, ink.takeIf { line }, CategoryArtDefaults.STROKE, StrokeCap.Butt, StrokeJoin.Miter) { c, s ->
            drawRect(c, Offset(x, y), Size(w, h), style = s)
        }

    fun circle(cx: Float, cy: Float, r: Float, fill: Color?, line: Boolean = false) =
        shape(fill, ink.takeIf { line }, CategoryArtDefaults.STROKE, StrokeCap.Butt, StrokeJoin.Miter) { c, s ->
            drawCircle(c, r, Offset(cx, cy), style = s)
        }

    /** Path from SVG path data; [line] adds the 3-unit ink outline. */
    fun path(d: String, fill: Color?, line: Boolean = false) = path(parse(d), fill, line)

    fun path(path: Path, fill: Color?, line: Boolean = false) =
        shape(fill, ink.takeIf { line }, CategoryArtDefaults.STROKE, StrokeCap.Butt, StrokeJoin.Miter) { c, s ->
            drawPath(path, c, style = s)
        }

    /** Stroke-only path from SVG path data. */
    fun stroke(
        d: String,
        color: Color,
        width: Float = CategoryArtDefaults.STROKE,
        cap: StrokeCap = StrokeCap.Butt,
        join: StrokeJoin = StrokeJoin.Miter,
    ) {
        val p = parse(d)
        shape(null, color, width, cap, join) { c, s -> drawPath(p, c, style = s) }
    }

    /** Closed polygon from alternating x, y values. */
    fun polygon(fill: Color?, line: Boolean, vararg xy: Float) {
        val p = Path().apply {
            moveTo(xy[0], xy[1])
            for (i in 2 until xy.size step 2) lineTo(xy[i], xy[i + 1])
            close()
        }
        path(p, fill, line)
    }

    /** Text centred on [cx] with its baseline at [baseline]; [size] is in viewBox units. */
    fun text(value: String, cx: Float, baseline: Float, size: Float, color: Color) {
        val layout = measurer.measure(
            value,
            textStyle.copy(
                fontSize = with(density) { size.toSp() },
                color = color,
                letterSpacing = TextUnit.Unspecified,
                lineHeight = TextUnit.Unspecified,
            ),
        )
        ops += {
            drawText(layout, topLeft = Offset(cx - layout.size.width / 2f, baseline - layout.firstBaseline))
        }
    }

    private fun parse(d: String): Path = PathParser().parsePathString(d).toPath()
}

// region Category drawings

private fun categoryDrawing(kind: CategoryArtKind, c: LexiColors): Drawing = when (kind) {
    CategoryArtKind.Mixed -> Drawing(c.ink) { mixed(c) }
    CategoryArtKind.DailyLife -> Drawing(c.blue) { dailyLife(c) }
    CategoryArtKind.FoodDrink -> Drawing(c.yellow) { foodDrink(c) }
    CategoryArtKind.Home -> Drawing(c.red) { home(c) }
    CategoryArtKind.FamilyPeople -> Drawing(c.cream) { familyPeople(c) }
    CategoryArtKind.ColorsNumbers -> Drawing(c.cream) { colorsNumbers(c) }
    CategoryArtKind.BodyHealth -> Drawing(c.yellow) { bodyHealth(c) }
    CategoryArtKind.SchoolWork -> Drawing(c.blue) { schoolWork(c) }
    CategoryArtKind.Animals -> Drawing(c.blue) { animals(c) }
    CategoryArtKind.CityTransport -> Drawing(c.cream) { cityTransport(c) }
    CategoryArtKind.TimeWeather -> Drawing(c.blue) { timeWeather(c) }
    CategoryArtKind.Travel -> Drawing(c.blue) { travel(c) }
    CategoryArtKind.Sports -> Drawing(c.red) { sports(c) }
    CategoryArtKind.Music -> Drawing(c.blue) { music(c) }
    CategoryArtKind.FilmTv -> Drawing(c.ink) { filmTv(c) }
    CategoryArtKind.Business -> Drawing(c.cream) { business(c) }
    CategoryArtKind.Technology -> Drawing(c.ink) { technology(c) }
    CategoryArtKind.FalseFriends -> Drawing(c.yellow) { falseFriends(c) }
    CategoryArtKind.Idioms -> Drawing(c.red) { idioms(c) }
    CategoryArtKind.PhrasalVerbs -> Drawing(c.ink) { phrasalVerbs(c) }
    CategoryArtKind.ClothesShopping -> Drawing(c.cream) { clothesShopping(c) }
    CategoryArtKind.Hobbies -> Drawing(c.blue) { hobbies(c) }
    CategoryArtKind.Verbs -> Drawing(c.yellow) { verbs(c) }
    CategoryArtKind.Adjectives -> Drawing(c.cream) { adjectives(c) }
    CategoryArtKind.Economy -> Drawing(c.cream) { economy(c) }
    CategoryArtKind.Feelings -> Drawing(c.cream) { feelings(c) }
    CategoryArtKind.SocietyLaw -> Drawing(c.blue) { societyLaw(c) }
    CategoryArtKind.Ideas -> Drawing(c.ink) { ideas(c) }
}

// Exact flow.html SVGs: mixed ... timeWeather.

private fun Sketch.mixed(c: LexiColors) {
    rect(24f, 30f, 38f, 38f, c.red)
    circle(76f, 40f, 20f, c.yellow)
    path("M96 70C96 42 116 30 140 32C142 52 128 70 96 70Z", c.blue)
}

private fun Sketch.foodDrink(c: LexiColors) {
    ink = c.ink
    circle(62f, 54f, 26f, c.red, line = true)
    rect(60f, 16f, 5f, 12f, c.ink)
    path("M66 22q12-10 22-2q-8 10-22 2z", c.blue, line = true)
    rect(104f, 40f, 32f, 38f, c.white, line = true)
    stroke("M136 48h8a8 8 0 0 1 0 16h-8", c.ink)
}

private fun Sketch.dailyLife(c: LexiColors) {
    ink = c.ink
    circle(84f, 49f, 34f, c.cream, line = true)
    stroke("M84 49V28M84 49L100 58", c.ink, width = 5f, cap = StrokeCap.Round)
    rect(82f, 19f, 4f, 5f, c.ink)
    rect(82f, 74f, 4f, 5f, c.ink)
    rect(113f, 47f, 5f, 4f, c.ink)
    rect(50f, 47f, 5f, 4f, c.ink)
    circle(84f, 49f, 4f, c.red)
}

private fun Sketch.home(c: LexiColors) {
    ink = c.ink
    rect(46f, 46f, 76f, 42f, c.cream, line = true)
    path("M36 50L84 12L132 50Z", c.ink)
    rect(74f, 60f, 20f, 28f, c.yellow, line = true)
    rect(100f, 56f, 14f, 14f, c.blue, line = true)
}

private fun Sketch.familyPeople(c: LexiColors) {
    ink = c.ink
    circle(62f, 30f, 14f, c.blue, line = true)
    path("M38 86A24 24 0 0 1 86 86Z", c.blue, line = true)
    circle(114f, 48f, 10f, c.yellow, line = true)
    path("M96 86A18 18 0 0 1 132 86Z", c.yellow, line = true)
}

private fun Sketch.colorsNumbers(c: LexiColors) {
    ink = c.ink
    circle(47f, 48f, 25f, c.red, line = true)
    circle(84f, 48f, 25f, c.yellow, line = true)
    circle(121f, 48f, 25f, c.blue, line = true)
    text("1", 47f, 58f, 28f, c.white)
    text("2", 84f, 58f, 28f, c.ink)
    text("3", 121f, 58f, 28f, c.white)
}

private fun Sketch.bodyHealth(c: LexiColors) {
    ink = c.ink
    path("M84 82C40 54 50 20 84 38C118 20 128 54 84 82Z", c.red, line = true)
    stroke("M8 56H52L64 40L78 76L92 48L102 56H161", c.ink, join = StrokeJoin.Round)
}

private fun Sketch.schoolWork(c: LexiColors) {
    ink = c.ink
    path("M20 30h52v46H20z", c.cream, line = true)
    path("M76 30h52v46H76z", c.cream, line = true)
    stroke("M32 44h28M32 54h28M88 44h28M88 54h28", c.ink)
    path("M108 72L130 38L139 45L118 80Z", c.yellow, line = true)
    path("M108 72L118 80L102 84Z", c.ink)
}

private fun Sketch.animals(c: LexiColors) {
    ink = c.ink
    path("M56 38L58 10L78 26Z", c.yellow, line = true)
    path("M112 38L110 10L90 26Z", c.yellow, line = true)
    circle(84f, 54f, 32f, c.yellow, line = true)
    circle(72f, 50f, 4f, c.ink)
    circle(96f, 50f, 4f, c.ink)
    path("M78 62h12l-6 7z", c.red, line = true)
    stroke("M44 58h14M44 66h14M110 58h14M110 66h14", c.ink, width = 2.5f)
}

private fun Sketch.cityTransport(c: LexiColors) {
    ink = c.ink
    rect(26f, 30f, 116f, 44f, c.yellow, line = true)
    rect(36f, 38f, 22f, 16f, c.cream, line = true)
    rect(64f, 38f, 22f, 16f, c.cream, line = true)
    rect(92f, 38f, 22f, 16f, c.cream, line = true)
    rect(120f, 38f, 16f, 30f, c.cream, line = true)
    circle(52f, 76f, 9f, c.ink)
    circle(116f, 76f, 9f, c.ink)
}

private fun Sketch.timeWeather(c: LexiColors) {
    ink = c.ink
    circle(62f, 38f, 22f, c.yellow, line = true)
    circle(92f, 62f, 17f, c.white)
    circle(112f, 54f, 21f, c.white)
    circle(134f, 64f, 15f, c.white)
    rect(92f, 62f, 42f, 17f, c.white)
}

// Traced from art_v3.png in the same 163 x 95 coordinate system.

private fun Sketch.travel(c: LexiColors) {
    ink = c.ink
    circle(26f, 24f, 7f, c.white)
    // Airliner seen from above, nose up: wings and tail behind the fuselage.
    polygon(c.yellow, true, 91f, 38f, 134f, 62f, 134f, 72f, 91f, 58f)
    polygon(c.yellow, true, 77f, 38f, 34f, 62f, 34f, 72f, 77f, 58f)
    polygon(c.yellow, true, 91f, 68f, 110f, 78f, 110f, 84f, 91f, 80f)
    polygon(c.yellow, true, 77f, 68f, 58f, 78f, 58f, 84f, 77f, 80f)
    path("M77 30Q77 10 84 10Q91 10 91 30V84H77Z", c.white, line = true)
    rect(81f, 22f, 6f, 5f, c.ink)
}

private fun Sketch.sports(c: LexiColors) {
    ink = c.ink
    circle(84f, 48f, 34f, c.yellow, line = true)
    stroke("M84 14V82M50 48H118", c.ink)
    stroke("M64 20.5C52 34 52 62 64 75.5M104 20.5C116 34 116 62 104 75.5", c.ink)
}

private fun Sketch.music(c: LexiColors) {
    ink = c.ink
    rect(66f, 21f, 4f, 50f, c.ink)
    rect(118f, 21f, 4f, 41f, c.ink)
    polygon(c.yellow, false, 68f, 20f, 121f, 13f, 121f, 26f, 68f, 33f)
    path("M47 71a13 10 0 1 0 26 0a13 10 0 1 0 -26 0z", c.ink)
    path("M96 62a13 10 0 1 0 26 0a13 10 0 1 0 -26 0z", c.ink)
}

private fun Sketch.filmTv(c: LexiColors) {
    ink = c.ink
    rect(39f, 44f, 89f, 39f, c.cream)
    polygon(c.red, false, 73f, 53f, 73f, 76f, 97f, 64.5f)
    rect(37f, 26f, 93f, 16f, c.cream)
    for (x in floatArrayOf(40f, 58f, 76f, 94f)) {
        polygon(c.ink, false, x, 42f, x + 6f, 26f, x + 13f, 26f, x + 7f, 42f)
    }
}

private fun Sketch.business(c: LexiColors) {
    ink = c.ink
    rect(40f, 57f, 23f, 28f, c.red, line = true)
    rect(71f, 44f, 22f, 41f, c.yellow, line = true)
    rect(101f, 27f, 22f, 58f, c.blue, line = true)
    stroke("M37 42L61 31L79 38.5L124 14", c.ink, join = StrokeJoin.Round)
    polygon(c.ink, false, 128f, 10f, 114f, 13f, 124f, 22f)
}

private fun Sketch.technology(c: LexiColors) {
    ink = c.ink
    rect(44f, 18f, 76f, 48f, c.cream)
    circle(65.5f, 42f, 10f, c.yellow)
    rect(84f, 35f, 28f, 14f, c.red)
    polygon(c.white, false, 33f, 73f, 130f, 73f, 122f, 81.5f, 41f, 81.5f)
}

private fun Sketch.falseFriends(c: LexiColors) {
    ink = c.ink
    rect(20f, 27f, 44f, 44f, c.blue, line = true)
    text("?", 42f, 61f, 34f, c.white)
    stroke("M71 47H89M71 55H89M76 62L85 40", c.ink, width = 4f)
    circle(122f, 49f, 22f, c.red, line = true)
    text("?", 122f, 61f, 34f, c.white)
}

private fun Sketch.idioms(c: LexiColors) {
    ink = c.ink
    polygon(c.cream, true, 52f, 62f, 44f, 86f, 76f, 62f)
    rect(28f, 14f, 106f, 50f, c.cream, line = true)
    rect(31f, 59f, 44f, 8f, c.cream)
    // Two closing quotation marks: a ball with a tail curling down to the left.
    for (cx in floatArrayOf(65f, 99f)) {
        circle(cx, 33f, 8f, c.ink)
        path("M${cx + 8} 33C${cx + 8} 45 ${cx + 2} 51 ${cx - 8} 54L${cx - 8} 48C${cx - 3} 45 $cx 41 $cx 33Z", c.ink)
    }
}

private fun Sketch.phrasalVerbs(c: LexiColors) {
    ink = c.ink
    rect(25f, 18f, 75f, 26f, c.cream)
    text("give", 62.5f, 38f, 24f, c.ink)
    rect(65f, 53f, 75f, 26f, c.yellow)
    text("up", 102.5f, 73f, 24f, c.ink)
    stroke("M113 30H138M130 23L138 30L130 37", c.white)
}

private fun Sketch.clothesShopping(c: LexiColors) {
    ink = c.ink
    // T-shirt on the left:
    rect(38f, 34f, 32f, 44f, c.blue, line = true)
    polygon(c.blue, true, 38f, 34f, 22f, 45f, 27f, 54f, 38f, 46f)
    polygon(c.blue, true, 70f, 34f, 86f, 45f, 81f, 54f, 70f, 46f)
    path("M47 34 C47 41 61 41 61 34 Z", c.cream, line = true)
    rect(44f, 46f, 8f, 8f, c.red, line = true)
    // Shopping bag on the right:
    rect(94f, 38f, 40f, 42f, c.red, line = true)
    stroke("M104 38 V28 C104 20 124 20 124 28 V38", c.ink, width = 3f)
    stroke("M114 38 V80", c.ink, width = 1.5f)
    circle(114f, 58f, 7f, c.yellow, line = true)
}

private fun Sketch.hobbies(c: LexiColors) {
    ink = c.ink
    // Artist paint palette on the left:
    path(
        "M28 46 C28 26 52 22 76 26 C90 28 98 38 94 48 C90 58 94 64 100 64 C103 64 104 61 104 58 C108 68 94 78 78 78 C50 78 28 66 28 46 Z",
        c.yellow,
        line = true,
    )
    circle(86f, 62f, 5.5f, c.blue, line = true)
    circle(42f, 38f, 4.5f, c.red)
    circle(58f, 34f, 4.5f, c.white)
    circle(74f, 38f, 4.5f, c.ink)
    // Ball on the right:
    circle(122f, 50f, 18f, c.white, line = true)
    stroke("M110 40 C118 46 126 46 134 40 M110 60 C118 54 126 54 134 60", c.ink, width = 2.5f)
    stroke("M114 74 C122 76 128 76 134 74", c.white, width = 3f, cap = StrokeCap.Round)
}

private fun Sketch.verbs(c: LexiColors) {
    ink = c.ink
    // Running figure:
    circle(82f, 22f, 9f, c.ink)
    polygon(c.red, true, 73f, 32f, 84f, 32f, 77f, 54f, 67f, 53f)
    stroke("M78 36 L94 42 L90 54", c.ink, width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    stroke("M71 38 L58 43 L62 52", c.ink, width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    stroke("M74 54 L86 64 L82 78", c.ink, width = 4.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    stroke("M69 54 L54 62 L42 58", c.ink, width = 4.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    // Forward motion arrow:
    stroke("M104 36 H134 M124 26 L134 36 L124 46", c.blue, width = 4.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    // Speed lines trailing behind:
    stroke("M22 46 H42 M16 54 H36 M24 62 H44", c.ink, width = 3f, cap = StrokeCap.Round)
}

private fun Sketch.adjectives(c: LexiColors) {
    ink = c.ink
    stroke("M22 78 H142", c.ink, width = 3f)
    // Big circle on the left:
    circle(52f, 50f, 28f, c.blue, line = true)
    circle(44f, 42f, 7f, c.white)
    // Small circle on the right:
    circle(116f, 68f, 10f, c.red, line = true)
    // Sparkle stars:
    polygon(c.yellow, true, 116f, 16f, 119f, 25f, 128f, 28f, 119f, 31f, 116f, 40f, 113f, 31f, 104f, 28f, 113f, 25f)
    polygon(c.yellow, true, 86f, 30f, 88f, 36f, 94f, 38f, 88f, 40f, 86f, 46f, 84f, 40f, 78f, 38f, 84f, 36f)
}

private fun Sketch.economy(c: LexiColors) {
    ink = c.ink
    stroke("M24 78 H140", c.ink, width = 3f)
    // Stacked coins on the left:
    rect(30f, 64f, 36f, 14f, c.yellow, line = true)
    rect(30f, 50f, 36f, 14f, c.yellow, line = true)
    rect(30f, 36f, 36f, 14f, c.yellow, line = true)
    stroke("M38 43 H58 M38 57 H58 M38 71 H58", c.ink, width = 2f)
    // Front coin standing:
    circle(60f, 58f, 16f, c.yellow, line = true)
    circle(60f, 58f, 11f, null, line = true)
    circle(60f, 58f, 3f, c.ink)
    // Small bar chart on the right:
    rect(88f, 56f, 14f, 22f, c.red, line = true)
    rect(106f, 42f, 14f, 36f, c.blue, line = true)
    rect(124f, 26f, 14f, 52f, c.yellow, line = true)
    // Upward trend arrow:
    stroke("M86 52 L104 38 L122 28 L136 18", c.ink, width = 3f)
    polygon(c.ink, false, 138f, 14f, 128f, 17f, 134f, 24f)
}

private fun Sketch.feelings(c: LexiColors) {
    ink = c.ink
    // Smiling face on the left:
    circle(52f, 48f, 25f, c.yellow, line = true)
    circle(44f, 42f, 3.5f, c.ink)
    circle(60f, 42f, 3.5f, c.ink)
    stroke("M43 54 C47 65 57 65 61 54", c.ink, width = 3f, cap = StrokeCap.Round)
    circle(39f, 52f, 3f, c.red)
    circle(65f, 52f, 3f, c.red)
    // Sad face on the right:
    circle(110f, 48f, 25f, c.blue, line = true)
    circle(102f, 42f, 3.5f, c.ink)
    circle(118f, 42f, 3.5f, c.ink)
    stroke("M101 62 C105 53 115 53 119 62", c.ink, width = 3f, cap = StrokeCap.Round)
    circle(102f, 52f, 2.5f, c.white)
}

private fun Sketch.societyLaw(c: LexiColors) {
    ink = c.ink
    // Stand & pillar:
    rect(66f, 78f, 32f, 8f, c.yellow, line = true)
    polygon(c.ink, false, 82f, 68f, 74f, 78f, 90f, 78f)
    rect(80f, 22f, 4f, 56f, c.ink)
    circle(82f, 18f, 5f, c.yellow, line = true)
    // Horizontal beam:
    stroke("M40 28 H124", c.ink, width = 4f, cap = StrokeCap.Round)
    circle(82f, 28f, 4f, c.ink)
    // Chains:
    stroke("M42 28 L32 50 M42 28 L52 50 M122 28 L112 50 M122 28 L132 50", c.ink, width = 2f)
    // Pans:
    path("M30 50 C30 62 54 62 54 50 Z", c.yellow, line = true)
    path("M110 50 C110 62 134 62 134 50 Z", c.yellow, line = true)
    // Weights:
    rect(38f, 44f, 8f, 6f, c.red, line = true)
    circle(122f, 47f, 3f, c.white, line = true)
}

private fun Sketch.ideas(c: LexiColors) {
    ink = c.ink
    // Glowing bulb:
    path("M66 52 C58 44 58 30 68 20 C76 12 88 12 96 20 C106 30 106 44 98 52 L94 64 H70 Z", c.yellow, line = true)
    // Filament:
    stroke("M77 64 V50 L82 44 L87 50 V64", c.ink, width = 2.5f, join = StrokeJoin.Round)
    // Screw cap base:
    rect(72f, 66f, 20f, 4f, c.cream, line = true)
    rect(73f, 72f, 18f, 4f, c.cream, line = true)
    path("M76 78 H88 L85 84 H79 Z", c.cream)
    // Radiant rays:
    val rays = "M82 4 V10 M48 14 L53 19 M116 14 L111 19 M36 36 H42 M122 36 H128 M48 58 L53 53 M116 58 L111 53"
    stroke(
        d = rays,
        color = c.yellow,
        width = 3.5f,
        cap = StrokeCap.Round,
    )
}

// endregion

// region Exam drawings

private fun examDrawing(kind: ExamArtKind, c: LexiColors): Drawing = when (kind) {
    ExamArtKind.Lgs -> Drawing(c.cream) { lgs(c) }
    ExamArtKind.YdsYokdil -> Drawing(c.yellow) { yds(c) }
    ExamArtKind.YksDil -> Drawing(c.blue) { yksDil(c) }
    ExamArtKind.Toefl -> Drawing(c.cream) { toefl(c) }
    ExamArtKind.Ielts -> Drawing(c.red) { ielts(c) }
}

private fun Sketch.lgs(c: LexiColors) {
    ink = c.ink
    val cols = floatArrayOf(38f, 69f, 98f, 127.5f)
    val rows = floatArrayOf(24f, 47.5f, 71f)
    val filled = setOf(0 to 0, 0 to 3, 1 to 1, 2 to 2)
    rows.forEachIndexed { r, y ->
        cols.forEachIndexed { col, x ->
            circle(x, y, 9f, if ((r to col) in filled) c.ink else c.cream, line = true)
        }
    }
}

private fun Sketch.yds(c: LexiColors) {
    ink = c.ink
    path("M60 50V66Q85 80 110 66V50Z", c.cream, line = true)
    polygon(c.ink, true, 33f, 44f, 85f, 21f, 137f, 44f, 85f, 65f)
    stroke("M128 46V66", c.ink)
    circle(128f, 70f, 4f, c.red)
}

private fun Sketch.yksDil(c: LexiColors) {
    ink = c.ink
    rect(30f, 25f, 56f, 53f, c.cream, line = true)
    text("EN", 58f, 62f, 30f, c.ink)
    rect(86f, 25f, 53f, 53f, c.cream, line = true)
    stroke("M98 45H124M98 53H124M98 61H124", c.ink)
}

private fun Sketch.toefl(c: LexiColors) {
    ink = c.ink
    circle(84.5f, 46f, 35f, c.blue, line = true)
    stroke("M84.5 11V81M49.5 46H119.5M54 30H115M54 62H115", c.cream, width = 2.5f)
    stroke("M84.5 11C62 30 62 62 84.5 81M84.5 11C107 30 107 62 84.5 81", c.cream, width = 2.5f)
    circle(84.5f, 46f, 35f, null, line = true)
}

/** Needs design: IELTS is cut off in the draft; headphones around a speech bubble from the same primitives. */
private fun Sketch.ielts(c: LexiColors) {
    ink = c.ink
    stroke("M38 56A46 46 0 0 1 130 56", c.ink, width = 5f)
    rect(30f, 50f, 16f, 32f, c.yellow, line = true)
    rect(122f, 50f, 16f, 32f, c.yellow, line = true)
    polygon(c.cream, true, 66f, 66f, 66f, 82f, 80f, 66f)
    rect(56f, 38f, 56f, 28f, c.cream, line = true)
    rect(59f, 63f, 14f, 6f, c.cream)
    circle(72f, 52f, 3f, c.ink)
    circle(84f, 52f, 3f, c.ink)
    circle(96f, 52f, 3f, c.ink)
}

// endregion

// design sample, not user-facing
@Preview(widthDp = 880)
@Composable
private fun CategoryArtPreview() {
    LexiTheme {
        Column(Modifier.background(LexiTheme.colors.white).padding(8.dp), Arrangement.spacedBy(8.dp)) {
            CategoryArtKind.entries.chunked(5).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { CategoryArt(it, Modifier.size(163.dp, 95.dp)) }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExamArtKind.entries.forEach {
                    ExamArt(it, Modifier.size(163.dp, 95.dp), locked = it != ExamArtKind.Lgs)
                }
            }
        }
    }
}
