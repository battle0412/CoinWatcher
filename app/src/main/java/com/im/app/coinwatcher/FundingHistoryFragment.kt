package com.im.app.coinwatcher

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.im.app.coinwatcher.databinding.FragmentFundingHistoryBinding

class FundingHistoryFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val activity = activity as MainActivity
        val binding = FragmentFundingHistoryBinding.inflate(inflater, container, false)
        val tabLayout = binding.tabLayout

        if(savedInstanceState == null){
            with(activity.supportFragmentManager.beginTransaction()){
                add(R.id.tabContent, PortfolioFragment.getInstance())
                    .commit()
            }
        }

        tabLayout.addOnTabSelectedListener(object: OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab) {
                val param = when(tab.position){
                    0 -> ""
                    1 -> ""
                    else -> throw IllegalStateException("Unexpected value: ${tab.position}")
                }

                val ft = activity.supportFragmentManager.beginTransaction()
                ft.replace(R.id.tabContent, PortfolioFragment.getInstance())
                    .commit()

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        return binding.root
    }

    companion object{
        fun getInstance() = FundingHistoryFragment()
    }
}