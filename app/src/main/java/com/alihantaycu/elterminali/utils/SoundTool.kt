import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import com.alihantaycu.elterminali.R

class SoundTool private constructor(context: Context) {
    private val soundPool: SoundPool = SoundPool(3, AudioManager.STREAM_MUSIC, 1)
    private val soundBeep: Int = soundPool.load(context, R.raw.scan_buzzer, 1)

    companion object {
        @Volatile
        private var instance: SoundTool? = null

        fun getInstance(context: Context): SoundTool {
            return instance ?: synchronized(this) {
                instance ?: SoundTool(context).also { instance = it }
            }
        }
    }

    fun playBeep() {
        soundPool.play(soundBeep, 1f, 1f, 1, 0, 1f)
    }

    fun release() {
        soundPool.release()
        instance = null
    }
}