package dev.talhabayhan.lexi.core.designsystem.motion

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.toSize

/**
 * Window rectangles of elements that a [MorphOverlayHost] morph starts from or lands on (the OYNA button on one
 * screen, the wheel marker on another). Each key has one owner at a time: the newest layout wins, and a leaving
 * screen's release does not remove the entering screen's rectangle.
 */
@Stable
public class MorphAnchors {
    private val entries = mutableStateMapOf<Any, Pair<Any, Rect>>()

    /** Window rectangle of [key], or null when nothing with that key is laid out. */
    public operator fun get(key: Any): Rect? = entries[key]?.second

    public fun update(key: Any, owner: Any, rect: Rect) {
        val current = entries[key]
        if (current == null || current.first !== owner || current.second != rect) entries[key] = owner to rect
    }

    public fun release(key: Any, owner: Any) {
        if (entries[key]?.first === owner) entries.remove(key)
    }
}

/** The anchors of the nearest [MorphOverlayHost]; null outside one ([morphAnchor] then does nothing). */
public val LocalMorphAnchors: androidx.compose.runtime.ProvidableCompositionLocal<MorphAnchors?> =
    staticCompositionLocalOf { null }

/**
 * Publishes this element's window rectangle under [key] to the nearest [MorphOverlayHost] after every layout
 * (`onGloballyPositioned` as a modifier node: no recomposition) and removes it when the element leaves.
 */
public fun Modifier.morphAnchor(key: Any): Modifier = this then MorphAnchorElement(key)

private data class MorphAnchorElement(val key: Any) : ModifierNodeElement<MorphAnchorNode>() {
    override fun create(): MorphAnchorNode = MorphAnchorNode(key)

    override fun update(node: MorphAnchorNode) {
        if (node.key != key) {
            node.releaseAnchor()
            node.key = key
        }
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "morphAnchor"
        properties["key"] = key
    }
}

private class MorphAnchorNode(var key: Any) :
    Modifier.Node(),
    GlobalPositionAwareModifierNode,
    CompositionLocalConsumerModifierNode {
    private var anchors: MorphAnchors? = null

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        val registry = currentValueOf(LocalMorphAnchors) ?: return
        anchors = registry
        registry.update(key, this, Rect(coordinates.positionInWindow(), coordinates.size.toSize()))
    }

    fun releaseAnchor() {
        anchors?.release(key, this)
    }

    override fun onDetach() {
        releaseAnchor()
        anchors = null
    }
}
