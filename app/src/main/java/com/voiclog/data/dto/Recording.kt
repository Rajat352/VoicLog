package com.voiclog.data.dto

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "recording"
)
data class Recording(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate,
    val transcript: String = "",
    val summary: List<String> = emptyList()
)
