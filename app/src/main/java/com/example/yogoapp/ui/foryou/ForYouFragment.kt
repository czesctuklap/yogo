package com.example.yogoapp.ui.foryou

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.yogoapp.databinding.FragmentForyouBinding

class ForYouFragment : Fragment() {

    private var _binding: FragmentForyouBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val forYouViewModel =
            ViewModelProvider(this).get(ForYouViewModel::class.java)

        _binding = FragmentForyouBinding.inflate(inflater, container, false)
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