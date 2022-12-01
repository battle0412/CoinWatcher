package com.im.app.coinwatcher

import android.annotation.SuppressLint
import android.app.UiModeManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import com.im.app.coinwatcher.JWT.GeneratorJWT.JWTGenerator.generateJWT
import com.im.app.coinwatcher.common.IS_NIGHT
import com.im.app.coinwatcher.common.decimalFormat
import com.im.app.coinwatcher.common.getGsonList
import com.im.app.coinwatcher.common.responseUpbitAPI
import com.im.app.coinwatcher.databinding.FragmentMyAssetsBinding
import com.im.app.coinwatcher.json_data.Accounts
import com.im.app.coinwatcher.okhttp_retrofit.RetrofitOkHttpManagerUpbit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyAssetsFragment: Fragment() {
    private lateinit var binding: FragmentMyAssetsBinding
    private lateinit var chart: PieChart
    private var color = 0
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyAssetsBinding.inflate(inflater, container, false)
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
        initMyAssets()
    }

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
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.R)
    private fun initMyAssets(){
        CoroutineScope(Dispatchers.IO).launch{
            val rest = RetrofitOkHttpManagerUpbit(generateJWT()).restService
            val responseStr = responseUpbitAPI(rest.requestAccounts())
            val accountList = getGsonList(responseStr, Accounts::class.java)
            var assetKrw = 0F
            var assetKrwTotal = 0F
            var profitOrLoss = 0F
            var myBuy = 0F
            var myEvaluation = 0F
            var rateOfReturn = 0F
            accountList.forEach {
                when(it.currency){
                    "KRW" -> {
                        assetKrw += it.balance.toFloat()
                        myBuy += it.locked.toFloat()
                    }
                    else -> {
                        myEvaluation += (it.balance.toFloat() * it.avg_buy_price.toFloat()).toInt()
                    }
                }
            }
            if(myBuy.toInt() == 0){
                assetKrwTotal = assetKrw + profitOrLoss
                with(binding){
                    this.myAssetKrw.text = "0"//decimalFormat(assetKrw, 0)
                    this.myBuy.text = "0"
                    this.myEvaluation.text = ""
                    this.myAssetTotal.text = "0"//decimalFormat(assetKrwTotal, 0)
                    this.myProfitAndLoss.text = ""
                    this.myRateOfReturn.text = ""
                }
            } else {
                profitOrLoss = myEvaluation - myBuy
                rateOfReturn = profitOrLoss / myBuy * 100
                assetKrwTotal = assetKrw + profitOrLoss

                with(binding){
                    /*this.myAssetKrw.text = decimalFormat(assetKrw, 0)
                    this.myBuy.text = decimalFormat(myBuy, 0)
                    this.myEvaluation.text = decimalFormat(myEvaluation, 0)
                    this.myAssetTotal.text = decimalFormat(assetKrwTotal, 0)
                    this.myProfitAndLoss.text = decimalFormat(profitOrLoss, 0)
                    this.myRateOfReturn.text = decimalFormat(rateOfReturn, 2) + "%"*/
                    this.myAssetKrw.text = "0"//decimalFormat(assetKrw, 0)
                    this.myBuy.text = "0"
                    this.myEvaluation.text = ""
                    this.myAssetTotal.text = "0"//decimalFormat(assetKrwTotal, 0)
                    this.myProfitAndLoss.text = ""
                    this.myRateOfReturn.text = ""
                }
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
    }
    companion object{
        fun getInstance() = MyAssetsFragment()
    }
}