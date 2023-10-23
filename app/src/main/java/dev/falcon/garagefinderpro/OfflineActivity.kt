package dev.falcon.garagefinderpro

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class OfflineActivity : AppCompatActivity() {
    lateinit var networkReceiver : CheckConnectivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.offline_activity)

        networkReceiver = CheckConnectivity()
    }

    fun openOfflineSupport(view: View) {
        val intent = Intent(this, SupportActivity::class.java)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
        registerReceiver(networkReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(networkReceiver)
    }

    fun showInfo(view: View) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Info")
            .setMessage("Welcome to Garage Finder Pro! \n\n" +
                    "Make Sure that you have an Active Internet Connection \n\n" +
                    "Developed By - Vikas Jaiswal\n\n" +
                    "For any queries, please contact us")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }


}