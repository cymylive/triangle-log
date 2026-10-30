package com.cymylive.trianglelog.web

import android.content.Context

/** 保存局域网服务实例，避免 Composable 重组时重复启动 */
object ServerHolder {
    private var server: LocalWebServer? = null

    val running: Boolean get() = server?.isAlive == true

    fun start(context: Context): String? {
        if (server?.isAlive == true) return NetUtils.localIp()
        val s = LocalWebServer(context.applicationContext)
        return try {
            s.start(SOCKET_READ_TIMEOUT, false)
            server = s
            NetUtils.localIp()
        } catch (e: Exception) {
            server = null
            null
        }
    }

    fun stop() {
        try {
            server?.stop()
        } catch (e: Exception) {
        }
        server = null
    }

    const val PORT = 8765
    private const val SOCKET_READ_TIMEOUT = 5000
}
