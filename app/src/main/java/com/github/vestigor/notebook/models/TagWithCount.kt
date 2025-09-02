package com.github.vestigor.notebook.models

data class TagWithCount(
    val id: Long,
    val name: String,
    val color: String,
    val noteCount: Int
)