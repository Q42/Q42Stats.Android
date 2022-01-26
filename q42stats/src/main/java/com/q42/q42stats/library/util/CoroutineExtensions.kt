package com.q42.q42stats.library.util

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout

internal suspend inline fun <T> suspendCoroutineWithTimeout(
    timeoutMs: Long,
    crossinline block: (CancellableContinuation<T>) -> Unit
) = withTimeout(timeoutMs) {
    suspendCancellableCoroutine(block = block)
}