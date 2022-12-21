package com.im.app.coinwatcher.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Point
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.im.app.coinwatcher.BottomSheetFragment
import com.im.app.coinwatcher.CoinListFragment
import com.im.app.coinwatcher.R
import com.im.app.coinwatcher.common.SingleClickListener
import com.im.app.coinwatcher.common.decimalFormat
import com.im.app.coinwatcher.databinding.RecyclerCoinItemBinding
import com.im.app.coinwatcher.json_data.MarketAll
import com.im.app.coinwatcher.json_data.MarketTicker


class CoinListFragmentAdapter(private var marketList: MutableList<MarketTicker>
, private val kwrMap: HashMap<String, MarketAll>
, private val owner: CoinListFragment
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
    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onBindViewHolder(holder: CoinListViewHolder, position: Int) {
        with(holder.binding){
            with(marketList[position]){
                val marketKor = marketMapping(market)
                coinNameKor.text = marketKor
                tradePrice.text = decimalFormat(trade_price)
                changeRate.text = (decimalFormat(change_rate  * 100, true)) + "%"
                accTradePrice24h.text = """${decimalFormat(acc_trade_price_24h / 1000000)}백만"""
                marketName.text = """${market.split("-")[1]}/${market.split("-")[0]}""" //KRW-BTC -> BTC/KRW
                //EVEN : 보합 RISE : 상승 FALL : 하락	String
                when(this.change){
                    "RISE" -> {
                        tradePrice.setTextColor(Color.parseColor("#C84A31"))
                        changeRate.setTextColor(Color.parseColor("#C84A31"))
                    }
                    "FALL" -> {
                        tradePrice.setTextColor(Color.parseColor("#1261C6"))
                        changeRate.setTextColor(Color.parseColor("#1261C6"))
                    }
                    else -> {}
                }
                root.setOnClickListener(object: SingleClickListener(){
                    override fun onSingleClick(v: View?) {
                        val bottomSheet =
                            BottomSheetFragment.newInstance(marketList[position].market, marketKor)
                        bottomSheet.show(owner.childFragmentManager, bottomSheet.tag)
                    }
                })
            }

        }
    }
    override fun getItemCount() = marketList.size

    @RequiresApi(Build.VERSION_CODES.N)
    private fun marketMapping(market: String): String{
        kwrMap.forEach{ (key, value) ->
            if(key == market) return value.korean_name
        }
        return market
    }
}