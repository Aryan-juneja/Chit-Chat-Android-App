package utils
import android.content.Context
import android.os.Build
import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.time.LocalDate
import java.util.Date



fun showCurrentLocale(context: Context) {
    val currentLocale: Locale = getCurrentLocale(context)
    println("Current Locale: ${currentLocale.language}-${currentLocale.country}")
}
private fun getCurrentLocale(context: Context): Locale {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        // For Android N and above, use the first locale
        context.resources.configuration.locales[0]
    } else {
        // For Android M and below, use the older locale method
        context.resources.configuration.locale
    }
}
fun Date.isThisWeek(): Boolean {
    // Convert Date to LocalDate
    val givenDate = this.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()

    // Get the current date
    val currentDate = LocalDate.now()

    // Find the start and end of the current week (Monday to Sunday)
    val startOfWeek = currentDate.with(java.time.DayOfWeek.MONDAY)
    val endOfWeek = currentDate.with(java.time.DayOfWeek.SUNDAY)

    // Check if the given date is within the current week
    return givenDate.isEqual(startOfWeek) || givenDate.isAfter(startOfWeek) && givenDate.isBefore(endOfWeek)
}



        fun Date. isToday(): Boolean {
                return DateUtils. isToday(this. time)}
        fun Date.isYesterday(): Boolean {
    // Create a calendar instance for the current date
    val calendar = Calendar.getInstance()

    // Create another calendar instance for yesterday
    val yesterdayCalendar = Calendar.getInstance()
    yesterdayCalendar.add(Calendar.DAY_OF_YEAR, -1) // Subtract one day from the current date

    // Set the calendar time to the given date
    calendar.time = this

    // Compare the year and day of the year
    return yesterdayCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
            yesterdayCalendar.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR)
}

fun Date.isThisYear(): Boolean {
    val currentCalendar = Calendar.getInstance() // Current date
    val dateCalendar = Calendar.getInstance()
    dateCalendar.time = this // Set to the date of the calling object

    // Compare the year
    return currentCalendar.get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR)
}

fun Date.sameDayAs(date: Date): Boolean {
    val calendar1 = Calendar.getInstance()
    val calendar2 = Calendar.getInstance()

    // Set calendars to the respective dates
    calendar1.time = this
    calendar2.time = date

    // Compare both the year and day of the year
    return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
            calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
}

fun Date.toCalendar(): Calendar {
    val cal = Calendar.getInstance()
    cal.time = this // Set the calendar to the date `this` refers to
    return cal
}

fun Date.formatAsTime(): String {
    val calendar = this.toCalendar()

    // Get hours and minutes, format them
    val hour = calendar.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
    val minute = calendar.get(Calendar.MINUTE).toString().padStart(2, '0')

    return "$hour:$minute"
}

fun Date.formatAsYesterday(context: Context): String {
    val yesterdayCalendar = Calendar.getInstance()
    yesterdayCalendar.add(Calendar.DAY_OF_YEAR, -1) // Move calendar to yesterday

    val dateCalendar = this.toCalendar()

    // Check if the date is the same as yesterday
    return if (yesterdayCalendar.get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR) &&
        yesterdayCalendar.get(Calendar.DAY_OF_YEAR) == dateCalendar.get(Calendar.DAY_OF_YEAR)) {
        "Yesterday"
    } else {
        "Not Yesterday" // or any other fallback behavior you need
    }
}

// Format Date as Weekday
fun Date.formatAsWeekDay(context: Context): String {
    val calendar = Calendar.getInstance()
    calendar.time = this

    return when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> "Monday"
        Calendar.TUESDAY -> "Tuesday"
        Calendar.WEDNESDAY -> "Wednesday"
        Calendar.THURSDAY -> "Thursday"
        Calendar.FRIDAY -> "Friday"
        Calendar.SATURDAY -> "Saturday"
        Calendar.SUNDAY -> "Sunday"
        else -> SimpleDateFormat("d LLL", getCurrentLocale(context)).format(this)
    }
}

// Format Date as Full Date
fun Date.formatAsFull(context: Context, abbreviated: Boolean = false): String {
    val monthPattern = if (abbreviated) "LLL" else "LLLL" // LLL for short month, LLLL for full month name
    val dateFormat = "d $monthPattern yyyy" // Formatting pattern

    return SimpleDateFormat(dateFormat, getCurrentLocale(context)).format(this)
}


fun Date.formatAsListItem(context: Context): String {
    val currentLocale = getCurrentLocale(context)

    return when {
        isToday() -> formatAsTime() // If today, format as time
        isYesterday() -> formatAsYesterday(context) // If yesterday, use "Yesterday"
        isThisWeek() -> formatAsWeekDay(context) // If this week, format as a weekday
        isThisYear() -> SimpleDateFormat("d LLL", currentLocale).format(this) // If this year, format as day and abbreviated month
        else -> formatAsFull(context, abbreviated = true) // Otherwise, format as full date with abbreviated month
    }
}

// Format Date as Header
fun Date.formatAsHeader(context: Context): String {
    return when {
        isToday() -> "Today" // If today, return "Today"
        isYesterday() -> formatAsYesterday(context) // If yesterday, return "Yesterday"
        isThisWeek() -> formatAsWeekDay(context) // If this week, return the weekday name
        isThisYear() -> SimpleDateFormat("d LLLL", getCurrentLocale(context)).format(this) // If this year, format with full month name
        else -> formatAsFull(context, abbreviated = false) // Otherwise, return full date
    }
}