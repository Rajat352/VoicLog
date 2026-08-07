package com.voiclog.data

import androidx.room3.ColumnTypeConverter
import androidx.room3.ColumnTypeConverters
import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.voiclog.data.dao.RecordingDao
import com.voiclog.data.dto.Recording
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Converters {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE // Format: YYYY-MM-DD
    private val json = Json { ignoreUnknownKeys = true }

    @ColumnTypeConverter
    fun fromString(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it, formatter) }
    }

    @ColumnTypeConverter
    fun dateToString(date: LocalDate?): String? {
        return date?.format(formatter)
    }

    @ColumnTypeConverter
    fun fromListString(value: List<String>): String {
        return json.encodeToString(value)
    }

    @ColumnTypeConverter
    fun toListString(value: String): List<String> {
        return json.decodeFromString<List<String>>(value)
    }

}

@Database(
    entities = [Recording::class],
    version = 1,
    exportSchema = false
)
@ColumnTypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {

    abstract fun recordingDao(): RecordingDao

}