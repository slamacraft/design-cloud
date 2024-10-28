package org.jdjzj.design.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.jdjzj.design.common.annotation.Anonymous
import org.jdjzj.design.common.component.toJson
import org.jdjzj.design.common.model.RespWrapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
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
@EnableWebSecurity
class SecurityConfig {
    @Autowired
    lateinit var authenticationEntryPoint: AuthFailEntryPoint
    @Autowired
    lateinit var authenticationConfiguration: AuthenticationConfiguration

    @Autowired
    lateinit var requestMappingHandlerMapping: RequestMappingHandlerMapping

    /**
     * 解决 无法直接注入 AuthenticationManager
     *
     * @return
     * @throws Exception
     */
    @Bean
    fun authenticationManagerBean(): AuthenticationManager {
        return authenticationConfiguration.authenticationManager
    }

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

    /**
     * 跨域配置
     */
    @Bean
    fun corsFilter(): CorsFilter {
        val config = CorsConfiguration()
        // 允许跨域
        config.allowCredentials = true
        // 设置访问源地址
        config.addAllowedOriginPattern("*")
        // 设置访问源请求头
        config.addAllowedHeader("*")
        // 设置访问源请求方法
        config.addAllowedMethod("*")
        // 有效期 1800秒
        config.maxAge = 1800L
        // 添加映射路径，对所有路径生效
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)
        // 返回新的CorsFilter
        return CorsFilter(source)
    }
}

/**
 * 登录失败处理端点
 */
@Component
class AuthFailEntryPoint : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException?
    ) {
        response.status = 200
        response.contentType = "application/json"
        response.characterEncoding = "utf-8"
        response.writer.print(
            RespWrapper(
                code = HttpStatus.UNAUTHORIZED.value(),
                msg = "请求地址：${request.requestURI}，认证失败"
            ).toJson()
        )
    }
}

private fun AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry.permitPath(
    paths: List<String>,
    annotation: Anonymous
) {
    paths.forEach { path ->
        when (annotation.type) {
            org.jdjzj.design.common.annotation.AnonymousType.ALL -> requestMatchers(path).permitAll()
            org.jdjzj.design.common.annotation.AnonymousType.Anonymous -> requestMatchers(path).anonymous()
        }
    }
}

private fun RequestMappingInfo.listPath(): List<String> = patternsCondition?.patterns?.map {
    PATTERN.matcher(it).replaceAll("*")
} ?: listOf()

