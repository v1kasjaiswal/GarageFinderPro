package dev.falcon.garagefinderpro

import android.R.attr.entries
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry


class OwnerInsightsActivity : Fragment() {

    lateinit var BarChart : BarChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.ownerinsights_activity, container, false)
        val barChart: BarChart = view.findViewById(R.id.barChart)
        val pieChart: PieChart = view.findViewById(R.id.pieChart)
        val lineChart: LineChart = view.findViewById(R.id.lineChart)

        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(1f, 50f))
        entries.add(BarEntry(2f, 80f))
        entries.add(BarEntry(3f, 60f))
        entries.add(BarEntry(4f, 70f))
        entries.add(BarEntry(5f, 30f))

        val dataSet = BarDataSet(entries, "Sample Data")

        dataSet.color = Color.rgb(255, 49, 49)
        dataSet.valueTextColor = Color.BLACK

        val barData = BarData(dataSet)

        // Set the animation for the chart
        barChart.animateY(1000) // 1000 milliseconds (1 second) animation duration

        barChart.data = barData
        barChart.invalidate()

        val entries2 = ArrayList<PieEntry>()
        entries2.add(PieEntry(30f, "Label 1"))
        entries2.add(PieEntry(45f, "Label 2"))
        entries2.add(PieEntry(25f, "Label 3"))

        val dataSet2 = PieDataSet(entries2, "Sample Data")

        dataSet2.colors = mutableListOf(Color.BLUE, Color.GREEN, R.color.blue)
        dataSet2.valueTextColor = Color.BLACK

        val pieData = PieData(dataSet2)

        // Set the animation for the chart
        pieChart.animateXY(1000, 1000) // 1000 milliseconds for both X and Y-axis animations

        pieChart.data = pieData
        pieChart.invalidate()

//        add a line chart
         val entries3 = ArrayList<Entry>()
        entries3.add(Entry(1f, 50f))
        entries3.add(Entry(2f, 80f))
        entries3.add(Entry(3f, 60f))
        entries3.add(Entry(4f, 70f))
        entries3.add(Entry(5f, 30f))

        val dataSet3 = LineDataSet(entries3, "Sample Data")

        dataSet3.color = Color.rgb(255, 49, 49)
        dataSet3.valueTextColor = Color.BLACK

        val lineData = LineData(dataSet3)

        // Set the animation for the chart
        lineChart.animateY(1000) // 1000 milliseconds (1 second) animation duration

        lineChart.data = lineData
        lineChart.invalidate()


        return view
    }

}