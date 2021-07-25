package com.krant.daniil.pet.gpxrallyparser;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    GPXDataRoutine mGpxParser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGpxParser = new GPXDataRoutine(getApplicationContext());
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            mGpxParser.parseGpx();
            ArrayList<RallyPoint> rallyPoints = new ArrayList<>(mGpxParser.getRallyPoints());
            for (RallyPoint rp: rallyPoints) {
                LatLng point = new LatLng(rp.getLatitude(), rp.getLongitude());
                mMap.addMarker(new MarkerOptions()
                        .position(point)
                        .title(LexicalProcessor.getHint(rp)));
                Log.e("Tag", rp.toString());
            }
            mMap.moveCamera(CameraUpdateFactory.
                    newLatLngZoom(new LatLng(rallyPoints.get(0).getLatitude(),
                    rallyPoints.get(0).getLongitude()), 20));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}