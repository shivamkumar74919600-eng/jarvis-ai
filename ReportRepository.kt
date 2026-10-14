package com.jarvis.assistant.reports

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ReportItem(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val category: String,
    val details: String,
    val timestamp: String = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
)

class ReportRepository {
    private val reportsList = mutableListOf<ReportItem>()

    init {
        reportsList.add(
            ReportItem(
                title = "Daily Summary",
                category = "Work Project",
                details = "Sample initialized report. All systems ready."
            )
        )
    }

    fun addReport(title: String, category: String, details: String): ReportItem {
        val report = ReportItem(title = title, category = category, details = details)
        reportsList.add(0, report)
        return report
    }

    fun getAllReports(): List<ReportItem> = reportsList.toList()

    fun deleteReport(id: Long) {
        reportsList.removeAll { it.id == id }
    }
}
