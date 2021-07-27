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
import io.ticofab.androidgpxparser.parser.domain.Point;
import io.ticofab.androidgpxparser.parser.domain.Route;
import io.ticofab.androidgpxparser.parser.domain.RoutePoint;
import io.ticofab.androidgpxparser.parser.domain.Track;
import io.ticofab.androidgpxparser.parser.domain.TrackPoint;
import io.ticofab.androidgpxparser.parser.domain.TrackSegment;


public class GPXDataRoutine {

    static GPXDataRoutine instance;
    private static Context mContext;
    private final GPXParser mParser = new GPXParser();
    private Gpx mParsedGpx;
    private List<RallyPoint> mRallyPoints;
    private boolean mIsTracksFound = false;
    private boolean mIsRoutesFound = false;
    private boolean mIsWayPointsFound = false;
    private final LexicalProcessor mLexicalProcessor;

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
            mParsedGpx = mParser.parse(inputStream);
            if (mParsedGpx.getRoutes().size() != 0) {
                mIsRoutesFound = true;
            }
            if (mParsedGpx.getTracks().size() != 0) {
                mIsTracksFound = true;
            }
            if (mParsedGpx.getWayPoints().size() != 0) {
                mIsWayPointsFound = true;
            }
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
            return false;
        }
        return mIsRoutesFound || mIsTracksFound || mIsWayPointsFound;
    }

    public List<RallyPoint> getRallyPoints() {
        if (mRallyPoints != null) {
            return mRallyPoints;
        }
        mRallyPoints = new ArrayList<>();
        ArrayList<Point> trackPoints = new ArrayList<>();
        if (mIsTracksFound) {
            trackPoints.addAll(getAllTrackPoints());
        }
        if (mIsRoutesFound) {
            trackPoints.addAll(getAllRoutePoints());
        }
        if (mIsWayPointsFound) {
            if (!mParsedGpx.getCreator().equals("mapstogpx.com")) {
                trackPoints.addAll(getAllWayPoints());
            }
        }

        RallyPoint first = new RallyPoint(0, trackPoints.get(0).getLatitude(),
                trackPoints.get(0).getLongitude(),
                0, 0,
                trackPoints.get(0).getDesc(), trackPoints.get(0).getName(),
                new Turn(180, Turn.Direction.RIGHT));
        first.setIsFirst(true);
        first.setHint(mLexicalProcessor.getHint(first));
        mRallyPoints.add(first);

        for (int i = 1; i < trackPoints.size() - 1; i++) {
            int pointDistance = calcDistanceBtwPoints(trackPoints.get(i),
                    trackPoints.get(i + 1));
            int pointElevation = calcElevationBtwPoints(trackPoints.get(i),
                    trackPoints.get(i + 1));
            int turnAngle = calcTurnAngleBtwPoints(trackPoints.get(i - 1),
                    trackPoints.get(i),
                    trackPoints.get(i + 1));
            Turn.Direction turnDirection = calcTurnDirectionBtwPoints(trackPoints.get(i - 1),
                    trackPoints.get(i),
                    trackPoints.get(i + 1));

            RallyPoint rallyPoint = new RallyPoint(i, trackPoints.get(i).getLatitude(),
                    trackPoints.get(i).getLongitude(), pointDistance, pointElevation,
                    trackPoints.get(i).getDesc(), trackPoints.get(i).getName(),
                    new Turn(turnAngle, turnDirection));
            rallyPoint.setHint(mLexicalProcessor.getHint(rallyPoint));
            mRallyPoints.add(rallyPoint);
        }

        int lastIndex = trackPoints.size() - 1;
        RallyPoint last = new RallyPoint(lastIndex,
                trackPoints.get(lastIndex).getLatitude(),
                trackPoints.get(lastIndex).getLongitude(),
                0, 0,
                trackPoints.get(lastIndex).getDesc(),
                trackPoints.get(lastIndex).getName(),
                new Turn(180, Turn.Direction.RIGHT));
        last.setIsLast(true);
        last.setHint(mLexicalProcessor.getHint(last));
        mRallyPoints.add(last);

        return mRallyPoints;
    }

    public void cleanRallyPoints() {
        mRallyPoints = null;
    }

    private List<RoutePoint> getAllRoutePoints() {
        ArrayList<RoutePoint> ret = new ArrayList<>();
        if (mParsedGpx != null) {
            ArrayList<Route> routes = new ArrayList<>(mParsedGpx.getRoutes());
            for (Route r : routes) {
                ArrayList<RoutePoint> routePoints = new ArrayList<>(r.getRoutePoints());
                ret.addAll(routePoints);
            }
        } else {
            Log.e("TAG", "Parse error");
        }
        return ret;
    }

    private List<Point> getAllWayPoints() {
        ArrayList<Point> ret = new ArrayList<>();
        if (mParsedGpx != null) {
            ret = new ArrayList<>(mParsedGpx.getWayPoints());
        } else {
            Log.e("TAG", "Parse error");
        }
        return ret;
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

    public RallyPoint getNearestRallyPoint(double from_lat, double from_lon) {
        RallyPoint nearest = mRallyPoints.get(0);
        int nearest_dist = calcDistanceBtwPoints(from_lat, nearest.getLatitude(),
                from_lon, nearest.getLongitude());
        for (int i = 1; i < mRallyPoints.size(); i++) {
            int dist = calcDistanceBtwPoints(from_lat, mRallyPoints.get(i).getLatitude(),
                    from_lon, mRallyPoints.get(i).getLongitude());
            if (dist < nearest_dist) {
                nearest_dist = dist;
                nearest = mRallyPoints.get(i);
            }
        }
        return nearest;
    }

    private int calcDistanceBtwPoints(Point tp1, Point tp2) {
        int distance = 0;
        try {
            distance = calcDistanceBtwPoints(tp1.getLatitude(), tp2.getLatitude(),
                    tp1.getLongitude(), tp2.getLongitude(), tp1.getElevation(), tp2.getElevation());
        } catch (NullPointerException e) {
            distance = calcDistanceBtwPoints(tp1.getLatitude(), tp2.getLatitude(),
                    tp1.getLongitude(), tp2.getLongitude());
        }
        return distance;
    }

    private int calcDistanceBtwPoints(double lat1, double lat2, double lon1,
                                      double lon2) {
        return calcDistanceBtwPoints(lat1, lat2, lon1, lon2, 0, 0);
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

    private int calcElevationBtwPoints(Point tp1, Point tp2) {
        int distance = 0;
        try {
            distance = calcElevationBtwPoints(tp1.getElevation(), tp2.getElevation());
        } catch (NullPointerException e) { }
        return distance;
    }

    private int calcElevationBtwPoints(double from, double to) {
        return (int) (from - to);
    }

    private int calcTurnAngleBtwPoints(Point tp1, Point tp2, Point tp3) {
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
        } else {
            return (int) angleAbs;
        }
    }

    private Turn.Direction calcTurnDirectionBtwPoints(Point tp1, Point tp2, Point tp3) {
        return calcTurnDirectionBtwPoints(tp1.getLatitude(), tp1.getLongitude(),
                tp2.getLatitude(), tp2.getLongitude(), tp3.getLatitude(), tp3.getLongitude());
    }

    private Turn.Direction calcTurnDirectionBtwPoints(double lat1, double lon1, double lat2, double lon2,
                                                      double lat3, double lon3) {
        double angle = Math.toDegrees(Math.atan2(lat3 - lat2, lon3 - lon2)
                - Math.atan2(lat1 - lat2, lon1 - lon2));
        if (((angle > 175) && (angle < 185)) ||
                ((angle > -185) && (angle < -175))) {
            return Turn.Direction.FORWARD;
        }
        if (((angle > 0) && (angle < 180)) || ((angle > -360) && (angle < -180))) {
            return Turn.Direction.RIGHT;
        } else {
            return Turn.Direction.LEFT;
        }
    }

}
