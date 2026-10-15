package dev.talhabayhan.lexi.core.data.db

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Opens a database once, on first use and on the IO dispatcher, so no caller ever opens it on the main thread.
 * A failed open is retried by the next caller.
 */
internal class LazyDatabase<T : Any>(private val io: CoroutineDispatcher, private val open: suspend () -> T) {
    private val mutex = Mutex()
    private var value: T? = null

    suspend fun get(): T = mutex.withLock {
        value ?: withContext(io) { open() }.also { value = it }
    }
}
