package com.im.app.coinwatcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.im.app.coinwatcher.databinding.FragmentAutoSellingBinding

class AutoSellingFragment: Fragment() {
    private lateinit var binding: FragmentAutoSellingBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAutoSellingBinding.inflate(inflater, container, false)
        return binding.root
    }
    companion object{
        fun getInstance() = AutoSellingFragment()
    }
}