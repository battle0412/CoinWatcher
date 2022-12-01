package com.im.app.coinwatcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.im.app.coinwatcher.databinding.FragmentMyAssetsBinding
import com.im.app.coinwatcher.databinding.FragmentTransactionHistoryBinding

class TransactionHistoryFragment: Fragment() {
    private lateinit var binding: FragmentTransactionHistoryBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTransactionHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }
    companion object{
        fun getInstance() = TransactionHistoryFragment()
    }
}