package com.example.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.Job
import com.example.data.JobApplication
import com.example.data.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilmCrewApp(viewModel: FilmCrewViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val activeUser by viewModel.activeUser.collectAsState()
    val allCrews by viewModel.allCrews.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val allJobs by viewModel.allJobs.collectAsState()
    val myApplications by viewModel.myApplications.collectAsState()

    val context = LocalContext.current

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        topBar = {
            if (currentScreen != ScreenState.Onboarding) {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MovieFilter,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Film Crew",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    },
                    navigationIcon = {
                        if (currentScreen is ScreenState.JobDetails || currentScreen is ScreenState.CrewDetails || currentScreen == ScreenState.PostJob) {
                            IconButton(
                                onClick = { viewModel.navigateTo(ScreenState.Dashboard) },
                                modifier = Modifier.testTag("back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back to dashboard"
                                )
                            }
                        } else {
                            // Quick Switch User Action
                            IconButton(onClick = { viewModel.navigateTo(ScreenState.Onboarding) }) {
                                Icon(
                                    imageVector = Icons.Default.SwitchAccount,
                                    contentDescription = "Switch Account Profile",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.logout() }) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Log Out"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        bottomBar = {
            if (currentScreen != ScreenState.Onboarding && currentScreen != ScreenState.CreateProfile &&
                currentScreen !is ScreenState.JobDetails && currentScreen !is ScreenState.CrewDetails &&
                currentScreen != ScreenState.PostJob
            ) {
                FilmCrewBottomBar(
                    activeUser = activeUser,
                    currentScreen = currentScreen,
                    onTabSelected = { screen ->
                        viewModel.navigateTo(screen)
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    (fadeIn() + slideInHorizontally { it }).togetherWith(fadeOut() + slideOutHorizontally { -it })
                },
                label = "ScreenTransition"
            ) { targetScreen ->
                when (targetScreen) {
                    is ScreenState.Onboarding -> OnboardingScreen(
                        allUsers = allUsers,
                        onLoginUser = { user -> viewModel.loginAs(user.id) },
                        onCreateProfileClick = { viewModel.navigateTo(ScreenState.CreateProfile) }
                    )
                    is ScreenState.CreateProfile -> CreateProfileScreen(
                        onCreate = { role, name, title, exp, bio, sample, email, phone ->
                            viewModel.createProfile(role, name, title, exp, bio, sample, email, phone)
                        },
                        onBack = { viewModel.navigateTo(ScreenState.Onboarding) }
                    )
                    is ScreenState.Dashboard -> DashboardScreen(
                        activeUser = activeUser,
                        allCrews = allCrews,
                        allJobs = allJobs,
                        viewModel = viewModel
                    )
                    is ScreenState.PostJob -> PostJobScreen(
                        onPost = { title, role, prod, salary, desc, expReq, loc ->
                            viewModel.postJob(title, role, prod, salary, desc, expReq, loc)
                        }
                    )
                    is ScreenState.JobDetails -> JobDetailsScreen(
                        job = targetScreen.job,
                        activeUser = activeUser,
                        viewModel = viewModel
                    )
                    is ScreenState.CrewDetails -> CrewDetailsScreen(
                        crew = targetScreen.crew,
                        activeUser = activeUser,
                        viewModel = viewModel
                    )
                    is ScreenState.ApplicationsList -> ApplicationsListScreen(
                        activeUser = activeUser,
                        myApplications = myApplications,
                        viewModel = viewModel
                    )
                    is ScreenState.MyProfile -> MyProfileScreen(
                        activeUser = activeUser,
                        onUpdate = { updatedUser ->
                            viewModel.updateProfile(updatedUser)
                            Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FilmCrewBottomBar(
    activeUser: User?,
    currentScreen: ScreenState,
    onTabSelected: (ScreenState) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        val isDirector = activeUser?.role == "DIRECTOR"

        // Tab 1: Home/Dashboard
        NavigationBarItem(
            selected = currentScreen == ScreenState.Dashboard,
            onClick = { onTabSelected(ScreenState.Dashboard) },
            label = { Text("Feed") },
            icon = {
                Icon(
                    imageVector = if (isDirector) Icons.Default.Groups else Icons.Default.Movie,
                    contentDescription = "Feed"
                )
            },
            modifier = Modifier.testTag("nav_feed")
        )

        // Tab 2: Applications / Recruits
        NavigationBarItem(
            selected = currentScreen == ScreenState.ApplicationsList,
            onClick = { onTabSelected(ScreenState.ApplicationsList) },
            label = { Text(if (isDirector) "Recruits" else "Applied") },
            icon = {
                Icon(
                    imageVector = if (isDirector) Icons.Default.AssignmentInd else Icons.Default.CheckCircle,
                    contentDescription = "Applications"
                )
            },
            modifier = Modifier.testTag("nav_applications")
        )

        // Tab 3: Profile
        NavigationBarItem(
            selected = currentScreen == ScreenState.MyProfile,
            onClick = { onTabSelected(ScreenState.MyProfile) },
            label = { Text("Profile") },
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile"
                )
            },
            modifier = Modifier.testTag("nav_profile")
        )
    }
}

@Composable
fun OnboardingScreen(
    allUsers: List<User>,
    onLoginUser: (User) -> Unit,
    onCreateProfileClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // High-quality cinematic image generated earlier
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_film_hero),
                        contentDescription = "Film Set Hero Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                                    startY = 200f
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("CINEMA PRODUCTION HUB") },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                labelColor = MaterialTheme.colorScheme.primary
                            ),
                            border = SuggestionChipDefaults.suggestionChipBorder(
                                enabled = true,
                                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Shoot Your Next Film",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Film Crew Recruiting",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Connect directors looking for talent with crew seeking production jobs like cameramen, actors, ward boys, and helpers.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        item {
            Button(
                onClick = onCreateProfileClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("create_profile_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Your Free Profile", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        if (allUsers.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        text = " OR LOGIN AS ",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }
            }

            items(allUsers) { user ->
                val interactionSource = remember { MutableInteractionSource() }
                val isHovered by interactionSource.collectIsHoveredAsState()
                val scale by animateFloatAsState(
                    targetValue = if (isHovered) 1.02f else 1.0f,
                    animationSpec = tween(durationMillis = 200),
                    label = "login_hover_scale"
                )
                val borderAlpha by animateFloatAsState(
                    targetValue = if (isHovered) 0.6f else 0.3f,
                    animationSpec = tween(durationMillis = 200),
                    label = "login_hover_border"
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .hoverable(interactionSource)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = LocalIndication.current
                        ) { onLoginUser(user) }
                        .testTag("login_user_${user.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isHovered) 0.8f else 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = borderAlpha))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (user.role == "DIRECTOR") MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (user.role == "DIRECTOR") Icons.Default.MovieCreation else Icons.Default.Person,
                                contentDescription = null,
                                tint = if (user.role == "DIRECTOR") MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = user.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${user.title} • ${user.role.lowercase().replaceFirstChar { it.uppercase() }}",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                        IconButton(onClick = { onLoginUser(user) }) {
                            Icon(
                                imageVector = Icons.Default.ArrowForwardIos,
                                contentDescription = "Login",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
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
fun CreateProfileScreen(
    onCreate: (role: String, name: String, title: String, experienceYears: Int, bio: String, sampleWorkUrl: String, email: String, phone: String) -> Unit,
    onBack: () -> Unit
) {
    var role by remember { mutableStateOf("CREW") } // "CREW" or "DIRECTOR"
    var name by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var experienceYears by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var sampleWorkUrl by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    var titleError by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Create Free Profile",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            Text(
                "Join our community and pitch for real production sets.",
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                modifier = Modifier.padding(start = 48.dp)
            )
        }

        item {
            Text("Select Your Account Type:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            role = "CREW"
                            if (title == "Director") title = ""
                        }
                        .testTag("select_crew_role"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (role == "CREW") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        width = if (role == "CREW") 2.dp else 1.dp,
                        color = if (role == "CREW") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            tint = if (role == "CREW") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Film Crew", fontWeight = FontWeight.Bold)
                        Text("Looking for job", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            role = "DIRECTOR"
                            title = "Director"
                        }
                        .testTag("select_director_role"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (role == "DIRECTOR") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        width = if (role == "DIRECTOR") 2.dp else 1.dp,
                        color = if (role == "DIRECTOR") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.MovieCreation,
                            contentDescription = null,
                            tint = if (role == "DIRECTOR") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Director", fontWeight = FontWeight.Bold)
                        Text("Hiring Crew", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = it.isBlank()
                },
                label = { Text("Full Name") },
                isError = nameError,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_name"),
                leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) }
            )
            if (nameError) {
                Text("Name cannot be empty", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }

        if (role == "CREW") {
            item {
                Text("Select your specialization:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                val roles = listOf("Cameraman", "Actor", "Ward Boy", "Sound Designer", "Makeup Artist", "Helper")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    roles.take(3).forEach { r ->
                        FilterChip(
                            selected = title == r,
                            onClick = { title = r },
                            label = { Text(r) },
                            modifier = Modifier.testTag("role_chip_$r")
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    roles.drop(3).forEach { r ->
                        FilterChip(
                            selected = title == r,
                            onClick = { title = r },
                            label = { Text(r) },
                            modifier = Modifier.testTag("role_chip_$r")
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = it.isBlank()
                    },
                    label = { Text("Custom Role Title (if not in above chips)") },
                    isError = titleError,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_title")
                )
            }
        }

        item {
            OutlinedTextField(
                value = experienceYears,
                onValueChange = { experienceYears = it.filter { char -> char.isDigit() } },
                label = { Text("Years of Experience") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_exp"),
                leadingIcon = { Icon(imageVector = Icons.Default.Timeline, contentDescription = null) }
            )
        }

        item {
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Tell us about yourself (Bio / On-set experience)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .testTag("input_bio"),
                maxLines = 5,
                leadingIcon = { Icon(imageVector = Icons.Default.Description, contentDescription = null) }
            )
        }

        if (role == "CREW") {
            item {
                OutlinedTextField(
                    value = sampleWorkUrl,
                    onValueChange = { sampleWorkUrl = it },
                    label = { Text("Sample Work URL (Vimeo, YouTube, Drive)") },
                    placeholder = { Text("https://vimeo.com/...") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_sample_work"),
                    leadingIcon = { Icon(imageVector = Icons.Default.Link, contentDescription = null) }
                )
            }
        }

        item {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_email"),
                leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null) }
            )
        }

        item {
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_phone"),
                leadingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = null) }
            )
        }

        item {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    if (role == "CREW" && title.isBlank()) {
                        titleError = true
                        return@Button
                    }

                    val expVal = experienceYears.toIntOrNull() ?: 0
                    onCreate(
                        role,
                        name,
                        if (role == "DIRECTOR") "Director" else title,
                        expVal,
                        bio,
                        if (role == "DIRECTOR") "" else sampleWorkUrl,
                        email,
                        phone
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("submit_profile"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Create Profile & Enter Dashboard", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun DashboardScreen(
    activeUser: User?,
    allCrews: List<User>,
    allJobs: List<Job>,
    viewModel: FilmCrewViewModel
) {
    if (activeUser == null) return

    val isDirector = activeUser.role == "DIRECTOR"
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showHireDialog by remember { mutableStateOf(false) }
    var selectedCrewForHire by remember { mutableStateOf<User?>(null) }

    val categories = listOf("All", "Cameraman", "Actor", "Ward Boy", "Sound Designer", "Makeup Artist", "Helper")

    val filteredCrews = remember(allCrews, searchQuery, selectedCategory) {
        allCrews.filter { crew ->
            val matchesQuery = crew.name.contains(searchQuery, ignoreCase = true) ||
                    crew.bio.contains(searchQuery, ignoreCase = true) ||
                    crew.title.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == "All" || crew.title.equals(selectedCategory, ignoreCase = true)
            matchesQuery && matchesCategory
        }
    }

    val filteredJobs = remember(allJobs, searchQuery, selectedCategory) {
        allJobs.filter { job ->
            val matchesQuery = job.title.contains(searchQuery, ignoreCase = true) ||
                    job.description.contains(searchQuery, ignoreCase = true) ||
                    job.productionName.contains(searchQuery, ignoreCase = true) ||
                    job.location.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == "All" || job.roleType.equals(selectedCategory, ignoreCase = true)
            matchesQuery && matchesCategory
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Active User Header Widget with cinematic glowing look
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
            ),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDirector) Icons.Default.MovieCreation else Icons.Default.Engineering,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Lights, Camera, Action! 🎬",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = activeUser.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isDirector) "Director / Producer Mode" else "Crew Member Profile • ${activeUser.title}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Search Bar (Interactive and responsive)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search crews, locations, roles...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Filters",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dashboard_search_input"),
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontally Scrollable Category Chips (Sophisticated Dark HTML theme replication!)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = category },
                    label = { Text(category, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)) },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    } else null,
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        selectedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("dashboard_chip_$category")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isDirector) "Recommended Crews" else "Open Film Castings",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            )

            if (isDirector) {
                Button(
                    onClick = { viewModel.navigateTo(ScreenState.PostJob) },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("post_job_btn")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Post Opening", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isDirector) {
            // Direct Hiring Board (View filtered needy crews)
            if (filteredCrews.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MovieFilter,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No matches found",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Adjust your search parameters or check other roles.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredCrews) { crew ->
                        CrewListItemCard(
                            crew = crew,
                            isDirector = isDirector,
                            onHireClick = {
                                selectedCrewForHire = crew
                                showHireDialog = true
                            },
                            onClick = { viewModel.navigateTo(ScreenState.CrewDetails(crew)) }
                        )
                    }
                }
            }
        } else {
            // Film Openings Board
            if (filteredJobs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No active production roles",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No job postings matched this role or keyword right now.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredJobs) { job ->
                        JobListItemCard(job = job, onClick = { viewModel.navigateTo(ScreenState.JobDetails(job)) })
                    }
                }
            }
        }
    }

    if (showHireDialog && selectedCrewForHire != null) {
        val crew = selectedCrewForHire!!
        val context = LocalContext.current
        val directorJobs = allJobs.filter { it.directorId == activeUser.id }

        var selectedJob by remember { mutableStateOf<Job?>(if (directorJobs.isNotEmpty()) directorJobs.first() else null) }
        var dropdownExpanded by remember { mutableStateOf(false) }
        var inquiryMessage by remember { mutableStateOf("Hi ${crew.name}, I checked your film crew profile and experience as a ${crew.title}. I am really impressed by your background and would love to hire/collaborate with you for my upcoming film production!") }

        AlertDialog(
            onDismissRequest = {
                showHireDialog = false
                selectedCrewForHire = null
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Hire ${crew.name}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Send a direct casting or crew invitation to ${crew.name}. They will see this inquiry in their job dashboard.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Associate Production Job:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (directorJobs.isEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "No active job listings. This will be sent as a direct casting invitation.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { dropdownExpanded = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("hire_dialog_job_dropdown"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedJob?.title ?: "General Direct Inquiry",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.85f)
                            ) {
                                directorJobs.forEach { job ->
                                    DropdownMenuItem(
                                        text = { Text(job.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                        onClick = {
                                            selectedJob = job
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = inquiryMessage,
                        onValueChange = { inquiryMessage = it },
                        label = { Text("Inquiry Message") },
                        placeholder = { Text("Write details of your role, payment, or schedule...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("hire_dialog_message_input"),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 3,
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val jobId = selectedJob?.id ?: -1L
                        viewModel.sendDirectInquiry(
                            crewId = crew.id,
                            crewName = crew.name,
                            crewRole = crew.title,
                            jobId = jobId,
                            message = inquiryMessage,
                            directorName = activeUser.name
                        )
                        Toast.makeText(context, "Direct Inquiry sent to ${crew.name}! 🎬", Toast.LENGTH_LONG).show()
                        showHireDialog = false
                        selectedCrewForHire = null
                    },
                    modifier = Modifier.testTag("hire_dialog_send_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Send Inquiry", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showHireDialog = false
                        selectedCrewForHire = null
                    },
                    modifier = Modifier.testTag("hire_dialog_cancel_btn")
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun CrewListItemCard(
    crew: User,
    isDirector: Boolean = false,
    onHireClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.02f else 1.0f,
        animationSpec = tween(durationMillis = 200),
        label = "crew_hover_scale"
    )
    val borderAlpha by animateFloatAsState(
        targetValue = if (isHovered) 0.6f else 0.2f,
        animationSpec = tween(durationMillis = 200),
        label = "crew_hover_border"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .hoverable(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current
            ) { onClick() }
            .testTag("crew_card_${crew.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHovered) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = borderAlpha))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = when (crew.title) {
                        "Cameraman" -> Icons.Default.Videocam
                        "Actor", "Lead Actress" -> Icons.Default.Face
                        "Ward Boy" -> Icons.Default.Checkroom
                        else -> Icons.Default.Engineering
                    }
                    Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = crew.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text(crew.title) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = null,
                            modifier = Modifier.height(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${crew.experienceYears} yrs exp",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "View Details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = crew.bio,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (crew.sampleWorkUrl.isNotBlank() && crew.sampleWorkUrl != "N/A") {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = "Has Work Sample",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Sample Work Available",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (isDirector && onHireClick != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onHireClick() },
                    modifier = Modifier
                        .align(Alignment.End)
                        .testTag("hire_crew_btn_${crew.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Hire Crew", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun JobListItemCard(job: Job, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.02f else 1.0f,
        animationSpec = tween(durationMillis = 200),
        label = "job_hover_scale"
    )
    val borderAlpha by animateFloatAsState(
        targetValue = if (isHovered) 0.6f else 0.2f,
        animationSpec = tween(durationMillis = 200),
        label = "job_hover_border"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .hoverable(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current
            ) { onClick() }
            .testTag("job_card_${job.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHovered) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = borderAlpha))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = job.productionName,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = job.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Text(
                    text = job.salaryRange,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = job.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = {},
                        label = { Text(job.roleType) },
                        leadingIcon = {
                            val icon = when (job.roleType) {
                                "Cameraman" -> Icons.Default.Videocam
                                "Actor" -> Icons.Default.Face
                                "Ward Boy" -> Icons.Default.Checkroom
                                else -> Icons.Default.Movie
                            }
                            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    )

                    AssistChip(
                        onClick = {},
                        label = { Text(job.location) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Place, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    )
                }

                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Details",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun PostJobScreen(
    onPost: (title: String, roleType: String, productionName: String, salaryRange: String, description: String, experienceRequired: String, location: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var roleType by remember { mutableStateOf("Cameraman") }
    var productionName by remember { mutableStateOf("") }
    var salaryRange by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var experienceRequired by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    var titleError by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Post Crew Opening",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                "Specify production requirements to get pitches from eligible crew.",
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }

        item {
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    titleError = it.isBlank()
                },
                label = { Text("Job Opening Title") },
                placeholder = { Text("Need Senior Cameraman / Ward Boy Assistant") },
                isError = titleError,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("post_job_title")
            )
        }

        item {
            Text("Needed Specialty:", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            val crewRoles = listOf("Cameraman", "Actor", "Ward Boy", "Makeup Artist", "Sound Designer", "Other")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                crewRoles.take(3).forEach { r ->
                    FilterChip(
                        selected = roleType == r,
                        onClick = { roleType = r },
                        label = { Text(r) },
                        modifier = Modifier.testTag("post_role_$r")
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                crewRoles.drop(3).forEach { r ->
                    FilterChip(
                        selected = roleType == r,
                        onClick = { roleType = r },
                        label = { Text(r) },
                        modifier = Modifier.testTag("post_role_$r")
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = productionName,
                onValueChange = { productionName = it },
                label = { Text("Production Name / Project") },
                placeholder = { Text("Project: Dark Knight (Short Film)") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("post_production")
            )
        }

        item {
            OutlinedTextField(
                value = salaryRange,
                onValueChange = { salaryRange = it },
                label = { Text("Salary / Pay Scale") },
                placeholder = { Text("$200/Day or Fixed Pay") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("post_salary")
            )
        }

        item {
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Shoot Location") },
                placeholder = { Text("Mumbai / Delhi / Set 4") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("post_location")
            )
        }

        item {
            OutlinedTextField(
                value = experienceRequired,
                onValueChange = { experienceRequired = it },
                label = { Text("Experience Required") },
                placeholder = { Text("2+ Years with showreel or Freshers welcome") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("post_experience_required")
            )
        }

        item {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Detailed Job Requirements") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .testTag("post_description"),
                maxLines = 5
            )
        }

        item {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                        return@Button
                    }
                    onPost(title, roleType, productionName, salaryRange, description, experienceRequired, location)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("submit_job"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Publish Production Job", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailsScreen(
    job: Job,
    activeUser: User?,
    viewModel: FilmCrewViewModel
) {
    if (activeUser == null) return

    val isDirector = activeUser.role == "DIRECTOR"
    var showApplyForm by remember { mutableStateOf(false) }

    var applyExperience by remember { mutableStateOf("") }
    var applySampleWork by remember { mutableStateOf(activeUser.sampleWorkUrl) }

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = job.productionName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = job.title,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text(job.roleType) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                labelColor = MaterialTheme.colorScheme.primary
                            ),
                            border = null
                        )
                        Text(
                            text = job.salaryRange,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }
                }
            }
        }

        item {
            Text("Specifications", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Shoot Location", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(job.location, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider()

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Timeline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Experience Required", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(job.experienceRequired, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider()

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Posted By (Director)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(job.directorName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Text("Job Requirements", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Text(
                    text = job.description,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (isDirector) {
            if (job.directorId == activeUser.id) {
                item {
                    Button(
                        onClick = {
                            viewModel.deleteJob(job.id)
                            Toast.makeText(context, "Job deleted!", Toast.LENGTH_SHORT).show()
                            viewModel.navigateTo(ScreenState.Dashboard)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Opening", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Apply Flow for Crew
            item {
                AnimatedVisibility(visible = !showApplyForm) {
                    Button(
                        onClick = { showApplyForm = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("apply_for_job_btn"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pitch for this Job (Apply)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                AnimatedVisibility(visible = showApplyForm) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Your Job Pitch Details", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                            OutlinedTextField(
                                value = applyExperience,
                                onValueChange = { applyExperience = it },
                                label = { Text("Why are you a good fit for this production?") },
                                placeholder = { Text("E.g. I have worked on 2 similar projects, available on dates...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .testTag("apply_experience_input")
                            )

                            OutlinedTextField(
                                value = applySampleWork,
                                onValueChange = { applySampleWork = it },
                                label = { Text("Add Showreel / Sample Work URL") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("apply_sample_input")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showApplyForm = false },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Cancel")
                                }

                                Button(
                                    onClick = {
                                        viewModel.applyForJob(job.id, applyExperience, applySampleWork)
                                        Toast.makeText(context, "Application pitched successfully! 🎬", Toast.LENGTH_LONG).show()
                                        showApplyForm = false
                                        viewModel.navigateTo(ScreenState.ApplicationsList)
                                    },
                                    modifier = Modifier
                                        .weight(1.5f)
                                        .testTag("submit_application_btn"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Submit Pitch", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CrewDetailsScreen(
    crew: User,
    activeUser: User?,
    viewModel: FilmCrewViewModel
) {
    if (activeUser == null) return

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        val icon = when (crew.title) {
                            "Cameraman" -> Icons.Default.Videocam
                            "Actor", "Lead Actress" -> Icons.Default.Face
                            "Ward Boy" -> Icons.Default.Checkroom
                            else -> Icons.Default.Person
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = crew.name,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    SuggestionChip(
                        onClick = {},
                        label = { Text(crew.title) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            labelColor = MaterialTheme.colorScheme.primary
                        ),
                        border = null
                    )
                }
            }
        }

        item {
            Text("Career Bio & Experience", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Timeline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Total Experience: ${crew.experienceYears} Years",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    HorizontalDivider()
                    Text(
                        text = crew.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        if (crew.sampleWorkUrl.isNotBlank() && crew.sampleWorkUrl != "N/A") {
            item {
                Text("Sample Showreel / Portfolio", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.PlayCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Showreel Link:",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = crew.sampleWorkUrl,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(crew.sampleWorkUrl))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Invalid link, copied to clipboard instead.", Toast.LENGTH_SHORT).show()
                                        clipboardManager.setText(AnnotatedString(crew.sampleWorkUrl))
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Open Reel", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(crew.sampleWorkUrl))
                                    Toast.makeText(context, "Link copied!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy Link", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        item {
            Text("Contact Crew Member", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (crew.email.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Email Address", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(crew.email, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (crew.phone.isNotBlank()) {
                        if (crew.email.isNotBlank()) HorizontalDivider()
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Phone Number", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(crew.phone, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = {
                    try {
                        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:${crew.email}")
                            putExtra(Intent.EXTRA_SUBJECT, "Film Crew Hiring Invitation")
                        }
                        context.startActivity(emailIntent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Hiring request sent to: ${crew.email}", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("hire_crew_btn"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Handshake, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hire / Invite for Shoot", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ApplicationsListScreen(
    activeUser: User?,
    myApplications: List<JobApplication>,
    viewModel: FilmCrewViewModel
) {
    if (activeUser == null) return

    val isDirector = activeUser.role == "DIRECTOR"
    val allJobs by viewModel.allJobs.collectAsState()
    val context = LocalContext.current

    if (isDirector) {
        val directorJobs = allJobs.filter { it.directorId == activeUser.id }
        var selectedDirectorJob by remember { mutableStateOf<Job?>(null) }

        LaunchedEffect(directorJobs) {
            if (selectedDirectorJob == null && directorJobs.isNotEmpty()) {
                selectedDirectorJob = directorJobs.first()
                viewModel.selectJobForApplications(directorJobs.first().id)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Applications Received", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Text("Review film crews pitching to work on your movies.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(12.dp))

            if (directorJobs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("You haven't posted any jobs yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                // Dropdown to select director's jobs
                var dropdownExpanded by remember { mutableStateOf(false) }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { dropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth().testTag("select_job_dropdown"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = selectedDirectorJob?.title ?: "Select Production Job",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        directorJobs.forEach { job ->
                            DropdownMenuItem(
                                text = { Text(job.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                onClick = {
                                    selectedDirectorJob = job
                                    viewModel.selectJobForApplications(job.id)
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val appsByJob by viewModel.selectedJobForAppsApplications.collectAsState()

                if (appsByJob.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No applicants have pitched for this job yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(appsByJob) { app ->
                            DirectorApplicationItemCard(
                                application = app,
                                onAccept = {
                                    viewModel.updateApplicationStatus(app.id, "ACCEPTED")
                                    Toast.makeText(context, "Applicant Accepted! 🎉 Contact them.", Toast.LENGTH_SHORT).show()
                                },
                                onDecline = {
                                    viewModel.updateApplicationStatus(app.id, "DECLINED")
                                    Toast.makeText(context, "Applicant Declined.", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Crew's own pitches
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("My Job Pitches", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Text("Track status of your showreels pitched to directors.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(16.dp))

            if (myApplications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("You haven't pitched to any production jobs yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(myApplications) { app ->
                        val job = allJobs.find { it.id == app.jobId }
                        CrewApplicationItemCard(application = app, job = job)
                    }
                }
            }
        }
    }
}

@Composable
fun DirectorApplicationItemCard(
    application: JobApplication,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.02f else 1.0f,
        animationSpec = tween(durationMillis = 200),
        label = "dir_app_hover_scale"
    )
    val borderAlpha by animateFloatAsState(
        targetValue = if (isHovered) 0.6f else 0.2f,
        animationSpec = tween(durationMillis = 200),
        label = "dir_app_hover_border"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .hoverable(interactionSource)
            .testTag("director_app_card_${application.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isHovered) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = borderAlpha))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = application.crewName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = application.crewRole,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Status Badge
                AssistChip(
                    onClick = {},
                    label = { Text(application.status) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = when (application.status) {
                            "ACCEPTED" -> Color(0xFF2E7D32).copy(alpha = 0.2f)
                            "DECLINED" -> Color(0xFFC62828).copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        labelColor = when (application.status) {
                            "ACCEPTED" -> Color(0xFF4CAF50)
                            "DECLINED" -> Color(0xFFF44336)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    ),
                    border = null
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Crew Experience Pitch:",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = application.experience,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )

            if (application.sampleWork.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Work Reel / Portfolio:",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = application.sampleWork,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (application.status == "PENDING") {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDecline,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Decline")
                    }

                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1.2f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hire Crew", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CrewApplicationItemCard(application: JobApplication, job: Job?) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.02f else 1.0f,
        animationSpec = tween(durationMillis = 200),
        label = "crew_app_hover_scale"
    )
    val borderAlpha by animateFloatAsState(
        targetValue = if (isHovered) 0.6f else 0.2f,
        animationSpec = tween(durationMillis = 200),
        label = "crew_app_hover_border"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .hoverable(interactionSource),
        colors = CardDefaults.cardColors(
            containerColor = if (isHovered) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = borderAlpha))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = job?.productionName ?: "Film Production Set",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = job?.title ?: "Film Crew Requirement",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                AssistChip(
                    onClick = {},
                    label = { Text(application.status) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = when (application.status) {
                            "ACCEPTED" -> Color(0xFF2E7D32).copy(alpha = 0.2f)
                            "DECLINED" -> Color(0xFFC62828).copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        labelColor = when (application.status) {
                            "ACCEPTED" -> Color(0xFF4CAF50)
                            "DECLINED" -> Color(0xFFF44336)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    ),
                    border = null
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Your Submitted Pitch:",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = application.experience,
                style = MaterialTheme.typography.bodyMedium
            )

            if (application.status == "ACCEPTED") {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32).copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Celebration, contentDescription = null, tint = Color(0xFF4CAF50))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Director has approved your pitch! Check your registered email/phone for shooting schedule.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF81C784),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MyProfileScreen(
    activeUser: User?,
    onUpdate: (User) -> Unit
) {
    if (activeUser == null) return

    var name by remember { mutableStateOf(activeUser.name) }
    var title by remember { mutableStateOf(activeUser.title) }
    var experienceYears by remember { mutableStateOf(activeUser.experienceYears.toString()) }
    var bio by remember { mutableStateOf(activeUser.bio) }
    var sampleWorkUrl by remember { mutableStateOf(activeUser.sampleWorkUrl) }
    var email by remember { mutableStateOf(activeUser.email) }
    var phone by remember { mutableStateOf(activeUser.phone) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "My Film Profile",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                "Update your contact information, credentials, and portfolios.",
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }

        item {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Display Name") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_name")
            )
        }

        if (activeUser.role == "CREW") {
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Specialty Title") },
                    placeholder = { Text("Cameraman, Actor, Ward Boy...") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_title")
                )
            }
        }

        item {
            OutlinedTextField(
                value = experienceYears,
                onValueChange = { experienceYears = it.filter { char -> char.isDigit() } },
                label = { Text("Years of Experience") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_exp")
            )
        }

        item {
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Professional Bio") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .testTag("edit_bio"),
                maxLines = 5
            )
        }

        if (activeUser.role == "CREW") {
            item {
                OutlinedTextField(
                    value = sampleWorkUrl,
                    onValueChange = { sampleWorkUrl = it },
                    label = { Text("Work Reel URL") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_sample_work")
                )
            }
        }

        item {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_email")
            )
        }

        item {
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_phone")
            )
        }

        item {
            Button(
                onClick = {
                    onUpdate(
                        activeUser.copy(
                            name = name,
                            title = if (activeUser.role == "DIRECTOR") "Director" else title,
                            experienceYears = experienceYears.toIntOrNull() ?: 0,
                            bio = bio,
                            sampleWorkUrl = sampleWorkUrl,
                            email = email,
                            phone = phone
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("save_profile_edit"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Profile Changes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
