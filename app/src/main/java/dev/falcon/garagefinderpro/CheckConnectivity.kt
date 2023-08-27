package dev.falcon.garagefinderpro

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection

class CheckConnectivity : BroadcastReceiver()
{
    private val scope = CoroutineScope(Dispatchers.Main)

    fun isInternetAvailable(): Boolean {
        try {
            val urlc: URLConnection = URL("https://www.google.com/").openConnection()
            (urlc as HttpURLConnection).requestMethod = "HEAD"
            urlc.connectTimeout = 1500
            urlc.connect()
            return true
        } catch (e: Exception) {
            return false
        }
    }

    override fun onReceive(context: Context, intent: Intent)
    {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

        if (capabilities != null)
        {
            if ((capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) || (capabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI)))
            {
                scope.launch {
                    val result = withContext(Dispatchers.IO) {
                        isInternetAvailable()
                    }

                    if (result)
                    {

                    }
                    else{
                        val intent = Intent(context, OfflineActivity::class.java)
                        context.startActivity(intent)
                    }
                }
            }
            else
            {
                val intent = Intent(context, OfflineActivity::class.java)
                context.startActivity(intent)
            }
        }
        else
        {
            val intent = Intent(context, OfflineActivity::class.java)
            context.startActivity(intent)
        }

    }

}
