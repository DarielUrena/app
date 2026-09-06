package edu.qc.seclass.cfl.model

data class MenuItem(
    val itemId: Long,
    var name: String,
    var description: String,
    var price: Double,
    var category: String,
    var availability: Availability,
    var dietTags: Set<String>,
)