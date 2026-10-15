package dev.talhabayhan.lexi.core.data.db

import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/** java.io file helpers shared by the Android and JVM targets (`src/jvmShared`). */
internal object AtomicFiles {
    /**
     * Replaces the SQLite file [target] with [bytes]: temp file in the same directory (synced to disk), stale side
     * files of the old database removed, then an atomic rename over [target]. A crash or a failed write/rename leaves
     * the old file in place and no temp file behind. [write] and [move] are seams for failure tests.
     */
    fun replaceDatabaseFile(
        target: File,
        bytes: ByteArray,
        write: (File, ByteArray) -> Unit = ::writeSynced,
        move: (Path, Path) -> Unit = ::moveAtomically,
    ) {
        val dir = checkNotNull(target.absoluteFile.parentFile) { "no parent directory: $target" }
        dir.mkdirs()
        val temp = File(dir, "${target.name}.tmp")
        try {
            write(temp, bytes)
            DatabaseFiles.SIDE_FILE_SUFFIXES.forEach { File(dir, target.name + it).delete() }
            move(temp.toPath(), target.toPath())
        } finally {
            temp.delete()
        }
    }

    /** Writes [bytes] and fsyncs before returning. */
    fun writeSynced(file: File, bytes: ByteArray) {
        FileOutputStream(file).use { out ->
            out.write(bytes)
            out.fd.sync()
        }
    }

    fun moveAtomically(from: Path, to: Path) {
        Files.move(from, to, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
    }
}
