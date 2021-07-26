package com.krant.daniil.pet.gpxrallyparser.ui.main;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.krant.daniil.pet.gpxrallyparser.GPXDataRoutine;
import com.krant.daniil.pet.gpxrallyparser.LexicalProcessor;
import com.krant.daniil.pet.gpxrallyparser.R;
import com.krant.daniil.pet.gpxrallyparser.RallyPoint;

import java.util.ArrayList;
import java.util.Locale;

public class MapViewFragment extends Fragment implements OnMapReadyCallback,
        TextToSpeech.OnInitListener, GoogleMap.OnMarkerClickListener {

    private final static String TITLE_ID_DELIM = ": ";

    private GoogleMap mMap;
    GPXDataRoutine mGpxParser;
    TextToSpeech mTextToSpeech;
    LexicalProcessor mLexicalProcessor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        mGpxParser = new GPXDataRoutine(getContext());
        mLexicalProcessor = new LexicalProcessor(getContext());
        mTextToSpeech = new TextToSpeech(getContext(), this);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            mGpxParser.parseGpx();
            ArrayList<RallyPoint> rallyPoints = new ArrayList<>(mGpxParser.getRallyPoints());
            addMarkersToMap(rallyPoints);

            mMap.setOnMarkerClickListener(this);
            mMap.moveCamera(CameraUpdateFactory.
                    newLatLngZoom(new LatLng(rallyPoints.get(0).getLatitude(),
                            rallyPoints.get(0).getLongitude()), 20));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInit(int i) {
        if (i == TextToSpeech.SUCCESS) {
            int result = mTextToSpeech.setLanguage(Locale.getDefault());
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("error", "This Language is not supported");
            }
        } else {
            Log.e("error", "Failed to Initialize");
        }
    }

    public void textToSpeech(String text) {
        mTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
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
        for (RallyPoint rp: rallyPoints) {
            LatLng point = new LatLng(rp.getLatitude(), rp.getLongitude());
            String title = addIdToMarkerTitle(mLexicalProcessor.getHint(rp), rp.getId());
            mMap.addMarker(new MarkerOptions()
                    .position(point)
                    .title(title));
        }
    }
}