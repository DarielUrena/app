package edu.qc.seclass.uiprototype

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.collections.component1
import kotlin.collections.component2

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FoodLocatorApp()
        }
    }
}

const val OPENING_TIME = "10:00 AM"
const val CLOSING_TIME = "6:00 PM"

val defaultRestaurants = listOf(
    "Student Union",
    "Kiely Hall",
    "Remsen Hall"
)

data class MenuItem(
    val name: String,
    val dietTags: List<String>,
    val quantity: Int
)

data class Review(
    val reviewer: String,
    val rating: Int
)

val mockMenus = mutableStateMapOf(
    "Student Union" to mutableStateListOf(
        MenuItem("Burger", listOf("Contains Meat", "Contains Gluten"), 10),
        MenuItem("Chicken Wrap", listOf("Contains Meat", "Contains Gluten"), 5),
        MenuItem("Salad", listOf("Vegetarian", "Gluten-Free Option"), 0)
    ),
    "Kiely Hall" to mutableStateListOf(
        MenuItem("Chips", listOf("Vegan", "Gluten-Free"), 20),
        MenuItem("Granola Bar", listOf("Vegetarian", "Contains Nuts"), 8),
        MenuItem("Fruit Cup", listOf("Vegan", "Gluten-Free"), 15)
    ),
    "Remsen Hall" to mutableStateListOf(
        MenuItem("Latte", listOf("Vegetarian", "Contains Dairy"), 12),
        MenuItem("Muffin", listOf("Vegetarian", "Contains Gluten"), 4),
        MenuItem("Iced Coffee", listOf("Vegan Option Available"), 10)
    )
)

val mockReviews = mutableStateMapOf(
    "Student Union" to mutableStateListOf(
        Review("Alice", 5),
        Review("Bob", 4)
    ),
    "Kiely Hall" to mutableStateListOf(
        Review("Chris", 4)
    ),
    "Remsen Hall" to mutableStateListOf(
        Review("Dana", 5)
    )
)

fun updateMenuItemQuantity(menu: MutableList<MenuItem>, item: MenuItem, newQuantity: Int) {
    val index = menu.indexOf(item)
    if (index != -1) {
        val clamped = if (newQuantity < 0) 0 else newQuantity
        val current = menu[index]
        menu[index] = current.copy(quantity = clamped)
    }
}

@Composable
fun FoodLocatorApp() {
    var currentScreen by remember { mutableStateOf("welcome") }
    var selectedRestaurant by remember { mutableStateOf("") }

    when (currentScreen) {
        "welcome" -> WelcomeScreen(
            onStudent = { currentScreen = "student_map" },
            onEmployee = { currentScreen = "employee_home" }
        )
        "student_map" -> StudentMapScreen(
            onBack = { currentScreen = "welcome" },
            onRestaurantSelected = {
                selectedRestaurant = it
                currentScreen = "student_menu"
            }
        )
        "student_menu" -> RestaurantMenuScreen(
            restaurant = selectedRestaurant,
            onBack = { currentScreen = "student_map" }
        )
        "employee_home" -> EmployeeRestaurantSelect(
            onBack = { currentScreen = "welcome" },
            onRestaurantSelected = {
                selectedRestaurant = it
                currentScreen = "employee_menu_edit"
            }
        )
        "employee_menu_edit" -> EmployeeMenuEditor(
            restaurant = selectedRestaurant,
            onBack = { currentScreen = "employee_home" }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(onStudent: () -> Unit, onEmployee: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome to Queens College", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Food Locator", fontSize = 22.sp, modifier = Modifier.padding(bottom = 40.dp))
        Button(onClick = onStudent, modifier = Modifier.fillMaxWidth().height(60.dp)) {
            Text("Student", fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = onEmployee, modifier = Modifier.fillMaxWidth().height(60.dp)) {
            Text("Employee", fontSize = 20.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentMapScreen(
    onBack: () -> Unit,
    onRestaurantSelected: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf(listOf<Pair<String, MenuItem>>()) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Student - Campus Map") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )
        Text(
            "Search by food name or dietary restriction",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("e.g. burger, vegan, gluten-free") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                val q = searchQuery.trim().lowercase()
                if (q.isEmpty()) {
                    searchResults = emptyList()
                } else {
                    val results = mutableListOf<Pair<String, MenuItem>>()
                    mockMenus.forEach { (restaurant, items) ->
                        items.forEach { item ->
                            val inName = item.name.lowercase().contains(q)
                            val inDiet = item.dietTags.any { tag ->
                                tag.lowercase().contains(q)
                            }
                            if (inName || inDiet) {
                                results.add(restaurant to item)
                            }
                        }
                    }
                    searchResults = results
                }
            }) {
                Text("Search")
            }
        }
        Text(
            "Tap a pin to view menu and reviews",
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp)
        ) {
            val mapWidth = maxWidth
            val mapHeight = maxHeight
            Image(
                painter = painterResource(R.drawable.qc_campus_map),
                contentDescription = "Queens College Campus Map",
                modifier = Modifier.fillMaxSize()
            )
            Text(
                text = "📍",
                fontSize = 32.sp,
                modifier = Modifier
                    .offset(x = mapWidth * 0.33f - 16.dp, y = mapHeight * 0.60f - 16.dp)
                    .clickable { onRestaurantSelected("Student Union") }
            )
            Text(
                text = "📍",
                fontSize = 32.sp,
                modifier = Modifier
                    .offset(x = mapWidth * 0.47f - 16.dp, y = mapHeight * 0.68f - 16.dp)
                    .clickable { onRestaurantSelected("Kiely Hall") }
            )
            Text(
                text = "📍",
                fontSize = 32.sp,
                modifier = Modifier
                    .offset(x = mapWidth * 0.28f - 16.dp, y = mapHeight * 0.45f - 16.dp)
                    .clickable { onRestaurantSelected("Remsen Hall") }
            )
        }
        if (searchResults.isNotEmpty()) {
            Text(
                "Search results",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 220.dp)
                    .padding(horizontal = 8.dp)
            ) {
                items(searchResults) { (restaurant, item) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onRestaurantSelected(restaurant) },
                        elevation = CardDefaults.cardElevation(3.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("${item.name}  •  $restaurant", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            if (item.dietTags.isNotEmpty()) {
                                Text("Dietary: ${item.dietTags.joinToString(", ")}", fontSize = 13.sp)
                            }
                            Text(
                                if (item.quantity > 0) "Available: ${item.quantity} in stock"
                                else "Unavailable: Out of stock",
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        } else if (searchQuery.isNotBlank()) {
            Text(
                "No items match \"$searchQuery\"",
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantMenuScreen(restaurant: String, onBack: () -> Unit) {
    val menu = mockMenus.getOrPut(restaurant) { mutableStateListOf() }
    val reviews = mockReviews.getOrPut(restaurant) { mutableStateListOf() }
    var reviewerName by remember { mutableStateOf("") }
    var ratingText by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(15.dp)
    ) {
        TopAppBar(
            title = { Text("$restaurant Menu") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )
        Text("Hours: $OPENING_TIME – $CLOSING_TIME", fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
        Text("Menu", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        menu.forEach { item ->
            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(item.name, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    if (item.dietTags.isNotEmpty()) {
                        Text("Dietary: ${item.dietTags.joinToString(", ")}", fontSize = 14.sp)
                    }
                    Text(
                        if (item.quantity > 0) "Available: ${item.quantity} in stock"
                        else "Unavailable: Out of stock",
                        fontSize = 14.sp
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Reviews", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        if (reviews.isEmpty()) {
            Text("No reviews yet. Be the first to add one!", fontSize = 14.sp)
        } else {
            reviews.forEach { review ->
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("${review.reviewer} – ${review.rating}/5", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Add a Review", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = reviewerName,
            onValueChange = { reviewerName = it },
            label = { Text("Your name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )
        OutlinedTextField(
            value = ratingText,
            onValueChange = { ratingText = it },
            label = { Text("Rating (1–5)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )
        Button(
            onClick = {
                val rating = ratingText.toIntOrNull() ?: 0
                if (reviewerName.isNotBlank() && rating in 1..5) {
                    reviews.add(Review(reviewerName, rating))
                    reviewerName = ""
                    ratingText = ""
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Text("Submit Review")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeRestaurantSelect(
    onBack: () -> Unit,
    onRestaurantSelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(15.dp)) {
        TopAppBar(
            title = { Text("Employee Portal") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )
        Text("Select a restaurant to manage", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
        LazyColumn {
            items(defaultRestaurants) { store ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable { onRestaurantSelected(store) },
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(store, fontSize = 18.sp)
                        Text("Hours: $OPENING_TIME – $CLOSING_TIME", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeMenuEditor(restaurant: String, onBack: () -> Unit) {
    val menu = mockMenus.getOrPut(restaurant) { mutableStateListOf() }
    var newItemName by remember { mutableStateOf("") }
    var newItemDietTags by remember { mutableStateOf("") }
    var newItemQuantity by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(15.dp)) {
        TopAppBar(
            title = { Text("Edit Menu – $restaurant") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )
        Text("Hours: $OPENING_TIME – $CLOSING_TIME", fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = newItemName,
            onValueChange = { newItemName = it },
            label = { Text("Menu item name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )
        OutlinedTextField(
            value = newItemDietTags,
            onValueChange = { newItemDietTags = it },
            label = { Text("Dietary tags (comma separated)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )
        OutlinedTextField(
            value = newItemQuantity,
            onValueChange = { newItemQuantity = it },
            label = { Text("Initial stock quantity") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )
        Button(
            onClick = {
                if (newItemName.isNotBlank()) {
                    val tags = newItemDietTags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    val qty = newItemQuantity.toIntOrNull() ?: 0
                    menu.add(MenuItem(newItemName, tags, qty))
                    newItemName = ""
                    newItemDietTags = ""
                    newItemQuantity = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Item")
        }
        LazyColumn {
            items(menu) { item ->
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(item.name, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        if (item.dietTags.isNotEmpty()) {
                            Text("Dietary: ${item.dietTags.joinToString(", ")}", fontSize = 14.sp)
                        }
                        Text("Stock: ${item.quantity}", fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { updateMenuItemQuantity(menu, item, item.quantity - 1) },
                                enabled = item.quantity > 0
                            ) {
                                Text("-1")
                            }
                            Button(
                                onClick = { updateMenuItemQuantity(menu, item, item.quantity + 1) }
                            ) {
                                Text("+1")
                            }
                            Button(
                                onClick = { updateMenuItemQuantity(menu, item, 0) }
                            ) {
                                Text("Set 0")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun PreviewFoodLocator() {
    FoodLocatorApp()
}