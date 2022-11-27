package com.example.zadanie.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.example.zadanie.databinding.FragmentDetailBarBinding
import com.example.zadanie.helpers.Injection
import com.example.zadanie.helpers.PreferenceData
import com.example.zadanie.ui.viewmodels.DetailViewModel
import com.example.zadanie.ui.widget.detailList.BarDetailItem
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class BarDetailFragment : Fragment(), OnMapReadyCallback {
    private lateinit var binding: FragmentDetailBarBinding

    private lateinit var viewModel: DetailViewModel
    private val navigationArgs: BarDetailFragmentArgs by navArgs()

    private var map: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            Injection.provideViewModelFactory(requireContext())
        )[DetailViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailBarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment =
            this.childFragmentManager.findFragmentById(com.example.zadanie.R.id.map_view) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        val x = PreferenceData.getInstance().getUserItem(requireContext())
        if ((x?.uid ?: "").isBlank()) {
            view.findNavController().navigate(com.example.zadanie.R.id.action_to_login)
            return
        }

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            model = viewModel
            //TODO
//            counter.text = navigationArgs.users.toString()
            counter.text = "0"
        }.also { bnd ->
            viewModel.details.observe(viewLifecycleOwner){
                it?.let {
                    if(it.isNotEmpty()) {
                        logItems(it)
                        var isWebsitePresent = false
                        for(item in it){
                            when (item.key) {
                                "website" -> {
                                    bnd.webButton.isEnabled = true
                                    bnd.webButton.setOnClickListener{
                                        val queryUrl: Uri = Uri.parse(item.value)
                                        val intent = Intent(Intent.ACTION_VIEW, queryUrl)
                                        startActivity(intent)
                                    }
                                    isWebsitePresent = true
                                }
                                "phone" -> {
                                    bnd.phoneNumber.isVisible = true
                                    bnd.phoneNumber.text = item.value

                                }
                                "opening_hours" -> {
                                    val lined = item.value.replace("; ", "\n⚬ ")
                                    bnd.hours.text = "⚬ ${lined}"
                                    bnd.openingHours.isVisible = true

                                }
                            }
                        }
                        if(!isWebsitePresent)
                            bnd.webButton.isEnabled = false
                    }else{
                        bnd.webButton.isEnabled = false
                    }
                }
            }

            bnd.mapButton.setOnClickListener {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            "geo:0,0,?q=" +
                                    "${viewModel.bar.value?.lat ?: 0}," +
                                    "${viewModel.bar.value?.lon ?: 0}" +
                                    "(${viewModel.bar.value?.name ?: ""}"
                        )
                    )
                )
            }
        }

        viewModel.bar.observe(viewLifecycleOwner){
            it?.let {
                (requireActivity() as AppCompatActivity).supportActionBar?.title = it.name
                map?.addMarker(
                    MarkerOptions()
                        .title(it.name)
                        .position(LatLng(it.lat, it.lon))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                )
                map?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.lat, it.lon), 15f))
            }
        }

        viewModel.loadBar(navigationArgs.id)
    }

    private fun logItems(items : List<BarDetailItem>){
        for(item in items){
            Log.d("--- ${item.key}", "--- ${item.value}")
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
    }
}