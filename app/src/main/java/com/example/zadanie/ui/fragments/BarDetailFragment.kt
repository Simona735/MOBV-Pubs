package com.example.zadanie.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.example.zadanie.R
import com.example.zadanie.databinding.FragmentDetailBarBinding
import com.example.zadanie.helpers.Injection
import com.example.zadanie.helpers.PreferenceData
import com.example.zadanie.ui.viewmodels.DetailViewModel

class BarDetailFragment : Fragment() {
    private lateinit var binding: FragmentDetailBarBinding

    private lateinit var viewModel: DetailViewModel
    private val navigationArgs: BarDetailFragmentArgs by navArgs()

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

        val x = PreferenceData.getInstance().getUserItem(requireContext())
        if ((x?.uid ?: "").isBlank()) {
            view.findNavController().navigate(R.id.action_to_login)
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
                        var isWebsitePresent = false
                        for(item in it){
                            if(item.key == "website"){
                                bnd.webButton.isEnabled = true
                                bnd.webButton.setOnClickListener{
                                    val queryUrl: Uri = Uri.parse(item.value)
                                    val intent = Intent(Intent.ACTION_VIEW, queryUrl)
                                    startActivity(intent)
                                }
                                isWebsitePresent = true
                                break
                            }
                        }

                        if(!isWebsitePresent)
                            bnd.webButton.isEnabled = false
                    }else{
                        bnd.webButton.isEnabled = false
                    }
                }
            }
            viewModel.details.observe(viewLifecycleOwner){
                it?.let {
                    bnd.phoneNumber.isVisible = false
                    for(item in it) {
                        if(item.key == "phone") {
                            bnd.phoneNumber.isVisible = true
                            bnd.phoneNumber.text = item.value
                            break
                        }
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
            }
        }

        viewModel.loadBar(navigationArgs.id)
    }
}