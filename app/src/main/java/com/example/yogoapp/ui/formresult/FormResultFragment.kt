package com.example.yogoapp.ui.formresult

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.yogoapp.databinding.FragmentFormresultBinding
import com.example.yogoapp.ui.common.initAndLogOnPlay
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
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

        viewLifecycleOwner.lifecycle.addObserver(binding.youtubePlayerViewFR)

        val payload = arguments?.getString("result").orEmpty()
        if (payload.isNotBlank()) {
            viewModel.setUserInput(payload)
        }

        viewModel.videoId.observe(viewLifecycleOwner) { id ->
            if (id.isNullOrBlank()) return@observe
            if (!playerInitialized) initPlayerAndLoad(id) else youTubePlayerRef?.cueVideo(id, 0f)
        }

        binding.buttonNextFr.setOnClickListener { viewModel.next() }
    }

    private fun initPlayerAndLoad(videoId: String) {
        val options = IFramePlayerOptions.Builder(requireContext())
            .controls(1)
            .autoplay(0)
            .build()

        binding.youtubePlayerViewFR.initAndLogOnPlay(
            lifecycleOwner = viewLifecycleOwner,
            initialYoutubeId = videoId,
            loggerScope = viewLifecycleOwner.lifecycleScope,
            appContextProvider = { requireContext().applicationContext },
            options = options,
            cueInsteadOfLoad = true,
            onReady = { player -> youTubePlayerRef = player }
        )

        playerInitialized = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        youTubePlayerRef = null
        _binding = null
    }
}
