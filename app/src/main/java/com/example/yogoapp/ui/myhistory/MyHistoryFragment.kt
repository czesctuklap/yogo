package com.example.yogoapp.ui.myhistory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.yogoapp.databinding.FragmentMyhistoryBinding

class MyHistoryFragment : Fragment() {

    private var _binding: FragmentMyhistoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val myHistoryViewModel =
            ViewModelProvider(this).get(MyHistoryViewModel::class.java)

        _binding = FragmentMyhistoryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textMyhistory
        myHistoryViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}