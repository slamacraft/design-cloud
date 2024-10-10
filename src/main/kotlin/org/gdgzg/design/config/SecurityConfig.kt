package org.gdgzg.design.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.gdgzg.design.common.annotation.Anonymous
import org.gdgzg.design.common.annotation.AnonymousType
import org.gdgzg.design.common.model.RespWrapper
import org.gdgzg.design.system.security.AuthenticationEntryPointImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.stereotype.Component
import org.springframework.web.filter.CorsFilter
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import java.util.regex.Pattern

/**
 * 系统安全配置
 *
 * 这里会写跨域，登录授权，以及免登录等相关配置
 */

// 匹配路径里类似/{id}之类的正则
private val PATTERN: Pattern = Pattern.compile("\\{(.*?)\\}")

@Component
class SecurityConfig {
    @Autowired
    lateinit var authenticationEntryPoint: AuthenticationEntryPointImpl

    @Autowired
    lateinit var requestMappingHandlerMapping: RequestMappingHandlerMapping

    /**
     * 跨域过滤器
     */
    @Autowired
    lateinit var corsFilter: CorsFilter

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf {
            it.disable() // 禁用CSRF保护
        }.exceptionHandling {
            // 认证失败处理
            it.authenticationEntryPoint(authenticationEntryPoint)
        }.sessionManagement {
            // 不使用session
            it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        }.authorizeHttpRequests {
            // 登录以及验证接口可以匿名调用
            it.requestMatchers("/login", "/register", "/captchaImage")
                .anonymous()

            // 静态资源直接放开校验
            it.requestMatchers("/swagger-ui.html", "/swagger-resources/**", "/webjars/**", "/*/api-docs", "/druid/**")
                .permitAll()
            it.requestMatchers(HttpMethod.GET, "/", "/*.html", "/**/*.html", "/**/*.css", "/**/*.js", "/profile/**")
                .permitAll()

            // 将所有通过@Anonymous注解标注的接口或类开放校验
            setAnonymousAnnotationPath(it)
        }.headers { headers ->
            headers.frameOptions {
                it.disable()
            }
        }
        return http.build()
    }

    /**
     * 将所有通过[Anonymous]注解标注的接口或类,放开校验
     */
    private fun setAnonymousAnnotationPath(it: AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry) {
        requestMappingHandlerMapping.handlerMethods.forEach { (info, handlerMethod) ->
            val paths = info.listPath()

            // 方法上的匿名注解
            AnnotationUtils.findAnnotation(handlerMethod.method, Anonymous::class.java)?.let { annotation ->
                it.permitPath(paths, annotation)
            }
            // 类上的注解
            AnnotationUtils.findAnnotation(handlerMethod.beanType, Anonymous::class.java)?.let { annotation ->
                it.permitPath(paths, annotation)
            }
        }
    }
}

private fun AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry.permitPath(
    paths: List<String>,
    annotation: Anonymous
) {
    paths.forEach { path ->
        when (annotation.type) {
            AnonymousType.ALL -> requestMatchers(path).permitAll()
            AnonymousType.Anonymous -> requestMatchers(path).anonymous()
        }
    }
}

private fun RequestMappingInfo.listPath(): List<String> = patternsCondition?.patterns?.map {
    PATTERN.matcher(it).replaceAll("*")
} ?: listOf()

