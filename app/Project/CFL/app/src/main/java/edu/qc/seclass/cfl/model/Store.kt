package edu.qc.seclass.cfl.model

data class Store(
    val storeId: Long,
    var name: String,
    var location: String,
    var latitude: Double,
    var longitude: Double,
    var operatingHours: String,
    var dietaryOptions: String
)