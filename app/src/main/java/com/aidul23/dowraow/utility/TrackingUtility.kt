package com.aidul23.dowraow.utility

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Build
import com.aidul23.dowraow.constants.Constant
import com.aidul23.dowraow.services.Polyline
import pub.devrel.easypermissions.EasyPermissions
import java.util.concurrent.TimeUnit

object TrackingUtility {
    fun hasLocationPermissions(context: Context) =
        EasyPermissions.hasPermissions(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )


//        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
//            EasyPermissions.hasPermissions(
//                context,
//                Manifest.permission.ACCESS_FINE_LOCATION,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            )
//        } else {
//            EasyPermissions.hasPermissions(
//                context,
//                Manifest.permission.ACCESS_FINE_LOCATION,
//                Manifest.permission.ACCESS_COARSE_LOCATION,
//                Manifest.permission.ACCESS_BACKGROUND_LOCATION
//            )
//        }

    fun calculatePolylineDistance(polyline: Polyline): Float {
        var distance = 0f
        for (i in 0..polyline.size - 2) {
            val pos1 = polyline[i]
            val pos2 = polyline[i + 1]

            val results = FloatArray(1)
            Location.distanceBetween(
                pos1.latitude, pos1.longitude,
                pos2.latitude, pos2.longitude, results
            )

            distance += results[0]

        }

        return distance
    }

    fun getFormattedStopWatchTime(ms: Long, includeMillis: Boolean = false): String {
        var milliseconds = ms
        val hour = TimeUnit.MILLISECONDS.toHours(milliseconds)
        milliseconds -= TimeUnit.HOURS.toMillis(hour)

        val minute = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds -= TimeUnit.MINUTES.toMillis(minute)

        val second = TimeUnit.MILLISECONDS.toSeconds(milliseconds)

        if (!includeMillis) {
            return "${if (hour < 10) "0" else ""}$hour:" +
                    "${if (minute < 10) "0" else ""}$minute:" +
                    "${if (second < 10) "0" else ""}$second"
        }
        milliseconds - TimeUnit.SECONDS.toMillis(second)
        milliseconds /= 10
        return "${if (hour < 10) "0" else ""}$hour:" +
                "${if (minute < 10) "0" else ""}$minute:" +
                "${if (second < 10) "0" else ""}$second:" +
                "${if (milliseconds < 10) "0" else ""}$milliseconds"
    }
}