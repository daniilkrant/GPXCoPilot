package com.krant.daniil.pet.gpxrallyparser;

import java.util.List;

public interface GPXDataChangedListener {

    void onDataSetChanged(List<RallyPoint> newPoints);
}
