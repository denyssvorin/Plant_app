package com.example.recycleview.ui.edit

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.recycleview.R
import com.example.recycleview.data.Plant
import com.example.recycleview.databinding.FragmentEditPlantBinding
import com.example.recycleview.ui.home.HomeFragmentDirections
import com.example.recycleview.ui.home.HomeViewModel
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditPlantFragment : Fragment() {
    private var _binding: FragmentEditPlantBinding? = null

    // This property below is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private val viewModel by viewModels<EditPlantViewModel>()
    private lateinit var imageUri: Uri


    private val pickImageRequest = registerForActivityResult(
        ActivityResultContracts.GetContent()) { uri ->
        uri?.let { imageUri ->

            this.imageUri = imageUri

            Picasso.with(requireContext())
                .load(imageUri)
                .error(R.drawable.ic_error_24)
                .into(binding.imageViewPlant)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditPlantBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.editPlantEvent.collect() { event ->
                when (event) {
                    EditPlantViewModel.EditPlantEvent.NavigateToHomeScreen -> {
                        val action = EditPlantFragmentDirections.actionEditFragmentToHomeFragment()
                        findNavController().navigate(action)
                    }
                }
            }
        }
    }

    private fun initUI() {
        binding.apply {
            imageButtonPickFromGallery.setOnClickListener {
                onFileChoose()
            }
            fab.setOnClickListener {
                val editTextTitle = editTextPlantTitle.text.toString()
                if (editTextTitle.isNotBlank()) {
                    val editTextDescription = editTextPlantDescription.text.toString()
                    val plant = Plant(
                        plantImagePath = imageUri.toString(),
                        plantName = editTextTitle,
                        plantDescription = editTextDescription
                    )
                    viewModel.savePlant(plant)
                } else {
                    Snackbar.make(it, "Plant name must be filled", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun onFileChoose() {
        pickImageRequest.launch("image/*")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}