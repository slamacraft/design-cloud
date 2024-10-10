package org.gdgzg.design.module.entity

import org.gdgzg.design.module.base.BaseEntity
import org.gdgzg.design.module.base.BaseTable
import org.gdgzg.design.module.base.Entity


interface UserEntity : Entity<UserEntity>, BaseEntity {
    companion object : Entity.Factory<UserEntity>()

    var username: String // 用户名，也是登录名
    var nickname: String // 昵称
    var email: String // 邮箱
    var mobile: Long // 手机号
    var password: String // 密码
    var avatarUrl: String // 头像图片地址
    var delFlag: Boolean // 是否删除
}

object UserTable: BaseTable<UserEntity>("user"){

}