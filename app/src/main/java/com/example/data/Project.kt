package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.NumberFormat
import java.util.Locale

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val responsiblePerson: String,
    val projectManagerName: String,
    val contactNumber: String,
    val contactEmail: String,
    val projectNameEn: String,
    val projectNameBn: String,
    val sector: String,
    val donorName: String,
    val status: String, // Active, Completed, Pipeline
    val upazila: String,
    val union: String,
    val officeAddress: String,
    val startDate: String, // YYYY-MM-DD
    val endDate: String,   // YYYY-MM-DD
    val totalBudgetBdt: Double,
    val totalBeneficiaries: Int,
    val maleStaff: Int,
    val femaleStaff: Int,
    val maleHostVolunteer: Int,
    val femaleHostVolunteer: Int,
    val maleFdmnVolunteer: Int,
    val femaleFdmnVolunteer: Int,
    val completionPercentage: Int = 0,
    val lastUpdatedDate: String = ""
) {
    // System Tracking ID: e.g. MUKTI-2026-001
    val systemTrackingId: String
        get() = String.format(Locale.US, "MUKTI-2026-%03d", if (id == 0) 1 else id)

    // Smart Automation Engine Computed Properties
    val totalStaff: Int
        get() = maleStaff + femaleStaff

    val totalHostVolunteers: Int
        get() = maleHostVolunteer + femaleHostVolunteer

    val totalFdmnVolunteers: Int
        get() = maleFdmnVolunteer + femaleFdmnVolunteer

    val totalMaleHR: Int
        get() = maleStaff + maleHostVolunteer + maleFdmnVolunteer

    val totalFemaleHR: Int
        get() = femaleStaff + femaleHostVolunteer + femaleFdmnVolunteer

    val totalHR: Int
        get() = totalMaleHR + totalFemaleHR

    val formattedBudget: String
        get() {
            return try {
                val formatter = NumberFormat.getCurrencyInstance(Locale("en", "BD"))
                // Format in local currency
                var formatted = formatter.format(totalBudgetBdt)
                // Clean up default symbol etc. if needed
                if (formatted.startsWith("BDT") || formatted.contains("৳")) {
                     formatted
                } else {
                     "৳ " + String.format(Locale.US, "%,.2f", totalBudgetBdt)
                }
            } catch (e: Exception) {
                "৳ " + String.format(Locale.US, "%,.2f", totalBudgetBdt)
            }
        }

    val formattedBudgetShort: String
        get() {
            return if (totalBudgetBdt >= 10_000_000) {
                // In Crores (1 Crore = 10 Million)
                String.format(Locale.US, "%.2f Crore ৳", totalBudgetBdt / 10_000_000.0)
            } else if (totalBudgetBdt >= 100_000) {
                // In Lakhs (1 Lakh = 100,000)
                String.format(Locale.US, "%.2f Lakh ৳", totalBudgetBdt / 100_000.0)
            } else {
                String.format(Locale.US, "%,.0f ৳", totalBudgetBdt)
            }
        }
}
