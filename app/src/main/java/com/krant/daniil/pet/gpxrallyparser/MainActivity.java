package com.krant.daniil.pet.gpxrallyparser;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

import io.ticofab.androidgpxparser.parser.domain.TrackPoint;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GPXParserRoutine gpxParser = new GPXParserRoutine(getApplicationContext());
        try {
            gpxParser.parseGpx();
            ArrayList<TrackPoint> trackPoints = new ArrayList<>(gpxParser.getAllTrackPoints());

//            for (int i = 0; i < trackPoints.size()-2; i++) {
//                Log.e("TAG", "-------------------------------------------------------");
//                Log.e("TAG", "Point: " + gpxParser.pointToString(trackPoints.get(i)));
//                Log.e("TAG", "Point1: " + gpxParser.pointToString(trackPoints.get(i+1)));
//                Log.e("TAG", "Distance: " + gpxParser.calcDistanceBtwPoints(trackPoints.get(i),
//                        trackPoints.get(i+1)));
//                Log.e("TAG", "Elevation: " + gpxParser.calcElevationBtwPoints(trackPoints.get(i),
//                        trackPoints.get(i+1)));
//                Log.e("TAG", "Angle: " + gpxParser.calcAngleBtwPoints(trackPoints.get(i),
//                        trackPoints.get(i+1), trackPoints.get(i+2)));
//                Log.e("TAG", "-------------------------------------------------------");
//            }

            for (int i = 1; i < trackPoints.size()-2; i++) {
                Log.e("TAG", "-------------------------------------------------------");
                Log.e("TAG", "Point " + (i-1) + ":" + gpxParser.pointToString(trackPoints.get(i-1)));
                Log.e("TAG", "Point " + (i) + ":" + gpxParser.pointToString(trackPoints.get(i)));
                Log.e("TAG", "Point " + (i+1) + ":" + gpxParser.pointToString(trackPoints.get(i+1)));
                Log.e("TAG", "Angle: " + gpxParser.calcAngleBtwPoints(trackPoints.get(i-1),
                        trackPoints.get(i), trackPoints.get(i+1)));
                Log.e("TAG", "-------------------------------------------------------");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}