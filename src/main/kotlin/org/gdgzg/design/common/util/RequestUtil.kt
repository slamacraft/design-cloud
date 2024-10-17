package org.gdgzg.design.common.util

import cn.hutool.core.util.StrUtil
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

object RequestUtil {

    fun getRequest(): HttpServletRequest? {
        val servletRequestAttributes = RequestContextHolder.getRequestAttributes() as ServletRequestAttributes?
        return servletRequestAttributes?.request ?: null
    }

    /**
     * 从多级反向代理中获得第一个非unknown IP地址
     *
     * @param ip 获得的IP地址
     * @return 第一个非unknown IP地址
     */
    fun getMultistageReverseProxyIp(ip: String?): String {
        return ip?.split(",")
            ?.map(String::trim)
            ?.firstOrNull(String?::isValidIp) ?: ""
    }
}

fun HttpServletRequest.getIp(): String {
    var ip: String? = getHeader("x-forwarded-for")
    if (!ip.isValidIp()) {
        ip = getHeader("Proxy-Client-IP")
    }
    if (!ip.isValidIp()) {
        ip = getHeader("X-Forwarded-For")
    }
    if (!ip.isValidIp()) {
        ip = getHeader("WL-Proxy-Client-IP")
    }
    if (!ip.isValidIp()) {
        ip = getHeader("X-Real-IP")
    }
    if (!ip.isValidIp()) {
        ip = remoteAddr
    }

    return if (StrUtil.equals(ip, "0:0:0:0:0:0:0:1")) {
        "127.0.0.1"
    } else {
        RequestUtil.getMultistageReverseProxyIp(ip)
    }
}

private fun String?.isValidIp(): Boolean {
    return this != null
            && StrUtil.isNotBlank(this)
            && !StrUtil.equals(this, "unknown", true)
}