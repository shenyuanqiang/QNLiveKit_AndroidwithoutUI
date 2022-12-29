package com.qlive.qnlivekit.uitil

import com.qiniu.qnnouidemo.uitil.BZUser
import com.qiniu.qnnouidemo.uitil.SpUtil

object UserManager {
    var user: BZUser? = null
        private set

    fun init() {
        user = JsonUtils.parseObject(
            SpUtil.get("UserManager").readString("BZUser") ?: "",
            BZUser::class.java
        )
    }

    fun onLogin(user: BZUser) {
        this.user = user
        SpUtil.get("UserManager").saveData("BZUser",JsonUtils.toJson(user))
    }

}