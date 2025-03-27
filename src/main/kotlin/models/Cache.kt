package net.blophy.workspace.models

import io.github.crackthecodeabhi.kreds.connection.Endpoint
import io.github.crackthecodeabhi.kreds.connection.KredsClient
import io.github.crackthecodeabhi.kreds.connection.newClient
import net.blophy.workspace.Config

class KredsResourceWrapper(private val origin: KredsClient) {
    suspend fun set(key: String, value: String, expireSeconds: ULong? = null) {
        origin.set(key, value)
        if (expireSeconds != null)
            origin.expire(key, expireSeconds)
    }

    suspend fun exists(key: String): Boolean = origin.exists(key) == 1.toLong()
    suspend fun get(key: String): String? = origin.get(key)
    suspend fun del(key: String) = origin.del(key)
}

object Cache {
    suspend fun <R> useWrapper(body: suspend (KredsResourceWrapper) -> R): R {
        return newClient(Endpoint.from(Config.redis)).use { client ->
            body(KredsResourceWrapper(client))
        }
    }
}

suspend fun <R> useCache(body: suspend (KredsResourceWrapper) -> R): R {
    return Cache.useWrapper(body)
}
