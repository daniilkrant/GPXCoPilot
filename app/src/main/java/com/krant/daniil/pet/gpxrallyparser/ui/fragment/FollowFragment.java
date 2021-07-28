package com.krant.daniil.pet.gpxrallyparser.ui.fragment;

import android.view.View;

import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.krant.daniil.pet.gpxrallyparser.GPXDataChangedListener;
import com.krant.daniil.pet.gpxrallyparser.R;
import com.krant.daniil.pet.gpxrallyparser.RallyPoint;
import com.krant.daniil.pet.gpxrallyparser.SpeechProcessor;

import java.util.ArrayList;

public abstract class FollowFragment extends Fragment implements RouteFollowingListener, GPXDataChangedListener {
    protected boolean mIsFollowingActivated = false;
    protected boolean mIsVoiceActivated = false;
    protected SpeechProcessor mSpeechProcessor;
    protected ArrayList<RallyPoint> mRallyPoints;
    protected View mRootView;


    public void textToSpeech(String text) {
        if (!mSpeechProcessor.textToSpeech(text)) {
            Snackbar.make(mRootView, getContext().getString(R.string.tts_not_ready),
                    Snackbar.LENGTH_LONG).show();
        }
    }

    public abstract void follow(int number);

    public abstract void initSpeech();

    @Override
    public void startFollowing() {
        mIsFollowingActivated = true;
    }

    @Override
    public void stopFollowing() {
        mIsFollowingActivated = false;
    }

    @Override
    public void onSoundEnabled() {
        mIsVoiceActivated = true;
    }

    @Override
    public void onSoundDisabled() {
        mIsVoiceActivated = false;
    }
}

