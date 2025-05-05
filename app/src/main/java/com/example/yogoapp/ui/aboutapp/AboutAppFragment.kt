package com.example.yogoapp.ui.aboutapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.yogoapp.databinding.FragmentAboutappBinding

class AboutAppFragment : Fragment() {

    private var _binding: FragmentAboutappBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val aboutAppViewModel =
            ViewModelProvider(this).get(AboutAppViewModel::class.java)

        _binding = FragmentAboutappBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textAboutapp
        aboutAppViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
