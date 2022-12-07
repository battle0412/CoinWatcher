package com.im.app.coinwatcher

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.im.app.coinwatcher.common.DATABASE_NAME
import com.im.app.coinwatcher.common.SoundSearcher
import com.im.app.coinwatcher.databinding.FragmentTransactionHistoryBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TradeHistoryFragment: Fragment() {
    private lateinit var binding: FragmentTransactionHistoryBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTransactionHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CoroutineScope(Dispatchers.IO).launch {
            val mDataBase = requireActivity().openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null)
            val resultList = SQLiteManager.getDBInstance(requireContext()).selectTrading(mDataBase)
            CoroutineScope(Dispatchers.Main).launch {
                with(binding.tradesRV){
                    val manager = LinearLayoutManager(activity as Activity, LinearLayoutManager.VERTICAL, false)
                    val deco = MarketItemDecoration(requireContext(), 5, 8, 5, 8)
                    addItemDecoration(deco)
                    layoutManager = manager
                    adapter = TradeHistoryFragmentAdapter(resultList)
                }
            }
        }
    }

    companion object{
        fun newInstance() = TradeHistoryFragment()
    }
}