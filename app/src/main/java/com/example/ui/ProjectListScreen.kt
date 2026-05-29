package com.example.ui

import android.content.Context
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Project

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectListScreen(
    viewModel: ProjectViewModel,
    onEditProject: (Project) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val projects by viewModel.filteredProjects.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val activeSectors by viewModel.selectedSectors.collectAsState()
    val activeUpazilas by viewModel.selectedUpazilas.collectAsState()
    val activeDonors by viewModel.selectedDonors.collectAsState()
    val activeStatuses by viewModel.selectedStatuses.collectAsState()
    val currentSort by viewModel.sortOrder.collectAsState()
    val currentUserState by viewModel.currentUser.collectAsState()
    val isAdmin = currentUserState?.role == "Admin"

    // Filter properties available
    val allSectors by viewModel.availableSectors.collectAsState()
    val allUpazilas by viewModel.availableUpazilas.collectAsState()
    val allDonors by viewModel.availableDonors.collectAsState()
    val allStatuses by viewModel.availableStatuses.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }
    var selectedDetailProject by remember { mutableStateOf<Project?>(null) }

    val activeFilterCount = activeSectors.size + activeUpazilas.size + activeDonors.size + activeStatuses.size

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Search Bar & Filter Toggle Row
            SearchBarAndFiltersRow(
                query = query,
                onQueryChange = { viewModel.searchQuery.value = it },
                activeFilterCount = activeFilterCount,
                onFilterClick = { showFilterSheet = true },
                onExportClick = {
                    exportProjectsAsCsv(context, projects, viewModel)
                },
                projectsList = projects
            )

            // Dynamic Active Filter Tags list
            if (activeFilterCount > 0) {
                ActiveFiltersChipsRow(
                    activeSectors = activeSectors,
                    onRemoveSector = { viewModel.selectedSectors.value = activeSectors - it },
                    activeUpazilas = activeUpazilas,
                    onRemoveUpazila = { viewModel.selectedUpazilas.value = activeUpazilas - it },
                    activeDonors = activeDonors,
                    onRemoveDonor = { viewModel.selectedDonors.value = activeDonors - it },
                    activeStatuses = activeStatuses,
                    onRemoveStatus = { viewModel.selectedStatuses.value = activeStatuses - it },
                    onResetAll = { viewModel.resetFilters() }
                )
            }

            // Project Count and Sorting Dropdown
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Showing ${projects.size} project registers",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )

                SortDropdownSelector(
                    currentSort = currentSort,
                    onSortChanged = { viewModel.sortOrder.value = it }
                )
            }

            // Empty State Check / Project List View
            if (projects.isEmpty()) {
                EmptyStateView(
                    queryIsEmpty = query.isBlank() && activeFilterCount == 0,
                    onReset = { viewModel.resetFilters() }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("projects_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(projects, key = { it.id }) { project ->
                        ProjectItemCard(
                            project = project,
                            onCardClick = { selectedDetailProject = project },
                            onEditClick = { onEditProject(project) },
                            onDeleteClick = { viewModel.deleteProject(project) },
                            isAdmin = isAdmin
                        )
                    }
                }
            }
        }

        // Advanced Filters Sliding Bottom Sheet
        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                modifier = Modifier.testTag("filter_bottom_sheet")
            ) {
                FiltersPanelContent(
                    allSectors = allSectors,
                    selectedSectors = activeSectors,
                    onSectorsChange = { viewModel.selectedSectors.value = it },
                    allUpazilas = allUpazilas,
                    selectedUpazilas = activeUpazilas,
                    onUpazilasChange = { viewModel.selectedUpazilas.value = it },
                    allDonors = allDonors,
                    selectedDonors = activeDonors,
                    onDonorsChange = { viewModel.selectedDonors.value = it },
                    allStatuses = allStatuses,
                    selectedStatuses = activeStatuses,
                    onStatusesChange = { viewModel.selectedStatuses.value = it },
                    onClose = { showFilterSheet = false },
                    onReset = {
                        viewModel.resetFilters()
                        showFilterSheet = false
                    }
                )
            }
        }

        // Project Detailed View Overlay Modal dialog
        selectedDetailProject?.let { project ->
            ProjectDetailDialog(
                project = project,
                onDismiss = { selectedDetailProject = null },
                onEditClick = {
                    selectedDetailProject = null
                    onEditProject(project)
                },
                onDeleteClick = {
                    selectedDetailProject = null
                    viewModel.deleteProject(project)
                }
            )
        }
    }
}

@Composable
fun SearchBarAndFiltersRow(
    query: String,
    onQueryChange: (String) -> Unit,
    activeFilterCount: Int,
    onFilterClick: () -> Unit,
    onExportClick: () -> Unit,
    projectsList: List<Project>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, start = 16.dp, end = 16.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Search Input Box
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search projects, manager, donor...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = if (query.isNotBlank()) {
                {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear text")
                    }
                }
            } else null,
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
                .testTag("search_bar"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            ),
            singleLine = true
        )

        // Filter Drawer Trigger
        Box(contentAlignment = Alignment.TopEnd) {
            FilledIconButton(
                onClick = onFilterClick,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (activeFilterCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .size(52.dp)
                    .testTag("filters_toggle_button")
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "Filters toggle",
                    tint = if (activeFilterCount > 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            if (activeFilterCount > 0) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary)
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = activeFilterCount.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // CSV Export Action
        FilledIconButton(
            onClick = onExportClick,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = Color(0xFF3BB273) // Seafoam Green
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .size(52.dp)
                .testTag("export_csv_button"),
            enabled = projectsList.isNotEmpty()
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Export data spreadsheet",
                tint = Color.White
            )
        }
    }
}

@Composable
fun ActiveFiltersChipsRow(
    activeSectors: Set<String>,
    onRemoveSector: (String) -> Unit,
    activeUpazilas: Set<String>,
    onRemoveUpazila: (String) -> Unit,
    activeDonors: Set<String>,
    onRemoveDonor: (String) -> Unit,
    activeStatuses: Set<String>,
    onRemoveStatus: (String) -> Unit,
    onResetAll: () -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            Text(
                text = "Clear All",
                style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.Underline),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable { onResetAll() }
                    .padding(end = 6.dp)
            )
        }

        items(activeSectors.toList()) { s ->
            FilterChipTag(text = "Sector: $s", onRemove = { onRemoveSector(s) })
        }
        items(activeUpazilas.toList()) { u ->
            FilterChipTag(text = "Upazila: $u", onRemove = { onRemoveUpazila(u) })
        }
        items(activeDonors.toList()) { d ->
            FilterChipTag(text = "Donor: $d", onRemove = { onRemoveDonor(d) })
        }
        items(activeStatuses.toList()) { st ->
            FilterChipTag(text = "Status: $st", onRemove = { onRemoveStatus(st) })
        }
    }
}

@Composable
fun FilterChipTag(text: String, onRemove: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.SemiBold
            )
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier
                    .size(14.dp)
                    .clickable { onRemove() },
                tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun SortDropdownSelector(
    currentSort: String,
    onSortChanged: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val sorts = listOf("Latest", "Budget Desc", "Budget Asc", "Beneficiaries Desc")

    Box {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { expanded = true }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sort: $currentSort",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            sorts.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item, style = MaterialTheme.typography.bodyMedium) },
                    onClick = {
                        onSortChanged(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ProjectItemCard(
    project: Project,
    onCardClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isAdmin: Boolean = true
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                shape = RoundedCornerShape(24.dp)
            )
            .testTag("project_card_${project.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Tracking ID & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = project.systemTrackingId,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                // Status Badge
                StatusBadge(project.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Project Names
            Text(
                text = project.projectNameEn,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = project.projectNameBn,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Grid Details (Sector, Donor, Location, Budget)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoLabelChip(
                    label = "Sector/Thematic",
                    value = project.sector,
                    icon = Icons.Default.Home,
                    modifier = Modifier.weight(1f)
                )
                InfoLabelChip(
                    label = "Donor Agency",
                    value = project.donorName,
                    icon = Icons.Default.List,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoLabelChip(
                    label = "Location",
                    value = "${project.upazila} (${project.union})",
                    icon = Icons.Default.LocationOn,
                    modifier = Modifier.weight(1f)
                )
                InfoLabelChip(
                    label = "Total Budget",
                    value = project.formattedBudgetShort,
                    icon = Icons.Default.Star,
                    valueColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Project Progress",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${project.completionPercentage}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = project.completionPercentage / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = when {
                        project.status.lowercase() == "completed" || project.completionPercentage == 100 -> MaterialTheme.colorScheme.secondary
                        project.status.lowercase() == "pipeline" -> MaterialTheme.colorScheme.outline
                        else -> MaterialTheme.colorScheme.primary
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
                if (project.lastUpdatedDate.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Last updated: ${project.lastUpdatedDate}",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Staff & Outreach Quick metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "HR Force",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${project.totalHR} personnel",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column {
                        Text(
                            text = "Beneficiaries",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatNumber(project.totalBeneficiaries.toLong()),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Edit/Delete Actions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(36.dp).testTag("edit_project_${project.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit project",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (isAdmin) {
                        var showDeleteConfirm by remember { mutableStateOf(false) }
                        IconButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.size(36.dp).testTag("delete_project_${project.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete project",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Delete safety confirmation dialog
                        if (showDeleteConfirm) {
                            AlertDialog(
                                onDismissRequest = { showDeleteConfirm = false },
                                title = { Text("Delete Project Register?") },
                                text = { Text("Are you sure you want to delete ${project.projectNameEn}? This operation is irreversible.") },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            onDeleteClick()
                                            showDeleteConfirm = false
                                        }
                                    ) {
                                        Text("Yes, Delete", color = MaterialTheme.colorScheme.tertiary)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteConfirm = false }) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val statusColor = when (status.lowercase()) {
        "active" -> MaterialTheme.colorScheme.primary // Forest Teal / Brand Teal
        "completed" -> Color(0xFF3B82F6) // Clean Indigo/Blue
        "pipeline" -> Color(0xFFF59E0B) // Amber Accent
        else -> Color.Gray
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(statusColor.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Text(
                text = status,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
        }
    }
}

@Composable
fun InfoLabelChip(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun FiltersPanelContent(
    allSectors: Set<String>,
    selectedSectors: Set<String>,
    onSectorsChange: (Set<String>) -> Unit,
    allUpazilas: Set<String>,
    selectedUpazilas: Set<String>,
    onUpazilasChange: (Set<String>) -> Unit,
    allDonors: Set<String>,
    selectedDonors: Set<String>,
    onDonorsChange: (Set<String>) -> Unit,
    allStatuses: Set<String>,
    selectedStatuses: Set<String>,
    onStatusesChange: (Set<String>) -> Unit,
    onClose: () -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .heightIn(max = 600.dp)
    ) {
        // Sheet Title bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Advanced Search Filters",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )
            TextButton(onClick = onReset) {
                Text("Reset All", color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sectors Selection
                item {
                    MultiSelectFilterSection(
                        title = "Intervention Sectors / Theme",
                        options = allSectors,
                        selected = selectedSectors,
                        onSelectionChanged = onSectorsChange
                    )
                }

                // Upazilas Selection
                item {
                    MultiSelectFilterSection(
                        title = "Working Locations (Upazila)",
                        options = allUpazilas,
                        selected = selectedUpazilas,
                        onSelectionChanged = onUpazilasChange
                    )
                }

                // Donors Selection
                item {
                    MultiSelectFilterSection(
                        title = "Donor Agencies",
                        options = allDonors,
                        selected = selectedDonors,
                        onSelectionChanged = onDonorsChange
                    )
                }

                // Statuses Selection
                item {
                    MultiSelectFilterSection(
                        title = "Project Implementation Status",
                        options = allStatuses,
                        selected = selectedStatuses,
                        onSelectionChanged = onStatusesChange
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Apply Filters", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MultiSelectFilterSection(
    title: String,
    options: Set<String>,
    selected: Set<String>,
    onSelectionChanged: (Set<String>) -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (options.isEmpty()) {
            Text(
                text = "No registers metadata matches",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                options.forEach { option ->
                    val isSelected = selected.contains(option)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val newSet = if (isSelected) selected - option else selected + option
                            onSelectionChanged(newSet)
                        },
                        label = { Text(option) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(
    queryIsEmpty: Boolean,
    onReset: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
            Text(
                text = if (queryIsEmpty) "No project registered yet!" else "No search results match",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (queryIsEmpty) "Click on 'Register Project' tab to catalog your first initiative into the Mukti database." else "Adjust your fuzzy keywords or remove active multi-select chips to expand matches.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            if (!queryIsEmpty) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onReset,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Clear All Filters", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ProjectDetailDialog(
    project: Project,
    onDismiss: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onEditClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("Edit Project")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDeleteClick,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("Delete")
                }
            }
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.projectNameEn,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = project.projectNameBn,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close detailed view")
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tracking & Meta Section
                item {
                    DetailSectionTitle("Project Meta Parameters")
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        DetailRow("Tracking ID", project.systemTrackingId)
                        DetailRow("Sector Theme", project.sector)
                        DetailRow("Donor Agency", project.donorName)
                        DetailRow("Status Value", project.status)
                    }
                }

                // Contact & Personnel Section
                item {
                    DetailSectionTitle("Assigned Personnel & Contact")
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        DetailRow("Project Manager", project.projectManagerName)
                        DetailRow("Responsible Role", project.responsiblePerson)
                        DetailRow("Phone Number", project.contactNumber)
                        DetailRow("Email Register", project.contactEmail)
                    }
                }

                // Geographic Scope
                item {
                    DetailSectionTitle("Geographic Mapping Parameters")
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        DetailRow("Upazila Location", project.upazila)
                        DetailRow("Union Location", project.union)
                        DetailRow("Office Address", project.officeAddress)
                    }
                }

                // Financial Schedules
                item {
                    DetailSectionTitle("Financials & Calendars")
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        DetailRow("Total Budget", project.formattedBudget)
                        DetailRow("Start Date", project.startDate)
                        DetailRow("End Date", project.endDate)
                        DetailRow("Target Outreach", "${formatNumber(project.totalBeneficiaries.toLong())} beneficiaries")
                    }
                }

                // HR Forces & Volunteers Breakdowns
                item {
                    DetailSectionTitle("Field Forces & Volunteers Breakdown")
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        GroupedVolunteersDisplay("Regular Paid Staff", project.maleStaff, project.femaleStaff)
                        GroupedVolunteersDisplay("Host Community Volunteers", project.maleHostVolunteer, project.femaleHostVolunteer)
                        GroupedVolunteersDisplay("FDMN (Refugee) Volunteers", project.maleFdmnVolunteer, project.femaleFdmnVolunteer)
                        
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Combined HR Force", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("${project.totalHR} personnel", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun DetailSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.primary
    )
    Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), thickness = 0.8.dp, modifier = Modifier.padding(top = 2.dp))
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1.5f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

@Composable
fun GroupedVolunteersDisplay(title: String, maleCount: Int, femaleCount: Int) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text("${maleCount + femaleCount} active", fontSize = 12.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Male: $maleCount", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiary))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Female: $femaleCount", fontSize = 11.sp, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Share system CSV document exporter trigger action
fun exportProjectsAsCsv(context: Context, projects: List<Project>, viewModel: ProjectViewModel) {
    try {
        val csv = viewModel.generateCsvString(projects)
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, csv)
            putExtra(Intent.EXTRA_TITLE, "Mukti Project Database - Filtered Registers Export")
            type = "text/csv"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Filtered Projects Spreadsheet CSV")
        context.startActivity(shareIntent)
    } catch (e: Exception) {
         e.printStackTrace()
    }
}
