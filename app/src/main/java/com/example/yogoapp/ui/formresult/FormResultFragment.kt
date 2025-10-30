package com.example.yogoapp.ui.formresult

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.yogoapp.databinding.FragmentFormresultBinding
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
        val factory = ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        viewModel = ViewModelProvider(this, factory)[FormResultViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // lifecycle pod widok
        viewLifecycleOwner.lifecycle.addObserver(binding.youtubePlayerViewFR)

        // odbierz payload z bundla (przyszedł z NewWorkoutFragment)
        val payload = arguments?.getString("result").orEmpty()
        if (payload.isNotBlank()) {
            viewModel.setUserInput(payload)
        }

        // obserwuj aktualne videoId
        viewModel.videoId.observe(viewLifecycleOwner) { id ->
            if (id.isNullOrBlank()) return@observe
            if (!playerInitialized) initPlayerAndLoad(id) else youTubePlayerRef?.cueVideo(id, 0f)
        }

        // next -> kolejny dopasowany url z listy
        binding.buttonNextFr.setOnClickListener { viewModel.next() }
    }

    private fun initPlayerAndLoad(videoId: String) {
        binding.youtubePlayerViewFR.enableAutomaticInitialization = false

        val options = IFramePlayerOptions.Builder(requireContext())
            .controls(1)
            .autoplay(0) // ważne: brak autoplay
            .build()

        binding.youtubePlayerViewFR.initialize(object : AbstractYouTubePlayerListener() {
            override fun onReady(player: YouTubePlayer) {
                youTubePlayerRef = player
                player.cueVideo(videoId, 0f) // miniatura + ▶️, bez autoplay
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
