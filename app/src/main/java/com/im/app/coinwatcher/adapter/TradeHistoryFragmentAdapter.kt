package com.im.app.coinwatcher.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.im.app.coinwatcher.common.decimalFormat
import com.im.app.coinwatcher.databinding.RecyclerTradeItemBinding
import com.im.app.coinwatcher.json_data.TradeOrder

class TradeHistoryFragmentAdapter(private var tradeList: MutableList<TradeOrder>
): RecyclerView.Adapter<TradeHistoryFragmentAdapter.TransactionHistoryViewHolder>() {
    inner class TransactionHistoryViewHolder(val binding: RecyclerTradeItemBinding):
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionHistoryViewHolder {
        val binding = RecyclerTradeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionHistoryViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TransactionHistoryViewHolder, position: Int) {
        with(holder.binding){
            with(tradeList[position]){
                tradesCreatedAt.text = created_at.substring(0, 11).replace("T", "")
                tradesCreatedAtDetail.text = created_at.substring(11, 16).replace("T", "")
                tradesMarket.text = market.split("-")[1]
                tradesSide.text = when(side){
                    "bid" -> {
                        tradesSide.setTextColor(Color.parseColor("#C84A31"))
                        "매수"
                    }
                    "ask" -> {
                        tradesSide.setTextColor(Color.parseColor("#1261C6"))
                        "매도"
                    }
                    else -> ""
                }
                tradesPrice.text = decimalFormat(trades_price.toFloat())
                tradesFunds.text = decimalFormat(trades_funds.toFloat())
            }
        }
    }
    override fun getItemCount() = tradeList.size
}