package edu.qc.seclass.cfl.model

data class Category(
    val name: String,
    val parent: Category? = null,
    val children: List<Category> = emptyList()
)