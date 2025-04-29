package dev.falcon.garagefinderpro

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.dropin.NavigationView
import com.mapbox.navigation.ui.speedlimit.api.MapboxSpeedLimitApi
import com.mapbox.navigation.ui.speedlimit.model.PostedAndCurrentSpeedFormatter
import com.mapbox.navigation.ui.speedlimit.model.SpeedLimitFormatter

class UserNavigationActivity : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.usernavigation_activity, container, false)

        // Setup Mapbox if not already done
        if (!MapboxNavigationApp.isSetup()) {
            MapboxNavigationApp.setup {
                NavigationOptions.Builder(requireContext())
                    .accessToken(BuildConfig.MAPBOX_DOWNLOADS_TOKEN)  // Ensure you use your valid access token here
                    .build()
            }
        }

        return rootView
    }
}
