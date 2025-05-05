package com.example.yogoapp.ui.privacypolicy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.yogoapp.databinding.FragmentPrivacypolicyBinding

class PrivacyPolicyFragment : Fragment() {

    private var _binding: FragmentPrivacypolicyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val privacyPolicyViewModel =
            ViewModelProvider(this).get(PrivacyPolicyViewModel::class.java)

        _binding = FragmentPrivacypolicyBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textPrivacypolicy
        privacyPolicyViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}