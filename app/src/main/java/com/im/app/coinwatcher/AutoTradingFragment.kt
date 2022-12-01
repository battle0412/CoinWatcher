package com.im.app.coinwatcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.im.app.coinwatcher.databinding.FragmentAutoTradingBinding

class AutoTradingFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val activity = activity as MainActivity
        val binding = FragmentAutoTradingBinding.inflate(inflater, container, false)
        val tabLayout = binding.tabLayout

        if(savedInstanceState == null){
            with(activity.supportFragmentManager.beginTransaction()){
                add(R.id.tabContent, AutoBuyingFragment.getInstance())
                    .commit()
            }
        }

        tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val instance = when(tab.position){
                    0 -> AutoBuyingFragment.getInstance()
                    1 -> AutoSellingFragment.getInstance()
                    else -> throw IllegalStateException("Unexpected value: ${tab.position}")
                }

                val ft = activity.supportFragmentManager.beginTransaction()
                ft.replace(R.id.tabContent, instance)
                    .commit()

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        return binding.root
    }

    companion object{
        fun getInstance() = AutoTradingFragment()
    }
}