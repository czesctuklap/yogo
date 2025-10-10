package com.example.yogoapp.ui.searchbytags

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.yogoapp.databinding.FragmentSearchbytagsBinding

// <--- IMPORTY do YouTube playera:
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener

class SearchByTagsFragment : Fragment() {

    private var _binding: FragmentSearchbytagsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SearchByTagsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchbytagsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(SearchByTagsViewModel::class.java)
        setupClickHandlers()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupYouTubePlayers()  // <-- dopiero tutaj
    }

    private fun setupClickHandlers() {
        val container = binding.buttonsContainer
        for (i in 0 until container.childCount) {
            (container.getChildAt(i) as? Button)?.setOnClickListener { v ->
                val labelWithHash = (v as Button).text.toString()
                viewModel.onTagClicked(labelWithHash)
            }
        }
    }

    private fun setupYouTubePlayers() {
        val playerViews = listOf(
            binding.youtubePlayerView1,
            binding.youtubePlayerView2,
            binding.youtubePlayerView3,
            binding.youtubePlayerView4
        )

        val ids = viewModel.videoIds  // ["AWM5ZNdWlqw", ...]

        playerViews.forEachIndexed { index, view ->
            // 1) lifecycle POD WIDOK
            viewLifecycleOwner.lifecycle.addObserver(view)

            // 2) ręczna inicjalizacja
            view.enableAutomaticInitialization = false

            view.initialize(object : AbstractYouTubePlayerListener() {
                override fun onReady(player: YouTubePlayer) {
                    val id = ids.getOrNull(index) ?: return
                    // miniatura / stan „gotowy” bez autoplay:
                    player.cueVideo(id, 0f)
                    // jeśli ma się od razu odtwarzać:
                    // player.loadVideo(id, 0f)
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


