package Adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.whatsappclone.Views.views.Chats
import com.example.whatsappclone.Views.views.People

class ScreenSliderAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> Chats() // Return the ChatsFragment for position 0
            else -> People() // Return the PeopleFragment for position
        }
    }
}
