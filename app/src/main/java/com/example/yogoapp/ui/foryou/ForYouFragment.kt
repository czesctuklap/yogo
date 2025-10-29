package com.example.yogoapp.ui.foryou

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.yogoapp.databinding.FragmentForyouBinding
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions

class ForYouFragment : Fragment() {

    private var _binding: FragmentForyouBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ForYouViewModel

    private var playerInitialized = false
    private var youTubePlayerRef: YouTubePlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForyouBinding.inflate(inflater, container, false)

        val factory = ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        viewModel = ViewModelProvider(this, factory)[ForYouViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycle.addObserver(binding.youtubePlayerView)

        viewModel.videoId.observe(viewLifecycleOwner) { id ->
            if (id.isNullOrBlank()) return@observe

            if (!playerInitialized) {
                initPlayerAndLoad(id)
            } else {
                youTubePlayerRef?.loadVideo(id, 0f)
            }
        }

        binding.buttonNext.setOnClickListener { viewModel.loadRecommendation() }

    }

    private fun initPlayerAndLoad(videoId: String) {
        binding.youtubePlayerView.enableAutomaticInitialization = false

        val options = IFramePlayerOptions.Builder(requireContext())
            .controls(1)
            .autoplay(0)
            .build()

        binding.youtubePlayerView.initialize(object : AbstractYouTubePlayerListener() {
            override fun onReady(player: YouTubePlayer) {
                youTubePlayerRef = player
                player.mute()
                player.cueVideo(videoId, 0f)
            }
        }, options)

        playerInitialized = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        youTubePlayerRef = null
        _binding = null
    }
}
