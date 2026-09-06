package edu.qc.seclass.cfl

import edu.qc.seclass.cfl.model.Availability
import org.junit.Test


import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class AvailabilityTest {
    @Test
    fun isOutOfStock_returnsTrue_WhenZero(){
        val emptyItem = Availability(quantity = 0, lastUpdated = "N/A")
        assertTrue("Item should be out of stock", emptyItem.isOutOfStock())
    }

    @Test
    fun isOutOfStock_returnsFalse(){
        val stockedItem = Availability(quantity = 10, lastUpdated = "N/A")
        assertFalse("Item should NOT be out of stock", stockedItem.isOutOfStock())
    }



}