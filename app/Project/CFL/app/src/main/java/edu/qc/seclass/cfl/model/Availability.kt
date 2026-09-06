package edu.qc.seclass.cfl.model

data class Availability(
    var quantity: Int,
    var lastUpdated: String
) {
    fun update(q: Int) {
        quantity = q
        lastUpdated = System.currentTimeMillis().toString()
    }
    fun isOutOfStock(): Boolean = (quantity <= 0)
}
