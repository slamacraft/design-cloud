package org.jdjzj.design.common.exception

/**
 * 服务异常
 *
 * 当系统的基础功能出现异常时，例如登陆，注册，获取用户信息等，会抛出此异常
 */
class ServiceException(msg: String, code: String = "500") : RuntimeException(msg) {
}