package com.im.app.coinwatcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.im.app.coinwatcher.databinding.FragmentAutoTradingTabBinding
import kotlinx.android.synthetic.main.fragment_auto_trading_tab.*

class AutoTradingTabFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val activity = activity as MainActivity
        val binding = FragmentAutoTradingTabBinding.inflate(inflater, container, false)
        val tabLayout = binding.tabLayout

        if(savedInstanceState == null){
            with(childFragmentManager.beginTransaction()){
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

                val ft = childFragmentManager.beginTransaction()
                ft.replace(R.id.tabContent, instance)
                    .commit()

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        return binding.root
    }

    companion object{
        fun getInstance() = AutoTradingTabFragment()
    }
}