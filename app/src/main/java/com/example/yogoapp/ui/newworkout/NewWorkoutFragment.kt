package com.example.yogoapp.ui.newworkout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewworkoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.result.observe(viewLifecycleOwner) { payload ->
            val bundle = Bundle().apply { putString("result", payload) }
            view.findNavController().navigate(R.id.action_newWorkout_to_formResult, bundle)
        }

        binding.buttonSubmit.setOnClickListener {
            val state = FormState(
                fitness   = binding.rgFitness.checkedTagOrNull(),
                health    = binding.rgHealth.checkedTagOrNull(),
                mainGoal  = binding.rgMainGoal.checkedTagOrNull(),
                duration  = binding.rgDuration.checkedTagOrNull(),
                energy    = binding.rgEnergy.checkedTagOrNull(),
                props     = binding.rgProps.checkedTagOrNull(),
                yoga      = binding.rgYoga.checkedTagOrNull()
            )
            viewModel.submit(state)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
