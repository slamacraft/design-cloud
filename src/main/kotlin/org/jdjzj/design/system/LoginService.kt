package org.jdjzj.design.system

import org.jdjzj.design.common.exception.ServiceException
import org.jdjzj.design.module.entity.UserTable
import org.jdjzj.design.module.entity.UserTable.notLogicDelete
import org.jdjzj.design.system.model.LoginReq
import org.jdjzj.design.system.model.LoginUser
import org.jetbrains.exposed.sql.selectAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component

/**
 * 这个文件是登录所需的所有组件
 */

/**
 * 登录处理组件
 */
@Component
class LoginService {
    /**
     * spring security的授权管理器
     */
    @Autowired
    lateinit var authenticationManager: AuthenticationManager
    @Autowired
    lateinit var tokenService: TokenService
    fun login(req: org.jdjzj.design.system.model.LoginReq) {
        // TODO 登录验证码

        // 这里会间接调用下面的UserDetailsServiceImpl.loadUserByUsername
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(req.userName, req.password)
        )

        // TODO 登录日志
        val loginUser = authentication.principal as LoginUser

        val token = tokenService.createAndCacheToken(loginUser)
    }
}


@Component
class UserDetailsServiceImpl : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val userEntity = (UserTable.selectAll()
            .where { UserTable.username eq username }
            .notLogicDelete()
            .firstOrNull()?.let(UserTable::buildEntity)
            ?: throw ServiceException("登录用户不存在"))

        return LoginUser(userEntity)
    }
}