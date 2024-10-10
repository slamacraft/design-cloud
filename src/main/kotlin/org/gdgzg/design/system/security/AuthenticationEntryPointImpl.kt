package org.gdgzg.design.system.security

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.gdgzg.design.common.model.RespWrapper
import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

/**
 * 登录失败处理端点
 */
@Component
class AuthenticationEntryPointImpl(val objectMapper: ObjectMapper) : AuthenticationEntryPoint {
    init {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException?
    ) {
        response.status = 200
        response.contentType = "application/json"
        response.characterEncoding = "utf-8"
        response.writer.print(
            objectMapper.writeValueAsString(
                RespWrapper(
                    code = HttpStatus.UNAUTHORIZED.value(),
                    msg = "请求地址：${request.requestURI}，认证失败"
                )
            )
        )
    }
}