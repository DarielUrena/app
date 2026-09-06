package edu.qc.seclass.cfl.data

import android.content.ContentValues
import android.content.Context
import edu.qc.seclass.cfl.model.*
import kotlin.math.round

class DataManager(context: Context) {

    private val db = DBHelper(context).writableDatabase

    private fun android.database.Cursor.str(col: String) = getString(getColumnIndexOrThrow(col)) ?: ""
    private fun android.database.Cursor.lng(col: String) = getLong(getColumnIndexOrThrow(col))
    private fun android.database.Cursor.dbl(col: String) = getDouble(getColumnIndexOrThrow(col))
    private fun android.database.Cursor.int(col: String) = getInt(getColumnIndexOrThrow(col))

    private inline fun <T> query(sql: String, args: Array<String>? = null, block: (android.database.Cursor) -> T): T {
        val c = db.rawQuery(sql, args)
        val result = block(c)
        c.close()
        return result
    }

    fun getStores(): List<Store> = query("SELECT * FROM stores") { c ->
        val list = mutableListOf<Store>()
        if (c.moveToFirst()) {
            do list.add(
                Store(
                    storeId = c.lng("storeId"),
                    name = c.str("name"),
                    location = c.str("location"),
                    latitude = c.dbl("latitude"),
                    longitude = c.dbl("longitude"),
                    operatingHours = c.str("operatingHours"),
                    dietaryOptions = c.str("dietaryOptions")
                )
            ) while (c.moveToNext())
        }
        list
    }

    fun searchStoresByName(name: String): List<Store> =
        query("SELECT * FROM stores WHERE name LIKE '%' || ? || '%'", arrayOf(name)) { c ->
            val list = mutableListOf<Store>()
            if (c.moveToFirst()) {
                do list.add(
                    Store(
                        storeId = c.lng("storeId"),
                        name = c.str("name"),
                        location = c.str("location"),
                        latitude = c.dbl("latitude"),
                        longitude = c.dbl("longitude"),
                        operatingHours = c.str("operatingHours"),
                        dietaryOptions = c.str("dietaryOptions")
                    )
                ) while (c.moveToNext())
            }
            list
        }

    private fun loadMenuItems(sql: String): List<MenuItem> =
        query(sql) { c ->
            val list = mutableListOf<MenuItem>()
            if (c.moveToFirst()) {
                do {
                    val itemId = c.lng("itemId")
                    list.add(
                        MenuItem(
                            itemId = itemId,
                            name = c.str("name"),
                            description = c.str("description"),
                            price = c.dbl("price"),
                            category = c.str("categoryId"),
                            dietTags = c.str("dietTags")
                                .split(",")
                                .filter { it.isNotBlank() }
                                .toSet(),
                            availability = getAvailability(itemId)
                        )
                    )
                } while (c.moveToNext())
            }
            list
        }

    fun getMenuItems(storeId: Long): List<MenuItem> =
        loadMenuItems("SELECT * FROM menu_items WHERE storeId = $storeId")

    fun searchMenuItemsInStore(storeId: Long, name: String): List<MenuItem> =
        loadMenuItems("SELECT * FROM menu_items WHERE storeId = $storeId AND name LIKE '%' || '$name' || '%'")

    fun filterByCategory(category: String): List<MenuItem> =
        loadMenuItems("SELECT * FROM menu_items WHERE categoryId = '$category'")

    fun filterByDietary(tag: String): List<MenuItem> =
        loadMenuItems("SELECT * FROM menu_items WHERE dietTags LIKE '%' || '$tag' || '%'")

    fun addMenuItem(item: MenuItem, storeId: Long) {
        val cv = ContentValues().apply {
            put("storeId", storeId)
            put("name", item.name)
            put("description", item.description)
            put("price", item.price)
            put("categoryId", item.category)
            put("dietTags", item.dietTags.joinToString(","))
        }

        val newId = db.insert("menu_items", null, cv)

        val av = ContentValues().apply {
            put("itemId", newId)
            put("quantity", item.availability.quantity)
            put("lastUpdated",item.availability.lastUpdated)
        }
        db.insert("availability", null, av)
    }

    fun updateMenuItem(item: MenuItem) {
        val cv = ContentValues().apply {
            put("name", item.name)
            put("description", item.description)
            put("price", item.price)
            put("categoryId", item.category)
            put("dietTags", item.dietTags.joinToString(","))
        }
        db.update("menu_items", cv, "itemId = ?", arrayOf(item.itemId.toString()))
    }

    fun deleteMenuItem(itemId: Long) {
        db.delete("availability", "itemId = ?", arrayOf(itemId.toString()))
        db.delete("menu_items", "itemId = ?", arrayOf(itemId.toString()))
    }

    fun getAvailability(itemId: Long): Availability =
        query("SELECT * FROM availability WHERE itemId = ?", arrayOf(itemId.toString())) { c ->
            if (c.moveToFirst()) {
                Availability(
                    quantity = c.int("quantity"),
                    lastUpdated = c.str("lastUpdated")
                )
            } else {
                Availability(quantity = 0, lastUpdated = "Unknown")
            }
        }

    fun updateAvailability(itemId: Long, qty: Int) {
        val cv = ContentValues().apply {
            put("quantity", qty)
            put("lastUpdated", System.currentTimeMillis().toString())
        }
        db.update("availability", cv, "itemId = ?", arrayOf(itemId.toString()))
    }

    fun addReview(review: Review) {
        val roundedRating = round(review.rating * 10) / 10.0
        val cv = ContentValues().apply {
            put("storeId", review.storeId)
            put("authorName", review.authorName)
            put("rating", roundedRating)
            put("comment", review.comment)
            put("timestamp", review.timestamp)
        }
        db.insert("reviews", null, cv)
    }

    fun deleteReview(reviewId: Long) {
        db.delete("reviews", "reviewId = ?", arrayOf(reviewId.toString()))
    }

    fun getReviews(storeId: Long): List<Review> =
        query(
            "SELECT * FROM reviews WHERE storeId = ? ORDER BY timestamp DESC",
            arrayOf(storeId.toString())
        ) { c ->
            val list = mutableListOf<Review>()
            if (c.moveToFirst()) {
                do list.add(
                    Review(
                        reviewId = c.lng("reviewId"),
                        storeId = storeId,
                        authorName = c.str("authorName"),
                        rating = c.dbl("rating"),
                        comment = c.str("comment"),
                        timestamp = c.str("timestamp")
                    )
                ) while (c.moveToNext())
            }
            list
        }


    fun seedDatabaseIfEmpty(){
        val cursor = db.rawQuery("SELECT COUNT(*) FROM stores", null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()

        if (count > 0) return

        val cv1 = ContentValues().apply{
            put("name", "Student Union")
            put ("location", "Student Union Building, 1st Floor")
            put("latitude", 40.7365)
            put("longitude", -73.8201)
            put("operatingHours", "Mon-Fri: 10 AM - 6 PM")
            put("dietaryOptions", "Vegan, Vegetarian, Gluten-Free")
        }
        val id1 = db.insert("stores", null, cv1)

        val cv2 = ContentValues().apply{
            put("name", "Kiely Hall")
            put ("location", "Kiely Hall, 2nd Floor Cafe")
            put("latitude", 40.7365)
            put("longitude", -73.8201)
            put("operatingHours", "Mon-Fri: 9 AM - 5 PM")
            put("dietaryOptions", "Vegan, Kosher")
        }

        val id2 = db.insert("stores", null, cv2)

        val cv3 = ContentValues().apply{
            put("name", "Remsen Hall")
            put ("location", "Remsen Hall Cafe")
            put("latitude", 40.7365)
            put("longitude", -73.8201)
            put("operatingHours", "Mon-Fri: 8 AM - 5 PM")
            put("dietaryOptions", "Vegetarian, Gluten-Free")
        }

        val id3 = db.insert("stores", null, cv3)

        if (id1 != -1L){
            val burger = MenuItem(itemId = 0, name = "Burger", description = "Grilled Beef Burger", price = 9.99, category = "Burgers", availability = Availability(quantity = 10, lastUpdated = "Just Added"), dietTags = setOf("Contains Meat")
            )

            val salad = MenuItem (
                itemId = 0,
                name = "Garden Salad",
                description = "Fresh lettuce and tomatoes",
                price = 5.99,
                category = "Salad",
                availability = Availability(quantity = 10, lastUpdated = "Just Added"),
                dietTags = setOf("Vegan", "Gluten-Free"),
            )

            addMenuItem(burger, id1)
            addMenuItem(salad, id1)

        }
    }

    fun getDistinctCategories(): List<String>{
        return query("SELECT DISTINCT categoryId FROM menu_items WHERE categoryId IS NOT NULL AND categoryId != ' ' ORDER BY categoryId ASC") { c ->
            val list = mutableListOf<String>()
            if (c.moveToFirst()) {
                do{
                    list.add(c.getString(0))
                }while (c.moveToNext())
            }
            list
        }
    }

    fun getDistinctDietTags(): List<String>{
        return query ("SELECT dietTags FROM menu_items WHERE dietTags IS NOT NULL AND dietTags != ' ' "){ c ->
            val uniqueTags = mutableSetOf<String>()
            if (c.moveToFirst()) {
                do {
                    val rawString = c.getString(0)
                    val splitTags = rawString.split(",").map {it.trim()}.filter {it.isNotEmpty()}
                    uniqueTags.addAll(splitTags.filter {it.isNotEmpty()})
                } while (c.moveToNext())
            }
            uniqueTags.toList().sorted()
        }
    }




}
