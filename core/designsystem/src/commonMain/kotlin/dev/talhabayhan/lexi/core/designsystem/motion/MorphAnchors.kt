package dev.talhabayhan.lexi.core.designsystem.motion

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.platform.InspectorInfo

/**
 * Window rectangles of elements that a [MorphOverlayHost] morph starts from or lands on (the OYNA button on one
 * screen, the wheel marker on another). Several elements may share a key while screens animate in and out: the most
 * recently **attached** one that has been laid out wins, so a leaving screen never takes the key back from the
 * entering one, and popping the newer screen hands the key back to the older one.
 */
@Stable
public class MorphAnchors {
    private class Entry(val owner: Any, val rect: Rect?)

    private val entries = mutableStateMapOf<Any, List<Entry>>()

    /** Window rectangle of [key], or null when nothing with that key is laid out. */
    public operator fun get(key: Any): Rect? = entries[key]?.lastOrNull { it.rect != null }?.rect

    /** [owner] starts using [key] (newest wins); its rectangle follows with [update]. */
    public fun attach(key: Any, owner: Any) {
        val list = entries[key].orEmpty()
        if (list.none { it.owner === owner }) entries[key] = list + Entry(owner, null)
    }

    /** New window rectangle of [owner] under [key]; attaches it first if needed. */
    public fun update(key: Any, owner: Any, rect: Rect) {
        val list = entries[key].orEmpty()
        val index = list.indexOfFirst { it.owner === owner }
        when {
            index < 0 -> entries[key] = list + Entry(owner, rect)
            list[index].rect != rect -> entries[key] = list.toMutableList().also { it[index] = Entry(owner, rect) }
        }
    }

    public fun release(key: Any, owner: Any) {
        val list = entries[key] ?: return
        val remaining = list.filterNot { it.owner === owner }
        if (remaining.isEmpty()) entries.remove(key) else if (remaining.size != list.size) entries[key] = remaining
    }
}

/** The anchors of the nearest [MorphOverlayHost]; null outside one ([morphAnchor] then does nothing). */
public val LocalMorphAnchors: ProvidableCompositionLocal<MorphAnchors?> = staticCompositionLocalOf { null }

/**
 * Publishes this element's window rectangle under [key] to the nearest [MorphOverlayHost] after every layout (a
 * modifier node: no recomposition). The rectangle includes layer transforms (e.g. a press scale) and is not clipped
 * by parents. Removed when the element leaves.
 */
public fun Modifier.morphAnchor(key: Any): Modifier = this then MorphAnchorElement(key)

private data class MorphAnchorElement(val key: Any) : ModifierNodeElement<MorphAnchorNode>() {
    override fun create(): MorphAnchorNode = MorphAnchorNode(key)

    override fun update(node: MorphAnchorNode) {
        node.changeKey(key)
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
    private var lastRect: Rect? = null

    override fun onAttach() {
        anchors = currentValueOf(LocalMorphAnchors)?.also { it.attach(key, this) }
    }

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        val root = coordinates.findRootCoordinates()
        val rect = root.localBoundingBoxOf(coordinates, clipBounds = false).translate(root.positionInWindow())
        lastRect = rect
        anchors?.update(key, this, rect)
    }

    fun changeKey(newKey: Any) {
        if (newKey == key) return
        anchors?.release(key, this)
        key = newKey
        anchors?.let { registry ->
            val rect = lastRect
            if (rect != null) registry.update(newKey, this, rect) else registry.attach(newKey, this)
        }
    }

    override fun onDetach() {
        anchors?.release(key, this)
        anchors = null
        lastRect = null
    }
}
