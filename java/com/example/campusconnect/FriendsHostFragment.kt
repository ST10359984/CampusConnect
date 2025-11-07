package com.example.campusconnect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class FriendsHostFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_friends_host, container, false)

        val tabLayout: TabLayout = view.findViewById(R.id.friendsTabLayout)
        val viewPager: ViewPager2 = view.findViewById(R.id.friendsViewPager)

        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 3

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> FriendsListFragment()
                    1 -> FriendsSearchFragment()
                    2 -> AcceptFriendRequestsFragment()
                    else -> FriendsListFragment()
                }
            }
        }

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "My Friends"
                1 -> "Add Friends"
                2 -> "Requests"
                else -> null
            }
        }.attach()

        return view
    }
}