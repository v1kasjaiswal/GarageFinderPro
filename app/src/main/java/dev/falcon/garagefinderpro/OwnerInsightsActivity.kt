package dev.falcon.garagefinderpro

import android.R.attr.entries
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class OwnerInsightsActivity : Fragment() {

    var db = Firebase.firestore

    var auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.ownerinsights_activity, container, false)
        val barChart: BarChart = view.findViewById(R.id.barChart)
        val pieChart: PieChart = view.findViewById(R.id.pieChart)
        val barChart2 : BarChart = view.findViewById(R.id.barChart2)

        db.collection("jobcards")
            .whereEqualTo("garageId", auth.currentUser!!.uid)
            .orderBy("date")
            .whereIn("status", listOf("Pending", "On Going", "Completed"))
            .get()
            .addOnSuccessListener {
                var pending = 0
                var onGoing = 0
                var completed = 0

                for (document in it) {
                    if (document.get("status") == "Pending") {
                        pending += 1
                    }
                    else if (document.get("status") == "On Going") {
                        onGoing += 1
                    }
                    else if (document.get("status") == "Completed") {
                        completed += 1
                    }

                    val entries = ArrayList<BarEntry>()
                    entries.add(BarEntry(1f, pending.toFloat()))
                    entries.add(BarEntry(2f, onGoing.toFloat()))
                    entries.add(BarEntry(3f, completed.toFloat()))

                    val dataSet = BarDataSet(entries, "Count of Jobcards Types")

                    barChart.description.text = "Count of Jobcards Types"

                    dataSet.colors = mutableListOf(Color.RED, Color.GREEN, Color.BLUE)
                    dataSet.valueTextColor = Color.BLACK

                    val labels = ArrayList<String>()
                    labels.add("Pending")
                    labels.add("On Going")
                    labels.add("Completed")
                    val xAxis = barChart.xAxis
                    xAxis.valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels)
                    xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                    xAxis.granularity = 1f
                    xAxis.setCenterAxisLabels(true)
                    xAxis.isGranularityEnabled = true

                    barChart.xAxis.setDrawGridLines(false)
                    barChart.axisLeft.setDrawGridLines(true)
                    barChart.axisRight.setDrawGridLines(true)

                    val barData = BarData(dataSet)

                    barChart.animateY(1000)

                    val legend = barChart.legend
                    legend.isEnabled = true

                    barChart.data = barData
                    barChart.invalidate()

                    var towrequired = 0
                    var towNotrequired = 0

                    for (document in it) {
                        if (document.get("towLocation") == "Not Required") {
                            towNotrequired += 1
                        }
                        else{
                            towrequired += 1
                        }
                    }

                    val entries2 = ArrayList<PieEntry>()
                    entries2.add(PieEntry(towrequired.toFloat(), "Tow Required"))
                    entries2.add(PieEntry(towNotrequired.toFloat(), "Tow Not Required"))

                    val dataSet2 = PieDataSet(entries2, "Tow Required or Not")

                    dataSet2.colors = mutableListOf( Color.LTGRAY, Color.DKGRAY)
                    dataSet2.valueTextColor = Color.BLACK

                    val pieData = PieData(dataSet2)

                    pieChart.animateXY(1000, 1000)

                    pieChart.data = pieData
                    pieChart.invalidate()


                    val entries3 = ArrayList<BarEntry>()

                    for (document in it){

                        var rating = 0f

                        if (document.get("rating").toString() == "null"){
                            rating = 1f
                        }
                        else{
                            rating = document.get("rating").toString().toFloat()
                        }

                        entries3.add(BarEntry(rating, rating))
                    }

                    val dataSet3 = BarDataSet(entries3, "Rating of Jobcards")

                    dataSet3.colors = mutableListOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN)

                    val barData2 = BarData(dataSet3)

                    barChart2.animateY(1000)

                    barChart2.data = barData2

                    barChart2.invalidate()

                }
            }
            .addOnFailureListener {
                Log.d("OwnerInsightsActivity", "Error getting documents: ", it)
            }

        return view
    }

}