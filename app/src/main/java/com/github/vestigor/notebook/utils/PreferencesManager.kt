package com.github.vestigor.notebook.utils

import android.content.Context
import android.content.SharedPreferences

// 偏好设置工具类
class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        Constants.PREFS_NAME,
        Context.MODE_PRIVATE
    )
}