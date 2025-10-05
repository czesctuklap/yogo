package com.example.yogoapp.ui.newworkout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.yogoapp.R
import androidx.navigation.fragment.findNavController
import com.example.yogoapp.databinding.FragmentNewworkoutBinding
import com.example.yogoapp.ui.formresult.FormResultFragment

class NewWorkoutFragment : Fragment(R.layout.fragment_newworkout) {

    private var _binding: FragmentNewworkoutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewworkoutBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSubmit.setOnClickListener {
            it.findNavController().navigate(R.id.action_newWorkout_to_formResult)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
