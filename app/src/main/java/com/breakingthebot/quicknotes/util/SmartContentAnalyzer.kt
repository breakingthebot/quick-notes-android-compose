/*
 * Analyzes transcribed text using heuristics to extract tags, folders, and dates.
 * Connects to: NotesViewModel and VoiceTranscriptionService.
 * Created: 2026-07-05
 */
package com.breakingthebot.quicknotes.util

import com.breakingthebot.quicknotes.model.Notebook
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

object SmartContentAnalyzer {

    data class AnalysisResult(
        val autoTags: List<String>,
        val notebookId: Int?,
        val detectedReminderTime: Long?
    )

    /**
     * Extracts tags, matches notebooks, and detects calendar alarm times from natural text.
     */
    fun analyze(text: String, notebooks: List<Notebook>): AnalysisResult {
        val lowerText = text.lowercase(Locale.getDefault())

        // 1. Auto-tagging based on keyword mappings
        val tags = mutableSetOf<String>()
        val tagRules = mapOf(
            "shopping" to listOf("shop", "shopping", "buy", "grocery", "groceries", "store", "market", "cart"),
            "food" to listOf("cook", "cooking", "food", "dinner", "lunch", "recipe", "eat", "meal", "kitchen"),
            "work" to listOf("work", "meeting", "deadline", "project", "office", "task", "assign", "job", "client"),
            "health" to listOf("gym", "workout", "exercise", "run", "fitness", "health", "doctor", "meds", "medicine"),
            "ideas" to listOf("idea", "creative", "draft", "brainstorm", "write", "thought", "inspiration")
        )
        for ((tag, keywords) in tagRules) {
            if (keywords.any { keyword -> lowerText.contains(keyword) }) {
                tags.add(tag)
            }
        }

        // 2. Auto-notebook/folder matching
        var matchedNotebookId: Int? = null
        for (notebook in notebooks) {
            val normalizedNotebook = notebook.name.lowercase(Locale.getDefault())
            if (lowerText.contains(normalizedNotebook)) {
                matchedNotebookId = notebook.id
                break
            }
        }
        if (matchedNotebookId == null) {
            for (notebook in notebooks) {
                val normalizedNotebook = notebook.name.lowercase(Locale.getDefault())
                if (normalizedNotebook.contains("work") || normalizedNotebook.contains("office")) {
                    if (lowerText.contains("meeting") || lowerText.contains("deadline") || lowerText.contains("project")) {
                        matchedNotebookId = notebook.id
                        break
                    }
                } else if (normalizedNotebook.contains("personal") || normalizedNotebook.contains("home")) {
                    if (lowerText.contains("family") || lowerText.contains("house") || lowerText.contains("buy")) {
                        matchedNotebookId = notebook.id
                        break
                    }
                }
            }
        }

        // 3. Natural language date & time detection
        val detectedReminder = parseReminderTime(lowerText)

        return AnalysisResult(
            autoTags = tags.toList(),
            notebookId = matchedNotebookId,
            detectedReminderTime = detectedReminder
        )
    }

    /**
     * Parses helper date phrases in text and maps them to target millisecond timestamps.
     */
    private fun parseReminderTime(text: String): Long? {
        val now = Calendar.getInstance()

        // Match: "tomorrow at 5 PM", "tomorrow at 10:30 am", "tomorrow at 3"
        val tomorrowPattern = Pattern.compile("tomorrow(?:\\s+at)?\\s+(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?")
        val tomorrowMatcher = tomorrowPattern.matcher(text)
        if (tomorrowMatcher.find()) {
            val hour = tomorrowMatcher.group(1)?.toInt() ?: 9
            val minute = tomorrowMatcher.group(2)?.toInt() ?: 0
            val amPm = tomorrowMatcher.group(3)

            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, 1)
            setCalTime(cal, hour, minute, amPm)
            return cal.timeInMillis
        }

        // Match: "today at 5 PM", "today at 10:30 am", "today at 3"
        val todayPattern = Pattern.compile("today(?:\\s+at)?\\s+(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?")
        val todayMatcher = todayPattern.matcher(text)
        if (todayMatcher.find()) {
            val hour = todayMatcher.group(1)?.toInt() ?: 9
            val minute = todayMatcher.group(2)?.toInt() ?: 0
            val amPm = todayMatcher.group(3)

            val cal = Calendar.getInstance()
            setCalTime(cal, hour, minute, amPm)
            if (cal.after(now)) {
                return cal.timeInMillis
            }
        }

        // Match: "in 3 hours", "in 30 minutes", "in 2 days"
        val relativePattern = Pattern.compile("in\\s+(\\d+)\\s*(hour|minute|day)s?")
        val relativeMatcher = relativePattern.matcher(text)
        if (relativeMatcher.find()) {
            val amount = relativeMatcher.group(1)?.toInt() ?: 0
            val unit = relativeMatcher.group(2)

            val cal = Calendar.getInstance()
            when (unit) {
                "minute" -> cal.add(Calendar.MINUTE, amount)
                "hour" -> cal.add(Calendar.HOUR_OF_DAY, amount)
                "day" -> cal.add(Calendar.DAY_OF_YEAR, amount)
            }
            return cal.timeInMillis
        }

        // Match monthly offsets: "july 6 at 10 am", "october 12 at 8 pm"
        val monthPattern = Pattern.compile("(january|february|march|april|may|june|july|august|september|october|november|december)\\s+(\\d{1,2})(?:\\s+at)?\\s+(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?")
        val monthMatcher = monthPattern.matcher(text)
        if (monthMatcher.find()) {
            val monthStr = monthMatcher.group(1)
            val day = monthMatcher.group(2)?.toInt() ?: 1
            val hour = monthMatcher.group(3)?.toInt() ?: 9
            val minute = monthMatcher.group(4)?.toInt() ?: 0
            val amPm = monthMatcher.group(5)

            val monthIndex = getMonthIndex(monthStr)
            if (monthIndex != -1) {
                val cal = Calendar.getInstance()
                cal.set(Calendar.MONTH, monthIndex)
                cal.set(Calendar.DAY_OF_MONTH, day)
                setCalTime(cal, hour, minute, amPm)
                if (cal.before(now)) {
                    cal.add(Calendar.YEAR, 1)
                }
                return cal.timeInMillis
            }
        }

        return null
    }

    private fun setCalTime(cal: Calendar, hour: Int, minute: Int, amPm: String?) {
        var resolvedHour = hour
        if (amPm != null) {
            if (amPm == "pm" && hour < 12) {
                resolvedHour += 12
            } else if (amPm == "am" && hour == 12) {
                resolvedHour = 0
            }
        } else {
            if (hour < 8) {
                resolvedHour += 12
            }
        }
        cal.set(Calendar.HOUR_OF_DAY, resolvedHour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
    }

    private fun getMonthIndex(month: String): Int {
        val months = listOf(
            "january", "february", "march", "april", "may", "june",
            "july", "august", "september", "october", "november", "december"
        )
        return months.indexOf(month)
    }
}
