package com.example.yogoapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.yogoapp.R
import com.example.yogoapp.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonNewworkout.setOnClickListener {
            findNavController().navigate(R.id.nav_newworkout)
        }

        binding.buttonSearchbytags.setOnClickListener {
            findNavController().navigate(R.id.nav_searchbytags)
        }

        binding.buttonForyou.setOnClickListener {
            findNavController().navigate(R.id.nav_foryou)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
