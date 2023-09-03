package dev.falcon.garagefinderpro

import android.os.Handler
import android.os.Looper
import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import okio.IOException
import org.json.JSONException
import org.json.JSONObject

public class FuelPriceAPI {
    val client = okhttp3.OkHttpClient()

    fun getFuelPrices(state: String, city: String,callback: (String, String, String) -> Unit) {

        val url =
            "https://daily-petrol-diesel-lpg-cng-fuel-prices-in-india.p.rapidapi.com/v1/fuel-prices/today/india/$state/$city"

        val request = okhttp3.Request.Builder()
            .url(url)
            .get()
            .addHeader("x-rapidapi-key","cfe8304bb0mshb6039f76000bf12p1f26e4jsn185ec7a27c8b")
            .addHeader(
                "x-rapidapi-host",
                "daily-petrol-diesel-lpg-cng-fuel-prices-in-india.p.rapidapi.com"
            )
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("TAG", "onFailure: " + e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()

                Log.d("TAG", "onResponse: $body")

                try {
                    val jsonObject = JSONObject(body)

                    Log.d("TAG", "JSON Object: $jsonObject")

                    if (jsonObject.has("fuel")) {
                        val fuel = jsonObject.getJSONObject("fuel")
                        val petrol = fuel.getJSONObject("petrol")
                        val diesel = fuel.getJSONObject("diesel")
                        val cng = fuel.getJSONObject("cng")

                        val petrolPrice = petrol.getString("retailPrice")
                        val dieselPrice = diesel.getString("retailPrice")
                        val cngPrice = cng.getString("retailPrice")

                        Handler(Looper.getMainLooper()).post {
                            callback(petrolPrice, dieselPrice, cngPrice)
                        }
                    } else {
                        Log.e("TAG", "JSON response is missing required keys.")
                    }
                } catch (e: JSONException) {
                    Log.e("TAG", "Error parsing JSON: ${e.message}")
                }
            }

        })
    }
}
