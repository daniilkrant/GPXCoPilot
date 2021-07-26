package com.krant.daniil.pet.gpxrallyparser.ui.main.fragment.map;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.krant.daniil.pet.gpxrallyparser.GPXDataRoutine;
import com.krant.daniil.pet.gpxrallyparser.R;
import com.krant.daniil.pet.gpxrallyparser.RallyPoint;
import com.krant.daniil.pet.gpxrallyparser.SpeechProcessor;

import java.util.ArrayList;

public class MapViewFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener {

    private final static String TITLE_ID_DELIM = ": ";

    private GoogleMap mMap;
    private GPXDataRoutine mGpxParser;
    private SpeechProcessor mSpeechProcessor;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_map, container, false);

        mGpxParser = GPXDataRoutine.getInstance();
        mSpeechProcessor = new SpeechProcessor(getContext());

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        ArrayList<RallyPoint> rallyPoints = new ArrayList<>();
        try {
            mGpxParser.parseGpx();
            rallyPoints = new ArrayList<>(mGpxParser.getRallyPoints());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMap = googleMap;
        mMap.setInfoWindowAdapter(new MarkerWindowAdapter(this, rallyPoints));
        addMarkersToMap(rallyPoints);

        mMap.setOnMarkerClickListener(this);
        mMap.moveCamera(CameraUpdateFactory.
                newLatLngZoom(new LatLng(rallyPoints.get(0).getLatitude(),
                        rallyPoints.get(0).getLongitude()), 10));

    }

    public void textToSpeech(String text) {
        if (!mSpeechProcessor.textToSpeech(text)) {
            Snackbar.make(rootView, getContext().getString(R.string.tts_not_ready),
                    Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        textToSpeech(removeIDFromMarkerTitle(marker.getTitle()));
        return false;
    }

    private String addIdToMarkerTitle(String title, int id) {
        return id + TITLE_ID_DELIM + title;
    }

    private String removeIDFromMarkerTitle(String title) {
        return title.substring(title.indexOf(TITLE_ID_DELIM) + TITLE_ID_DELIM.length());
    }

    private void addMarkersToMap(ArrayList<RallyPoint> rallyPoints) {
        for (RallyPoint rp : rallyPoints) {
            LatLng point = new LatLng(rp.getLatitude(), rp.getLongitude());
            String title = addIdToMarkerTitle(rp.getHint(), rp.getId());
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(point)
                    .title(title));
            marker.setTag(rp.getId());
        }
    }
}