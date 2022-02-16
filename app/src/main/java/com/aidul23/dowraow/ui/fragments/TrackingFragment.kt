package com.aidul23.dowraow.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.aidul23.dowraow.R
import com.aidul23.dowraow.constants.Constant.ACTION_PAUSE_SERVICE
import com.aidul23.dowraow.constants.Constant.ACTION_START_OR_RESUME_SERVICE
import com.aidul23.dowraow.constants.Constant.ACTION_STOP_SERVICE
import com.aidul23.dowraow.constants.Constant.MAP_ZOOM
import com.aidul23.dowraow.constants.Constant.POLYLINE_COLOR
import com.aidul23.dowraow.constants.Constant.POLYLINE_WIDTH
import com.aidul23.dowraow.databinding.FragmentRunBinding
import com.aidul23.dowraow.databinding.FragmentTrackingBinding
import com.aidul23.dowraow.db.Run
import com.aidul23.dowraow.services.Polyline
import com.aidul23.dowraow.services.TrackingService
import com.aidul23.dowraow.utility.TrackingUtility
import com.aidul23.dowraow.viewmodels.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject
import kotlin.math.round

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {
    private var _binding: FragmentTrackingBinding? = null
    private val binding get() = _binding!!
    private var map: GoogleMap?=null
    private var currentTimeInMillis = 0L
    private val viewModel : MainViewModel by viewModels()
    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()
    private var menu: Menu? = null
    @set:Inject
    var weight = 80f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTrackingBinding.bind(view)

        binding.mapView.onCreate(savedInstanceState)
        binding.btnToggleRun.setOnClickListener {
            toggleRun()
        }

        binding.btnFinishRun.setOnClickListener {
            zoomCameraToSeeWholeTrack()
            endRunAndSaveToDB()
        }

        binding.mapView.getMapAsync {
            map = it
            addAllPolyLines()
        }

        subscribeToObserver()
    }

    private fun subscribeToObserver(){
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestPolyLine()
            moveCameraToUser()
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            currentTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(currentTimeInMillis,true)
            binding.tvTimer.text = formattedTime
        })
    }

    private fun toggleRun() {
        if (isTracking) {
            menu?.getItem(0)?.isVisible = true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        }else{
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu,menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if(currentTimeInMillis > 0) {
            this.menu?.getItem(0)?.isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.miCancelTracking -> {
                showCancelTrackingDialog()
            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCancelTrackingDialog() {
        val dialog = MaterialAlertDialogBuilder(requireContext(),R.style.AlertDialogTheme)
            .setTitle("Cancel the run?")
            .setMessage("Are you sure to cancel the run and delete its data?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Yes"){_,_ ->
                stopRun()
            }
            .setNegativeButton("No") {dialogInterface,_ ->
                dialogInterface.cancel()
            }
            .create()

        dialog.show()
    }

    private fun stopRun() {
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if(!isTracking) {
            binding.btnToggleRun.text = "Start"
            binding.btnFinishRun.visibility = View.VISIBLE
        } else {
            binding.btnToggleRun.text = "Stop"
            menu?.getItem(0)?.isVisible = true
            binding.btnFinishRun.visibility = View.GONE
        }
    }

    private fun moveCameraToUser() {
        if(pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()){
            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(
                pathPoints.last().last(),
                MAP_ZOOM
            ))
        }
    }

    private fun zoomCameraToSeeWholeTrack() {
        val bounds = LatLngBounds.Builder()
        for(polyline in pathPoints) {
            for(pos in polyline) {
                bounds.include(pos)
            }
        }
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                binding.mapView.width,
                binding.mapView.height,
                (binding.mapView.height * 0.05f).toInt()
            )
        )
    }

    private fun endRunAndSaveToDB() {
        map?.snapshot { bmp ->
            var distanceInMeter = 0
            for(polyline in pathPoints) {
                distanceInMeter +=  TrackingUtility.calculatePolylineDistance(polyline).toInt()
            }
            val avgSpeed = round((distanceInMeter / 1000f) / (currentTimeInMillis/1000f/60/60) * 10) / 10f
            val dataTimeStamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeter / 1000f) * weight).toInt()
            val run = Run(bmp, dataTimeStamp, avgSpeed, distanceInMeter, currentTimeInMillis, caloriesBurned)
            viewModel.insertRun(run)
            Snackbar.make(requireActivity().findViewById(R.id.rootView),
            "Run save successfully",
            Snackbar.LENGTH_LONG).show()
            stopRun()
        }
    }

    private fun addAllPolyLines() {
        for (polyline in pathPoints) {
            val polyLineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polyLineOptions)
        }
    }

    private fun addLatestPolyLine() {
        if(pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()

            val polyLineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            map?.addPolyline(polyLineOptions)
        }
    }


    private fun sendCommandToService(action: String) {
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}