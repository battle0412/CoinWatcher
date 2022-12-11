package com.im.app.coinwatcher.history

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.im.app.coinwatcher.R
import com.im.app.coinwatcher.common.IS_NIGHT
import com.im.app.coinwatcher.common.decimalFormat
import com.im.app.coinwatcher.databinding.FragmentMyAssetsBinding
import com.im.app.coinwatcher.json_data.Accounts
import com.im.app.coinwatcher.json_data.MarketTicker
import com.im.app.coinwatcher.model.factory.UpbitViewModel
import com.im.app.coinwatcher.model.factory.UpbitViewModelFactory
import com.im.app.coinwatcher.okhttp_retrofit.RetrofitOkHttpManagerUpbit
import com.im.app.coinwatcher.repository.UpbitRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.round

class MyAssetsFragment: Fragment() {
    private lateinit var binding: FragmentMyAssetsBinding
    private lateinit var chart: PieChart
    private lateinit var viewModel: UpbitViewModel
    private var accountList = mutableListOf<Accounts>()
    private var marketTicker = HashMap<String, MarketTicker>()//mutableListOf<MarketTicker>()
    private var color = 0
    private var flag = true
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyAssetsBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(
            this, UpbitViewModelFactory(
                UpbitRepository(RetrofitOkHttpManagerUpbit().restService)
            )
        )[UpbitViewModel::class.java]
        viewModel.accounts.observe(viewLifecycleOwner){
            accountList = it
            val marketList = mutableListOf<String>()
            accountList.forEach {account -> if(account.currency != "KRW") marketList.add("KRW-" + account.currency)  }
            viewModel.getMarketTickerFromViewModel(marketList.joinToString(","))
        }
        viewModel.marketTicker.observe(viewLifecycleOwner){
            it.forEach { MarketTicker -> marketTicker[MarketTicker.market] = MarketTicker }
            initMyAssets()
        }
        color = if(IS_NIGHT)
            this.resources.getColor(R.color.itemTextColor)
        else
            this.resources.getColor(R.color.textColorPrimary)
        initPortfolioChart()
        return binding.root
    }
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CoroutineScope(Dispatchers.IO).launch {
            while (flag){
                delay(5000L)
                requestViewModel()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun initPortfolioChart(){
        chart = binding.portfolioChart
        chart.setUsePercentValues(true)
        chart.description.isEnabled = false
        chart.setExtraOffsets(0f, 10f, 20f, 5f)

        chart.dragDecelerationFrictionCoef = 0.95f

        //chart.setCenterTextTypeface(tfLight)
        //chart.centerText = generateCenterSpannableText()

        chart.isDrawHoleEnabled = true
        chart.setHoleColor(Color.TRANSPARENT)
        chart.setCenterTextColor(color)
        chart.centerText = this.resources.getString(R.string.my_array_rate) //"보유 비중(%)"
        /*chart.setTransparentCircleColor(Color.TRANSPARENT)
        chart.setTransparentCircleAlpha(255)*/

        chart.holeRadius = 45f
        chart.transparentCircleRadius = 45f

        chart.setDrawCenterText(true)

        chart.rotationAngle = 0f
        // enable rotation of the chart by touch
        // enable rotation of the chart by touch
        chart.isRotationEnabled = true
        chart.isHighlightPerTapEnabled = true

        chart.animateY(1400, Easing.EaseInOutQuad)
        // chart.spin(2000, 0, 360);

        // chart.spin(2000, 0, 360);
        val l = chart.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.CENTER
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.orientation = Legend.LegendOrientation.VERTICAL
        l.setDrawInside(false)
        l.xEntrySpace = 10f
        l.yEntrySpace = 0f
        l.yOffset = 0f
        l.textColor = color
        l.textSize = 12f

        // entry label styling

        // entry label styling
        chart.setEntryLabelColor(color)
        //chart.setEntryLabelTypeface(tfRegular)
        chart.setEntryLabelTextSize(12f)
        requestViewModel()
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.R)
    private fun initMyAssets(){
        var assetKrw = 0F
        var assetKrwTotal = 0F
        var profitOrLoss = 0F
        var myBuy = 0F
        var myEvaluation = 0F
        var rateOfReturn = 0F
        with(binding){
            accountList.forEach {
                when(it.currency){
                    "KRW" -> {
                        assetKrw += it.balance.toFloat()
                        //myBuy += it.locked.toFloat()
                    }
                    else -> {
                        myBuy += (it.balance.toFloat() * it.avg_buy_price.toFloat())
                        myEvaluation = it.balance.toFloat() * (marketTicker["KRW-" + it.currency]?.trade_price ?: 0).toFloat()
                    }
                }
            }
            if(myBuy.toInt() == 0){
                assetKrwTotal = assetKrw + profitOrLoss
                this.myAssetKrw.text = "0" //decimalFormat(assetKrw, 0) //"0"
                this.myBuy.text = "0"
                this.myEvaluation.text = ""
                this.myAssetTotal.text = "0" //decimalFormat(assetKrwTotal, 0) //"0"
                this.myProfitAndLoss.text = ""
                this.myRateOfReturn.text = ""
            } else {
                profitOrLoss = myEvaluation - myBuy
                rateOfReturn = profitOrLoss / myBuy * 100
                assetKrwTotal = assetKrw + profitOrLoss

                    this.myAssetKrw.text = "0"//decimalFormat(round(assetKrw), 0)
                    this.myBuy.text = decimalFormat(round(myBuy), 0)
                    this.myEvaluation.text = decimalFormat(round(myEvaluation), 0)
                    this.myAssetTotal.text = "0"//decimalFormat(round(assetKrwTotal), 0)
                    this.myProfitAndLoss.text = decimalFormat(round(profitOrLoss), 0)
                    this.myRateOfReturn.text = decimalFormat(rateOfReturn, 2) + "%"
                    /*this.myAssetKrw.text = "0"//decimalFormat(assetKrw, 0)
                    this.myBuy.text = "0"
                    this.myEvaluation.text = ""
                    this.myAssetTotal.text = "0"//decimalFormat(assetKrwTotal, 0)
                    this.myProfitAndLoss.text = ""
                    this.myRateOfReturn.text = ""*/
            }
            /*tradesSide.setTextColor(Color.parseColor("#C84A31"))
            tradesSide.setTextColor(Color.parseColor("#1261C6"))*/
            if(profitOrLoss > 0){
                myProfitAndLoss.setTextColor(Color.parseColor("#C84A31"))
            } else if(profitOrLoss < 0)
                myProfitAndLoss.setTextColor(Color.parseColor("#1261C6"))
            else
                myProfitAndLoss.setTextColor(myProfitAndLoss.textColors.defaultColor)

            if(rateOfReturn > 0){
                myRateOfReturn.setTextColor(Color.parseColor("#C84A31"))
            } else if(rateOfReturn < 0)
                myRateOfReturn.setTextColor(Color.parseColor("#1261C6"))
            else
                myRateOfReturn.setTextColor(myRateOfReturn.textColors.defaultColor)

        }

        //그래프
        val entries = ArrayList<PieEntry>()
        accountList.forEach {
            entries.add(
                PieEntry(
                    when(it.currency){
                        "KRW" -> it.balance.toFloat()
                        else -> (it.balance.toFloat() * it.avg_buy_price.toFloat())
                    },
                    it.currency
                )
            )
        }
        val dataSet = PieDataSet(entries, "")

        dataSet.setDrawIcons(false)

        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f

        val colors = ArrayList<Int>()

        for (c in ColorTemplate.VORDIPLOM_COLORS) colors.add(c)

        for (c in ColorTemplate.JOYFUL_COLORS) colors.add(c)

        for (c in ColorTemplate.COLORFUL_COLORS) colors.add(c)

        for (c in ColorTemplate.LIBERTY_COLORS) colors.add(c)

        for (c in ColorTemplate.PASTEL_COLORS) colors.add(c)

        colors.add(ColorTemplate.getHoloBlue())

        dataSet.colors = colors
        //dataSet.setSelectionShift(0f);

        //dataSet.setSelectionShift(0f);
        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter())
        data.setValueTextSize(12f)

        data.setValueTextColor(color)

        //data.setValueTextColor(Color.WHITE)
        //data.setValueTypeface(tfLight)
        chart.data = data

        // undo all highlights

        // undo all highlights
        chart.highlightValues(null)
        chart.invalidate()
        //그래프 끝
    }
    private fun requestViewModel(){
        viewModel.getAccountsFromViewModel()
    }

    companion object{
        fun newInstance() = MyAssetsFragment()
    }

    override fun onDestroy() {
        super.onDestroy()
        flag = false
    }
}