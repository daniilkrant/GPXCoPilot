package com.krant.daniil.pet.gpxrallyparser.ui.fragment.map;

import android.location.Location;

public interface RouteFollowingListener {

    void onLocationObtained(Location location);

    void startVoiceFollowing();

    void stopVoiceFollowing();
}
