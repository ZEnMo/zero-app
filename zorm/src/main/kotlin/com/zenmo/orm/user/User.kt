package com.zenmo.orm.user

data class User(
    val id: String,
    val projects: List<String>,
    val note: String,
)
