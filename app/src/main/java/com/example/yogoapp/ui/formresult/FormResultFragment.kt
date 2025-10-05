package com.example.yogoapp.ui.formresult

import com.example.yogoapp.ui.formresult.FormResultViewModel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.yogoapp.databinding.FragmentFormresultBinding

class FormResultFragment : Fragment() {

    private var _binding: FragmentFormresultBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val formResultViewModel =
            ViewModelProvider(this).get(FormResultViewModel::class.java)

        _binding = FragmentFormresultBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val textView: TextView = binding.textForyou
//        forYouViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}