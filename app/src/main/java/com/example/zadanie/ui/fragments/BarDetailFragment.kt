package com.example.zadanie.ui.fragments

import android.content.Context
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
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.example.zadanie.R
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
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup


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
            this.childFragmentManager.findFragmentById(R.id.map_view) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        val x = PreferenceData.getInstance().getUserItem(requireContext())
        if ((x?.uid ?: "").isBlank()) {
            view.findNavController().navigate(R.id.action_to_login)
            return
        }

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            model = viewModel
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
                                    bnd.phoneNumber.text = item.value
                                    bnd.phoneBlock.isVisible = true

                                }
                                "opening_hours" -> {
                                    val lined = item.value.replace("; ", "\n⚬ ")
                                    bnd.hours.text = "⚬ ${lined}"
                                    bnd.openingHours.isVisible = true

                                }
                                "cuisine" -> {
                                    val tags = item.value.split(";")
                                    if (bnd.tagsGroup.childCount == 1){
                                        for (tag in tags){
                                            bnd.tagsGroup.addChip(requireContext(), tag.replace("_", " "))
                                        }
                                    }
                                }
                                "internet_access" -> {
                                    if (bnd.iconGroup.isGone) bnd.iconGroup.isVisible = true
                                    if(item.value == "wlan" || item.value == "yes"){
                                        bnd.wifiOn.isVisible = true
                                    }
                                    if(item.value == "no"){
                                        bnd.wifiOff.isVisible = false
                                    }
                                }
                                "smoking" -> {
                                    if (bnd.iconGroup.isGone) bnd.iconGroup.isVisible = true
                                    if(item.value == "yes"){
                                        bnd.smokingAllowed.isVisible = true
                                    }
                                    if(item.value == "no"){
                                        bnd.smokingNotAllowed.isVisible = false
                                    }

                                }
                                "wheelchair" -> {
                                    if (bnd.iconGroup.isGone) bnd.iconGroup.isVisible = true
                                    if(item.value == "yes"){
                                        bnd.accessible.isVisible = true
                                    }
                                    if(item.value == "no"){
                                        bnd.notAccessible.isVisible = false
                                    }

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
            viewModel.bars.observe(viewLifecycleOwner){
                it?.let {
                    if(it.isNotEmpty()) {
                        bnd.counter.text = it[0].users.toString()
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

        viewModel.setId(navigationArgs.id)
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

    private fun ChipGroup.addChip(context: Context, label: String){
        Chip(context).apply {
            id = View.generateViewId()
            text = label
            addView(this)
        }
    }

}