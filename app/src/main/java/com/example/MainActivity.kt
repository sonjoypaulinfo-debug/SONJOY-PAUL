package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Lock
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.Project
import com.example.data.ProjectRepository
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Core Data Architecture Hook (Room Database & Repository Setup)
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = ProjectRepository(database.projectDao, database.userDao, database.notificationDao)
        val viewModelFactory = ProjectViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, viewModelFactory)[ProjectViewModel::class.java]

        setContent {
            MyApplicationTheme {
                MainAppLayout(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppLayout(viewModel: ProjectViewModel) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(0) } // 0 = Dashboard, 1 = Projects Registers List

    // State controlling Form visibility (Add vs Edit context)
    var isAddingProject by remember { mutableStateOf(false) }
    var editingProject by remember { mutableStateOf<Project?>(null) }

    // Screen Class Adaptation metrics
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val stats by viewModel.dashboardStats.collectAsState()
    val isSeeding by viewModel.isSeeding.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isAdmin = currentUser?.role == "Admin"

    // Determine if form is active
    val isFormActive = isAddingProject || editingProject != null

    if (isFormActive) {
        // Full screen focused Form UI
        ProjectFormScreen(
            editingProject = editingProject,
            isAdmin = isAdmin,
            onSaveProject = { projectSaved ->
                if (editingProject != null) {
                    viewModel.updateProject(projectSaved)
                    Toast.makeText(context, "Project Register Updated Successfully !", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.addProject(projectSaved)
                    Toast.makeText(context, "New Project Registered Successfully !", Toast.LENGTH_SHORT).show()
                }
                // Dismiss form sheet
                editingProject = null
                isAddingProject = false
                currentTab = 1 // Pivot to project catalog
            },
            onCancel = {
                editingProject = null
                isAddingProject = false
            }
        )
    } else {
        // Main Dashboard / Grid Structure
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                
                // Adaptive Side Navigation Rail for tablets/expanded screens (Better ergonomics!)
                if (isTablet) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        header = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "MUKTI",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp
                                )
                            }
                        },
                        modifier = Modifier.testTag("tablet_navigation_rail")
                    ) {
                        Spacer(modifier = Modifier.weight(1f))

                        NavigationRailItem(
                            selected = currentTab == 0,
                            onClick = { currentTab = 0 },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard view") },
                            label = { Text("Dashboard", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                            modifier = Modifier.testTag("rail_tab_dashboard")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        NavigationRailItem(
                            selected = currentTab == 1,
                            onClick = { currentTab = 1 },
                            icon = { Icon(Icons.Default.List, contentDescription = "Projects catalog") },
                            label = { Text("Registers", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                            modifier = Modifier.testTag("rail_tab_projects")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val alertsList by viewModel.allNotifications.collectAsState()
                        val unreadAlerts = alertsList.count { !it.isRead }

                        NavigationRailItem(
                            selected = currentTab == 2,
                            onClick = { currentTab = 2 },
                            icon = {
                                BadgedBox(badge = {
                                    if (unreadAlerts > 0) {
                                        Badge { Text("$unreadAlerts") }
                                    }
                                }) {
                                    Icon(Icons.Default.Notifications, contentDescription = "System alerts log")
                                }
                            },
                            label = { Text("Alerts", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                            modifier = Modifier.testTag("rail_tab_alerts")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val rawUser by viewModel.currentUser.collectAsState()
                        NavigationRailItem(
                            selected = currentTab == 3,
                            onClick = { currentTab = 3 },
                            icon = {
                                Icon(
                                    imageVector = if (rawUser != null) Icons.Default.Person else Icons.Default.Lock,
                                    contentDescription = "Identity secure portal"
                                )
                            },
                            label = {
                                Text(
                                    text = if (rawUser != null) rawUser!!.name.substringBefore(" ") else "Portal",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            modifier = Modifier.testTag("rail_tab_auth")
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        // Floating Action Button in Rail for adding new projects
                        FloatingActionButton(
                            onClick = {
                                if (isAdmin) {
                                    isAddingProject = true
                                } else {
                                    Toast.makeText(context, "Access Denied: Only Admins can register new projects. Log in as Admin under secure portal.", Toast.LENGTH_LONG).show()
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .padding(bottom = 24.dp)
                                .testTag("rail_fab_add")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Register project")
                        }
                    }
                }

                // Main Scaffold content block
                Scaffold(
                    modifier = Modifier.weight(1f),
                    topBar = {
                        TopAppBar(
                            title = {
                                Column {
                                    Text(
                                        text = "MUKTI COX'S BAZAR",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 11.sp,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "Project Database - 2026",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    bottomBar = {
                        // Standard Bottom Navigation Bar for Mobile screens
                        if (!isTablet) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.testTag("mobile_navigation_bar")
                            ) {
                                NavigationBarItem(
                                    selected = currentTab == 0,
                                    onClick = { currentTab = 0 },
                                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                                    label = { Text("Dashboard", fontWeight = FontWeight.Bold) },
                                    modifier = Modifier.testTag("nav_tab_dashboard")
                                )

                                NavigationBarItem(
                                    selected = currentTab == 1,
                                    onClick = { currentTab = 1 },
                                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                                    label = { Text("Registers", fontWeight = FontWeight.Bold) },
                                    modifier = Modifier.testTag("nav_tab_projects")
                                )

                                val alertsList by viewModel.allNotifications.collectAsState()
                                val unreadAlerts = alertsList.count { !it.isRead }

                                NavigationBarItem(
                                    selected = currentTab == 2,
                                    onClick = { currentTab = 2 },
                                    icon = {
                                        BadgedBox(badge = {
                                            if (unreadAlerts > 0) {
                                                Badge { Text("$unreadAlerts") }
                                            }
                                        }) {
                                            Icon(Icons.Default.Notifications, contentDescription = null)
                                        }
                                    },
                                    label = { Text("Alerts", fontWeight = FontWeight.Bold) },
                                    modifier = Modifier.testTag("nav_tab_alerts")
                                )

                                val rawUser by viewModel.currentUser.collectAsState()
                                NavigationBarItem(
                                    selected = currentTab == 3,
                                    onClick = { currentTab = 3 },
                                    icon = {
                                        Icon(
                                            imageVector = if (rawUser != null) Icons.Default.Person else Icons.Default.Lock,
                                            contentDescription = null
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = if (rawUser != null) rawUser!!.name.substringBefore(" ") else "Portal",
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    modifier = Modifier.testTag("nav_tab_auth")
                                )
                            }
                        }
                    },
                    floatingActionButton = {
                        // Mobile FAB trigger to register project with role check
                        if (!isTablet) {
                            FloatingActionButton(
                                onClick = {
                                    if (isAdmin) {
                                        isAddingProject = true
                                    } else {
                                        Toast.makeText(context, "Access Denied: Only Admins can register new projects. Log in as Admin under secure portal.", Toast.LENGTH_LONG).show()
                                    }
                                },
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.testTag("mobile_fab_add")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add project")
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (isSeeding) {
                            // Loading/Seeding shimmer fallback
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator()
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Seeding Mukti Project Database registers...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            // Page Routing
                            when (currentTab) {
                                0 -> DashboardScreen(
                                    stats = stats,
                                    modifier = Modifier.fillMaxSize()
                                )
                                1 -> ProjectListScreen(
                                    viewModel = viewModel,
                                    onEditProject = { projectToEdit ->
                                        editingProject = projectToEdit
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                                2 -> NotificationScreen(
                                    viewModel = viewModel,
                                    modifier = Modifier.fillMaxSize()
                                )
                                3 -> AuthScreen(
                                    viewModel = viewModel,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
