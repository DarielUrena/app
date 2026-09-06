package edu.qc.seclass.cfl.model

data class Review(
    val reviewId: Long,
    val storeId: Long,
    val authorName: String,
    val rating: Double,
    val comment: String,
    val timestamp: String,

)
