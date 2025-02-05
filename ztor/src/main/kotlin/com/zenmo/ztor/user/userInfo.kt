package com.zenmo.ztor.user
import kotlinx.serialization.Serializable

@Serializable
data class UserInfo(
    val isAdmin: Boolean,
    val decodedAccessToken: AccessTokenPayload
)
