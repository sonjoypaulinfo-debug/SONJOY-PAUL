package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Project
import com.example.data.ProjectRepository
import com.example.data.User
import com.example.data.Notification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

data class QuadFilter(
    val sectors: Set<String>,
    val upazilas: Set<String>,
    val donors: Set<String>,
    val statuses: Set<String>
)

class ProjectViewModel(private val repository: ProjectRepository) : ViewModel() {

    // Filter states
    val searchQuery = MutableStateFlow("")
    val selectedSectors = MutableStateFlow<Set<String>>(emptySet())
    val selectedUpazilas = MutableStateFlow<Set<String>>(emptySet())
    val selectedDonors = MutableStateFlow<Set<String>>(emptySet())
    val selectedStatuses = MutableStateFlow<Set<String>>(emptySet())
    val sortOrder = MutableStateFlow("Latest") // "Latest", "Budget Desc", "Budget Asc", "Beneficiaries Desc"

    // Seed data indicator
    val isSeeding = MutableStateFlow(false)

    // Auth and Notification States
    val currentUser = MutableStateFlow<User?>(null)

    val allNotifications: StateFlow<List<Notification>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        checkAndSeedDatabase()
        viewModelScope.launch {
            val sonjoy = repository.getUserByEmail("Sonjoypaul.info@gmail.com")
            if (sonjoy != null) {
                currentUser.value = sonjoy
            } else {
                val adminObj = repository.getUserByEmail("admin@mukti.org")
                currentUser.value = adminObj
            }
        }
    }

    private fun checkAndSeedDatabase() {
        viewModelScope.launch {
            isSeeding.value = true

            // Seed Users
            val currentUsers = repository.getAllUsers().first()
            if (currentUsers.isEmpty()) {
                repository.insertUser(User("admin@mukti.org", "Admin Director", "adminpassword", "Admin"))
                repository.insertUser(User("staff@mukti.org", "Staff Representative", "staffpassword", "Regular"))
                repository.insertUser(User("Sonjoypaul.info@gmail.com", "Sonjoy Paul", "sonjoypassword", "Regular"))
            }

            // Seed Notifications
            val currentAlerts = repository.allNotifications.first()
            if (currentAlerts.isEmpty()) {
                repository.insertNotification(
                    Notification(
                        title = "Upcoming Project End Date",
                        message = "URGENT NOTICE: 'Emergency Food Security and Livelihoods Support (EFSL)' is ending soon on 2026-05-31. Plan administrative transition.",
                        date = "2026-05-29",
                        projectId = 2,
                        type = "EndDate"
                    )
                )
                repository.insertNotification(
                    Notification(
                        title = "Significant Budget Allocation",
                        message = "Annual funding of BDT 24,500,000.00 assigned to 'Integrated WASH Response' has been approved by UNHCR.",
                        date = "2026-05-28",
                        projectId = 1,
                        type = "BudgetChange"
                    )
                )
                repository.insertNotification(
                    Notification(
                        title = "Status Update Alert",
                        message = "Milestone: 'Sustained Protection and GBV Prevention Program' is now fully Active in Ukhiya Balukhali.",
                        date = "2026-05-27",
                        projectId = 3,
                        type = "StatusUpdate"
                    )
                )
            }

            val currentList = repository.allProjects.first()
            if (currentList.isEmpty()) {
                val seedData = listOf(
                    Project(
                        id = 0,
                        projectNameEn = "Integrated WASH Response for FDMN in Camps 11 & 12 and Host Community",
                        projectNameBn = "এফডিএমএন ক্যাম্প ১১ এবং ১২ ও হোস্ট কমিউনিটির জন্য সমন্বিত ওয়াশ কার্যক্রম রেসপন্স",
                        sector = "WASH",
                        donorName = "UNHCR",
                        status = "Active",
                        responsiblePerson = "Project Director (WASH)",
                        projectManagerName = "Anisur Rahman",
                        contactNumber = "+8801712345678",
                        contactEmail = "anisur.wash@mukticxb.org",
                        upazila = "Ukhiya",
                        union = "Palongkhali",
                        officeAddress = "Mukti WASH Field Office, Camp 11, Ukhiya, Cox's Bazar",
                        startDate = "2026-01-01",
                        endDate = "2026-12-31",
                        totalBudgetBdt = 24500000.0,
                        totalBeneficiaries = 45000,
                        maleStaff = 12,
                        femaleStaff = 8,
                        maleHostVolunteer = 15,
                        femaleHostVolunteer = 10,
                        maleFdmnVolunteer = 35,
                        femaleFdmnVolunteer = 25
                    ),
                    Project(
                        id = 0,
                        projectNameEn = "Emergency Food Security and Livelihoods Support (EFSL)",
                        projectNameBn = "জরুরি খাদ্য নিরাপত্তা এবং জীবিকায়ন সহায়তা (ইএফএসএল)",
                        sector = "Food Security & Livelihoods",
                        donorName = "WFP",
                        status = "Active",
                        responsiblePerson = "Program Manager (FSL)",
                        projectManagerName = "Farhana Akter",
                        contactNumber = "+8801819876543",
                        contactEmail = "farhana.livelihoods@mukticxb.org",
                        upazila = "Teknaf",
                        union = "Sabrang",
                        officeAddress = "Mukti Livelihoods Hub, Sabrang, Teknaf, Cox's Bazar",
                        startDate = "2025-06-01",
                        endDate = "2026-05-31",
                        totalBudgetBdt = 18200000.0,
                        totalBeneficiaries = 12500,
                        maleStaff = 10,
                        femaleStaff = 14,
                        maleHostVolunteer = 22,
                        femaleHostVolunteer = 18,
                        maleFdmnVolunteer = 0,
                        femaleFdmnVolunteer = 0
                    ),
                    Project(
                        id = 0,
                        projectNameEn = "Sustained Protection and GBV Prevention Program in Balukhali Camp 9",
                        projectNameBn = "বালুখালী ক্যাম্প ৯-এ টেকসই সুরক্ষা এবং লিঙ্গভিত্তিক সহিংসতা প্রতিরোধ কার্যক্রম",
                        sector = "Protection",
                        donorName = "UNICEF",
                        status = "Active",
                        responsiblePerson = "Protection Coordinator",
                        projectManagerName = "Sonjoy Paul",
                        contactNumber = "+8801912443322",
                        contactEmail = "Sonjoypaul.info@gmail.com",
                        upazila = "Ukhiya",
                        union = "Rajapalong",
                        officeAddress = "Mukti Safe Space for Women, Camp 9, Balukhali, Ukhiya, Cox's Bazar",
                        startDate = "2026-03-01",
                        endDate = "2027-02-28",
                        totalBudgetBdt = 11500000.0,
                        totalBeneficiaries = 8000,
                        maleStaff = 4,
                        femaleStaff = 16,
                        maleHostVolunteer = 5,
                        femaleHostVolunteer = 15,
                        maleFdmnVolunteer = 10,
                        femaleFdmnVolunteer = 30
                    ),
                    Project(
                        id = 0,
                        projectNameEn = "Primary Health and Nutrition Support for Vulnerable Population",
                        projectNameBn = "ঝুঁকিপূর্ণ জনগোষ্ঠীর জন্য প্রাথমিক স্বাস্থ্য ও পুষ্টি সহায়তা প্রকল্প",
                        sector = "Health & Nutrition",
                        donorName = "USAID",
                        status = "Pipeline",
                        responsiblePerson = "Medical Director",
                        projectManagerName = "Dr. Rakib Hasan",
                        contactNumber = "+8801511223344",
                        contactEmail = "rakib.health@mukticxb.org",
                        upazila = "Kutubdia",
                        union = "Ali Akbar Deil",
                        officeAddress = "Mukti Health Clinic, Kutubdia, Cox's Bazar",
                        startDate = "2026-07-01",
                        endDate = "2027-06-30",
                        totalBudgetBdt = 32000000.0,
                        totalBeneficiaries = 22000,
                        maleStaff = 15,
                        femaleStaff = 18,
                        maleHostVolunteer = 25,
                        femaleHostVolunteer = 25,
                        maleFdmnVolunteer = 0,
                        femaleFdmnVolunteer = 0
                    ),
                    Project(
                        id = 0,
                        projectNameEn = "Non-Formal Primary Education and Life Skills Training for Displaced Youth",
                        projectNameBn = "অনানুষ্ঠানিক প্রাথমিক শিক্ষা এবং জীবন দক্ষতা প্রশিক্ষণ",
                        sector = "Education",
                        donorName = "Donor Trust-Fund",
                        status = "Completed",
                        responsiblePerson = "Education Officer",
                        projectManagerName = "Sujit Kumar Das",
                        contactNumber = "+8801614567890",
                        contactEmail = "sujit.edu@mukticxb.org",
                        upazila = "Ramu",
                        union = "Fatekharkul",
                        officeAddress = "Mukti Learning Center, Ramu HQ, Cox's Bazar",
                        startDate = "2025-01-01",
                        endDate = "2025-12-31",
                        totalBudgetBdt = 6800000.0,
                        totalBeneficiaries = 45000,
                        maleStaff = 5,
                        femaleStaff = 7,
                        maleHostVolunteer = 8,
                        femaleHostVolunteer = 12,
                        maleFdmnVolunteer = 0,
                        femaleFdmnVolunteer = 0
                    )
                )
                repository.seedProjects(seedData)
            }
            isSeeding.value = false
        }
    }

    // Helper grouped intermediate flows
    private val filterState = combine(
        selectedSectors,
        selectedUpazilas,
        selectedDonors,
        selectedStatuses
    ) { sectors, upazilas, donors, statuses ->
        QuadFilter(sectors, upazilas, donors, statuses)
    }

    private val searchAndSortState = combine(
        searchQuery,
        sortOrder
    ) { query, sort ->
        Pair(query, sort)
    }

    // Reactive Combined Streams
    val filteredProjects: StateFlow<List<Project>> = combine(
        repository.allProjects,
        filterState,
        searchAndSortState
    ) { projects, filters, textSort ->
        val query = textSort.first
        val sort = textSort.second
        val sectors = filters.sectors
        val upazilas = filters.upazilas
        val donors = filters.donors
        val statuses = filters.statuses

        projects.filter { project ->
            // Fuzzy search match
            val matchesSearch = query.isBlank() ||
                    project.projectNameEn.contains(query, ignoreCase = true) ||
                    project.projectNameBn.contains(query, ignoreCase = true) ||
                    project.projectManagerName.contains(query, ignoreCase = true) ||
                    project.donorName.contains(query, ignoreCase = true) ||
                    project.sector.contains(query, ignoreCase = true) ||
                    project.systemTrackingId.contains(query, ignoreCase = true)

            // Multi-select filters
            val matchesSector = sectors.isEmpty() || sectors.contains(project.sector)
            val matchesUpazila = upazilas.isEmpty() || upazilas.contains(project.upazila)
            val matchesDonor = donors.isEmpty() || donors.contains(project.donorName)
            val matchesStatus = statuses.isEmpty() || statuses.contains(project.status)

            matchesSearch && matchesSector && matchesUpazila && matchesDonor && matchesStatus
        }.sortedWith { a, b ->
            when (sort) {
                "Budget Desc" -> b.totalBudgetBdt.compareTo(a.totalBudgetBdt)
                "Budget Asc" -> a.totalBudgetBdt.compareTo(b.totalBudgetBdt)
                "Beneficiaries Desc" -> b.totalBeneficiaries.compareTo(a.totalBeneficiaries)
                else -> b.id.compareTo(a.id) // Default "Latest"
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI helper options derived from global list of projects
    val availableSectors: StateFlow<Set<String>> = repository.allProjects.combine(MutableStateFlow(Unit)) { projects, _ ->
        projects.map { it.sector }.filter { it.isNotBlank() }.toSet()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val availableUpazilas: StateFlow<Set<String>> = repository.allProjects.combine(MutableStateFlow(Unit)) { projects, _ ->
        projects.map { it.upazila }.filter { it.isNotBlank() }.toSet()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val availableDonors: StateFlow<Set<String>> = repository.allProjects.combine(MutableStateFlow(Unit)) { projects, _ ->
        projects.map { it.donorName }.filter { it.isNotBlank() }.toSet()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val availableStatuses: StateFlow<Set<String>> = repository.allProjects.combine(MutableStateFlow(Unit)) { projects, _ ->
        projects.map { it.status }.filter { it.isNotBlank() }.toSet()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // Executive Analytical Dashboard Derived Stats
    val dashboardStats: StateFlow<DashboardStats> = filteredProjects.combine(MutableStateFlow(Unit)) { projects, _ ->
        val activeCount = projects.count { it.status.equals("Active", ignoreCase = true) }
        val accumBudget = projects.sumOf { it.totalBudgetBdt }
        val totalBenef = projects.sumOf { it.totalBeneficiaries }

        // HR categories splits for Chart
        var mStaff = 0; var fStaff = 0
        var mHost = 0; var fHost = 0
        var mFdmn = 0; var fFdmn = 0

        projects.forEach {
            mStaff += it.maleStaff; fStaff += it.femaleStaff
            mHost += it.maleHostVolunteer; fHost += it.femaleHostVolunteer
            mFdmn += it.maleFdmnVolunteer; fFdmn += it.femaleFdmnVolunteer
        }

        // Sector Distribution
        val sectorCountMap = mutableMapOf<String, Int>()
        val sectorBudgetMap = mutableMapOf<String, Double>()
        projects.forEach {
            sectorCountMap[it.sector] = (sectorCountMap[it.sector] ?: 0) + 1
            sectorBudgetMap[it.sector] = (sectorBudgetMap[it.sector] ?: 0.0) + it.totalBudgetBdt
        }

        val avgProgress = if (projects.isEmpty()) 0.0 else projects.map { it.completionPercentage }.average()
        val progressList = projects.map { Pair(it.projectNameEn, it.completionPercentage) }

        DashboardStats(
            totalActiveProjects = activeCount,
            totalAccumulatedBudget = accumBudget,
            totalTargetBeneficiaries = totalBenef,
            maleStaffTotal = mStaff,
            femaleStaffTotal = fStaff,
            maleHostTotal = mHost,
            femaleHostTotal = fHost,
            maleFdmnTotal = mFdmn,
            femaleFdmnTotal = fFdmn,
            sectorCounts = sectorCountMap,
            sectorBudgets = sectorBudgetMap,
            averageCompletion = avgProgress,
            projectProgressList = progressList
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    // Operations
    fun addProject(project: Project) {
        viewModelScope.launch {
            val insertedId = repository.insert(project)
            repository.insertNotification(
                Notification(
                    title = "New Project Registered",
                    message = "Project '${project.projectNameEn}' successfully registered with initial budget BDT ${project.formattedBudgetShort}.",
                    date = "2026-05-29",
                    projectId = insertedId.toInt(),
                    type = "General"
                )
            )
        }
    }

    fun updateProject(project: Project) {
        viewModelScope.launch {
            val old = repository.getProjectById(project.id).first()
            repository.update(project)
            if (old != null) {
                if (old.totalBudgetBdt != project.totalBudgetBdt) {
                    repository.insertNotification(
                        Notification(
                            title = "Significant Budget Change",
                            message = "Budget for project '${project.projectNameEn}' changed from ${old.formattedBudgetShort} to ${project.formattedBudgetShort}.",
                            date = "2026-05-29",
                            projectId = project.id,
                            type = "BudgetChange"
                        )
                    )
                }
                if (old.status != project.status) {
                    repository.insertNotification(
                        Notification(
                            title = "Status Update Alert",
                            message = "Status of project '${project.projectNameEn}' updated to '${project.status}'.",
                            date = "2026-05-29",
                            projectId = project.id,
                            type = "StatusUpdate"
                        )
                    )
                }
                if (old.completionPercentage != project.completionPercentage) {
                    repository.insertNotification(
                        Notification(
                            title = "Progress Tracking Update",
                            message = "Completion progress for '${project.projectNameEn}' updated to ${project.completionPercentage}%.",
                            date = "2026-05-29",
                            projectId = project.id,
                            type = "StatusUpdate"
                        )
                    )
                }
            } else {
                repository.insertNotification(
                    Notification(
                        title = "Project Updated",
                        message = "Project '${project.projectNameEn}' updated successfully.",
                        date = "2026-05-29",
                        projectId = project.id,
                        type = "General"
                    )
                )
            }
        }
    }

    fun deleteProject(project: Project) {
        viewModelScope.launch {
            repository.delete(project)
            repository.insertNotification(
                Notification(
                    title = "Project Registry Removed",
                    message = "Project '${project.projectNameEn}' was completely removed from system catalogs.",
                    date = "2026-05-29",
                    projectId = null,
                    type = "General"
                )
            )
        }
    }

    fun deleteProjectById(id: Int) {
        viewModelScope.launch {
            val old = repository.getProjectById(id).first()
            repository.deleteById(id)
            if (old != null) {
                repository.insertNotification(
                    Notification(
                        title = "Project Registry Removed",
                        message = "Project '${old.projectNameEn}' (Tracking ID: ${old.systemTrackingId}) was completely removed from system catalogs.",
                        date = "2026-05-29",
                        projectId = null,
                        type = "General"
                    )
                )
            }
        }
    }

    // User Session Operations
    fun login(email: String, passwordRaw: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(email.trim())
            if (user == null) {
                onResult(false, "Unknown email address domain or account.")
            } else if (user.passwordHash != passwordRaw) {
                onResult(false, "Incorrect credentials or password.")
            } else {
                currentUser.value = user
                onResult(true, "Successfully authenticated as ${user.name}.")
            }
        }
    }

    fun register(name: String, email: String, passwordRaw: String, role: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val existing = repository.getUserByEmail(email.trim())
            if (existing != null) {
                onResult(false, "Email is already registered.")
            } else {
                val newUser = User(email = email.trim(), name = name.trim(), passwordHash = passwordRaw, role = role)
                repository.insertUser(newUser)
                currentUser.value = newUser
                onResult(true, "Account registered successfully!")
            }
        }
    }

    fun logout() {
        currentUser.value = null
    }

    // Notification Operations
    fun markNotificationAsRead(id: Int) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }

    fun deleteNotification(id: Int) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }

    // Export Feature: Create standard CSV string of current filtered projects
    fun generateCsvString(projects: List<Project>): String {
        val s = StringBuilder()
        s.append("Tracking ID,Project Name (EN),Project Name (BN),Sector,Donor,Status,Upazila,Union,Manager,Contact,Email,Budget (BDT),Beneficiaries,Male Staff,Female Staff,Male Host Vol,Female Host Vol,Male FDMN Vol,Female FDMN Vol\n")
        projects.forEach { p ->
            val cleanNameEn = p.projectNameEn.replace(",", " ")
            val cleanNameBn = p.projectNameBn.replace(",", " ")
            val cleanSector = p.sector.replace(",", " ")
            val cleanDonor = p.donorName.replace(",", " ")
            val cleanManager = p.projectManagerName.replace(",", " ")
            val cleanAddress = p.officeAddress.replace(",", " ")
            
            s.append("${p.systemTrackingId},")
            s.append("\"$cleanNameEn\",")
            s.append("\"$cleanNameBn\",")
            s.append("$cleanSector,")
            s.append("$cleanDonor,")
            s.append("${p.status},")
            s.append("${p.upazila},")
            s.append("${p.union},")
            s.append("\"$cleanManager\",")
            s.append("${p.contactNumber},")
            s.append("${p.contactEmail},")
            s.append("${p.totalBudgetBdt},")
            s.append("${p.totalBeneficiaries},")
            s.append("${p.maleStaff},")
            s.append("${p.femaleStaff},")
            s.append("${p.maleHostVolunteer},")
            s.append("${p.femaleHostVolunteer},")
            s.append("${p.maleFdmnVolunteer},")
            s.append("${p.femaleFdmnVolunteer}\n")
        }
        return s.toString()
    }

    // Reset filters
    fun resetFilters() {
        searchQuery.value = ""
        selectedSectors.value = emptySet()
        selectedUpazilas.value = emptySet()
        selectedDonors.value = emptySet()
        selectedStatuses.value = emptySet()
        sortOrder.value = "Latest"
    }
}

// Data class to represent computing stats for dashboard
data class DashboardStats(
    val totalActiveProjects: Int = 0,
    val totalAccumulatedBudget: Double = 0.0,
    val totalTargetBeneficiaries: Int = 0,
    val maleStaffTotal: Int = 0,
    val femaleStaffTotal: Int = 0,
    val maleHostTotal: Int = 0,
    val femaleHostTotal: Int = 0,
    val maleFdmnTotal: Int = 0,
    val femaleFdmnTotal: Int = 0,
    val sectorCounts: Map<String, Int> = emptyMap(),
    val sectorBudgets: Map<String, Double> = emptyMap(),
    val averageCompletion: Double = 0.0,
    val projectProgressList: List<Pair<String, Int>> = emptyList()
) {
    val totalMaleHR: Int get() = maleStaffTotal + maleHostTotal + maleFdmnTotal
    val totalFemaleHR: Int get() = femaleStaffTotal + femaleHostTotal + femaleFdmnTotal
    val totalHR: Int get() = totalMaleHR + totalFemaleHR
}

// Simple Factory for ProjectViewModel
@Suppress("UNCHECKED_CAST")
class ProjectViewModelFactory(private val repository: ProjectRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProjectViewModel::class.java)) {
            return ProjectViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
