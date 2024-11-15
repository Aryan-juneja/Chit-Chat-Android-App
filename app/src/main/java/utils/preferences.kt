package utils

import android.content.Context

fun saveUserPreferences(context: Context, isDarkMode: Boolean) {
    val sharedPreferences = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putBoolean("is_dark_mode", isDarkMode)
    editor.apply() // or editor.commit()
}
fun loadUserPreferences(context: Context): Boolean {
    val sharedPreferences = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
    return sharedPreferences.getBoolean("is_dark_mode", false) // default is false
}
