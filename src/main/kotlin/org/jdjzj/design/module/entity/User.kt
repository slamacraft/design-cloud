package org.jdjzj.design.module.entity

import org.jdjzj.design.module.base.BaseEntity
import org.jdjzj.design.module.base.BaseTable
import org.jdjzj.design.module.base.Entity


interface UserEntity : Entity<UserEntity>, BaseEntity {
    companion object : Entity.Factory<UserEntity>()

    var username: String // 用户名，也是登录名
    var nickname: String // 昵称
    var email: String // 邮箱
    var mobile: Long // 手机号
    var password: String // 密码
    var avatarUrl: String // 头像图片地址

    var deleted: Boolean // 是否删除
}

object UserTable : BaseTable<UserEntity>("user") {

    val username = varchar("username", 32)
    val nickname = varchar("nickname", 32)
    val email = varchar("email", 64)
    val mobile = long("mobile")
    val password = varchar("password", 64)
    val avatarUrl = varchar("avatar_url", 128)

    val deleted = bool("deleted").logicDelete()
}