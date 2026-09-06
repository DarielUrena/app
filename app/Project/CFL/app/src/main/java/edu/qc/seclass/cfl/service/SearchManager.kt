package edu.qc.seclass.cfl.service

import android.content.Context
import edu.qc.seclass.cfl.data.DataManager
import edu.qc.seclass.cfl.model.MenuItem
import edu.qc.seclass.cfl.model.Store

class SearchManager(context: Context) {

    private val dataManager = DataManager(context)

    fun searchStoresByName(name: String): List<Store> {
        return dataManager.searchStoresByName(name)
    }

    fun searchMenuItemsInStore(storeId: Long, name: String): List<MenuItem> {
        return dataManager.searchMenuItemsInStore(storeId, name)
    }

    fun filterByCategory(category: String): List<MenuItem> {
        return dataManager.filterByCategory(category)
    }

    fun filterByDietary(tag: String): List<MenuItem> {
        return dataManager.filterByDietary(tag)
    }
}
