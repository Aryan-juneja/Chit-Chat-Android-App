package Models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _darkMode = MutableLiveData<Boolean>().apply { value = false } // Initialize to false
    val darkMode: LiveData<Boolean> = _darkMode
    // LiveData to hold the search query
    private val _searchQuery = MutableLiveData<String>()

    val searchQuery: LiveData<String> = _searchQuery
    private val _refetch =MutableLiveData<Boolean>()
     val refetch: LiveData<Boolean> = _refetch
    // Function to update search query
    fun setSearchQuery(query: String) {
        Log.d("TAG", "setSearchQuery: $query")
        _searchQuery.value = query
    }
    fun setRefetch(value:Boolean){
        Log.d("TAG", "setRefetch: $value")  // for debugging purposes only, remove before release
        _refetch.value = value
    }

    fun toggleDarkMode() {
        _darkMode.value = _darkMode.value?.not() // Toggle the current value
    }

}
