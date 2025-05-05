package com.example.yogoapp.ui.newworkout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.yogoapp.databinding.FragmentNewworkoutBinding

class NewWorkoutFragment : Fragment() {

    private var _binding: FragmentNewworkoutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val newWorkoutViewModel =
            ViewModelProvider(this).get(NewWorkoutViewModel::class.java)

        _binding = FragmentNewworkoutBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textNewworkout
        newWorkoutViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}