package com.krant.daniil.pet.gpxrallyparser.ui.fragment.list;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.Marker;
import com.google.android.material.snackbar.Snackbar;
import com.krant.daniil.pet.gpxrallyparser.GPXDataRoutine;
import com.krant.daniil.pet.gpxrallyparser.MainActivity;
import com.krant.daniil.pet.gpxrallyparser.R;
import com.krant.daniil.pet.gpxrallyparser.RallyPoint;
import com.krant.daniil.pet.gpxrallyparser.SpeechProcessor;
import com.krant.daniil.pet.gpxrallyparser.ui.fragment.map.RouteFollowingListener;

import java.util.ArrayList;

public class ListViewFragment extends Fragment implements RouteFollowingListener {

    private ArrayList<RallyPoint> mRallyPoints;
    private View rootView;
    private boolean mIsFollowingActivated = false;
    private SpeechProcessor mSpeechProcessor;
    private RecyclerView mRecyclerView;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        mSpeechProcessor = new SpeechProcessor(getContext());
        MainActivity.addLocationChangedListener(this);

        rootView = inflater.inflate(R.layout.fragment_list, container, false);
        mRallyPoints = new ArrayList<>(GPXDataRoutine.getInstance().getRallyPoints());
        mRecyclerView = rootView.findViewById(R.id.recyclerview);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new TopLayoutManager(rootView.getContext()));
        mRecyclerView.setAdapter(new RallyPointsListAdapter(mRallyPoints, getContext()));

        return rootView;
    }

    public void textToSpeech(String text) {
        if (!mSpeechProcessor.textToSpeech(text)) {
            Snackbar.make(rootView, getContext().getString(R.string.tts_not_ready),
                    Snackbar.LENGTH_LONG).show();
        }
    }

    private void follow(int number) {
        mRecyclerView.smoothScrollToPosition(number);
        textToSpeech(mRallyPoints.get(number).getHint());
    }

    @Override
    public void onLocationObtained(Location location) {
        if (mIsFollowingActivated) {
            RallyPoint nearestPoint = GPXDataRoutine.getInstance().getNearestRallyPoint(location.getLatitude(),
                    location.getLongitude());
            Log.e("Log", nearestPoint.toString());
            follow(nearestPoint.getId());
        }
    }

    @Override
    public void startVoiceFollowing() {
        mIsFollowingActivated = true;
    }

    @Override
    public void stopVoiceFollowing() {
        mIsFollowingActivated = false;
    }
}
