package com.cyclesync.domain.entity

data class CustomTag(
    val id: String,
    val name: String,
    val category: String? = null,
    val color: String? = null,
    val icon: String? = null,
    val sortOrder: Int = 0
) {
    val hasColor: Boolean
        get() = !color.isNullOrBlank()

    val hasIcon: Boolean
        get() = !icon.isNullOrBlank()

    val hasCategory: Boolean
        get() = !category.isNullOrBlank()
}
