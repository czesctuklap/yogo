package com.example.yogoapp.ui.searchbytags

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.yogoapp.databinding.FragmentSearchbytagsBinding

class SearchByTagsFragment : Fragment() {

    private var _binding: FragmentSearchbytagsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val searchByTagsViewModel =
            ViewModelProvider(this).get(SearchByTagsViewModel::class.java)

        _binding = FragmentSearchbytagsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textSearchbytags
        searchByTagsViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}