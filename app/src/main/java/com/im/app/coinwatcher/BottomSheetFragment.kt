package com.im.app.coinwatcher

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Insets
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.im.app.coinwatcher.chart.CustomMarkerView
import com.im.app.coinwatcher.chart.MarkerContent
import com.im.app.coinwatcher.common.*
import com.im.app.coinwatcher.databinding.FragmentBottomSheetBinding
import com.im.app.coinwatcher.json_data.Candles
import kotlinx.android.synthetic.main.fragment_bottom_sheet.*
import kotlinx.android.synthetic.main.fragment_bottom_sheet.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class BottomSheetFragment: BottomSheetDialogFragment() {
    private lateinit var binding: FragmentBottomSheetBinding
    private lateinit var csChart: CandleStickChart
    private lateinit var bChart: BarChart
    private var flag = true
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBottomSheetBinding.inflate(inflater, container, false)
        with(binding){
            val market = arguments?.let {
                it.getString("bottomSheetMarket") ?: ""
            } ?: ""
            val marketNm = arguments?.let {
                it.getString("bottomSheetMarketName") ?: ""
            } ?: ""
            marketName.text = "$marketNm($market)"
            val adapter = ArrayAdapter(
                requireContext(),
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                getUnits()
            )
            val sharedPreferences = SharedPreferenceManager.getSettingsPreference(requireContext())
            unitItems.setText(sharedPreferences.getString("chartUnit", "5분"))
            unitItems.setAdapter(adapter)

            unitItems.setOnItemClickListener { _, _, _, _ ->
                val unitText = unitItems.text.toString()
                sharedPreferences.edit().putString("chartUnit",unitText ).apply()
                CoroutineScope(Dispatchers.IO).launch {
                    val candles = getCandles(market,unitText)
                    flag = false
                    setCandleChartData(candles)
                    setBarChartData(candles)
                    flag = true
                    startMinuteCandleAPI(market)
                }
            }
        }
        return binding.root
    }



    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setBottomSheetHeight(70)

        val market = arguments?.let {
            it.getString("bottomSheetMarket") ?: ""
        } ?: ""
        val marketUnit = SharedPreferenceManager.getSettingsPreference(requireContext()).getString("marketUnit", "5분") ?: ""
        CoroutineScope(Dispatchers.IO).launch {
            val candles = getCandles(market, marketUnit)
            setCandleChartData(candles)
            setBarChartData(candles)
            startMinuteCandleAPI(market)
        }
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun setCandleChartData(candles: MutableList<Candles>){
        with(binding){
            csChart = candleStickChart
        }

        csChart.description.isEnabled = false
        /*val description = Description().apply {
            this.text = ""
        }
        chart.description = description*/
        //최개 표시 개수
        csChart.setMaxVisibleValueCount(1000)
        //false: 확대 축소가 x, y축 독립적으로 실행
        csChart.setPinchZoom(true)
        csChart.legend.isEnabled = false
        csChart.setDrawGridBackground(false)
        val xAxis = csChart.xAxis
        xAxis.position = XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false) //x축 선 표시 안함
        xAxis.setDrawLabels(true) //x축 라벨 표시 안함
        xAxis.xOffset = 30f
        val leftAxis = csChart.axisLeft
        leftAxis.isEnabled = false
//        leftAxis.setEnabled(false);
        leftAxis.setLabelCount(25, false)
        leftAxis.setDrawGridLines(false)
        leftAxis.setDrawAxisLine(false)

        val rightAxis = csChart.axisRight
        rightAxis.isEnabled = true
        rightAxis.setLabelCount(5, false)
        rightAxis.setDrawGridLines(false)
        rightAxis.setDrawAxisLine(true)
        rightAxis.minWidth = 60f

//        rightAxis.setStartAtZero(false);
        // setting data
/*            seekBarX.progress = 40
            seekBarY.progress = 100*/



        csChart.resetTracking()

        /*val values = ArrayList<CandleEntry>()
        for (i in dataset.lastIndex downTo 0 ) {
            with(dataset[i]){
                val decimalFormat = DecimalFormat("#,###")
                val decimalFormat2 = DecimalFormat("#,###.###")
                val markerContent = MarkerContent(
                    decimalFormat.format(high_price),
                    decimalFormat.format(low_price),
                    decimalFormat.format(opening_price),
                    decimalFormat.format(trade_price),
                    //candle_date_time_kst yyyy-MM-ddTHH:mm:ss
                    candle_date_time_kst,
                    decimalFormat2.format(candle_acc_trade_volume)
                )
                values.add(
                    CandleEntry(
                        dataset.size - i.toFloat(),
                        high_price.toFloat(),
                        low_price.toFloat(),
                        opening_price.toFloat(),
                        trade_price.toFloat(),
                        markerContent
                    )
                )
            }
        }*/
        val set1 = CandleDataSet(getCandleEntry(candles), "")
        set1.setDrawIcons(false)
        set1.axisDependency = YAxis.AxisDependency.RIGHT
//        set1.setColor(Color.rgb(80, 80, 80));
        set1.shadowColor = Color.DKGRAY
        //set1.shadowWidth = 0.7f
        set1.decreasingColor = Color.BLUE
        set1.decreasingPaintStyle = Paint.Style.FILL //FILL: 캔들 채움, STROKE: 테두리만 표시
        set1.increasingColor = Color.RED//Color.rgb(122, 242, 84)
        set1.increasingPaintStyle = Paint.Style.FILL
        set1.neutralColor = Color.WHITE
        set1.setDrawValues(false)//캔들의 데이터 표시 숨김
        //set1.setHighlightLineWidth(1f);
        val data = CandleData(set1)
        csChart.marker = CustomMarkerView(context, R.layout.custom_marker)
        csChart.data = data
        CoroutineScope(Dispatchers.Main).launch{
            //csChart.setBackgroundColor(Color.WHITE)
            csChart.invalidate()
        }
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCandleEntry(dataset: List<Candles>, isPreRemove: Boolean = true): ArrayList<CandleEntry> {
        val values = ArrayList<CandleEntry>()

        val index: Int = csChart.data?.let {
            if(isPreRemove) it.entryCount else it.entryCount - 1
        } ?: 0
        /*val index: Int = chart.data?.apply {
            if(isRemove) this.dataSets[0].removeEntry(this.entryCount - 1)
        }?.let {
            it.entryCount - 1
        } ?: 0*/

        for (i in dataset.lastIndex downTo 0 ) {
            with(dataset[i]){
                val markerContent = MarkerContent(
                    decimalFormat(high_price),
                    decimalFormat(low_price),
                    decimalFormat(opening_price),
                    decimalFormat(trade_price),
                    //candle_date_time_kst foramt yyyy-MM-dd'T'HH:mm:ss
                    candle_date_time_kst,
                    decimalFormat(candle_acc_trade_volume, 3)
                )
                values.add(
                    CandleEntry(
                        (dataset.lastIndex - i + index).toFloat(),
                        high_price.toFloat(),
                        low_price.toFloat(),
                        opening_price.toFloat(),
                        trade_price.toFloat(),
                        markerContent
                    )
                )
            }
        }
        return values
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun setBarChartData(candles: MutableList<Candles>){
        with(binding){
            bChart = barChart
        }
        bChart.setDrawBarShadow(false)
        bChart.setDrawValueAboveBar(false)

        bChart.description.isEnabled = false
        // if more than 60 entries are displayed in the chart, no values will be
        // drawn

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        bChart.setMaxVisibleValueCount(1000)

        // scaling can now only be done on x- and y-axis separately

        // scaling can now only be done on x- and y-axis separately
        bChart.setPinchZoom(true)

        bChart.setDrawGridBackground(false)
        // chart.setDrawYLabels(false);

        // chart.setDrawYLabels(false);

        val xAxis = bChart.xAxis
        xAxis.position = XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        //xAxis.granularity = 1f // only intervals of 1 day
        xAxis.labelCount = 7


        /*val leftAxis = bChart.axisLeft
        leftAxis.setLabelCount(8, false)
        leftAxis.setPosition(YAxisLabelPosition.OUTSIDE_CHART)
        leftAxis.spaceTop = 15f
        leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)*/
        bChart.axisLeft.axisMinimum = 0f;
        bChart.axisLeft.isEnabled = false

        val rightAxis = bChart.axisRight
        rightAxis.setDrawGridLines(false)
        rightAxis.setLabelCount(5, false)
        rightAxis.spaceTop = 15f
        rightAxis.axisMinimum = 0f // this replaces setStartAtZero(true)
        rightAxis.minWidth = 60f

        /*val l = bChart.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.setDrawInside(false)
        l.form = LegendForm.SQUARE
        l.formSize = 9f
        l.textSize = 11f
        l.xEntrySpace = 4f*/


        val set1 = BarDataSet(getBarCandleEntry(candles), "")

        /*set1.setDrawIcons(false)
        val startColor1 = ContextCompat.getColor(this, android.R.color.holo_orange_light)
        val startColor2 = ContextCompat.getColor(this, android.R.color.holo_blue_light)
        val startColor3 = ContextCompat.getColor(this, android.R.color.holo_orange_light)
        val startColor4 = ContextCompat.getColor(this, android.R.color.holo_green_light)
        val startColor5 = ContextCompat.getColor(this, android.R.color.holo_red_light)
        val endColor1 = ContextCompat.getColor(this, android.R.color.holo_blue_dark)
        val endColor2 = ContextCompat.getColor(this, android.R.color.holo_purple)
        val endColor3 = ContextCompat.getColor(this, android.R.color.holo_green_dark)
        val endColor4 = ContextCompat.getColor(this, android.R.color.holo_red_dark)
        val endColor5 = ContextCompat.getColor(this, android.R.color.holo_orange_dark)
        val gradientFills: MutableList<Fill> = java.util.ArrayList<Fill>()
        gradientFills.add(Fill(startColor1, endColor1))
        gradientFills.add(Fill(startColor2, endColor2))
        gradientFills.add(Fill(startColor3, endColor3))
        gradientFills.add(Fill(startColor4, endColor4))
        gradientFills.add(Fill(startColor5, endColor5))
        set1.setFills(gradientFills)*/
        set1.color = getColor(requireContext(), android.R.color.holo_orange_dark)
        val dataSets = ArrayList<IBarDataSet>()
        dataSets.add(set1)
        val data = BarData(dataSets)
        //data.setValueTextSize(10f)
        data.setDrawValues(false)
        data.barWidth = 0.9f
        bChart.data = data
        bChart.legend.isEnabled = false
        CoroutineScope(Dispatchers.Main).launch{
            //bChart.setBackgroundColor(Color.WHITE)

            bChart.invalidate()
        }
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getBarCandleEntry(dataset: List<Candles>, isPreRemove: Boolean = true): ArrayList<BarEntry> {
        val values = ArrayList<BarEntry>()

        val index: Int = bChart.data?.let {
            if(isPreRemove) it.entryCount else it.entryCount - 1
        } ?: 0
        /*val index: Int = chart.data?.apply {
            if(isRemove) this.dataSets[0].removeEntry(this.entryCount - 1)
        }?.let {
            it.entryCount - 1
        } ?: 0*/

        for (i in dataset.lastIndex downTo 0 ) {
            with(dataset[i]){
                values.add(
                    BarEntry(
                        (dataset.lastIndex - i + index).toFloat(),
                        candle_acc_trade_volume.toFloat()
                    )
                )
            }
        }
        return values
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun setBottomSheetHeight(heightRate: Int) {
        val bottomSheet = dialog!!.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val behavior = BottomSheetBehavior.from(bottomSheet)
        val layoutParams = bottomSheet.layoutParams
        /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            val windowManager = requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val windowMetrics = windowManager.defaultDisplay
            requireContext().display!!.height
        } else {

            val layoutParams = bottomSheet.layoutParams
            val displayMetrics = DisplayMetrics()
            (requireContext() as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
            layoutParams.height = displayMetrics.heightPixels * heightRate / 100
            bottomSheet.layoutParams = layoutParams
        }*/
        layoutParams.height = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = requireActivity().windowManager.currentWindowMetrics
            val insets: Insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            (windowMetrics.bounds.height() - insets.top - insets.bottom) * heightRate / 100
        } else {
            val displayMetrics = DisplayMetrics()
            requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.heightPixels * heightRate / 100
        }
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun startMinuteCandleAPI(market: String){
        while(flag){
            delay(5000L)
            val values = ArrayList<Candles>()
            if(!this::csChart.isInitialized || !this::bChart.isInitialized)
                return
            val marketUnit = binding.unitItems.text.toString()
            val candleData = getCandles(market, marketUnit)
            with(csChart.data.dataSets[0]){//차트테이터
                val bThis = bChart.data.dataSets[0]
                val index = this.entryCount - 1 //데이터 크기-1 -> 인덱스
                val chartData = this.getEntryForIndex(index).data as MarkerContent //마커 데이터 클래스로 형변환
                if(candleData[0].candle_date_time_kst == chartData.dateTime){//API 값 == 차트 데이터 날짜 -> 마지막 차트 데이터만 업데이트
                    values.add(candleData[0])
                    this.removeEntry(index)
                    bThis.removeEntry(index)
                    this.addEntry(getCandleEntry(values)[0])
                    bThis.addEntry(getBarCandleEntry(values)[0])
                } else if(candleData[0].candle_date_time_kst > chartData.dateTime) {//API 값 > 차트 데이터 날짜 -> 마지막 데이터 업데이트 후 API 최신데이터 추가
                    //최신 데이터가 먼저 오기 때문에 차트에 데이터를 넣기 전 getCandleEntry에서 downTo로 리스트를 만듦
                    //candleData[0]이 entries[1]이 된다
                    values.add(candleData[0])
                    values.add(candleData[1])
                    this.removeEntry(index)
                    bThis.removeEntry(index)
                    val entries = getCandleEntry(values)
                    val barEntries = getBarCandleEntry(values)
                    this.addEntry(entries[0])
                    this.addEntry(entries[1])
                    bThis.addEntry(barEntries[0])
                    bThis.addEntry(barEntries[1])
                } else { }
                values.clear()
            }
            csChart.data.notifyDataChanged()
            csChart.notifyDataSetChanged()
            bChart.data.notifyDataChanged()
            bChart.notifyDataSetChanged()
            CoroutineScope(Dispatchers.Main).launch {
                csChart.invalidate()
                bChart.invalidate()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        flag = false
    }

    companion object{
        fun newInstance(market: String, marketName: String): BottomSheetFragment{
            val bundle = Bundle()
            bundle.putString("bottomSheetMarket", market)
            bundle.putString("bottomSheetMarketName", marketName)
            val fg = BottomSheetFragment()
            fg.arguments = bundle
            return fg
        }
    }
}