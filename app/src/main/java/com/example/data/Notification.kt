package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val date: String,             // YYYY-MM-DD
    val isRead: Boolean = false,
    val projectId: Int? = null,
    val type: String              // "EndDate" or "BudgetChange" or "StatusUpdate" or "General"
)
