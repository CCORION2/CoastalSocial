package com.coastalsocial.app.ui.components

import java.text.SimpleDateFormat
import java.util.*

fun formatTimeAgo(dateString: String): String {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("UTC")
        val date = format.parse(dateString) ?: return dateString
        
        val now = Date()
        val diffInMillis = now.time - date.time
        val diffInSeconds = diffInMillis / 1000
        val diffInMinutes = diffInSeconds / 60
        val diffInHours = diffInMinutes / 60
        val diffInDays = diffInHours / 24
        val diffInWeeks = diffInDays / 7
        val diffInMonths = diffInDays / 30
        val diffInYears = diffInDays / 365
        
        when {
            diffInSeconds < 60 -> "Gerade eben"
            diffInMinutes < 60 -> "vor ${diffInMinutes} Min."
            diffInHours < 24 -> "vor ${diffInHours} Std."
            diffInDays < 7 -> "vor ${diffInDays} Tagen"
            diffInWeeks < 4 -> "vor ${diffInWeeks} Wochen"
            diffInMonths < 12 -> "vor ${diffInMonths} Monaten"
            else -> "vor ${diffInYears} Jahren"
        }
    } catch (e: Exception) {
        dateString
    }
}

fun formatMessageTime(dateString: String): String {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("UTC")
        val date = format.parse(dateString) ?: return dateString
        
        val now = Calendar.getInstance()
        val messageDate = Calendar.getInstance().apply { time = date }
        
        val isToday = now.get(Calendar.DAY_OF_YEAR) == messageDate.get(Calendar.DAY_OF_YEAR) &&
                      now.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR)
        
        val isYesterday = now.get(Calendar.DAY_OF_YEAR) - messageDate.get(Calendar.DAY_OF_YEAR) == 1 &&
                          now.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR)
        
        when {
            isToday -> {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
            }
            isYesterday -> {
                "Gestern"
            }
            else -> {
                SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date)
            }
        }
    } catch (e: Exception) {
        dateString
    }
}
