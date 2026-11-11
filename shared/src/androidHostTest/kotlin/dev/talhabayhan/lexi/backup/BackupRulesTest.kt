package dev.talhabayhan.lexi.backup

import dev.talhabayhan.lexi.reminder.NOTIFICATION_PERMISSION_PREFS
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The app's backup rules (androidApp resources) exclude only the device-specific notification-permission prefs. */
class BackupRulesTest {
    private val appMain = File("../androidApp/src/main")
    private val onlyPermissionPrefs = listOf("exclude sharedpref $NOTIFICATION_PERMISSION_PREFS.xml")

    private fun parse(file: File): Element =
        DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }.newDocumentBuilder().parse(file)
            .documentElement

    private fun Element.children(): List<Element> =
        List(childNodes.length) { childNodes.item(it) }.filterIsInstance<Element>()

    private fun Element.rules(): List<String> =
        children().map { "${it.tagName} ${it.getAttribute("domain")} ${it.getAttribute("path")}" }

    @Test
    fun manifestPointsAtBothRuleFiles() {
        val application = parse(File(appMain, "AndroidManifest.xml")).children().single { it.tagName == "application" }
        val android = "http://schemas.android.com/apk/res/android"

        assertEquals("true", application.getAttributeNS(android, "allowBackup"))
        assertEquals("@xml/backup_rules", application.getAttributeNS(android, "fullBackupContent"))
        assertEquals("@xml/data_extraction_rules", application.getAttributeNS(android, "dataExtractionRules"))
    }

    @Test
    fun legacyFullBackupExcludesOnlyThePermissionPrefs() {
        val root = parse(File(appMain, "res/xml/backup_rules.xml"))

        assertEquals("full-backup-content", root.tagName)
        assertEquals(onlyPermissionPrefs, root.rules())
    }

    @Test
    fun cloudBackupAndDeviceTransferExcludeOnlyThePermissionPrefs() {
        val root = parse(File(appMain, "res/xml/data_extraction_rules.xml"))

        assertEquals("data-extraction-rules", root.tagName)
        assertEquals(listOf("cloud-backup", "device-transfer"), root.children().map { it.tagName })
        for (section in root.children()) {
            assertEquals(onlyPermissionPrefs, section.rules(), section.tagName)
            assertTrue(section.attributes.length == 0, "no section attributes change the defaults")
        }
    }
}
