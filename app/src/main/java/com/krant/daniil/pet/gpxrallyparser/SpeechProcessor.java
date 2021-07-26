package com.krant.daniil.pet.gpxrallyparser;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

public class SpeechProcessor implements TextToSpeech.OnInitListener {
    TextToSpeech mTextToSpeech;
    boolean isReady = false;

    public SpeechProcessor(Context mContext) {
        mTextToSpeech = new TextToSpeech(mContext, this);
    }

    public boolean textToSpeech(String text) {
        if (!isReady) return false;
        mTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        return true;
    }

    @Override
    public void onInit(int i) {
        if (i == TextToSpeech.SUCCESS) {
            isReady = true;
            int result = mTextToSpeech.setLanguage(Locale.getDefault());
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("error", "This Language is not supported");
            }
        } else {
            Log.e("error", "Failed to Initialize");
        }
    }
}
