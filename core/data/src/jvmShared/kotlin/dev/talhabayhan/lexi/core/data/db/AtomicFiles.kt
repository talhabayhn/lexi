package dev.talhabayhan.lexi.core.data.db

import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/** java.io file helpers shared by the Android and JVM targets (`src/jvmShared`). */
internal object AtomicFiles {
    /**
     * Replaces the SQLite file [target] with [bytes]: temp file in the same directory (synced to disk), stale side
     * files of the old database removed, then an atomic rename over [target]. A crash leaves either the old or the
     * new file, never a partial one.
     */
    fun replaceDatabaseFile(target: File, bytes: ByteArray) {
        val dir = checkNotNull(target.absoluteFile.parentFile) { "no parent directory: $target" }
        dir.mkdirs()
        val temp = File(dir, "${target.name}.tmp")
        try {
            FileOutputStream(temp).use { out ->
                out.write(bytes)
                out.fd.sync()
            }
            DatabaseFiles.SIDE_FILE_SUFFIXES.forEach { File(dir, target.name + it).delete() }
            Files.move(
                temp.toPath(),
                target.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE,
            )
        } finally {
            temp.delete()
        }
    }
}
