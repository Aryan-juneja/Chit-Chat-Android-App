package com.example.whatsappclone.Views.views

import Models.SharedViewModel
import Models.User
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.paging.LoadState
import androidx.paging.PagingConfig
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsappclone.R
import com.example.whatsappclone.databinding.FragmentPeopleBinding
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class People() : Fragment() {
    private lateinit var binding: FragmentPeopleBinding
    private lateinit var pagingAdapter: FirestorePagingAdapter<User, RecyclerView.ViewHolder>
    private val normalViewHolder = 1
    private val deletedViewHolder = 2
    private val viewModel: SharedViewModel by activityViewModels()
    val Auth by lazy {
        FirebaseAuth.getInstance()
    }

    val Database by lazy {
        FirebaseFirestore.getInstance().collection("Users").orderBy("name", Query.Direction.ASCENDING)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPeopleBinding.inflate(inflater, container, false)
        setupAdapter()
        binding.rv.layoutManager = LinearLayoutManager(requireContext())
        binding.rv.adapter = pagingAdapter
        return binding.root
    }

    private fun setupAdapter() {
        // Create PagingConfig
        val pagingConfig = PagingConfig(
            pageSize = 10,
            prefetchDistance = 2,
            enablePlaceholders = false
        )

        // Firestore query
        val query = Database

        // Build FirestorePagingOptions using PagingConfig
        val options = FirestorePagingOptions.Builder<User>()
            .setLifecycleOwner(viewLifecycleOwner)
            .setQuery(query, pagingConfig, User::class.java)
            .build()

        // Initialize FirestorePagingAdapter
        pagingAdapter = object : FirestorePagingAdapter<User, RecyclerView.ViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                return when (viewType) {
                    normalViewHolder -> userViewHolder(
                        layoutInflater.inflate(R.layout.usermodal, parent, false),
                        viewModel
                    )
                    else -> hackedViewHolder(
                        layoutInflater.inflate(R.layout.hackedlayout, parent, false)
                    )
                }
            }

            override fun getItemViewType(position: Int): Int {
                val item = getItem(position)?.toObject(User::class.java)
                return if (Auth.currentUser?.uid == item?.uid) {
                    deletedViewHolder
                } else {
                    normalViewHolder
                }
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, user: User) {
                when (holder) {
                    is userViewHolder -> holder.bind(user){name, photo, UID ->
                        startActivity(Intent(requireContext(),PersonalChats::class.java).putExtra("name",name).putExtra("image",photo).putExtra("uid",UID))
                    }
                    is hackedViewHolder -> {
                        // Handle special case if necessary
                    }
                }
            }
        }
        pagingAdapter.addLoadStateListener { loadState ->
            when {
                // Show loading spinner when loading initial data
                loadState.refresh is LoadState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rv.visibility = View.GONE
                }
                // Show the RecyclerView once data is loaded
                loadState.refresh is LoadState.NotLoading -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rv.visibility = View.VISIBLE
                }
                // Handle errors if any
                loadState.refresh is LoadState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rv.visibility = View.VISIBLE
                    // Handle the error state (show error message, etc.)
                }
            }
        }
    }
}
