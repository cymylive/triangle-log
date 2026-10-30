package com.cymylive.trianglelog.web

import java.net.Inet4Address
import java.net.NetworkInterface

/** 取本机局域网 IPv4 地址 */
object NetUtils {
    fun localIp(): String? {
        try {
            val list = NetworkInterface.getNetworkInterfaces()
            for (nif in list) {
                if (!nif.isUp || nif.isLoopback) continue
                for (addr in nif.inetAddresses) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        val ip = addr.hostAddress
                        if (ip != null && !ip.startsWith("169.254")) return ip
                    }
                }
            }
        } catch (e: Exception) {
            return null
        }
        return null
    }
}
