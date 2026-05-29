package com.example.ui

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Project
import java.util.Calendar
import java.util.regex.Pattern

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectFormScreen(
    editingProject: Project?,
    isAdmin: Boolean = true,
    onSaveProject: (Project) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Steps/Tabs definition
    val steps = if (isAdmin) listOf("Identity", "Location", "Personnel", "Demographics") else listOf("Progress Updates")
    var currentStep by remember { mutableStateOf(0) }

    // Forms Form States
    var nameEn by remember { mutableStateOf(editingProject?.projectNameEn ?: "") }
    var nameBn by remember { mutableStateOf(editingProject?.projectNameBn ?: "") }
    var sector by remember { mutableStateOf(editingProject?.sector ?: "WASH") }
    var donor by remember { mutableStateOf(editingProject?.donorName ?: "UNHCR") }
    var status by remember { mutableStateOf(editingProject?.status ?: "Active") }
    var completionPercentage by remember { mutableStateOf(editingProject?.completionPercentage ?: 0) }
    var lastUpdatedDateState by remember { mutableStateOf(if (editingProject?.lastUpdatedDate.isNullOrBlank()) "2026-05-29" else editingProject!!.lastUpdatedDate) }

    var upazila by remember { mutableStateOf(editingProject?.upazila ?: "Ukhiya") }
    var union by remember { mutableStateOf(editingProject?.union ?: "") }
    var officeAddress by remember { mutableStateOf(editingProject?.officeAddress ?: "") }

    var responsiblePerson by remember { mutableStateOf(editingProject?.responsiblePerson ?: "") }
    var managerName by remember { mutableStateOf(editingProject?.projectManagerName ?: "") }
    var contactNumber by remember { mutableStateOf(editingProject?.contactNumber ?: "") }
    var contactEmail by remember { mutableStateOf(editingProject?.contactEmail ?: "") }

    var startDate by remember { mutableStateOf(editingProject?.startDate ?: "2026-01-01") }
    var endDate by remember { mutableStateOf(editingProject?.endDate ?: "2026-12-31") }
    var budgetStr by remember { mutableStateOf(editingProject?.totalBudgetBdt?.toLong()?.toString() ?: "") }
    var beneficiariesStr by remember { mutableStateOf(editingProject?.totalBeneficiaries?.toString() ?: "") }

    var mStaffStr by remember { mutableStateOf(editingProject?.maleStaff?.toString() ?: "0") }
    var fStaffStr by remember { mutableStateOf(editingProject?.femaleStaff?.toString() ?: "0") }
    var mHostStr by remember { mutableStateOf(editingProject?.maleHostVolunteer?.toString() ?: "0") }
    var fHostStr by remember { mutableStateOf(editingProject?.femaleHostVolunteer?.toString() ?: "0") }
    var mFdmnStr by remember { mutableStateOf(editingProject?.maleFdmnVolunteer?.toString() ?: "0") }
    var fFdmnStr by remember { mutableStateOf(editingProject?.femaleFdmnVolunteer?.toString() ?: "0") }

    // Field Error States
    var nameEnError by remember { mutableStateOf<String?>(null) }
    var nameBnError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var budgetError by remember { mutableStateOf<String?>(null) }
    var beneficiariesError by remember { mutableStateOf<String?>(null) }

    // Smart Selections dropdown structures
    val sectorOptions = listOf("WASH", "Food Security & Livelihoods", "Protection", "Health & Nutrition", "Education", "Shelter & DRR")
    val donorOptions = listOf("UNHCR", "UNICEF", "WFP", "IOM", "USAID", "GIZ", "Donor Trust-Fund")
    val upazilaOptions = listOf("Ukhiya", "Teknaf", "Cox's Bazar Sadar", "Ramu", "Chakaria", "Pekua", "Kutubdia", "Moheshkhali")
    val statusOptions = listOf("Active", "Completed", "Pipeline")

    // Validation patterns
    val emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)\$")
    val bdPhonePattern = Pattern.compile("^(\\+88)?01[3-9]\\d{8}\$")

    // Real-time calculation Preview estimates (Automation Engine display)
    val pMaleStaff = mStaffStr.toIntOrNull() ?: 0
    val pFemaleStaff = fStaffStr.toIntOrNull() ?: 0
    val pMaleHost = mHostStr.toIntOrNull() ?: 0
    val pFemaleHost = fHostStr.toIntOrNull() ?: 0
    val pMaleFdmn = mFdmnStr.toIntOrNull() ?: 0
    val pFemaleFdmn = fFdmnStr.toIntOrNull() ?: 0

    val totalStaff = pMaleStaff + pFemaleStaff
    val totalHost = pMaleHost + pFemaleHost
    val totalFdmn = pMaleFdmn + pFemaleFdmn
    val totalMale = pMaleStaff + pMaleHost + pMaleFdmn
    val totalFemale = pFemaleStaff + pFemaleHost + pFemaleFdmn
    val totalWorkers = totalMale + totalFemale

    // Validate a specific step before proceeding
    fun validateStep(step: Int): Boolean {
        var isValid = true
        when (step) {
            0 -> {
                if (nameEn.isBlank()) {
                    nameEnError = "Project Name (English) is required*"
                    isValid = false
                } else { nameEnError = null }

                if (nameBn.isBlank()) {
                    nameBnError = "Project Name (Bangla) is required*"
                    isValid = false
                } else { nameBnError = null }
            }
            2 -> {
                if (contactEmail.isNotBlank() && !emailPattern.matcher(contactEmail).matches()) {
                    emailError = "Invalid Bangladesh email format*"
                    isValid = false
                } else { emailError = null }

                if (contactNumber.isNotBlank() && !bdPhonePattern.matcher(contactNumber).matches()) {
                    phoneError = "BD Phone must format: 01XXXXXXXXX or +8801XXXXXXXXX*"
                    isValid = false
                } else { phoneError = null }
            }
            3 -> {
                val budgetVal = budgetStr.toDoubleOrNull()
                if (budgetStr.isBlank() || budgetVal == null || budgetVal <= 0) {
                    budgetError = "Enter a valid Total Budget (BDT) value*"
                    isValid = false
                } else { budgetError = null }

                val benefVal = beneficiariesStr.toIntOrNull()
                if (beneficiariesStr.isBlank() || benefVal == null || benefVal < 0) {
                    beneficiariesError = "Beneficiary outreach capacity required*"
                    isValid = false
                } else { beneficiariesError = null }
            }
        }
        return isValid
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("project_form_scaffold"),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isAdmin) (if (editingProject == null) "Register New Project" else "Edit Project Register") else "Update Completion Progress",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onCancel, modifier = Modifier.testTag("cancel_form")) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel edit")
                    }
                }

                // Steps Tab indicators
                TabRow(selectedTabIndex = currentStep) {
                    steps.forEachIndexed { index, title ->
                        Tab(
                            selected = currentStep == index,
                            onClick = {
                                if (index < currentStep || validateStep(currentStep)) {
                                    currentStep = index
                                }
                            },
                            text = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Navigation controls
                    if (currentStep > 0) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.width(110.dp)
                        ) {
                            Text("Back")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(110.dp))
                    }

                    if (currentStep < steps.size - 1) {
                        Button(
                            onClick = {
                                if (validateStep(currentStep)) {
                                    currentStep++
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.width(110.dp)
                        ) {
                            Text("Next")
                        }
                    } else {
                        Button(
                            onClick = {
                                if (!isAdmin) {
                                    val project = (editingProject ?: Project(
                                        id = 0,
                                        projectNameEn = "Standard Mock Title",
                                        projectNameBn = "বাংলা মক শিরোনাম",
                                        sector = "WASH",
                                        donorName = "UNHCR",
                                        status = "Active",
                                        upazila = "Ukhiya",
                                        union = "",
                                        officeAddress = "",
                                        responsiblePerson = "",
                                        projectManagerName = "",
                                        contactNumber = "",
                                        contactEmail = "",
                                        startDate = "2026-01-01",
                                        endDate = "2026-12-31",
                                        totalBudgetBdt = 0.0,
                                        totalBeneficiaries = 0,
                                        maleStaff = 0,
                                        femaleStaff = 0,
                                        maleHostVolunteer = 0,
                                        femaleHostVolunteer = 0,
                                        maleFdmnVolunteer = 0,
                                        femaleFdmnVolunteer = 0,
                                        completionPercentage = completionPercentage,
                                        lastUpdatedDate = lastUpdatedDateState
                                    )).copy(
                                        completionPercentage = completionPercentage,
                                        lastUpdatedDate = lastUpdatedDateState
                                    )
                                    onSaveProject(project)
                                } else if (validateStep(currentStep)) {
                                    // Save database
                                    val project = Project(
                                        id = editingProject?.id ?: 0,
                                        projectNameEn = nameEn.trim(),
                                        projectNameBn = nameBn.trim(),
                                        sector = sector,
                                        donorName = donor,
                                        status = status,
                                        upazila = upazila,
                                        union = union.trim(),
                                        officeAddress = officeAddress.trim(),
                                        responsiblePerson = responsiblePerson.trim(),
                                        projectManagerName = managerName.trim(),
                                        contactNumber = contactNumber.trim(),
                                        contactEmail = contactEmail.trim(),
                                        startDate = startDate,
                                        endDate = endDate,
                                        totalBudgetBdt = budgetStr.toDoubleOrNull() ?: 0.0,
                                        totalBeneficiaries = beneficiariesStr.toIntOrNull() ?: 0,
                                        maleStaff = mStaffStr.toIntOrNull() ?: 0,
                                        femaleStaff = fStaffStr.toIntOrNull() ?: 0,
                                        maleHostVolunteer = mHostStr.toIntOrNull() ?: 0,
                                        femaleHostVolunteer = fHostStr.toIntOrNull() ?: 0,
                                        maleFdmnVolunteer = mFdmnStr.toIntOrNull() ?: 0,
                                        femaleFdmnVolunteer = fFdmnStr.toIntOrNull() ?: 0,
                                        completionPercentage = completionPercentage,
                                        lastUpdatedDate = lastUpdatedDateState
                                    )
                                    onSaveProject(project)
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.width(140.dp).testTag("save_project_button")
                        ) {
                            Text("Save Registry", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (currentStep) {
                0 -> {
                    // TAB 1: IDENTITY
                    item {
                        FormSectionTitle("Project Identity Indices")
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        FormTextField(
                            value = nameEn,
                            onValueChange = { nameEn = it; nameEnError = null },
                            label = "Project Name (English)",
                            placeholder = "Enter English project identifier",
                            error = nameEnError,
                            enabled = isAdmin,
                            modifier = Modifier.testTag("form_name_en")
                        )
                    }

                    item {
                        FormTextField(
                            value = nameBn,
                            onValueChange = { nameBn = it; nameBnError = null },
                            label = "Project Name (Bangla)",
                            placeholder = "প্রকল্পের নাম (বাংলায় লিখুন)",
                            error = nameBnError,
                            enabled = isAdmin,
                            modifier = Modifier.testTag("form_name_bn")
                        )
                    }

                    item {
                        FormDropdownSelector(
                            label = "Sector/Thematic Area",
                            selected = sector,
                            options = sectorOptions,
                            onOptionSelected = { sector = it },
                            enabled = isAdmin
                        )
                    }

                    item {
                        FormDropdownSelector(
                            label = "Donor Agency / Fund Sourcing",
                            selected = donor,
                            options = donorOptions,
                            onOptionSelected = { donor = it },
                            enabled = isAdmin
                        )
                    }

                    item {
                        FormDropdownSelector(
                            label = "Current Registry Status",
                            selected = status,
                            options = statusOptions,
                            onOptionSelected = { status = it },
                            enabled = isAdmin
                        )
                    }

                    item {
                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }

                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Completion Progress",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$completionPercentage%",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Slider(
                                value = completionPercentage.toFloat(),
                                onValueChange = { completionPercentage = it.toInt() },
                                valueRange = 0f..100f,
                                steps = 100,
                                modifier = Modifier.testTag("form_completion_slider")
                            )
                        }
                    }

                    item {
                        DatePickerField(
                            label = "Last Progress Update Date",
                            value = lastUpdatedDateState,
                            onDateSelected = { lastUpdatedDateState = it }
                        )
                    }
                }

                1 -> {
                    // TAB 2: GEOGRAPHICS Mapping
                    item {
                        FormSectionTitle("Geographic Scope & Calendars")
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        FormDropdownSelector(
                            label = "Working Location (Upazila)",
                            selected = upazila,
                            options = upazilaOptions,
                            onOptionSelected = { upazila = it }
                        )
                    }

                    item {
                        FormTextField(
                            value = union,
                            onValueChange = { union = it },
                            label = "Working Location (Union / Refugee Camps)",
                            placeholder = "e.g., Palongkhali or Camp-9",
                            modifier = Modifier.testTag("form_union")
                        )
                    }

                    item {
                        FormTextField(
                            value = officeAddress,
                            onValueChange = { officeAddress = it },
                            label = "Registered Office Address",
                            placeholder = "e.g., Camp-11 Field Office, Ukhiya",
                            modifier = Modifier.testTag("form_address")
                        )
                    }

                    item {
                        Text(
                            text = "Implementation Calendars",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    item {
                        DatePickerField(
                            label = "Project Start Date",
                            value = startDate,
                            onDateSelected = { startDate = it }
                        )
                    }

                    item {
                        DatePickerField(
                            label = "Project End Date",
                            value = endDate,
                            onDateSelected = { endDate = it }
                        )
                    }
                }

                2 -> {
                    // TAB 3: PERSONNEL
                    item {
                        FormSectionTitle("Assigned Management Personnel")
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        FormTextField(
                            value = managerName,
                            onValueChange = { managerName = it },
                            label = "Name of Project Manager / Coordinator",
                            placeholder = "Enter responsible PM/PC name",
                            modifier = Modifier.testTag("form_manager")
                        )
                    }

                    item {
                        FormTextField(
                            value = responsiblePerson,
                            onValueChange = { responsiblePerson = it },
                            label = "Technical Director / Lead Role",
                            placeholder = "e.g., WASH Lead, Nutrition PD",
                            modifier = Modifier.testTag("form_director")
                        )
                    }

                    item {
                        FormTextField(
                            value = contactNumber,
                            onValueChange = { contactNumber = it; phoneError = null },
                            label = "Personnel Mobile Phone",
                            placeholder = "e.g., 017XXXXXXXX",
                            keyboardType = KeyboardType.Phone,
                            error = phoneError,
                            modifier = Modifier.testTag("form_phone")
                        )
                    }

                    item {
                        FormTextField(
                            value = contactEmail,
                            onValueChange = { contactEmail = it; emailError = null },
                            label = "Personnel Email Register",
                            placeholder = "e.g., manager@mukticxb.org",
                            keyboardType = KeyboardType.Email,
                            error = emailError,
                            modifier = Modifier.testTag("form_email")
                        )
                    }
                }

                3 -> {
                    // TAB 4: DEMOGRAPHICS, WORKFORCE & AUTOMATIONS
                    item {
                        FormSectionTitle("Outreach & Workforce Allocation")
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        FormTextField(
                            value = budgetStr,
                            onValueChange = { budgetStr = it; budgetError = null },
                            label = "Total Project Budget (BDT)",
                            placeholder = "e.g., 25000000",
                            keyboardType = KeyboardType.Number,
                            error = budgetError,
                            modifier = Modifier.testTag("form_budget")
                        )
                    }

                    item {
                        FormTextField(
                            value = beneficiariesStr,
                            onValueChange = { beneficiariesStr = it; beneficiariesError = null },
                            label = "Total Target Beneficiaries Outreach",
                            placeholder = "e.g., 45000",
                            keyboardType = KeyboardType.Number,
                            error = beneficiariesError,
                            modifier = Modifier.testTag("form_beneficiaries")
                        )
                    }

                    item {
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                        Text(
                            text = "Field Force Work Allocation Grid",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Specify Male vs Female headcount to activate automation counts.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // PAID STAFF
                    item {
                        HrRowInputs(
                            title = "Paid Field Staffing Allocation",
                            maleVal = mStaffStr,
                            onMaleChange = { mStaffStr = it },
                            femaleVal = fStaffStr,
                            onFemaleChange = { fStaffStr = it }
                        )
                    }

                    // HOST VOLUNTEERS
                    item {
                        HrRowInputs(
                            title = "Local Host Community Volunteers",
                            maleVal = mHostStr,
                            onMaleChange = { mHostStr = it },
                            femaleVal = fHostStr,
                            onFemaleChange = { fHostStr = it }
                        )
                    }

                    // Refugee (FDMN) Volunteers
                    item {
                        HrRowInputs(
                            title = "FDMN Community Camp Volunteers",
                            maleVal = mFdmnStr,
                            onMaleChange = { mFdmnStr = it },
                            femaleVal = fFdmnStr,
                            onFemaleChange = { fFdmnStr = it }
                        )
                    }

                    // Real-time Automations Review Card
                    item {
                        AutomationPreviewCard(
                            totalStaff = totalStaff,
                            totalHost = totalHost,
                            totalFdmn = totalFdmn,
                            totalMale = totalMale,
                            totalFemale = totalFemale,
                            totalWorkers = totalWorkers
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FormSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    error: String? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            isError = error != null,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(10.dp),
            modifier = modifier.fillMaxWidth(),
            singleLine = true,
            enabled = enabled
        )
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 2.dp, start = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormDropdownSelector(
    label: String,
    selected: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        ExposedDropdownMenuBox(
            expanded = expanded && enabled,
            onExpandedChange = { if (enabled) expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selected,
                onValueChange = {},
                readOnly = true,
                enabled = enabled,
                trailingIcon = { if (enabled) ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { selection ->
                    DropdownMenuItem(
                        text = { Text(selection) },
                        onClick = {
                            onOptionSelected(selection)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DatePickerField(
    label: String,
    value: String,
    onDateSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val year: Int
    val month: Int
    val day: Int

    val calendar = Calendar.getInstance()
    year = calendar.get(Calendar.YEAR)
    month = calendar.get(Calendar.MONTH)
    day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
            val cleanMonth = String.format("%02d", selectedMonth + 1)
            val cleanDay = String.format("%02d", selectedDay)
            onDateSelected("$selectedYear-$cleanMonth-$cleanDay")
        }, year, month, day
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { datePickerDialog.show() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Open date dialog", tint = MaterialTheme.colorScheme.primary)
                }
            },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() }
        )
    }
}

@Composable
fun HrRowInputs(
    title: String,
    maleVal: String,
    onMaleChange: (String) -> Unit,
    femaleVal: String,
    onFemaleChange: (String) -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = maleVal,
                onValueChange = { onMaleChange(it.filter { char -> char.isDigit() }) },
                label = { Text("Male Count") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1.5f),
                singleLine = true
            )

            OutlinedTextField(
                value = femaleVal,
                onValueChange = { onFemaleChange(it.filter { char -> char.isDigit() }) },
                label = { Text("Female Count") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1.5f),
                singleLine = true
            )
        }
    }
}

@Composable
fun AutomationPreviewCard(
    totalStaff: Int,
    totalHost: Int,
    totalFdmn: Int,
    totalMale: Int,
    totalFemale: Int,
    totalWorkers: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Smart Automation Tallies (Live Preview)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                TallyRow("Total Staff Base (M + F)", totalStaff)
                TallyRow("Total Host Community Volunteers", totalHost)
                TallyRow("Total Refugee FDMN Volunteers", totalFdmn)
                Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                TallyRow("Aggregated Male Human Resources", totalMale, textFontColor = MaterialTheme.colorScheme.primary)
                TallyRow("Aggregated Female Human Resources", totalFemale, textFontColor = MaterialTheme.colorScheme.tertiary)
                Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Calculated Force Power", fontWeight = FontWeight.Black, fontSize = 13.sp)
                    Text("$totalWorkers personnel", fontWeight = FontWeight.Black, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun TallyRow(label: String, count: Int, textFontColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = textFontColor)
        Text("$count personnel", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textFontColor)
    }
}

// Private helper to wrap target string matching checks
private fun Number?.benefStr(): String {
    return this?.toString() ?: ""
}
