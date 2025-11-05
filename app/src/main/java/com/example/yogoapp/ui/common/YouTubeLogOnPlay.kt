package com.example.yogoapp.ui.common

import androidx.lifecycle.LifecycleOwner
import com.example.yogoapp.data.HistoryRepository
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants // v13
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun YouTubePlayerView.initAndLogOnPlay(
    lifecycleOwner: LifecycleOwner,
    initialYoutubeId: String,
    loggerScope: CoroutineScope,
    appContextProvider: () -> android.content.Context,
    options: IFramePlayerOptions? = null,
    cueInsteadOfLoad: Boolean = true,
    onReady: ((YouTubePlayer) -> Unit)? = null,
    currentIdProvider: (() -> String)? = null
) {
    enableAutomaticInitialization = false
    lifecycleOwner.lifecycle.addObserver(this)

    var lastLoggedId: String? = null

    val listener = object : AbstractYouTubePlayerListener() {
        override fun onReady(player: YouTubePlayer) {
            onReady?.invoke(player)
            if (cueInsteadOfLoad) player.cueVideo(initialYoutubeId, 0f) else player.loadVideo(initialYoutubeId, 0f)
        }

        override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
            if (state == PlayerConstants.PlayerState.PLAYING) {
                val currentId = currentIdProvider?.invoke() ?: initialYoutubeId
                if (currentId.isNotBlank() && currentId != lastLoggedId) {
                    lastLoggedId = currentId
                    loggerScope.launch {
                        HistoryRepository.logPlayback(appContextProvider(), currentId)
                    }
                }
            }
        }
    }

    if (options != null) initialize(listener, options) else initialize(listener)
}
