package com.im.app.coinwatcher

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
                add(R.id.historyTabContent, MyAssetsFragment.newInstance())
                    .commit()
            }
        }

        tabLayout.addOnTabSelectedListener(object: OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab) {
                val instance = when(tab.position){
                    0 -> MyAssetsFragment.newInstance()
                    1 -> TradeHistoryFragment.newInstance()
                    else -> throw IllegalStateException("Unexpected value: ${tab.position}")
                }

                val ft = activity.supportFragmentManager.beginTransaction()
                ft.replace(R.id.historyTabContent, instance)
                    .commit()

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        return binding.root
    }

    companion object{
        fun newInstance() = FundingHistoryFragment()
    }
}