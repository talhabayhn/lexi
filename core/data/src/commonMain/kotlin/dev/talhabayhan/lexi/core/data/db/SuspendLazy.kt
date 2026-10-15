package dev.talhabayhan.lexi.core.data.db

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Computes a value once (e.g. opens a database), on first use and on the IO dispatcher, so no caller ever opens a
 * database on the main thread. A failed computation is retried by the next caller.
 */
internal class SuspendLazy<T : Any>(private val io: CoroutineDispatcher, private val open: suspend () -> T) {
    private val mutex = Mutex()
    private var value: T? = null

    suspend fun get(): T = mutex.withLock {
        value ?: withContext(io) { open() }.also { value = it }
    }
}
