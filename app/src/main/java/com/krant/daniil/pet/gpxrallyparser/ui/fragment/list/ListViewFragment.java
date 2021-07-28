package com.krant.daniil.pet.gpxrallyparser.ui.fragment.list;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.krant.daniil.pet.gpxrallyparser.GPXDataRoutine;
import com.krant.daniil.pet.gpxrallyparser.MainActivity;
import com.krant.daniil.pet.gpxrallyparser.R;
import com.krant.daniil.pet.gpxrallyparser.RallyPoint;
import com.krant.daniil.pet.gpxrallyparser.SpeechProcessor;
import com.krant.daniil.pet.gpxrallyparser.ui.fragment.FollowFragment;
import com.krant.daniil.pet.gpxrallyparser.ui.fragment.RouteFollowingListener;

import java.util.ArrayList;

public class ListViewFragment extends FollowFragment implements RouteFollowingListener {

    private RecyclerView mRecyclerView;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        initSpeech();
        MainActivity.addLocationChangedListener(this);

        mRootView = inflater.inflate(R.layout.fragment_list, container, false);
        mRallyPoints = new ArrayList<>(GPXDataRoutine.getInstance().getRallyPoints());
        mRecyclerView = mRootView.findViewById(R.id.recyclerview);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new TopLayoutManager(mRootView.getContext()));
        mRecyclerView.setAdapter(new RallyPointsListAdapter(mRallyPoints, getContext()));

        return mRootView;
    }

    @Override
    public void follow(int number) {
        mRecyclerView.smoothScrollToPosition(number);
        if (mIsVoiceActivated) {
            textToSpeech(mRallyPoints.get(number).getHint());
        }
    }

    @Override
    public void initSpeech() {
        mSpeechProcessor = new SpeechProcessor(getContext());
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


}
