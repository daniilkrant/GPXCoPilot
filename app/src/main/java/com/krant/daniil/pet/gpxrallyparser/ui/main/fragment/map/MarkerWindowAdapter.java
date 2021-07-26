package com.krant.daniil.pet.gpxrallyparser.ui.main.fragment.map;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.krant.daniil.pet.gpxrallyparser.R;
import com.krant.daniil.pet.gpxrallyparser.RallyPoint;
import com.krant.daniil.pet.gpxrallyparser.Turn;

import java.util.ArrayList;
import java.util.List;

public class MarkerWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private Fragment mFragment;
    private List<RallyPoint> mRallyPoints;
    private TextView mTurnCoord;
    private TextView mTurnId;
    private TextView mTurnHint;
    private ImageView mTurnDirection;

    private MarkerWindowAdapter() {};
    public MarkerWindowAdapter(Fragment mFragment, List<RallyPoint> rallyPoints) {
        this.mFragment = mFragment;
        this.mRallyPoints = rallyPoints;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        View v = mFragment.getLayoutInflater().inflate(R.layout.map_view_item, null);
        mTurnCoord = v.findViewById(R.id.turn_coord);
        mTurnId = v.findViewById(R.id.turn_id);
        mTurnHint = v.findViewById(R.id.turn_hint);
        mTurnDirection = v.findViewById(R.id.turn_image);
        fillView(v, mRallyPoints.get((Integer) marker.getTag()));
        return v;
    }

    public void fillView(View v, RallyPoint rallyPoint) {
        mTurnId.setText(Integer.toString(rallyPoint.getId()));
        mTurnCoord.setText(rallyPoint.getLatitude() + " " + rallyPoint.getLongitude());
        mTurnHint.setText(rallyPoint.getHint());
        if (rallyPoint.getTurn().getDirection() == Turn.Direction.RIGHT) {
            mTurnDirection.setImageDrawable(
                    ContextCompat.getDrawable(mFragment.getContext(), R.drawable.right_turn));
        } else {
            mTurnDirection.setImageDrawable(
                    ContextCompat.getDrawable(mFragment.getContext(), R.drawable.left_turn));
        }
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
