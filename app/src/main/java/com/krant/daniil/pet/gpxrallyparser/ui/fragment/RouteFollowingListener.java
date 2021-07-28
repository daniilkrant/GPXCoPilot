package com.krant.daniil.pet.gpxrallyparser.ui.fragment;

import android.location.Location;

public interface RouteFollowingListener {

    void onLocationObtained(Location location);

    void startFollowing();

    void stopFollowing();

    void onSoundEnabled();

    void onSoundDisabled();
}
