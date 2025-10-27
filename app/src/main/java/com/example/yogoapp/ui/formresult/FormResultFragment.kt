package com.example.yogoapp.ui.formresult

import com.example.yogoapp.ui.formresult.FormResultViewModel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.yogoapp.databinding.FragmentFormresultBinding
import com.example.yogoapp.databinding.FragmentForyouBinding
import com.example.yogoapp.ui.foryou.ForYouViewModel
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions

class FormResultFragment : Fragment() {

    private var _binding: FragmentFormresultBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: FormResultViewModel

    private var playerInitialized = false
    private var youTubePlayerRef: YouTubePlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFormresultBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[FormResultViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // lifecycle pod WI­DOK (ważne, żeby nie wyciekało po zniszczeniu widoku)
        viewLifecycleOwner.lifecycle.addObserver(binding.youtubePlayerViewFR)

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
        binding.youtubePlayerViewFR.enableAutomaticInitialization = false

        val options = IFramePlayerOptions.Builder(requireContext())
            .controls(1)
            .autoplay(1)
            .build()

        binding.youtubePlayerViewFR.initialize(object : AbstractYouTubePlayerListener() {
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