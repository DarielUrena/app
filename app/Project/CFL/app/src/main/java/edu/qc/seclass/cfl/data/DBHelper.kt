package edu.qc.seclass.cfl.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE stores (
                storeId INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                location TEXT,
                latitude REAL,
                longitude REAL,
                operatingHours TEXT,
                dietaryOptions TEXT
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE categories (
                categoryId INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                parentId INTEGER,
                FOREIGN KEY(parentId) REFERENCES categories(categoryId)
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE menu_items (
                itemId INTEGER PRIMARY KEY AUTOINCREMENT,
                storeId INTEGER NOT NULL,
                name TEXT NOT NULL,
                description TEXT,
                price REAL NOT NULL,
                categoryId INTEGER,
                dietTags TEXT,
                FOREIGN KEY(storeId) REFERENCES stores(storeId),
                FOREIGN KEY(categoryId) REFERENCES categories(categoryId)
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE availability (
                availId INTEGER PRIMARY KEY AUTOINCREMENT,
                itemId INTEGER UNIQUE NOT NULL,
                quantity INTEGER NOT NULL,
                lastUpdated TEXT,
                FOREIGN KEY(itemId) REFERENCES menu_items(itemId)
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE reviews (
                reviewId INTEGER PRIMARY KEY AUTOINCREMENT,
                storeId INTEGER NOT NULL,
                authorName TEXT,
                rating REAL NOT NULL,
                comment TEXT,
                timestamp TEXT,
                FOREIGN KEY(storeId) REFERENCES stores(storeId)
            );
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS reviews;")
        db.execSQL("DROP TABLE IF EXISTS availability;")
        db.execSQL("DROP TABLE IF EXISTS menu_items;")
        db.execSQL("DROP TABLE IF EXISTS categories;")
        db.execSQL("DROP TABLE IF EXISTS stores;")
        onCreate(db)
    }

    companion object {
        private const val DATABASE_NAME = "cfl.db"
        private const val DATABASE_VERSION = 1
    }
}