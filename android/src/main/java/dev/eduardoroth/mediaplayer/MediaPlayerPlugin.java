package dev.eduardoroth.mediaplayer;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import android.util.DisplayMetrics;
import android.util.Log;

import org.json.JSONException;

import dev.eduardoroth.mediaplayer.models.AndroidOptions;
import dev.eduardoroth.mediaplayer.models.ExtraOptions;
import dev.eduardoroth.mediaplayer.models.SubtitleOptions;

@CapacitorPlugin(name = "MediaPlayer")
public class MediaPlayerPlugin extends Plugin {
    private MediaPlayer implementation;

    @Override
    public void load() {
        bridge.getActivity().getSupportFragmentManager();
        implementation = new MediaPlayer(bridge.getActivity());
        MediaPlayerNotificationCenter.init(bridge.getActivity());
        MediaPlayerNotificationCenter.listenNotifications(nextNotification -> notifyListeners(nextNotification.getEventName(), nextNotification.getData()));
    }

    @PluginMethod
    public void create(final PluginCall call) {
        String playerId = call.getString("playerId");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "create");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        String url = call.getString("url");
        if (url == null) {
            JSObject ret = new JSObject();
            ret.put("method", "create");
            ret.put("result", false);
            ret.put("message", "Must provide a URL");
            call.resolve(ret);
            return;
        }

        JSObject androidOptions = call.getObject("android");
        JSObject extraOptions = call.getObject("extra");
        JSObject subtitleOptions = extraOptions != null ? extraOptions.getJSObject("subtitles") : null;

        DisplayMetrics metrics = bridge.getContext().getResources().getDisplayMetrics();

        Integer paramTop = androidOptions != null ? androidOptions.getInteger("top", null) : null;
        Integer paramStart = androidOptions != null ? androidOptions.getInteger("start", null) : null;
        Integer paramWidth = androidOptions != null ? androidOptions.getInteger("width", null) : null;
        Integer paramHeight = androidOptions != null ? androidOptions.getInteger("height", null) : null;

        int marginTop = paramTop == null ? 0 : (int) (paramTop * metrics.density);
        int marginStart = paramStart == null ? 0 : (int) (paramStart * metrics.density);
        int videoWidth = paramWidth == null ? (metrics.widthPixels - (marginStart * 2)) : (int) (paramWidth * metrics.density);
        int videoHeight = paramHeight == null ? (videoWidth * 9 / 16) : (int) (paramHeight * metrics.density);

        AndroidOptions android = new AndroidOptions(
                androidOptions == null || androidOptions.optBoolean("enableChromecast", true),
                androidOptions == null || androidOptions.optBoolean("enablePiP", true),
                androidOptions == null || androidOptions.optBoolean("enableBackgroundPlay", true),
                androidOptions != null && androidOptions.optBoolean("openInFullscreen", false),
                androidOptions != null && androidOptions.optBoolean("automaticallyEnterPiP", false),
                androidOptions == null || androidOptions.optBoolean("fullscreenOnLandscape", true),
                androidOptions == null || androidOptions.optBoolean("stopOnTaskRemoved", false),
                marginTop,
                marginStart,
                videoWidth,
                videoHeight);

        SubtitleOptions subtitles = null;
        if (subtitleOptions != null) {
            double fontSize = Double.parseDouble("12");
            try {
                fontSize = subtitleOptions.getDouble("fontSize");
            } catch (NullPointerException | JSONException ignored) {
            }
            subtitles = new SubtitleOptions(subtitleOptions.getString("url", null), subtitleOptions.getString("language", "English"), subtitleOptions.getString("foregroundColor", null), subtitleOptions.getString("backgroundColor", null), fontSize);
        }

        double rate = 1;
        try {
            rate = extraOptions.getDouble("rate");
        } catch (NullPointerException | JSONException ignored) {
        }

        ExtraOptions extra = new ExtraOptions(
                extraOptions != null ? extraOptions.getString("title") : null,
                extraOptions != null ? extraOptions.getString("subtitle") : null,
                extraOptions != null ? extraOptions.getString("poster", null) : null,
                extraOptions != null ? extraOptions.getString("artist", null) : null,
                rate,
                subtitles,
                extraOptions != null && extraOptions.optBoolean("autoPlayWhenReady", false),
                extraOptions != null && extraOptions.optBoolean("loopOnEnd", false),
                extraOptions == null || extraOptions.optBoolean("showControls", true),
                extraOptions != null ? extraOptions.getJSObject("headers") : null);
        bridge.getActivity().runOnUiThread(() -> implementation.create(call, playerId, url, android, extra));
    }

    @PluginMethod
    public void play(final PluginCall call) {
        String playerId = call.getString("playerId");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "play");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        bridge.getActivity().runOnUiThread(() -> implementation.play(call, playerId));
    }

    @PluginMethod
    public void pause(final PluginCall call) {
        String playerId = call.getString("playerId");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "pause");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        bridge.getActivity().runOnUiThread(() -> implementation.pause(call, playerId));
    }

    @PluginMethod
    public void getDuration(final PluginCall call) {
        String playerId = call.getString("playerId");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "getDuration");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        bridge.getActivity().runOnUiThread(() -> implementation.getDuration(call, playerId));
    }

    @PluginMethod
    public void getCurrentTime(final PluginCall call) {
        String playerId = call.getString("playerId");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "getCurrentTime");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        bridge.getActivity().runOnUiThread(() -> implementation.getCurrentTime(call, playerId));
    }

    @PluginMethod
    public void setCurrentTime(final PluginCall call) {
        String playerId = call.getString("playerId");
        Double time = call.getDouble("time");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "setCurrentTime");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        if (time == null) {
            JSObject ret = new JSObject();
            ret.put("method", "setCurrentTime");
            ret.put("result", false);
            ret.put("message", "Must provide a time");
            call.resolve(ret);
            return;
        }
        bridge.getActivity().runOnUiThread(() -> implementation.setCurrentTime(call, playerId, time.longValue()));
    }

    @PluginMethod
    public void isPlaying(final PluginCall call) {
        String playerId = call.getString("playerId");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "isPlaying");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        bridge.getActivity().runOnUiThread(() -> implementation.isPlaying(call, playerId));
    }

    @PluginMethod
    public void isMuted(final PluginCall call) {
        String playerId = call.getString("playerId");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "isMuted");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        bridge.getActivity().runOnUiThread(() -> implementation.isMuted(call, playerId));
    }

    @PluginMethod
    public void setVisibilityBackgroundForPiP(final PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("method", "setVisibilityBackgroundForPiP");
        ret.put("result", false);
        ret.put("message", "Method setVisibilityBackgroundForPiP not implemented for Android");
        call.resolve(ret);
    }

    @PluginMethod
    public void mute(final PluginCall call) {
        String playerId = call.getString("playerId");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "mute");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        bridge.getActivity().runOnUiThread(() -> implementation.mute(call, playerId));
    }

    @PluginMethod
    public void getVolume(final PluginCall call) {
        String playerId = call.getString("playerId");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "getVolume");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        bridge.getActivity().runOnUiThread(() -> implementation.getVolume(call, playerId));
    }

    @PluginMethod
    public void setVolume(final PluginCall call) {
        String playerId = call.getString("playerId");
        Double volume = call.getDouble("volume");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "setVolume");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        if (volume == null) {
            JSObject ret = new JSObject();
            ret.put("method", "setVolume");
            ret.put("result", false);
            ret.put("message", "Must provide a volume");
            call.resolve(ret);
            return;
        }
        bridge.getActivity().runOnUiThread(() -> implementation.setVolume(call, playerId, volume));
    }

    @PluginMethod
    public void getRate(final PluginCall call) {
        String playerId = call.getString("playerId");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "getRate");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        bridge.getActivity().runOnUiThread(() -> implementation.getRate(call, playerId));
    }

    @PluginMethod
    public void setRate(final PluginCall call) {
        String playerId = call.getString("playerId");
        Double rate = call.getDouble("rate");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "setRate");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        if (rate == null) {
            JSObject ret = new JSObject();
            ret.put("method", "setRate");
            ret.put("result", false);
            ret.put("message", "Must provide a rate");
            call.resolve(ret);
            return;
        }
        bridge.getActivity().runOnUiThread(() -> implementation.setRate(call, playerId, rate));
    }

    @PluginMethod
    public void remove(final PluginCall call) {
        String playerId = call.getString("playerId");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "remove");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        bridge.getActivity().runOnUiThread(() -> implementation.remove(call, playerId));
    }

    @PluginMethod
    public void removeAll(final PluginCall call) {
        bridge.getActivity().runOnUiThread(() -> implementation.removeAll(call));
    }

}
