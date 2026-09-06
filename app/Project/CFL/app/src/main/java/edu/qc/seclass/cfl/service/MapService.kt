package edu.qc.seclass.cfl.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import edu.qc.seclass.cfl.model.Store

class MapService(private val context: Context) {

    fun getMapWithStores(stores: List<Store>) {
        if (stores.isEmpty()) return

        val query = stores.joinToString(" | ") {
            "${it.name} @ ${it.latitude},${it.longitude}"
        }

        val encoded = Uri.encode(query)
        val uri = Uri.parse("geo:0,0?q=$encoded")

        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val browserIntent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(browserIntent)
        }
    }

    fun getWalkingDirections(fromLat: Double, fromLon: Double, toLat: Double, toLon: Double) {
        val uri = Uri.parse("google.navigation:q=$toLat,$toLon&mode=w")

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")  // Try Google Maps
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val browserIntent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(browserIntent)
        }
    }

    fun getWalkingDirections(fromStore: Store, toStore: Store) {
        getWalkingDirections(
            fromStore.latitude,
            fromStore.longitude,
            toStore.latitude,
            toStore.longitude
        )
    }

    fun navigateToStore(store: Store) {
        val uri = Uri.parse("google.navigation:q=${store.latitude},${store.longitude}&mode=w")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val browserIntent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(browserIntent)
        }
    }
}
