package com.example.recycleview.ui.home

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.recycleview.R
import com.example.recycleview.data.Plant
import com.example.recycleview.databinding.FragmentHomeBinding
import com.example.recycleview.ui.SharedTitleViewModel
import com.example.recycleview.utils.onQueryTextChanged
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(), PlantAdapter.OnPlantClickListener {

    private var _binding: FragmentHomeBinding? = null

    // This property below is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private val viewModel by viewModels<HomeViewModel>()

    private val plantAdapter: PlantAdapter by lazy {
        PlantAdapter(
            this,
            context?.applicationContext as Application
        )
    }

    private lateinit var searchView: SearchView
    private lateinit var searchViewIcon: MenuItem

    private var readPermissionGranted = false
    private var writePermissionGranted = false

    private lateinit var permissionsLauncher: ActivityResultLauncher<Array<String>>

    private lateinit var contentObserver: ContentObserver


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        setupToolbar()
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()
        initContentObserver()

        permissionsLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                readPermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissions[Manifest.permission.READ_MEDIA_IMAGES] ?: readPermissionGranted
                } else {
                    permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: readPermissionGranted
                }
                writePermissionGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE]
                    ?: writePermissionGranted

                if (readPermissionGranted) {
                    initRecView()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Can't read files without permission.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        updateOrRequestPermissions()
        initRecView()
    }

    private fun initContentObserver() {
        contentObserver = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                if (readPermissionGranted) {
                    initRecView()
                }
            }
        }
        requireContext().contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            contentObserver
        )
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun updateOrRequestPermissions() {

        val hasReadPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }

        val hasWritePermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        readPermissionGranted = hasReadPermission
        writePermissionGranted = hasWritePermission || minSdk29

        val permissionsToRequest = mutableListOf<String>()
        if (!writePermissionGranted) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (!readPermissionGranted) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
        }
        if (permissionsToRequest.isNotEmpty()) {
            permissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun initUI() {
        binding.apply {

            initRecView()

            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.plantPagingFlow.collect { plantPagingData ->
                    plantAdapter.submitData(plantPagingData)
                }
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.plantEvent.collect() { event ->
                    when (event) {
                        is HomeViewModel.HomeEvent.NavigateToDetailsScreen -> {
                            val action = HomeFragmentDirections.actionHomeFragmentToDetailsFragment(
                                event.plant.plantId
                            )
                            val sharedTitleViewModel = ViewModelProvider(requireActivity()).get(
                                SharedTitleViewModel::class.java
                            )
                            sharedTitleViewModel.title.value = event.plant.plantName

                            findNavController().navigate(action)
                        }

                        is HomeViewModel.HomeEvent.NavigateToEditScreen -> {
                            val action =
                                HomeFragmentDirections.actionHomeFragmentToEditFragment(
                                    Plant(
                                        plantImagePath = "empty"
                                    )
                                )
                            findNavController().navigate(action)
                        }
                    }
                }
            }
            fab.setOnClickListener {
                addNewPlant()
            }
        }
    }

    private fun setupToolbar() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {

            override fun onPrepareMenu(menu: Menu) {
                super.onPrepareMenu(menu)

                searchViewIcon = menu.findItem(R.id.action_search)
                searchViewIcon.isVisible = true
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.toolbar, menu)

                searchViewIcon = menu.findItem(R.id.action_search)
                searchView = searchViewIcon.actionView as SearchView

                val pendingQuery = viewModel.searchQuery.value
                if (pendingQuery != null) {
                    if (pendingQuery.isNotEmpty()) {
                        searchViewIcon.expandActionView()
                        searchView.setQuery(pendingQuery, false)
                    }
                }
                searchView.onQueryTextChanged {
                    viewModel.searchQuery.value = it
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean = false

        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun initRecView() {
        binding.rcView.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = plantAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPlantClick(plant: Plant) {
        viewModel.onPlantSelected(plant)
    }

    private fun addNewPlant() {
        viewModel.onAddNewPlant()
    }
}