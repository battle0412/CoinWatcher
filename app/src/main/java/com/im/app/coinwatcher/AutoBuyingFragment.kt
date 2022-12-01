package com.im.app.coinwatcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.im.app.coinwatcher.databinding.FragmentAutoBuyingBinding

class AutoBuyingFragment: Fragment() {
    private lateinit var binding: FragmentAutoBuyingBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAutoBuyingBinding.inflate(inflater, container, false)
        val items = ArrayList<String>()
        items.add("비트코인")
        items.add("이더리움")
        val adapter = ArrayAdapter<String>(
            requireContext(),
            com.google.android.material.R.layout.support_simple_spinner_dropdown_item,
            items
        )
        binding.marketMenu.adapter = adapter
        return binding.root
    }

    companion object{
        fun getInstance() = AutoBuyingFragment()
    }
}