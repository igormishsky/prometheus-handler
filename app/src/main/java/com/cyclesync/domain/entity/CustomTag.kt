package com.cyclesync.domain.entity

data class CustomTag(
    val id: String,
    val name: String,
    val category: String? = null,
    val color: String? = null,
    val icon: String? = null,
    val sortOrder: Int = 0
)
