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
        viewModel = ViewModelProvider(this)[ForYouViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // lifecycle pod WI­DOK (ważne, żeby nie wyciekało po zniszczeniu widoku)
        viewLifecycleOwner.lifecycle.addObserver(binding.youtubePlayerView)

        // Obserwuj videoId z ViewModelu
        viewModel.videoId.observe(viewLifecycleOwner) { id ->
            if (id.isNullOrBlank()) return@observe

            if (!playerInitialized) {
                initPlayerAndLoad(id)
            } else {
                // player już jest – załaduj nowe wideo
                youTubePlayerRef?.loadVideo(id, 0f)
            }
        }
    }

    private fun initPlayerAndLoad(videoId: String) {
        binding.youtubePlayerView.enableAutomaticInitialization = false

        val options = IFramePlayerOptions.Builder()
            .controls(1)
            .autoplay(1)
            .build()

        binding.youtubePlayerView.initialize(object : AbstractYouTubePlayerListener() {
            override fun onReady(player: YouTubePlayer) {
                youTubePlayerRef = player
                player.mute()                // autoplay na mobile działa, pewniej na mute
                player.loadVideo(videoId, 0f)
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
