package com.krant.daniil.pet.gpxrallyparser;

import android.content.Context;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import io.ticofab.androidgpxparser.parser.GPXParser;
import io.ticofab.androidgpxparser.parser.domain.Gpx;
import io.ticofab.androidgpxparser.parser.domain.Track;
import io.ticofab.androidgpxparser.parser.domain.TrackPoint;
import io.ticofab.androidgpxparser.parser.domain.TrackSegment;


public class GPXParserRoutine {

    private Context mContext;
    GPXParser mParser = new GPXParser();
    Gpx mParsedGpx = null;

    public GPXParserRoutine(Context context) {
        this.mContext = context;
    }

    public void parseGpx() {
        try {
            InputStream in = mContext.getAssets().open("gpx_example.xml");
            mParsedGpx = mParser.parse(in); // TODO: Run in bckg
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    public List<TrackPoint> getAllTrackPoints() {
        ArrayList<TrackPoint> ret = new ArrayList<>();
        if (mParsedGpx != null) {
            ArrayList<Track> tracks = new ArrayList<>(mParsedGpx.getTracks());
            for (Track t : tracks) {
                ArrayList<TrackSegment> trackSegments = new ArrayList<>(t.getTrackSegments());
                for (TrackSegment ts : trackSegments) {
                    ArrayList<TrackPoint> trackPoints = new ArrayList<>(ts.getTrackPoints());
                    ret.addAll(trackPoints);
                }
            }
        } else {
            Log.e("TAG", "Parse error");
        }
        return ret;
    }

    public String pointToString(TrackPoint tp) {
        return "Lat: " +
                (tp.getLatitude()) +
                " Long: " +
                (tp.getLongitude()) +
                " Elev: " +
                (tp.getElevation());
    }


    public int calcDistanceBtwPoints(TrackPoint tp1, TrackPoint tp2) {
        return calcDistanceBtwPoints(tp1.getLatitude(), tp2.getLatitude(),
                tp1.getLongitude(), tp2.getLongitude(), tp1.getElevation(), tp2.getElevation());
    }

    public int calcDistanceBtwPoints(double lat1, double lat2, double lon1,
                                     double lon2, double el1, double el2) {
        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return (int) Math.sqrt(distance);
    }

    public int calcElevationBtwPoints(TrackPoint tp1, TrackPoint tp2) {
        return calcElevationBtwPoints(tp1.getElevation(), tp2.getElevation());
    }

    public int calcElevationBtwPoints(double el1, double el2) {
        return (int) (el1 - el2);
    }

    public double calcAngleBtwPoints(TrackPoint tp1, TrackPoint tp2, TrackPoint tp3) {
//        return calcAngleBtwPoints(tp1.getLatitude(), tp2.getLatitude(),
//                tp1.getLongitude(), tp2.getLongitude());

        return calcAngleBtwPoints(tp1.getLatitude(), tp1.getLongitude(),
                tp2.getLatitude(), tp2.getLongitude(), tp3.getLatitude(), tp3.getLongitude());
    }

//    public double calcAngleBtwPoints(double lat1, double lat2, double lon1, double lon2) {
//        double latitude1 = Math.toRadians(lat1);
//        double latitude2 = Math.toRadians(lat2);
//        double longDiff = Math.toRadians(lon2 - lon1);
//        double y = Math.sin(longDiff)*Math.cos(latitude2);
//        double x = Math.cos(latitude1)*Math.sin(latitude2)-Math.sin(latitude1)*Math.cos(latitude2)*Math.cos(longDiff);
//
//        return (Math.toDegrees(Math.atan2(y, x))+360)%360;
//    }

    public double calcAngleBtwPoints(double lat1, double lon1, double lat2, double lon2,
                                     double lat3, double lon3) {
        double angle = 0;
        double angleAbs;
        angle = Math.toDegrees(Math.atan2(lat3 - lat2, lon3 - lon2) - Math.atan2(lat1 - lat2, lon1 - lon2));
        angleAbs = Math.abs(angle);
        if (angleAbs > 180) {
            return 360 - angleAbs;
        }
        else {
            return angleAbs;
        }
    }

}
