package com.vitesse.hr.data.local

import androidx.room.TypeConverter
import java.time.LocalDate

// dit à Room comment stocker LocalDate (qu'il ne connaît pas nativement)
class Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)
}
