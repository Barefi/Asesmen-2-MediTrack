package com.barefi0012.asesmen2.model

data class UserProfile(
    val name: String = "",
    val email: String = "",
    val photoUrl: String = ""
) {
    val isLoggedIn: Boolean
        get() = email.isNotBlank()
}
