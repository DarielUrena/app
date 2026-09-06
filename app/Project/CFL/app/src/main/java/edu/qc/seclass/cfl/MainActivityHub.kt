package edu.qc.seclass.cfl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.PaddingValues
import edu.qc.seclass.cfl.data.DataManager
import edu.qc.seclass.cfl.model.Availability
import edu.qc.seclass.cfl.model.MenuItem
import edu.qc.seclass.cfl.model.Review
import edu.qc.seclass.cfl.model.Store
import edu.qc.seclass.cfl.service.MapService
import androidx.compose.ui.text.input.PasswordVisualTransformation


class MainActivityHub : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dataManager = DataManager(this)

        dataManager.seedDatabaseIfEmpty()

        setContent {
            FoodLocatorApp(dataManager)
        }
    }
}

fun updateMenuItemQuantity(
    menu: MutableList<MenuItem>,
    item: MenuItem,
    newQuantity: Int
) {
    val index = menu.indexOf(item)
    if (index != -1) {
        val clamped = maxOf(newQuantity, 0)

        val updatedAvailability = item.availability.copy(
            quantity = clamped,
            lastUpdated = "Just now"
        )

        menu[index] = item.copy(availability = updatedAvailability)
    }
}

@Composable
fun FoodLocatorApp(dataManager: DataManager) {
    var currentScreen by remember { mutableStateOf("welcome") }
    var selectedStore by remember { mutableStateOf<Store?>(null) }
    var itemToEdit by remember { mutableStateOf<MenuItem?>(null) }
    var selectedMenuItem by remember { mutableStateOf<MenuItem?>(null) }

    when (currentScreen) {
        "welcome" -> WelcomeScreen(
            onStudent = { currentScreen = "student_map" },
            onEmployee = { currentScreen = "employee_login" }
        )
        "employee_login" -> EmployeeLoginScreen(
            onBack = { currentScreen = "welcome" },
            onLoginSuccess = { currentScreen = "employee_home" }
        )
        "student_map" -> StudentMapScreen(
            dataManager = dataManager,
            onBack = { currentScreen = "welcome" },
            onRestaurantSelected = { store ->
                selectedStore = store
                currentScreen = "student_menu"

            }
        )
        "student_menu" -> selectedStore?.let { store ->
            RestaurantMenuScreen(
                store = store,
                dataManager = dataManager,
                onBack = { currentScreen = "student_map" },
                onItemSelected = { item ->
                    selectedMenuItem = item
                    currentScreen = "student_item_details"
                }
            )
        }
        "employee_home" -> EmployeeRestaurantSelect(
            dataManager = dataManager,
            onBack = { currentScreen = "welcome" },
            onRestaurantSelected = { store ->
                selectedStore = store
                currentScreen = "employee_menu_edit"
            }
        )
        "employee_menu_edit" -> selectedStore?.let { store ->
            EmployeeMenuEditor(
                store = store,
                dataManager = dataManager,
                onBack = { currentScreen = "employee_home" },
                onEditItem = { item ->
                    itemToEdit = item
                    currentScreen = "employee_item_edit"
                }
            )
        }
        "employee_item_edit" -> itemToEdit?.let { item ->
            selectedStore?.let { store ->
                EditMenuItemScreen(
                    store = store,
                    item = item,
                    dataManager = dataManager,
                    onBack = { currentScreen = "employee_menu_edit" }
                )
            }
        }
        "student_item_details" -> selectedMenuItem?.let { item ->
            selectedStore?.let { store ->
                MenuItemDetailsScreen(
                    store = store,
                    item = item,
                    onBack = { currentScreen = "student_menu" }
                )
            }
        }
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
            Text("Employee login", fontSize = 20.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeLoginScreen(
    onBack: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    val validPassword = "1111"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        TopAppBar(
            title = { Text("Employee Login") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, "Back")
                }
            }
        )

        Spacer(Modifier.height(20.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        if (errorMsg.isNotEmpty()) {
            Text(errorMsg, color = Color.Red)
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = {
                if (password == validPassword) {
                    errorMsg = ""
                    onLoginSuccess()
                } else {
                    errorMsg = "Invalid password, please try again."
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentMapScreen(
    dataManager: DataManager,
    onBack: () -> Unit,
    onRestaurantSelected: (Store) -> Unit
) {


        BackHandler { onBack() }


        var searchQuery by remember { mutableStateOf("") }
        var selectedCategory by remember { mutableStateOf<String?>(null) }
        var selectedDiet by remember { mutableStateOf<String?>(null) }


        var results by remember { mutableStateOf(listOf<Pair<Store, MenuItem>>()) }
        var storeResults by remember { mutableStateOf(listOf<Store>()) }


        var allStores by remember { mutableStateOf(listOf<Store>()) }
        var categories by remember { mutableStateOf(listOf<String>()) }
        var dietTags by remember { mutableStateOf(listOf<String>()) }


        LaunchedEffect(Unit) {
            allStores = dataManager.getStores()
            categories = dataManager.getDistinctCategories()
            dietTags = dataManager.getDistinctDietTags()
        }

        val isSearching = searchQuery.isNotBlank() || selectedCategory != null || selectedDiet != null

        Column(modifier = Modifier.fillMaxSize()) {


            TopAppBar(
                title = { Text("Campus Map") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .background(MaterialTheme.colorScheme.surface)
            ) {

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    placeholder = { Text("Search food or stores...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true
                )

                if (categories.isNotEmpty()) {
                    Text(
                        "Categories",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = (selectedCategory == cat),
                                onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                                label = { Text(cat) },
                                leadingIcon = if (selectedCategory == cat) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }


                if (dietTags.isNotEmpty()) {
                    Text(
                        "Dietary Preferences",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(dietTags) { tag ->
                            FilterChip(
                                selected = (selectedDiet == tag),
                                onClick = { selectedDiet = if (selectedDiet == tag) null else tag },
                                label = { Text(tag) },
                                leadingIcon = if (selectedDiet == tag) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }


                Button(
                    onClick = {
                        val q = searchQuery.trim().lowercase()
                        val cat = selectedCategory?.lowercase() ?: ""
                        val diet = selectedDiet?.lowercase() ?: ""

                        storeResults = if (q.isEmpty()) {
                            emptyList()
                        } else {
                            dataManager.searchStoresByName(q)
                        }

                        val temp = mutableListOf<Pair<Store, MenuItem>>()

                        allStores.forEach { store ->
                            val dbItems = dataManager.getMenuItems(store.storeId)
                            dbItems.forEach { item ->

                                val matchName = q.isEmpty() || item.name.lowercase().contains(q) || store.name.lowercase().contains(q)
                                val matchCategory = cat.isEmpty() || item.category.trim().equals(cat, ignoreCase = true)
                                val matchDiet = diet.isEmpty() || item.dietTags.any { it.lowercase().contains(diet) }

                                if (matchName && matchCategory && matchDiet) {
                                    temp.add(store to item)
                                }
                            }
                        }
                        results = temp
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(48.dp)
                ) {
                    Text("Search")
                }
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(Color.LightGray)
            ) {
                val mapWidth = maxWidth
                val mapHeight = maxHeight

                Image(
                    painter = painterResource(R.drawable.qc_campus_map),
                    contentDescription = "Map",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                val su = allStores.find { it.name.contains("Student Union", ignoreCase = true) }
                if (su != null) {
                    Text(
                        text = "📍", fontSize = 32.sp,
                        modifier = Modifier.offset(x = mapWidth * 0.33f - 16.dp, y = mapHeight * 0.60f - 16.dp)
                            .clickable { onRestaurantSelected(su) }
                    )
                }
                val kiely = allStores.find { it.name.contains("Kiely", ignoreCase = true) }
                if (kiely != null) {
                    Text(
                        text = "📍", fontSize = 32.sp,
                        modifier = Modifier.offset(x = mapWidth * 0.47f - 16.dp, y = mapHeight * 0.68f - 16.dp)
                            .clickable { onRestaurantSelected(kiely) }
                    )
                }
                val remsen = allStores.find { it.name.contains("Remsen", ignoreCase = true) }
                if (remsen != null) {
                    Text(
                        text = "📍", fontSize = 32.sp,
                        modifier = Modifier.offset(x = mapWidth * 0.28f - 16.dp, y = mapHeight * 0.45f - 16.dp)
                            .clickable { onRestaurantSelected(remsen) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                if (isSearching) {
                    item { Text("Search Results", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp)) }

                    items(storeResults) { store ->
                        RestaurantCard(store, onRestaurantSelected)
                    }
                    items(results) { (store, item) ->
                        Card(Modifier.fillMaxWidth().padding(6.dp).clickable { onRestaurantSelected(store) }) {
                            Column(Modifier.padding(12.dp)) {
                                Text("${item.name}", fontWeight = FontWeight.Bold)
                                Text("at ${store.name}", fontSize = 12.sp, color = Color.Gray)
                                Text("Category: ${item.category}", fontSize = 12.sp)
                            }
                        }
                    }

                    if (storeResults.isEmpty() && results.isEmpty()) {
                        item {
                            Text("No results found.", modifier = Modifier.padding(16.dp), color = Color.Gray)
                        }
                    }
                } else {
                    item {
                        Text(
                            "All Locations",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 12.dp, bottom = 8.dp, start = 8.dp)
                        )
                    }
                    items(allStores) { store ->
                        RestaurantCard(store, onRestaurantSelected)
                    }
                }
            }
        }
    }

@Composable
fun RestaurantCard(store: Store, onClick: (Store) -> Unit){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 8.dp)
            .clickable {onClick(store)},
        elevation = CardDefaults.cardElevation(3.dp)
    ){
        Column(modifier = Modifier.padding(16.dp)){
            Text(store.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(store.location, fontSize = 14.sp, color = Color.Gray)

            Spacer(Modifier.height(4.dp))

            Text("Open: ${store.operatingHours}", fontSize = 12.sp)

            if (store.dietaryOptions.isNotEmpty()){
                Spacer(Modifier.height(4.dp))
                Text(
                    "Options: ${store.dietaryOptions}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantMenuScreen( store: Store, dataManager: DataManager,  onBack: () -> Unit, onItemSelected: (MenuItem) -> Unit) {
    var menu by remember {mutableStateOf(listOf<MenuItem>())}

    var reviews by remember {mutableStateOf(listOf<Review>())}


    LaunchedEffect(store) {
        menu = dataManager.getMenuItems(store.storeId)
        reviews = dataManager.getReviews(store.storeId)
    }

    var reviewerName by remember {mutableStateOf("")}
    var ratingText by remember {mutableStateOf("")}
    var commentText by remember {mutableStateOf("")}
    var scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(15.dp)
    ) {
        TopAppBar(
            title = { Text("${store.name} Menu") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )
        Text("Location: ${store.location}", fontSize = 14.sp)
        Spacer(Modifier.height(4.dp))

        Text("Hours:", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Text(store.operatingHours, fontSize = 13.sp)

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                val uri = Uri.parse(
                    "https://www.google.com/maps/dir/?api=1" +
                            "&origin=Queens+College+NY" +
                            "&destination=${store.latitude},${store.longitude}" +
                            "&travelmode=walking" +
                            "&dir_action=navigate"
                )

                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    setPackage("com.google.android.apps.maps")
                }

                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Get Walking Directions")
        }

        Spacer(Modifier.height(6.dp))


        if (store.dietaryOptions.isNotEmpty()) {
            Text("Dietary Options:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(store.dietaryOptions, fontSize = 13.sp)
        }

        Spacer(Modifier.height(12.dp))
        Text("Menu", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        if (menu.isEmpty()) {
            Text("No menu items available.", fontSize = 14.sp, modifier = Modifier.padding(8.dp))
        } else {
            val groupedMenu = menu.groupBy {it.category}

            groupedMenu.forEach { (categoryName, itemsInCategory) ->
                Text(
                    text = categoryName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                itemsInCategory.forEach { item ->
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { onItemSelected(item) },
                        elevation = CardDefaults.cardElevation(3.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(item.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text("$${"%.2f".format(item.price)}", fontWeight = FontWeight.Bold)
                            }

                            Spacer(Modifier.height(4.dp))
                            Text(item.description, fontSize = 14.sp)

                            if (item.dietTags.isNotEmpty()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Dietary: ${item.dietTags.joinToString(", ")}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }

                            Spacer(Modifier.height(4.dp))
                            if (!item.availability.isOutOfStock()) {
                                Text(
                                    "Available: ${item.availability.quantity}",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            } else {
                                Text(
                                    "OUT OF STOCK",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                }
            }
        }



        Spacer(modifier = Modifier.height(16.dp))



        Text("Reviews", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        if (reviews.isEmpty()) {
            Text("No reviews yet. Be the first to add one!", fontSize = 14.sp, modifier = Modifier.padding(8.dp))
        } else {
            reviews.forEach { review ->
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =  Arrangement.SpaceBetween
                        ){
                            Text(review.authorName, fontWeight = FontWeight.SemiBold)
                            Text("${review.rating}/5.0", fontWeight = FontWeight.Bold)
                    }

                        Text(review.timestamp, fontSize = 12.sp, color = Color.Gray)

                        if (review.comment.isNotBlank()){
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(review.comment, fontSize = 14.sp)
                        }

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
        OutlinedTextField(
            value = commentText,
            onValueChange = { if (it.length <= 280) commentText = it },
            label = { Text("Comment") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )

        Button(
            onClick = {
                val cleanName = reviewerName.trim()
                val rating = ratingText.toDoubleOrNull()
                if (cleanName.isNotEmpty() && rating != null && rating in 1.0..5.0) {
                    val newReview = Review(
                        reviewId = 0,
                        storeId = store.storeId,
                        authorName = cleanName,
                        rating = rating,
                        comment = commentText.trim(),
                        timestamp = System.currentTimeMillis().toString()

                    )

                    dataManager.addReview(newReview)

                    reviews = dataManager.getReviews(store.storeId)

                    reviewerName = ""
                    ratingText = ""
                    commentText = " "
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
    dataManager: DataManager,
    onBack: () -> Unit,
    onRestaurantSelected: (Store) -> Unit
) {


    var stores by remember {mutableStateOf(listOf<Store>())}

    LaunchedEffect(Unit) {
        stores = dataManager.getStores()
    }





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
            items(stores) { store ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable { onRestaurantSelected(store) },
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(store.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                        Text("Location: ${store.location}", fontSize = 14.sp)

                        Spacer(Modifier.height(4.dp))
                        Text("Operating Hours:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(store.operatingHours, fontSize = 13.sp)

                        if (store.dietaryOptions.isNotEmpty()){
                            Text(
                                "Dietary Option: ${store.dietaryOptions}",
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeMenuEditor(
    store: Store,
    dataManager: DataManager,
    onBack: () -> Unit,
    onEditItem: (MenuItem) -> Unit
) {

    var menu by remember {mutableStateOf(listOf<MenuItem>())}
    var reviews by remember { mutableStateOf(listOf<Review>()) }

    fun refreshMenu(){
        menu = dataManager.getMenuItems(store.storeId)
    }

    fun refreshReviews() {
        reviews = dataManager.getReviews(store.storeId)
    }

    LaunchedEffect(store) {
        refreshMenu()
        refreshReviews()
    }

    var newItemName by remember { mutableStateOf("") }
    var newItemDietTags by remember { mutableStateOf("") }
    var newItemQuantity by remember { mutableStateOf("") }
    var newItemDescription by remember { mutableStateOf("") }
    var newItemPrice by remember { mutableStateOf("") }
    var newItemCategory by remember { mutableStateOf("") }


    Column(modifier = Modifier.fillMaxSize()) {


        TopAppBar(
            title = { Text("Edit Menu – ${store.name}") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 15.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {


            item {
                Column {
                    Text(
                        "Hours: ${store.operatingHours}",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = newItemName,
                        onValueChange = { newItemName = it },
                        label = { Text("Menu item name") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = newItemDietTags,
                        onValueChange = { newItemDietTags = it },
                        label = { Text("Dietary tags (comma separated)") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = newItemQuantity,
                        onValueChange = { newItemQuantity = it },
                        label = { Text("Initial stock quantity") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = newItemDescription,
                        onValueChange = { newItemDescription = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = newItemPrice,
                        onValueChange = { newItemPrice = it },
                        label = { Text("Price (e.g. 4.99)") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = newItemCategory,
                        onValueChange = { newItemCategory = it },
                        label = { Text("Category (e.g. Drink, Snack)") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )

                    Button(
                        onClick = {
                            if (newItemName.isNotBlank() && newItemPrice.isNotBlank()) {
                                val item = MenuItem(
                                    itemId = 0,
                                    name = newItemName,
                                    description = newItemDescription,
                                    price = newItemPrice.toDoubleOrNull() ?: 0.0,
                                    category = newItemCategory,
                                    dietTags = newItemDietTags.split(",").map { it.trim() }
                                        .filter { it.isNotEmpty() }.toSet(),
                                    availability = Availability(
                                        quantity = newItemQuantity.toIntOrNull() ?: 0,
                                        lastUpdated = "Just now"
                                    )
                                )
                                dataManager.addMenuItem(item, store.storeId)

                                refreshMenu()

                                newItemName = ""
                                newItemDescription = ""
                                newItemPrice = ""
                                newItemCategory = ""
                                newItemDietTags = ""
                                newItemQuantity = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Text("Add Item")
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                    Text(
                        "Current Menu Items",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }


            items(items = menu) { menuItem ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { onEditItem(menuItem) },
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {

                        Text(menuItem.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Category: ${menuItem.category}", fontSize = 14.sp)
                        Text("Price: $${menuItem.price}", fontSize = 14.sp)
                        Text("Description: ${menuItem.description}", fontSize = 14.sp)

                        if (menuItem.dietTags.isNotEmpty()){
                            Text(
                                "Dietary: ${menuItem.dietTags.joinToString(", ")}",
                                fontSize = 14.sp
                            )
                        }

                        Text("Stock: ${menuItem.availability.quantity}", fontSize = 14.sp)
                        Text("Last Updated: ${menuItem.availability.lastUpdated}", fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(8.dp))


                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                val newQty = maxOf(menuItem.availability.quantity - 1, 0)
                                dataManager.updateAvailability(menuItem.itemId, newQty)
                                refreshMenu()
                            }) {
                                Text("-1")
                            }
                            Button(onClick = {
                                val newQty = menuItem.availability.quantity + 1
                                dataManager.updateAvailability(menuItem.itemId, newQty)
                                refreshMenu()
                            }) {
                                Text("+1")
                            }
                            Button(onClick = {
                                dataManager.updateAvailability(menuItem.itemId, 0)
                                refreshMenu()
                            }) {
                                Text("Set 0")
                            }
                        }
                    }
                }
            }
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                Text(
                    "Store Reviews",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(reviews) { review ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(review.authorName, fontWeight = FontWeight.Bold)
                                if (review.comment.isNotBlank()) {
                                    Text(review.comment, fontSize = 14.sp)
                                }
                            }
                            Button(
                                onClick = {
                                    dataManager.deleteReview(review.reviewId)
                                    refreshReviews()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMenuItemScreen(
    store: Store,
    item: MenuItem,
    dataManager: DataManager,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf(item.name) }
    var description by remember { mutableStateOf(item.description) }
    var price by remember { mutableStateOf(item.price.toString()) }
    var category by remember { mutableStateOf(item.category) }
    var dietTags by remember { mutableStateOf(item.dietTags.joinToString(", ")) }

    Column(
        Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("Edit ${item.name}") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            OutlinedTextField(name, { name = it }, label = { Text("Name") })
            OutlinedTextField(description, { description = it }, label = { Text("Description") })
            OutlinedTextField(price, { price = it }, label = { Text("Price (e.g. 4.99)") })
            OutlinedTextField(category, { category = it }, label = { Text("Category") })
            OutlinedTextField(dietTags, { dietTags = it }, label = { Text("Dietary Tags") })

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    val updatedItem = item.copy(
                        name = name,
                        description = description,
                        price = price.toDoubleOrNull() ?: item.price,
                        category = category,
                        dietTags = dietTags.split(",").map{it.trim() }.toSet(),
                        availability = item.availability
                    )
                    dataManager.updateMenuItem(updatedItem)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Changes")
            }
            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    dataManager.deleteMenuItem(item.itemId)
                    onBack()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Delete Item")
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuItemDetailsScreen(
    store: Store,
    item: MenuItem,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {

        TopAppBar(
            title = { Text(item.name) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )


        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(bottom = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Food Image Placeholder", fontSize = 14.sp)
            }

            Text("Category: ${item.category}", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text("Price: $${"%.2f".format(item.price)}")
            Spacer(Modifier.height(8.dp))

            Text("Description:", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(item.description, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))

            if (item.dietTags.isNotEmpty()) {
                Text("Dietary Tags:", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(item.dietTags.joinToString(", "), fontSize = 14.sp)
            }

            Spacer(Modifier.height(8.dp))

            if (!item.availability.isOutOfStock()) {
                Text("Available: ${item.availability.quantity} left", fontSize = 16.sp)
                Text("Last Updated: ${item.availability.lastUpdated}", fontSize = 14.sp)
            } else {
                Text(
                    "OUT OF STOCK",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "Sold at: ${store.name}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(store.location, fontSize = 12.sp)
        }
    }
}