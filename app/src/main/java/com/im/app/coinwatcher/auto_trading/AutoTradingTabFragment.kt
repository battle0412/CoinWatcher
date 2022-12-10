package com.im.app.coinwatcher.auto_trading

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.im.app.coinwatcher.R
import com.im.app.coinwatcher.databinding.FragmentAutoTradingTabBinding

class AutoTradingTabFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentAutoTradingTabBinding.inflate(inflater, container, false)
        val tabLayout = binding.tabLayout

        val buyFragment = AutoBuyingFragment.newInstance()
        val sellFragment = AutoSellingFragment.newInstance()

        if(savedInstanceState == null){
            with(childFragmentManager.beginTransaction()){
                add(R.id.tabContent, sellFragment).hide(sellFragment)
                add(R.id.tabContent, buyFragment)
                    .commit()
            }
        }

        tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val ft = childFragmentManager.beginTransaction()
                when(tab.position) {
                    0 -> {
                        ft.show(buyFragment)
                        ft.hide(sellFragment)
                            .commit()
                    }
                    1 -> {
                        ft.show(sellFragment)
                        ft.hide(buyFragment)
                            .commit()
                    }
                    else -> throw IllegalStateException("Unexpected value: ${tab.position}")
                }

                /*val ft = childFragmentManager.beginTransaction()
                ft.replace(R.id.tabContent, instance)
                    .commit()*/

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        return binding.root
    }

    companion object{
        fun newInstance() = AutoTradingTabFragment()
    }
}