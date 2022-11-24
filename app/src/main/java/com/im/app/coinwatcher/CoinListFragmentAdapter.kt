package com.im.app.coinwatcher

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.im.app.coinwatcher.common.decimalFormat
import com.im.app.coinwatcher.databinding.RecyclerCoinItemBinding
import com.im.app.coinwatcher.json_data.MarketAll
import com.im.app.coinwatcher.json_data.MarketTicker


class CoinListFragmentAdapter(private var marketList: MutableList<MarketTicker>
, private val owner: Activity
, private val kwrMarketList: MutableList<MarketAll>
): RecyclerView.Adapter<CoinListFragmentAdapter.CoinListViewHolder>() {
    inner class CoinListViewHolder(val binding: RecyclerCoinItemBinding):
            RecyclerView.ViewHolder(binding.root)

    /**
     * 한번 호출되면 한 행을 그리는 레이아웃이 만들어진다
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoinListViewHolder {
        val binding = RecyclerCoinItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CoinListViewHolder(binding)
    }

    /**
     * coinList의 index value
     */
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CoinListViewHolder, position: Int) {
        with(holder.binding){
            with(marketList[position]){
                coinNameKor.text = marketMapping(market)
                tradePrice.text = decimalFormat(trade_price, false)
                changeRate.text = (decimalFormat(change_rate  * 100, true)) + "%"
                accTradePrice24h.text = """${decimalFormat(acc_trade_price_24h / 1000000, false)}백만"""
                marketName.text = """${market.split("-")[1]}/${market.split("-")[0]}""" //KRW-BTC -> BTC/KRW
                //EVEN : 보합 RISE : 상승 FALL : 하락	String
                when(this.change){
                    "RISE" -> {
                        tradePrice.setTextColor(Color.parseColor("#E16553"))
                        changeRate.setTextColor(Color.parseColor("#E16553"))
                    }
                    "FALL" -> {
                        tradePrice.setTextColor(Color.parseColor("#288CFF"))
                        changeRate.setTextColor(Color.parseColor("#288CFF"))
                    }
                    else -> {}
                }
            }
            /*root.setOnLongClickListener {
                val intent = Intent()
                intent.putExtra("market", marketList[position].market)
                owner.startActivity(intent)
                true
            }*/
        }
    }
    override fun getItemCount() = marketList.size

    private fun marketMapping(market: String): String{
        kwrMarketList.forEach{ marketAll ->
            if(marketAll.market == market) return marketAll.korean_name
        }
        return market
    }
}