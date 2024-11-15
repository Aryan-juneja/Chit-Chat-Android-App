package com.example.whatsappclone.Views.views

import Models.SharedViewModel
import Models.chats
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsappclone.R
import com.example.whatsappclone.databinding.FragmentChatsBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import localService.chatsEntityClass
import localService.dbClass

class Chats : Fragment() {

    private lateinit var binding: FragmentChatsBinding
    private lateinit var adapter: FirebaseRecyclerAdapter<chats, ChatsViewHolder>
    private val viewModel: SharedViewModel by activityViewModels()
    private lateinit var roomDb: dbClass
    private val auth by lazy { FirebaseAuth.getInstance().currentUser }
    private val database by lazy { FirebaseDatabase.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatsBinding.inflate(inflater, container, false)

        // Set layout manager
        binding.rv.layoutManager = LinearLayoutManager(requireContext())
        roomDb = dbClass.getDatabase(requireContext())

        // Initialize the adapter here to ensure it's always done
        setupAdapter()
        viewModel.refetch.observe(viewLifecycleOwner) { query ->
            if (query == true) {
                Log.d("TAG", "onCreateView: query: $query")
                setupAdapter()
            }
        }

        // Observe the search query from ViewModel
        viewModel.searchQuery.observe(viewLifecycleOwner) { query ->
            if (query.isNullOrEmpty()) {
                // Reset to the original adapter

                setupAdapter() // Load all items
            } else {
                filterChats(query)  // Filter based on query
            }
        }

        return binding.root
    }

    private fun setupAdapter() {

        val uid = auth?.uid ?: return
        // Check network availability
        if (!isNetworkAvailable(requireContext())) {
            Log.d("TAG", "No network available, fetching from Room database")
            lifecycleScope.launch {
                roomDb.chatsDao().getAllChats().collect { chatList ->
                    if (chatList.isNotEmpty()) {
                        Log.d("TAG", "Loaded ${chatList.size} chats from Room DB")
                        val roomAdapter = RoomChatsAdapter(chatList) { name, photo, id ->
                            val intent = Intent(requireContext(), PersonalChats::class.java).apply {
                                putExtra("name", name)
                                putExtra("image", photo)
                                putExtra("uid", id)
                            }
                            startActivity(intent)
                        }
                        binding.rv.adapter = roomAdapter
                        binding.rv.visibility = View.VISIBLE // Show RecyclerView
                        binding.textViewwwww.visibility = View.GONE // Hide no chats message
                    } else {
                        Log.d("TAG", "No chats available in Room DB")
                        binding.textViewwwww.text = "No chats available. Start chatting with someone!"
                        binding.textViewwwww.visibility = View.VISIBLE // Show no chats message
                        binding.rv.visibility = View.GONE // Hide RecyclerView
                    }
                    binding.progressBar.visibility = View.GONE // Hide progress bar after loading
                }
            }
        } else {
            // Network available, fetch from Firebase
            val query = database.getReference("chats").child(uid)
            query.get().addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    val data = result.result
                    // Check if the data exists
                    if (data != null && data.hasChildren()) {
                    } else {
                        binding.rv.visibility = View.GONE // Hide RecyclerView
                        binding.textViewwwww.visibility = View.VISIBLE // Show no chats TextView
                        binding.textViewwwww.text = "No chats available. Start chatting with someone!"
                    }
                } else {
                    Log.d("TAG", "Failed to fetch chats: ${result.exception?.message}")
                    // Optionally handle the error case
                    binding.rv.visibility = View.GONE
                    binding.textViewwwww.visibility = View.VISIBLE
                    binding.textViewwwww.text = "Error fetching chats."
                }
            }
            val options = FirebaseRecyclerOptions.Builder<chats>()
                .setQuery(query, chats::class.java)
                .build()

            adapter = object : FirebaseRecyclerAdapter<chats, ChatsViewHolder>(options) {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsViewHolder {
                    val view = LayoutInflater.from(parent.context).inflate(R.layout.usermodal, parent, false)
                    return ChatsViewHolder(view,viewModel)
                }

                override fun onBindViewHolder(holder: ChatsViewHolder, position: Int, model: chats) {
                    saveToDb(model) // Save chat to Room DB

                    holder.bind(model) { name, photo, id ->
                        val intent = Intent(requireContext(), PersonalChats::class.java).apply {
                            putExtra("name", name)
                            putExtra("image", photo)
                            putExtra("uid", id)
                        }
                        startActivity(intent)
                    }
                }
            }
            binding.rv.adapter = adapter
            adapter.startListening() // Start listening for Firebase data
            binding.textViewwwww.visibility = View.GONE // Hide no chats message
            binding.progressBar.visibility = View.GONE
            binding.rv.visibility = View.VISIBLE

            // Observe data changes and update visibility based on item count
            adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                    Log.d("TAG", "onItemRangeChanged: ")
                    if (adapter.itemCount == 0) {
                        binding.rv.visibility = View.GONE // Hide RecyclerView
                        binding.progressBar.visibility = View.GONE
                        binding.textViewwwww.visibility = View.VISIBLE
                        binding.textViewwwww.text = "No chats available. Start chatting with someone!" // Update the message for new users
                    } else {
                        binding.textViewwwww.visibility = View.GONE
                        binding.progressBar.visibility = View.GONE
                        binding.rv.visibility = View.VISIBLE
                    }
                }
            })
        }
    }




    private fun  clearDb(){
        lifecycleScope.launch {
            roomDb.chatsDao().deleteAllChats()
        }
    }
    private fun saveToDb(chat: chats) {
        Log.d("TEST", "saveToDb: Cammmmmmmmmmme")
        val roomChat = chatsEntityClass(
            id = chat.from, // Unique ID based on from and time
            name = chat.name,
            image = chat.image,
            message = chat.mssg,
            from = chat.from,
            time = chat.time,
            count = chat.count
        )
        lifecycleScope.launch {
            // Fetch chats by the unique identifier (can be from and time or a combination)
            val existingChat = roomDb.chatsDao().getChatsByFrom(chat.from)
            // Check if any existing chat matches the current message and time
            val existingEntry = existingChat.firstOrNull {
                it.from == chat.from && (it.message == chat.mssg && it.count == chat.count)
            }
            if (existingEntry == null) {
                // If no matching chat exists, insert the new chat
                roomDb.chatsDao().insertChat(roomChat)
                Log.d("ChatInsert", "Chat inserted for ${chat.from}")
            } else {
                // If a matching chat exists with the same message and count, log it
                Log.d("ChatInsert", "Chat already exists for ${chat.from}, not inserting.")
                // Optionally update the chat if the message or count has changed
                if (existingEntry.message != chat.mssg || existingEntry.count != chat.count) {
                    roomDb.chatsDao().updateChat(roomChat) // Assuming updateChat method exists
                    Log.d("ChatUpdate", "Chat updated for ${chat.from} with new message/count.")
                }
            }
        }

        Log.d("TEST", "saveToDb:")
    }


    private fun filterChats(query: String) {
        val uid = auth?.uid ?: return
        val filteredQuery = database.getReference("chats").child(uid)
            .orderByChild("name").startAt(query).endAt(query + "\uf8ff")

        // Create new FirebaseRecyclerOptions with the filtered query
        val options = FirebaseRecyclerOptions.Builder<chats>()
            .setQuery(filteredQuery, chats::class.java)
            .build()

        // Create a new adapter instance for filtered results
        val filteredAdapter = object : FirebaseRecyclerAdapter<chats, ChatsViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.usermodal, parent, false)
                return ChatsViewHolder(view, viewModel)
            }

            override fun onBindViewHolder(
                holder: ChatsViewHolder, position: Int, model: chats
            ) {
                holder.bind(model) { name, photo, id ->
                    Log.d("TAG", "onBindViewHolder: $name $position $photo $id")
                    val intent = Intent(requireContext(), PersonalChats::class.java).apply {
                        putExtra("name", name)
                        putExtra("image", photo)
                        putExtra("uid", id)
                    }
                    startActivity(intent)
                }
            }
        }

        // Set the new adapter to the RecyclerView
        binding.rv.adapter = filteredAdapter
        // Start listening for changes
        filteredAdapter.startListening()
    }

    override fun onStart() {
        super.onStart()
//        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
//        adapter?.stopListening()
    }
}

fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    } else {
        // For devices below API 23 (Marshmallow)
        val networkInfo = connectivityManager.activeNetworkInfo ?: return false
        return networkInfo.isConnected
    }
}
