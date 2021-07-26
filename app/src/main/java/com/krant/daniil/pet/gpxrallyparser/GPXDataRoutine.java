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


public class GPXDataRoutine {

    static GPXDataRoutine instance;
    private static Context mContext;
    private final GPXParser mParser = new GPXParser();
    private Gpx mParsedGpx;
    private List<RallyPoint> mRallyPoints;
    private LexicalProcessor mLexicalProcessor;

    private GPXDataRoutine() {
        mLexicalProcessor = new LexicalProcessor(mContext);
    }

    public static synchronized GPXDataRoutine getInstance() {
        if (instance == null) {
            instance = new GPXDataRoutine();
        }
        return instance;
    }

    public static void setContext(Context context) {
        mContext = context;
    }

    public boolean parseGpx(InputStream inputStream) {
        try {
            mParsedGpx = mParser.parse(inputStream); // TODO: Run in bckg
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public List<RallyPoint> getRallyPoints() {
        if (mRallyPoints != null) {
            return mRallyPoints;
        }
        ArrayList<RallyPoint> ret = new ArrayList<>();
        ArrayList<TrackPoint> trackPoints = new ArrayList<>(getAllTrackPoints());

        for (int i = 1; i < trackPoints.size()-2; i++) {
            int pointDistance = calcDistanceBtwPoints(trackPoints.get(i),
                    trackPoints.get(i + 1));
            int pointElevation = calcElevationBtwPoints(trackPoints.get(i),
                    trackPoints.get(i + 1));
            int turnAngle = calcTurnAngleBtwPoints(trackPoints.get(i-1),
                    trackPoints.get(i), trackPoints.get(i+1));
            Turn.Direction turnDirection = calcTurnDirectionBtwPoints(trackPoints.get(i),
                    trackPoints.get(i+1));

            RallyPoint rallyPoint = new RallyPoint(i, trackPoints.get(i).getLatitude(),
                    trackPoints.get(i).getLongitude(), pointDistance, pointElevation,
                    new Turn(turnAngle, turnDirection));
            rallyPoint.setHint(mLexicalProcessor.getHint(rallyPoint));
            ret.add(rallyPoint);
        }

        mRallyPoints = ret;
        return mRallyPoints;
    }

    private List<TrackPoint> getAllTrackPoints() {
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

    private int calcDistanceBtwPoints(TrackPoint tp1, TrackPoint tp2) {
        return calcDistanceBtwPoints(tp1.getLatitude(), tp2.getLatitude(),
                tp1.getLongitude(), tp2.getLongitude(), tp1.getElevation(), tp2.getElevation());
    }

    private int calcDistanceBtwPoints(double lat1, double lat2, double lon1,
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

    private int calcElevationBtwPoints(TrackPoint tp1, TrackPoint tp2) {
        return calcElevationBtwPoints(tp1.getElevation(), tp2.getElevation());
    }

    private int calcElevationBtwPoints(double from, double to) {
        return (int) (from - to);
    }

    private int calcTurnAngleBtwPoints(TrackPoint tp1, TrackPoint tp2, TrackPoint tp3) {
        return calcTurnAngleBtwPoints(tp1.getLatitude(), tp1.getLongitude(),
                tp2.getLatitude(), tp2.getLongitude(), tp3.getLatitude(), tp3.getLongitude());
    }

    private int calcTurnAngleBtwPoints(double lat1, double lon1, double lat2, double lon2,
                                         double lat3, double lon3) {
        double angle = 0;
        double angleAbs;
        angle = Math.toDegrees(Math.atan2(lat3 - lat2, lon3 - lon2) - Math.atan2(lat1 - lat2, lon1 - lon2));
        angleAbs = Math.abs(angle);
        if (angleAbs > 180) {
            return (int) (360 - angleAbs);
        }
        else {
            return (int) angleAbs;
        }
    }

    private Turn.Direction calcTurnDirectionBtwPoints(TrackPoint tp1, TrackPoint tp2) {
        return calcTurnDirectionBtwPoints(tp1.getLatitude(), tp2.getLatitude());
    }

    private Turn.Direction calcTurnDirectionBtwPoints(double from, double to) {
        Turn.Direction direction;
        double webMercatorLatFrom = WebMercatorConvertor.latitudeToY(from);
        double webMercatorLatTo = WebMercatorConvertor.latitudeToY(to);
        if (webMercatorLatFrom < webMercatorLatTo) {
            direction =  Turn.Direction.RIGHT;
        } else {
            direction = Turn.Direction.LEFT;
        }
        return direction;
    }

}
