package org.gdgzg.design.system.security

import org.gdgzg.design.common.exception.ServiceException
import org.gdgzg.design.module.entity.UserTable
import org.gdgzg.design.module.entity.UserTable.notLogicDelete
import org.gdgzg.design.system.security.model.LoginReq
import org.gdgzg.design.system.security.model.LoginUser
import org.jetbrains.exposed.sql.selectAll
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component

/**
 * 登录处理组件
 */
@Component
class LoginService {

    /**
     * @param username 用户名
     * @param password 密码
     * @param code  验证码
     * @param uuid  验证码对应的uuid
     */
    fun login(req: LoginReq) {

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


    }

}