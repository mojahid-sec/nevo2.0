package com.example.alarm

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.content.res.Resources
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.data.model.AlarmSound

/**
 * Manages offline audio playback for both UI sound previews and active alarm ringing.
 */
object AlarmSoundPlayer {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var previewPlayer: MediaPlayer? = null
    private var previewAudioManager: AudioManager? = null
    private var previewFocusRequest: AudioFocusRequest? = null
    private var alarmPlayer: MediaPlayer? = null
    private var alarmAudioManager: AudioManager? = null
    private var alarmFocusRequest: AudioFocusRequest? = null
    private var vibrator: Vibrator? = null

    @Suppress("DEPRECATION")
    private val legacyFocusListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            stopPreview()
        }
    }

    private val alarmFocusListener = AudioManager.OnAudioFocusChangeListener { _ ->
        // Alarms hold transient focus until dismissed or snoozed by the user.
    }

    internal val previewAudioAttributes: AudioAttributes by lazy {
        AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
    }

    internal val alarmAudioAttributes: AudioAttributes by lazy {
        AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
    }

    @Synchronized
    fun isPreviewPlaying(): Boolean {
        return try {
            previewPlayer?.isPlaying == true
        } catch (_: IllegalStateException) {
            false
        }
    }

    @Synchronized
    fun isAlarmPlaying(): Boolean {
        return try {
            alarmPlayer?.isPlaying == true
        } catch (_: IllegalStateException) {
            false
        }
    }

    @Synchronized
    fun startPreview(
        context: Context,
        sound: AlarmSound,
        onCompleted: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        stopPreview()
        val appContext = context.applicationContext
        var afd: AssetFileDescriptor? = null
        var createdPlayer: MediaPlayer? = null

        try {
            if (sound.rawResId == 0) {
                throw Resources.NotFoundException("Invalid sound resource id for ${sound.id}")
            }
            val checkAfd = appContext.resources.openRawResourceFd(sound.rawResId)
                ?: throw Resources.NotFoundException("Missing raw audio resource for ${sound.id}")
            val resLength = checkAfd.length
            runCatching { checkAfd.close() }
            if (resLength <= 0L) {
                throw IllegalStateException("Empty raw audio resource for ${sound.id}")
            }

            requestPreviewAudioFocus(appContext)

            val player = MediaPlayer()
            createdPlayer = player
            player.setAudioAttributes(previewAudioAttributes)
            val resourceUri = Uri.parse("android.resource://${appContext.packageName}/${sound.rawResId}")
            try {
                player.setDataSource(appContext, resourceUri)
            } catch (_: Exception) {
                player.reset()
                player.setAudioAttributes(previewAudioAttributes)
                afd = appContext.resources.openRawResourceFd(sound.rawResId)
                    ?: throw Resources.NotFoundException("Missing raw audio resource for ${sound.id}")
                player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            }
            player.isLooping = false
            player.setVolume(1.0f, 1.0f)
            player.setOnCompletionListener { mp ->
                releaseSpecificPreviewPlayer(mp)
                postToMain(onCompleted)
            }
            player.setOnErrorListener { mp, what, extra ->
                releaseSpecificPreviewPlayer(mp)
                postToMain {
                    onError("Preview playback error ($what/$extra)")
                }
                true
            }
            player.prepare()
            // Keep fallback afd open until prepare() finishes so header parsing never reads a closed FD.
            runCatching { afd?.close() }
            afd = null

            player.setVolume(1.0f, 1.0f)
            previewPlayer = player
            player.start()
        } catch (e: Exception) {
            e.printStackTrace()
            runCatching { afd?.close() }
            runCatching {
                createdPlayer?.let {
                    it.reset()
                    it.release()
                }
            }
            if (previewPlayer === createdPlayer) {
                previewPlayer = null
            }
            abandonPreviewAudioFocus()
            val message = e.localizedMessage ?: "Unable to preview alarm sound"
            postToMain { onError(message) }
        }
    }

    @Synchronized
    fun stopPreview() {
        val player = previewPlayer
        previewPlayer = null
        if (player != null) {
            safelyReleaseMediaPlayer(player)
        }
        abandonPreviewAudioFocus()
    }

    @Synchronized
    private fun releaseSpecificPreviewPlayer(target: MediaPlayer) {
        if (previewPlayer === target) {
            previewPlayer = null
            safelyReleaseMediaPlayer(target)
            abandonPreviewAudioFocus()
        } else {
            safelyReleaseMediaPlayer(target)
        }
    }

    private fun safelyReleaseMediaPlayer(player: MediaPlayer) {
        try {
            if (player.isPlaying) {
                player.stop()
            }
        } catch (_: Exception) {
        }
        try {
            player.reset()
        } catch (_: Exception) {
        }
        try {
            player.release()
        } catch (_: Exception) {
        }
    }

    @Suppress("DEPRECATION")
    private fun requestPreviewAudioFocus(appContext: Context) {
        try {
            val am = appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
            previewAudioManager = am
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setAudioAttributes(previewAudioAttributes)
                    .setOnAudioFocusChangeListener(legacyFocusListener)
                    .build()
                previewFocusRequest = request
                am.requestAudioFocus(request)
            } else {
                am.requestAudioFocus(
                    legacyFocusListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
                )
            }
        } catch (_: Exception) {
        }
    }

    @Suppress("DEPRECATION")
    private fun abandonPreviewAudioFocus() {
        try {
            val am = previewAudioManager ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                previewFocusRequest?.let { am.abandonAudioFocusRequest(it) }
            } else {
                am.abandonAudioFocus(legacyFocusListener)
            }
        } catch (_: Exception) {
        } finally {
            previewFocusRequest = null
            previewAudioManager = null
        }
    }

    private fun postToMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            block()
        } else {
            mainHandler.post(block)
        }
    }

    @Synchronized
    fun startAlarmPlayback(context: Context, sound: AlarmSound) {
        stopPreview()
        stopAlarmPlayback()
        val appContext = context.applicationContext
        var afd: AssetFileDescriptor? = null
        var createdPlayer: MediaPlayer? = null

        try {
            if (sound.rawResId == 0) {
                throw Resources.NotFoundException("Invalid sound resource id for ${sound.id}")
            }
            val checkAfd = appContext.resources.openRawResourceFd(sound.rawResId)
                ?: throw Resources.NotFoundException("Missing raw audio resource for ${sound.id}")
            val resLength = checkAfd.length
            runCatching { checkAfd.close() }
            if (resLength <= 0L) {
                throw IllegalStateException("Empty raw audio resource for ${sound.id}")
            }

            requestAlarmAudioFocus(appContext)

            val player = MediaPlayer()
            createdPlayer = player
            player.setWakeMode(appContext, PowerManager.PARTIAL_WAKE_LOCK)
            player.setAudioAttributes(alarmAudioAttributes)
            val resourceUri = Uri.parse("android.resource://${appContext.packageName}/${sound.rawResId}")
            try {
                player.setDataSource(appContext, resourceUri)
            } catch (_: Exception) {
                player.reset()
                player.setWakeMode(appContext, PowerManager.PARTIAL_WAKE_LOCK)
                player.setAudioAttributes(alarmAudioAttributes)
                afd = appContext.resources.openRawResourceFd(sound.rawResId)
                    ?: throw Resources.NotFoundException("Missing raw audio resource for ${sound.id}")
                player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            }
            player.isLooping = true
            player.setVolume(1.0f, 1.0f)
            player.setOnErrorListener { mp, _, _ ->
                if (alarmPlayer === mp) {
                    alarmPlayer = null
                }
                safelyReleaseMediaPlayer(mp)
                true
            }
            player.prepare()
            runCatching { afd?.close() }
            afd = null

            player.setVolume(1.0f, 1.0f)
            alarmPlayer = player
            player.start()
        } catch (e: Exception) {
            e.printStackTrace()
            runCatching { afd?.close() }
            createdPlayer?.let { safelyReleaseMediaPlayer(it) }
            if (alarmPlayer === createdPlayer) {
                alarmPlayer = null
            }
            abandonAlarmAudioFocus()
        }

        startVibration(appContext)
    }

    @Suppress("DEPRECATION")
    private fun requestAlarmAudioFocus(appContext: Context) {
        try {
            val am = appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
            alarmAudioManager = am
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setAudioAttributes(alarmAudioAttributes)
                    .setOnAudioFocusChangeListener(alarmFocusListener)
                    .build()
                alarmFocusRequest = request
                am.requestAudioFocus(request)
            } else {
                am.requestAudioFocus(
                    alarmFocusListener,
                    AudioManager.STREAM_ALARM,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
                )
            }
        } catch (_: Exception) {
        }
    }

    @Suppress("DEPRECATION")
    private fun abandonAlarmAudioFocus() {
        try {
            val am = alarmAudioManager ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                alarmFocusRequest?.let { am.abandonAudioFocusRequest(it) }
            } else {
                am.abandonAudioFocus(alarmFocusListener)
            }
        } catch (_: Exception) {
        } finally {
            alarmFocusRequest = null
            alarmAudioManager = null
        }
    }

    @Suppress("DEPRECATION")
    private fun startVibration(context: Context) {
        try {
            val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            vibrator = vib
            if (vib != null && vib.hasVibrator()) {
                val pattern = longArrayOf(0L, 600L, 400L, 600L, 600L)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vib.vibrate(VibrationEffect.createWaveform(pattern, 0))
                } else {
                    vib.vibrate(pattern, 0)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Synchronized
    fun stopAlarmPlayback() {
        val player = alarmPlayer
        alarmPlayer = null
        if (player != null) {
            safelyReleaseMediaPlayer(player)
        }
        abandonAlarmAudioFocus()

        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            vibrator = null
        }
    }
}
