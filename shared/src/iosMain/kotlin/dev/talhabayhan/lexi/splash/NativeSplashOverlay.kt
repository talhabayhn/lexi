@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package dev.talhabayhan.lexi.splash

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import dev.talhabayhan.lexi.core.designsystem.component.SplashTimeline
import dev.talhabayhan.lexi.core.designsystem.component.SplashTrack
import dev.talhabayhan.lexi.core.designsystem.theme.CubicCurve
import dev.talhabayhan.lexi.core.designsystem.theme.StandardLexiMotion
import dev.talhabayhan.lexi.core.designsystem.theme.ViewportDefaults
import dev.talhabayhan.lexi.core.domain.AppInfo
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import kotlinx.cinterop.useContents
import platform.CoreFoundation.CFStringRef
import platform.CoreGraphics.CGAffineTransformIdentity
import platform.CoreGraphics.CGAffineTransformMakeScale
import platform.CoreGraphics.CGDataProviderCreateWithFilename
import platform.CoreGraphics.CGDataProviderRelease
import platform.CoreGraphics.CGFontCopyPostScriptName
import platform.CoreGraphics.CGFontCreateWithDataProvider
import platform.CoreGraphics.CGFontRelease
import platform.CoreGraphics.CGImageCreateWithImageInRect
import platform.CoreGraphics.CGImageRelease
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.CoreText.CTFontManagerRegisterGraphicsFont
import platform.Foundation.CFBridgingRelease
import platform.Foundation.NSAttributedString
import platform.Foundation.NSNumber
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.Foundation.setValue
import platform.QuartzCore.CABasicAnimation
import platform.QuartzCore.CAKeyframeAnimation
import platform.QuartzCore.CALayer
import platform.QuartzCore.CAMediaTimingFunction
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCAMediaTimingFunctionLinear
import platform.UIKit.NSFontAttributeName
import platform.UIKit.NSForegroundColorAttributeName
import platform.UIKit.NSKernAttributeName
import platform.UIKit.NSTextAlignmentCenter
import platform.UIKit.UIAccessibilityTraitImage
import platform.UIKit.UIColor
import platform.UIKit.UIContentSizeCategoryAccessibilityExtraExtraExtraLarge
import platform.UIKit.UIContentSizeCategoryAccessibilityExtraExtraLarge
import platform.UIKit.UIContentSizeCategoryAccessibilityExtraLarge
import platform.UIKit.UIContentSizeCategoryAccessibilityLarge
import platform.UIKit.UIContentSizeCategoryAccessibilityMedium
import platform.UIKit.UIContentSizeCategoryExtraExtraExtraLarge
import platform.UIKit.UIContentSizeCategoryExtraExtraLarge
import platform.UIKit.UIContentSizeCategoryExtraLarge
import platform.UIKit.UIContentSizeCategoryExtraSmall
import platform.UIKit.UIContentSizeCategoryLarge
import platform.UIKit.UIContentSizeCategoryMedium
import platform.UIKit.UIContentSizeCategorySmall
import platform.UIKit.UIEdgeInsets
import platform.UIKit.UIFont
import platform.UIKit.UIImage
import platform.UIKit.UIImageOrientation
import platform.UIKit.UIImageView
import platform.UIKit.UILabel
import platform.UIKit.UIScreen
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIView
import platform.UIKit.UIViewAutoresizingFlexibleHeight
import platform.UIKit.UIViewAutoresizingFlexibleWidth
import platform.UIKit.accessibilityLabel
import platform.UIKit.accessibilityTraits
import platform.UIKit.accessibilityViewIsModal
import platform.UIKit.isAccessibilityElement
import kotlin.math.PI
import kotlin.math.ceil

/**
 * The iOS native splash (ADR 0028, `motion.md` #9): the splash drawn with UIKit views and animated with Core Animation
 * only, so the render server plays it even while the main thread is busy (Compose's start-up, Skia / Metal pipeline
 * compiles, the first-launch warm-up). No per-frame main-thread work: [play] adds `CAKeyframeAnimation`s built from
 * [SplashTimeline] (the keyframes, easings and geometry `LexiSplash` uses) and returns.
 *
 * The four blocks are cut from the launch screen's own `LaunchLogo` image ([launchLogo], rendered from the Compose
 * logo), so their resting pixels are the launch screen's; the overlay's resting layout at font scale 1 is the launch
 * screen's (image centred on the screen). The shapes are plain coloured views (circle: corner radius), the tagline a
 * `UILabel` in Jost Bold. Everything sits in a design-unit stage scaled like `LexiViewport` (Expanded tier).
 *
 * Accessibility: one image element "Lexi" (plus the tagline once loaded), modal, so VoiceOver does not reach the
 * Compose screen being prepared under it. Light only ([UIUserInterfaceStyle.UIUserInterfaceStyleLight], ADR 0010).
 */
internal class NativeSplashOverlay(
    launchLogo: UIImage?,
    private val reducedMotion: Boolean,
    private val fontScale: Float = contentSizeFontScale(),
) {
    /** The overlay's root view: add it above the Compose view, full size. */
    val view: UIView = UIView(frame = CGRectMake(0.0, 0.0, DESIGN_WIDTH, DESIGN_HEIGHT))
    private val stage = UIView(frame = CGRectMake(0.0, 0.0, DESIGN_WIDTH, DESIGN_HEIGHT))
    private val sun = UIView(frame = CGRectMake(0.0, 0.0, SUN_SIDE, SUN_SIDE))
    private val blue = UIView(frame = CGRectMake(0.0, 0.0, BLUE_SIDE, BLUE_SIDE))
    private val blocks = List(BLOCK_COUNT) { UIImageView(frame = CGRectMake(0.0, 0.0, BLOCK_SIDE, BLOCK_SIDE)) }
    private val tagline = UILabel(frame = CGRectMake(0.0, 0.0, 0.0, 0.0))
    private val taglineFont = loadTaglineFont(SplashTimeline.taglineFontSizeSp * fontScale)
    private val restingTaglineHeight = lineHeight(loadTaglineFont(SplashTimeline.taglineFontSizeSp))

    /** The launch image's height (blocks, gap and the tagline's line at font scale 1), the column's height then. */
    private val restingColumnHeight =
        launchLogo?.size?.useContents { height } ?: (COLUMN_ABOVE_TAGLINE + restingTaglineHeight)

    /** True once [leave] has started. */
    var isLeaving: Boolean = false
        private set

    init {
        val colors = SplashTimeline.colors
        view.backgroundColor = colors.cream.toUIColor()
        view.overrideUserInterfaceStyle = UIUserInterfaceStyle.UIUserInterfaceStyleLight
        view.autoresizingMask = UIViewAutoresizingFlexibleWidth or UIViewAutoresizingFlexibleHeight
        view.isAccessibilityElement = true
        view.accessibilityLabel = AppInfo.NAME
        view.accessibilityTraits = UIAccessibilityTraitImage
        view.accessibilityViewIsModal = true
        // Absorbs touches: the screen under it is not shown yet.
        view.userInteractionEnabled = true
        view.addSubview(stage)

        sun.backgroundColor = colors.yellow.toUIColor()
        sun.layer.cornerRadius = SUN_SIDE / 2
        blue.backgroundColor = colors.blue.toUIColor()
        stage.addSubview(sun)
        stage.addSubview(blue)
        val cut = launchLogo?.let { cutBlocks(it) }
        blocks.forEachIndexed { index, block ->
            block.image = cut?.get(index)
            stage.addSubview(block)
        }
        tagline.textAlignment = NSTextAlignmentCenter
        stage.addSubview(tagline)
        setTagline("")
        layout(DESIGN_WIDTH, DESIGN_HEIGHT, safeArea = null)
        // The launch pose (the launch screen's image: the blocks at rest, no shapes, no tagline) until [play]; with
        // reduced motion the whole static logo at once (`motion.md`).
        if (!reducedMotion) setLaunchPose(true)
    }

    private fun setLaunchPose(launch: Boolean) {
        val shapeScale = NSNumber(double = if (launch) 0.0 else 1.0)
        sun.layer.setValue(shapeScale, forKeyPath = "transform.scale")
        blue.layer.setValue(shapeScale, forKeyPath = "transform.scale")
        tagline.layer.opacity = if (launch) 0f else 1f
    }

    /** The block views L, E, X, I (tests). */
    internal val blockViews: List<UIView> get() = blocks

    internal val sunView: UIView get() = sun
    internal val blueView: UIView get() = blue
    internal val taglineView: UILabel get() = tagline

    /** Sets the tagline's text ("A'DAN Z'YE İNGİLİZCE"), loaded from the app's string resources once they are read. */
    fun setTagline(text: String) {
        tagline.attributedText = NSAttributedString.create(
            string = text,
            attributes = mapOf<Any?, Any?>(
                NSFontAttributeName to taglineFont,
                NSKernAttributeName to NSNumber(
                    double = SplashTimeline.TAGLINE_LETTER_SPACING_EM.toDouble() * taglineFont.pointSize,
                ),
                NSForegroundColorAttributeName to SplashTimeline.colors.ink.toUIColor(),
            ),
        )
        view.accessibilityLabel = if (text.isEmpty()) AppInfo.NAME else "${AppInfo.NAME}, $text"
        layoutContent()
    }

    /**
     * Lays the stage out for a [width] x [height] (points) screen: scaled like `LexiViewport` for the space inside
     * [safeArea] (scale 1 up to the 390 x 844 design), centred, shapes in their corners, the column centred.
     */
    fun layout(width: Double, height: Double, safeArea: CValue<UIEdgeInsets>?) {
        val (insetWidth, insetHeight) = safeArea?.useContents { (left + right) to (top + bottom) } ?: (0.0 to 0.0)
        val scale = ViewportDefaults.scale(
            width = Dp((width - insetWidth).toFloat()),
            height = Dp((height - insetHeight).toFloat()),
        ).toDouble()
        view.setFrame(CGRectMake(0.0, 0.0, width, height))
        stage.setTransform(CGAffineTransformIdentity.readValue())
        stage.setFrame(CGRectMake(0.0, 0.0, width / scale, height / scale))
        stage.setCenter(CGPointMake(width / 2, height / 2))
        stage.setTransform(CGAffineTransformMakeScale(scale, scale))
        stageWidth = width / scale
        stageHeight = height / scale
        layoutContent()
    }

    private var stageWidth = DESIGN_WIDTH
    private var stageHeight = DESIGN_HEIGHT

    private fun layoutContent() {
        val offset = SplashTimeline.sunOffset.value.toDouble()
        sun.setFrame(CGRectMake(offset, offset, SUN_SIDE, SUN_SIDE))
        val blueOffset = SplashTimeline.blueOffset.value.toDouble()
        blue.setFrame(
            CGRectMake(stageWidth - BLUE_SIDE - blueOffset, stageHeight - BLUE_SIDE - blueOffset, BLUE_SIDE, BLUE_SIDE),
        )
        // The column (blocks, gap, tagline) is centred; at font scale 1 its blocks sit where the launch image has them.
        val labelSize = tagline.sizeThatFits(CGSizeMake(stageWidth, stageHeight))
        val labelHeight = lineHeight(taglineFont)
        val labelWidth = labelSize.useContents { width }
        val blocksTop = (stageHeight - restingColumnHeight) / 2 - (labelHeight - restingTaglineHeight) / 2
        val rowLeft = (stageWidth - ROW_WIDTH) / 2
        blocks.forEachIndexed { index, block ->
            block.setFrame(CGRectMake(rowLeft + index * (BLOCK_SIDE + BLOCK_GAP), blocksTop, BLOCK_SIDE, BLOCK_SIDE))
        }
        tagline.setFrame(
            CGRectMake((stageWidth - labelWidth) / 2, blocksTop + COLUMN_ABOVE_TAGLINE, labelWidth, labelHeight),
        )
    }

    /**
     * Starts the animation (`motion.md` #9, [SplashTimeline], [StandardLexiMotion.splashDurationMillis]) after
     * [handOffMillis] of launch pose; [onEnded] runs on the main thread when Core Animation has finished it (also when
     * the system removed it early, e.g. the app went to the background: every layer's model value is its resting pose,
     * so nothing is left mid-way). The timeline begins when the transaction is committed, i.e. with the first frame
     * shown, however late that is (a fixed begin time in the past skipped the whole animation on a slow first launch);
     * [animationBeginTime] gives it afterwards. The hand-off is part of the same timeline, so the render server starts
     * the animation on time even if the main thread is blocked then. Nothing is animated with reduced motion (the
     * resting pose is the static logo); [onEnded] is not called then.
     */
    fun play(handOffMillis: Long, onEnded: () -> Unit) {
        if (reducedMotion) return
        val timing = TrackTiming(
            leadIn = handOffMillis / MILLIS_PER_SECOND,
            duration = StandardLexiMotion.splashDurationMillis / MILLIS_PER_SECOND,
        )
        val curve = SplashTimeline.curve
        CATransaction.begin()
        CATransaction.setDisableActions(true)
        CATransaction.setCompletionBlock(onEnded)
        // The model values are the resting pose: what stays once the animation is over (or removed early).
        setLaunchPose(false)
        sun.layer.addTrack("transform.scale", SplashTimeline.shapeScale, curve, timing, launchValue = 0.0)
        blue.layer.addTrack("transform.scale", SplashTimeline.shapeScale, curve, timing, launchValue = 0.0)
        blocks[0].layer.addTrack("transform.translation.x", SplashTimeline.blockL, curve, timing, launchValue = 0.0)
        blocks[1].layer.addTrack("transform.translation.y", SplashTimeline.blockE, curve, timing, launchValue = 0.0)
        blocks[2].layer.addTrack(
            "transform.rotation.z",
            SplashTimeline.blockXSpin,
            curve,
            timing,
            launchValue = 0.0,
            factor = PI / DEGREES_HALF_TURN,
        )
        blocks[2].layer.addTrack("transform.scale", SplashTimeline.blockXScale, curve, timing, launchValue = 1.0)
        blocks[3].layer.addTrack("transform.translation.y", SplashTimeline.blockI, curve, timing, launchValue = 0.0)
        val taglineCurve = SplashTimeline.taglineCurve
        tagline.layer.addTrack("opacity", SplashTimeline.taglineAlpha, taglineCurve, timing, launchValue = 0.0)
        tagline.layer.addTrack(
            "transform.translation.y",
            SplashTimeline.taglineRise,
            taglineCurve,
            timing,
            launchValue = 0.0,
            factor = SplashTimeline.taglineRiseDistance.value.toDouble(),
        )
        CATransaction.commit()
    }

    /**
     * Commits what [play] added now, if the overlay is in a window (`CATransaction.flush`), instead of at the end of
     * the run-loop pass: the transaction then holds the overlay alone, so its timeline begins at once.
     */
    fun commitNow() {
        if (view.window != null) CATransaction.flush()
    }

    /**
     * The `CACurrentMediaTime` at which [play]'s timeline began, known once its transaction has been committed (null
     * before, or with nothing playing).
     */
    fun animationBeginTime(): Double? {
        val layer = blocks[0].layer
        val begin = layer.animationForKey("splash.transform.translation.x")?.beginTime ?: return null
        return if (begin > 0.0) layer.convertTime(begin, toLayer = null) else null
    }

    /**
     * Reveals the screen below (the old screen's half of #12, `motion.md`): the overlay moves 24 dp to the left over
     * [StandardLexiMotion.screenTransitionDurationMillis] and fades out over its first
     * [StandardLexiMotion.screenTransitionFadeOutMillis], `mechanical`; with reduced motion a
     * [StandardLexiMotion.screenTransitionReducedCrossfadeMillis] crossfade. Touches pass through from now on. Returns
     * how long it takes: remove the view after that ([remove]).
     */
    fun leave(): Long {
        isLeaving = true
        view.userInteractionEnabled = false
        val motion = StandardLexiMotion
        val curve = SplashTimeline.curve.toTimingFunction()
        CATransaction.begin()
        val fadeMillis =
            if (reducedMotion) motion.screenTransitionReducedCrossfadeMillis else motion.screenTransitionFadeOutMillis
        val fade = CABasicAnimation.animationWithKeyPath("opacity").apply {
            fromValue = NSNumber(double = 1.0)
            toValue = NSNumber(double = 0.0)
            duration = fadeMillis / MILLIS_PER_SECOND
            timingFunction = curve
        }
        view.layer.opacity = 0f
        view.layer.addAnimation(fade, forKey = "leave.opacity")
        var total = fadeMillis.toLong()
        if (!reducedMotion) {
            val distance = -motion.screenTransitionSlideDistance.value.toDouble()
            val slide = CABasicAnimation.animationWithKeyPath("transform.translation.x").apply {
                fromValue = NSNumber(double = 0.0)
                toValue = NSNumber(double = distance)
                duration = motion.screenTransitionDurationMillis / MILLIS_PER_SECOND
                timingFunction = curve
            }
            view.layer.setValue(NSNumber(double = distance), forKeyPath = "transform.translation.x")
            view.layer.addAnimation(slide, forKey = "leave.slide")
            total = motion.screenTransitionDurationMillis.toLong()
        }
        CATransaction.commit()
        return total
    }

    /** Takes the overlay out of the view hierarchy (its animations go with it). */
    fun remove() {
        view.layer.removeAllAnimations()
        view.removeFromSuperview()
    }

    private companion object {
        const val BLOCK_COUNT = 4
        const val MILLIS_PER_SECOND = 1000.0
        const val DEGREES_HALF_TURN = 180.0
        val DESIGN_WIDTH = ViewportDefaults.DesignWidth.value.toDouble()
        val DESIGN_HEIGHT = ViewportDefaults.DesignHeight.value.toDouble()
        val SUN_SIDE = SplashTimeline.sunSize.value.toDouble()
        val BLUE_SIDE = SplashTimeline.blueSize.value.toDouble()
        val BLOCK_SIDE = SplashTimeline.blockSize.value.toDouble()
        val BLOCK_GAP = SplashTimeline.blockGap.value.toDouble()
        val ROW_WIDTH = BLOCK_COUNT * BLOCK_SIDE + (BLOCK_COUNT - 1) * BLOCK_GAP

        /** Blocks plus the gap above the tagline. */
        val COLUMN_ABOVE_TAGLINE = BLOCK_SIDE + SplashTimeline.contentGap.value.toDouble()
    }
}

/**
 * The four blocks cut from the launch image: each [SplashTimeline.blockSize] square, [SplashTimeline.blockGap] apart,
 * at the image's top (the image is the logo column cropped to the blocks' left, top and right edges).
 */
private fun cutBlocks(image: UIImage): List<UIImage>? {
    val source = image.CGImage ?: return null
    val pixels = image.scale
    val side = SplashTimeline.blockSize.value * pixels
    val step = (SplashTimeline.blockSize.value + SplashTimeline.blockGap.value) * pixels
    return List(4) { index ->
        val cut = CGImageCreateWithImageInRect(source, CGRectMake(index * step, 0.0, side, side)) ?: return null
        val block = UIImage.imageWithCGImage(cut, scale = pixels, orientation = UIImageOrientation.UIImageOrientationUp)
        CGImageRelease(cut)
        block
    }
}

/** A track's timeline in seconds: [leadIn] of launch pose, then the animation's [duration]. */
internal class TrackTiming(val leadIn: Double, val duration: Double) {
    val total: Double get() = leadIn + duration

    /** Key time (0..1 of [total]) of the animation's [fraction]. */
    fun keyTime(fraction: Float): Double = (leadIn + fraction * duration) / total
}

/**
 * Adds [track] on [keyPath] as one `CAKeyframeAnimation` over [TrackTiming.total]: first [launchValue] (the launch
 * pose, × 1) for [TrackTiming.leadIn], then at once the animation's own stops (values × [factor]) with the [curve] per
 * interval like the CSS keyframes, the last value held to the end. The layer's model value stays its resting one (each
 * track ends at rest).
 */
private fun CALayer.addTrack(
    keyPath: String,
    track: SplashTrack,
    curve: CubicCurve,
    timing: TrackTiming,
    launchValue: Double,
    factor: Double = 1.0,
) {
    val values = mutableListOf<Any?>()
    val times = mutableListOf<Any?>()
    val functions = mutableListOf<Any?>()
    fun stop(time: Double, value: Double, function: CAMediaTimingFunction?) {
        if (function != null) functions += function
        values += NSNumber(double = value)
        times += NSNumber(double = time)
    }
    val linear = CAMediaTimingFunction.functionWithName(kCAMediaTimingFunctionLinear)
    if (timing.leadIn > 0) {
        stop(0.0, launchValue, null)
        // The jump from the launch pose to the first pose: one stop a hair before the animation's start.
        stop(timing.keyTime(0f) - HAND_OFF_JUMP / timing.total, launchValue, linear)
    }
    for (i in 0 until track.size) {
        val function = when {
            i > 0 -> curve.toTimingFunction()
            timing.leadIn > 0 -> linear
            else -> null
        }
        stop(timing.keyTime(track.fractionAt(i)), track.valueAt(i) * factor, function)
    }
    if (track.fractionAt(track.size - 1) < 1f) stop(1.0, track.valueAt(track.size - 1) * factor, linear)
    val animation = CAKeyframeAnimation.animationWithKeyPath(keyPath).apply {
        this.values = values
        keyTimes = times
        timingFunctions = functions
        duration = timing.total
    }
    addAnimation(animation, forKey = "splash.$keyPath")
}

/** Seconds between the launch pose's last stop and the animation's first: a jump (well under a frame). */
private const val HAND_OFF_JUMP = 0.000_1

private fun CubicCurve.toTimingFunction(): CAMediaTimingFunction =
    CAMediaTimingFunction.functionWithControlPoints(x1, y1, x2, y2)

private fun Color.toUIColor(): UIColor =
    UIColor.colorWithRed(red.toDouble(), green = green.toDouble(), blue = blue.toDouble(), alpha = alpha.toDouble())

private fun lineHeight(font: UIFont): Double = ceil(font.lineHeight)

/**
 * Jost Bold from the app's resources at [size] points (registered for this process once); the system's bold font if
 * the file cannot be read (tests without the app bundle).
 */
private fun loadTaglineFont(size: Float): UIFont {
    val name = TaglineFontName.value
    return name?.let { UIFont.fontWithName(it, size.toDouble()) } ?: UIFont.boldSystemFontOfSize(size.toDouble())
}

private object TaglineFontName {
    val value: String? by lazy { registerTaglineFont() }

    private fun registerTaglineFont(): String? {
        val path = NSURL.URLWithString(SplashTimeline.taglineFontUri())?.path ?: return null
        val provider = CGDataProviderCreateWithFilename(path) ?: return null
        val font = CGFontCreateWithDataProvider(provider)
        CGDataProviderRelease(provider)
        if (font == null) return null
        // Already registered (a second overlay in the same process) also leaves the font usable.
        CTFontManagerRegisterGraphicsFont(font, null)
        val postScript: CFStringRef? = CGFontCopyPostScriptName(font)
        CGFontRelease(font)
        return postScript?.let { CFBridgingRelease(it) as? String }
    }
}

/**
 * The font scale Compose uses for the content size category (`uiContentSizeCategoryToFontScaleMap` in Compose 1.12.1's
 * `Extensions.ios.kt`, copied: it is internal there), so the native tagline has the size of the Compose one.
 */
internal fun contentSizeFontScale(): Float {
    val category = UIScreen.mainScreen.traitCollection.preferredContentSizeCategory
    return ComposeFontScales[category] ?: 1f
}

private val ComposeFontScales: Map<String?, Float> = mapOf(
    UIContentSizeCategoryExtraSmall to 0.8f,
    UIContentSizeCategorySmall to 0.85f,
    UIContentSizeCategoryMedium to 0.9f,
    UIContentSizeCategoryLarge to 1f,
    UIContentSizeCategoryExtraLarge to 1.1f,
    UIContentSizeCategoryExtraExtraLarge to 1.2f,
    UIContentSizeCategoryExtraExtraExtraLarge to 1.3f,
    UIContentSizeCategoryAccessibilityMedium to 1.4f,
    UIContentSizeCategoryAccessibilityLarge to 1.5f,
    UIContentSizeCategoryAccessibilityExtraLarge to 1.6f,
    UIContentSizeCategoryAccessibilityExtraExtraLarge to 1.7f,
    UIContentSizeCategoryAccessibilityExtraExtraExtraLarge to 1.8f,
)
