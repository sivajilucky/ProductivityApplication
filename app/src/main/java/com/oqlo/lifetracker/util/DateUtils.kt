package com.oqlo.lifetracker.util

import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

object DateUtils {

    fun todayEpochDay(): Long = LocalDate.now().toEpochDay()

    fun epochDayToLocalDate(epochDay: Long): LocalDate = LocalDate.ofEpochDay(epochDay)

    fun currentYearMonth(): String = YearMonth.now().toString()

    fun startOfDayMillis(epochDay: Long): Long =
        LocalDate.ofEpochDay(epochDay).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    fun endOfDayMillis(epochDay: Long): Long = startOfDayMillis(epochDay + 1) - 1
}
