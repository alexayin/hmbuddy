package com.example.hmbuddy.util

object FormatUtils {
    fun formatPace(secondsPerKm: Int): String {
        val minutes = secondsPerKm / 60
        val seconds = secondsPerKm % 60
        return "$minutes:${seconds.toString().padStart(2, '0')} /km"
    }

    fun formatRaceTime(totalSeconds: Int): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            "$hours:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
        } else {
            "$minutes:${seconds.toString().padStart(2, '0')}"
        }
    }
}
