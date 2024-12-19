package dev.eduardoroth.mediaplayer;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.CaptionStyleCompat;
import androidx.media3.ui.PlayerView;
import androidx.mediarouter.app.MediaRouteButton;

import com.google.android.gms.cast.framework.CastButtonFactory;

import java.io.FileNotFoundException;

import dev.eduardoroth.mediaplayer.models.AndroidOptions;
import dev.eduardoroth.mediaplayer.models.ExtraOptions;
import dev.eduardoroth.mediaplayer.state.MediaPlayerState;
import dev.eduardoroth.mediaplayer.state.MediaPlayerStateProvider;

@UnstableApi
public class MediaPlayerControllerView extends Fragment {
    public enum MEDIA_PLAYER_VIEW_TYPE {
        FULLSCREEN, EMBEDDED,
    }

    private final MediaPlayerState _mediaPlayerState;
    private final MediaPlayerController _playerController;
    private final AndroidOptions _android;
    private final ExtraOptions _extra;
    private PlayerView playerView;
    private MediaRouteButton castButton;
    private ImageButton fullscreenToggle;
    private Drawable artwork;

    public MediaPlayerControllerView(String playerId) {
        _mediaPlayerState = MediaPlayerStateProvider.getState(playerId);
        _playerController = _mediaPlayerState.playerController.get();
        _android = _mediaPlayerState.androidOptions.get();
        _extra = _mediaPlayerState.extraOptions.get();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        _mediaPlayerState.fullscreenState.observe(state -> {
            switch (state) {
                case ACTIVE -> fullscreenToggle.setImageResource(R.drawable.ic_fullscreen_exit);
                case INACTIVE -> fullscreenToggle.setImageResource(R.drawable.ic_fullscreen_enter);
            }
        });
        _mediaPlayerState.pipState.observe(state -> {
            switch (state) {
                case ACTIVE -> playerView.setUseController(false);
                case INACTIVE -> playerView.setUseController(_extra.showControls);

            }
        });
        _mediaPlayerState.canCast.observe(isCastAvailable -> {
            castButton.setVisibility(isCastAvailable ? View.VISIBLE : View.GONE);
            castButton.setEnabled(isCastAvailable);
        });
        _mediaPlayerState.showSubtitles.observe(showSubtitles -> playerView.setShowSubtitleButton(showSubtitles));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View fragmentView = inflater.inflate(R.layout.media_player_controller_view, container, false);

        playerView = fragmentView.findViewById(R.id.MediaPlayerControllerView);
        playerView.setFocusableInTouchMode(true);

        playerView.findViewById(androidx.media3.ui.R.id.exo_repeat_toggle).setVisibility(View.GONE);
        playerView.findViewById(androidx.media3.ui.R.id.exo_fullscreen).setVisibility(View.GONE);
        playerView.findViewById(androidx.media3.ui.R.id.exo_minimal_fullscreen).setVisibility(View.GONE);
        playerView.findViewById(androidx.media3.ui.R.id.exo_extra_controls_scroll_view).setVisibility(View.VISIBLE);

        LinearLayout basicControls = playerView.findViewById(androidx.media3.ui.R.id.exo_basic_controls);
        View extraControls = inflater.inflate(R.layout.media_player_controller_view_extra_buttons, basicControls, true);

        castButton = extraControls.findViewById(R.id.cast_button);
        if (_android.enableChromecast) {
            CastButtonFactory.setUpMediaRouteButton(requireContext(), castButton);
        }

        ImageButton pipButton = extraControls.findViewById(R.id.pip_button);
        if (_mediaPlayerState.canUsePiP.get()) {
            pipButton.setVisibility(View.VISIBLE);
            pipButton.setOnClickListener(view -> _mediaPlayerState.pipState.set(MediaPlayerState.UI_STATE.WILL_ENTER));
        }

        fullscreenToggle = extraControls.findViewById(R.id.toggle_fullscreen);
        fullscreenToggle.setOnClickListener(view -> {
            switch (_mediaPlayerState.fullscreenState.get()) {
                case ACTIVE ->
                        _mediaPlayerState.fullscreenState.set(MediaPlayerState.UI_STATE.WILL_EXIT);
                case INACTIVE ->
                        _mediaPlayerState.fullscreenState.set(MediaPlayerState.UI_STATE.WILL_ENTER);
            }
        });

        playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS);
        playerView.setControllerAutoShow(_extra.showControls);
        playerView.setControllerHideOnTouch(true);
        playerView.setControllerShowTimeoutMs(2500);

        if (artwork != null) {
            try {
                artwork = Drawable.createFromStream(requireContext().getContentResolver().openInputStream(Uri.parse(_extra.poster)), _extra.poster);
            } catch (FileNotFoundException ignored) {
            }
            playerView.setDefaultArtwork(artwork);
            playerView.setArtworkDisplayMode(PlayerView.ARTWORK_DISPLAY_MODE_FILL);
        }

        playerView.setShowPreviousButton(false);
        playerView.setShowNextButton(false);
        playerView.setUseController(_extra.showControls);
        playerView.setControllerAnimationEnabled(_extra.showControls);
        playerView.setImageDisplayMode(PlayerView.IMAGE_DISPLAY_MODE_FIT);
        playerView.setShowPlayButtonIfPlaybackIsSuppressed(true);

        if (playerView.getSubtitleView() != null && _extra.subtitles != null) {
            playerView.getSubtitleView().setStyle(new CaptionStyleCompat(_extra.subtitles.settings.foregroundColor, _extra.subtitles.settings.backgroundColor, Color.TRANSPARENT, CaptionStyleCompat.EDGE_TYPE_NONE, Color.WHITE, null));
            playerView.getSubtitleView().setFixedTextSize(TypedValue.COMPLEX_UNIT_DIP, _extra.subtitles.settings.fontSize.floatValue());
        }

        playerView.setOnKeyListener((eventContainer, keyCode, keyEvent) -> {
            Player activePlayer = playerView.getPlayer();
            if (activePlayer != null && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                long duration = activePlayer.getDuration();
                long videoPosition = activePlayer.getCurrentPosition();
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        if (videoPosition < duration - MediaPlayer.VIDEO_STEP) {
                            activePlayer.seekTo(videoPosition + MediaPlayer.VIDEO_STEP);
                        }
                        return true;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        if (videoPosition - MediaPlayer.VIDEO_STEP > 0) {
                            activePlayer.seekTo(videoPosition - MediaPlayer.VIDEO_STEP);
                        } else {
                            activePlayer.seekTo(0);
                        }
                        return true;
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                        if (activePlayer.isPlaying()) {
                            activePlayer.pause();
                        } else {
                            activePlayer.play();
                        }
                        return true;
                    case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                        if (videoPosition < duration - (MediaPlayer.VIDEO_STEP * 2)) {
                            activePlayer.seekTo(videoPosition + (MediaPlayer.VIDEO_STEP * 2));
                        }
                        return true;
                    case KeyEvent.KEYCODE_MEDIA_REWIND:
                        if (videoPosition - (MediaPlayer.VIDEO_STEP * 2) > 0) {
                            activePlayer.seekTo(videoPosition - (MediaPlayer.VIDEO_STEP * 2));
                        } else {
                            activePlayer.seekTo(0);
                        }
                        return true;
                }
            }
            return false;
        });

        playerView.setPlayer(_playerController.getActivePlayer());

        _mediaPlayerState.isPlayerReady.set(true);

        return fragmentView;
    }
}
