package utils;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import java.util.Locale;

/**
 * Created by Adminis on 2016/5/29.
 */
public class TTS {

    /*
    *        语音播报系统
    *        @param context 上下文
    *        speak(String content);
    * */
    Context context;
    TextToSpeech tts;

    public TTS(Context context) {

        this.context = context;
        tts = new TextToSpeech(context, status -> {

            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.CHINA);

                if (result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED) {

                    Toast.makeText(context, "语音系统加载成功！", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void speak(String content){

        tts.speak(content, TextToSpeech.QUEUE_ADD, null);
    }

}
