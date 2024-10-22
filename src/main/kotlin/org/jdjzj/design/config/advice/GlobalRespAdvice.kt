package org.jdjzj.design.config.advice

import org.jdjzj.design.common.model.PlainText
import org.jdjzj.design.common.model.RespWrapper
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice

private val EXPAND_CLASS = listOf(
    PlainText::class.java
)

@RestControllerAdvice("org.jdjzj.design")
class GlobalRespAdvice : ResponseBodyAdvice<Any> {

    override fun supports(
        returnType: MethodParameter,
        converterType: Class<out HttpMessageConverter<*>>
    ): Boolean {
        val returnClazz = returnType.parameterType
        return !EXPAND_CLASS.contains(returnClazz)
    }

    override fun beforeBodyWrite(
        body: Any?,
        returnType: MethodParameter,
        selectedContentType: MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>>,
        request: ServerHttpRequest,
        response: ServerHttpResponse
    ): Any? {
        return RespWrapper(
            code = 200,
            data = body
        )
    }

}