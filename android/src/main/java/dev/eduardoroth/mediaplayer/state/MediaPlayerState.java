package dev.eduardoroth.mediaplayer.state;

import android.graphics.Rect;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModel;
import androidx.media3.session.MediaController;

import dev.eduardoroth.mediaplayer.models.AndroidOptions;
import dev.eduardoroth.mediaplayer.models.ExtraOptions;
import dev.eduardoroth.mediaplayer.models.PlacementOptions;

public class MediaPlayerState extends ViewModel {

    public enum UI_STATE {
        WILL_ENTER,
        ACTIVE,
        WILL_EXIT,
        INACTIVE,
    }

    public final MediaPlayerStateProperty<Boolean> canUsePiP;
    public final MediaPlayerStateProperty<Boolean> canCast;

    public final MediaPlayerStateProperty<UI_STATE> pipState;
    public final MediaPlayerStateProperty<UI_STATE> backgroundState;
    public final MediaPlayerStateProperty<UI_STATE> fullscreenState;
    public final MediaPlayerStateProperty<UI_STATE> landscapeState;
    public final MediaPlayerStateProperty<UI_STATE> castingState;

    public final MediaPlayerStateProperty<Rect> sourceRectHint;
    public final MediaPlayerStateProperty<String> currentMediaItemId;
    public final MediaPlayerStateProperty<Boolean> showSubtitles;
    public final MediaPlayerStateProperty<Long> getCurrentTime;
    public final MediaPlayerStateProperty<Long> getDuration;

    public final MediaPlayerStateProperty<MediaController> mediaController;
    public final MediaPlayerStateProperty<PlacementOptions> placementOptions;
    public final MediaPlayerStateProperty<AndroidOptions> androidOptions;
    public final MediaPlayerStateProperty<ExtraOptions> extraOptions;

    public MediaPlayerState(LifecycleOwner owner) {
        canUsePiP = new MediaPlayerStateProperty<>(owner, false, true);
        canCast = new MediaPlayerStateProperty<>(owner, false);

        pipState = new MediaPlayerStateProperty<>(owner, UI_STATE.INACTIVE);
        backgroundState = new MediaPlayerStateProperty<>(owner, UI_STATE.INACTIVE);
        fullscreenState = new MediaPlayerStateProperty<>(owner, UI_STATE.INACTIVE);
        landscapeState = new MediaPlayerStateProperty<>(owner, UI_STATE.INACTIVE);
        castingState = new MediaPlayerStateProperty<>(owner, UI_STATE.INACTIVE);

        sourceRectHint = new MediaPlayerStateProperty<>(owner);
        currentMediaItemId = new MediaPlayerStateProperty<>(owner);
        showSubtitles = new MediaPlayerStateProperty<>(owner, false);
        getCurrentTime = new MediaPlayerStateProperty<>(owner, 0L);
        getDuration = new MediaPlayerStateProperty<>(owner, 0L);

        mediaController = new MediaPlayerStateProperty<>(owner, null, true);
        placementOptions = new MediaPlayerStateProperty<>(owner, null, true);
        androidOptions = new MediaPlayerStateProperty<>(owner, null, true);
        extraOptions = new MediaPlayerStateProperty<>(owner, null, true);
    }

}

