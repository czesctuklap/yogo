package com.example.yogoapp.ui.newworkout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.example.yogoapp.R
import com.example.yogoapp.databinding.FragmentNewworkoutBinding

class NewWorkoutFragment : Fragment(R.layout.fragment_newworkout) {

    private var _binding: FragmentNewworkoutBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NewWorkoutViewModel by viewModels()

    private fun RadioGroup.checkedTagOrNull(): String? {
        val v = findViewById<View>(checkedRadioButtonId)
        return v?.tag?.toString()
    }

    private fun bindGroup(rg: RadioGroup, q: Question) {
        viewModel.onOptionSelected(q, rg.checkedTagOrNull())

        rg.setOnCheckedChangeListener { group, _ ->
            viewModel.onOptionSelected(q, group.checkedTagOrNull())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewworkoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindGroup(binding.rgFitness,    Question.FITNESS)
        bindGroup(binding.rgHealth,     Question.HEALTH)
        bindGroup(binding.rgMainGoal,   Question.MAIN_GOAL)
        bindGroup(binding.rgDuration,   Question.DURATION)
        bindGroup(binding.rgEnergy,     Question.ENERGY)
        bindGroup(binding.rgProps,      Question.PROPS)
        bindGroup(binding.rgYoga,       Question.YOGA)

        viewModel.isFormValid.observe(viewLifecycleOwner) { valid ->
                binding.buttonSubmit.isEnabled = valid
                binding.buttonSubmit.alpha = if (valid) 1f else 0.25f
        }

        viewModel.result.observe(viewLifecycleOwner) { payload ->
            val bundle = Bundle().apply { putString("result", payload) }
            view.findNavController().navigate(R.id.action_newWorkout_to_formResult, bundle)
        }

        binding.buttonSubmit.setOnClickListener {
            val produced = viewModel.submitIfValid()
            if (produced == null) {
                Toast.makeText(requireContext(), "Please answer every question", Toast.LENGTH_SHORT).show() // asleep
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
