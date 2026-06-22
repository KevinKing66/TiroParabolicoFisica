package com.king.kevin.tiroparabolico.data.repository

import android.content.Context
import com.king.kevin.tiroparabolico.domain.model.UserSession
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AuthSessionStorage @Inject constructor(
    @ApplicationContext context: Context
) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun save(session: UserSession) {
        preferences.edit()
            .putString(KEY_TOKEN, session.token)
            .putString(KEY_FULL_NAME, session.fullName)
            .putString(KEY_CODE, session.code)
            .putString(KEY_ROLE, session.role)
            .putString(KEY_COURSE, session.course)
            .apply()
    }

    fun get(): UserSession? {
        val token = preferences.getString(KEY_TOKEN, null) ?: return null
        return UserSession(
            token = token,
            fullName = preferences.getString(KEY_FULL_NAME, "").orEmpty(),
            code = preferences.getString(KEY_CODE, "").orEmpty(),
            role = preferences.getString(KEY_ROLE, "").orEmpty(),
            course = preferences.getString(KEY_COURSE, "").orEmpty()
        )
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "auth_session"
        const val KEY_TOKEN = "token"
        const val KEY_FULL_NAME = "full_name"
        const val KEY_CODE = "code"
        const val KEY_ROLE = "role"
        const val KEY_COURSE = "course"
    }
}
