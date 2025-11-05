package com.example.yogoapp.ui.myhistory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.yogoapp.databinding.FragmentMyhistoryBinding
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class MyHistoryFragment : Fragment() {

    private var _binding: FragmentMyhistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MyHistoryViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyhistoryBinding.inflate(inflater, container, false)

        val factory = ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        viewModel = ViewModelProvider(this, factory)[MyHistoryViewModel::class.java]

        observeUi()
        setupMoreButton()

        return binding.root
    }

    private fun observeUi() {
        viewModel.videoIds.observe(viewLifecycleOwner) { ids ->
            showVideos(ids)
        }
        viewModel.hasMore.observe(viewLifecycleOwner) { hasMore ->
            binding.buttonMore.isEnabled = true
            binding.buttonMore.text = if (hasMore) "more" else "back"
        }
    }

    private fun setupMoreButton() {
        binding.buttonMore.setOnClickListener {
            val hasMore = viewModel.hasMore.value == true
            if (hasMore) {
                viewModel.loadNext()
            } else {
                viewModel.loadInitial()
                binding.videosScroll.post { binding.videosScroll.scrollTo(0, 0) }
            }
        }
    }


    private fun showVideos(ids: List<String>) {
        val container = binding.videosContainer

        for (i in container.childCount - 1 downTo 0) {
            (container.getChildAt(i) as? YouTubePlayerView)?.let {
                it.release()
                container.removeViewAt(i)
            }
        }

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
                override fun onReady(player: YouTubePlayer) { player.cueVideo(videoId, 0f) }
            })

            val btnIndexNow = container.indexOfChild(binding.buttonMore)
            container.addView(playerView, btnIndexNow)
        }
    }


    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        val container = _binding?.videosContainer
        if (container != null) {
            for (i in 0 until container.childCount) {
                (container.getChildAt(i) as? YouTubePlayerView)?.release()
            }
            container.removeAllViews()
        }
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadInitial()
        binding.videosScroll.post { binding.videosScroll.scrollTo(0, 0) }
    }
}
