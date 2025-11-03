package com.example.yogoapp.ui.searchbytags

import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.yogoapp.databinding.FragmentSearchbytagsBinding
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class SearchByTagsFragment : Fragment() {
    private var _binding: FragmentSearchbytagsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SearchByTagsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchbytagsBinding.inflate(inflater, container, false)
        val factory = ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        viewModel = ViewModelProvider(this, factory)[SearchByTagsViewModel::class.java]
        setupClickHandlers()
        observeVideos()
        return binding.root
    }

    private fun setupClickHandlers() {
        val container = binding.buttonsContainer
        for (i in 0 until container.childCount) {
            (container.getChildAt(i) as? Button)?.setOnClickListener { v ->
                val tag = (v as Button).text.toString()
                viewModel.onTagClicked(tag)
            }
        }
    }

    private fun observeVideos() {
        viewModel.videoIds.observe(viewLifecycleOwner) { ids ->
            showVideos(ids)
        }
    }

    private fun showVideos(ids: List<String>) {
        val container = binding.videosContainer
        for (i in 0 until container.childCount) {
            (container.getChildAt(i) as? YouTubePlayerView)?.release()
        }
        container.removeAllViews()
        ids.forEach { videoId ->
            val playerView = YouTubePlayerView(requireContext()).apply {
                enableAutomaticInitialization = false
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(20)
                    bottomMargin = dp(10)
                }
            }

            viewLifecycleOwner.lifecycle.addObserver(playerView)
            playerView.initialize(object : AbstractYouTubePlayerListener() {
                override fun onReady(player: YouTubePlayer) {
                    player.cueVideo(videoId, 0f)
                }
            })
            container.addView(playerView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

}