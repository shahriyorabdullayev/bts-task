package com.example.bts_task.domain.util

object AddressFormat {
    fun split(full: String): Pair<String, String> {
        val parts = full.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        return when {
            parts.isEmpty() -> full to ""
            parts.size == 1 -> parts[0] to ""
            else -> parts[0] to parts.drop(1).joinToString(", ")
        }
    }
}
