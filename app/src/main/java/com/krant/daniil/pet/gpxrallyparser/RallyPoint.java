package com.krant.daniil.pet.gpxrallyparser;

public class RallyPoint {

    private final int mId;
    private final double mLatitude;
    private final double mLongitude;
    private final int mDistance;
    private final int mElevation;
    private final Turn mTurn;


    public RallyPoint(int id, double latitude, double longitude, int distance, int elevation, Turn turn) {
        this.mId = id;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        this.mDistance = distance;
        this.mElevation = elevation;
        this.mTurn = turn;
    }

    public double getLatitude() {
        return mLatitude;
    }


    public double getLongitude() {
        return mLongitude;
    }


    public int getDistance() {
        return mDistance;
    }


    public int getElevation() {
        return mElevation;
    }


    public Turn getTurn() {
        return mTurn;
    }

    public int getId() {
        return mId;
    }

    @Override
    public String toString() {
        return "RallyPoint{" +
                "mId=" + mId +
                ", mLatitude=" + mLatitude +
                ", mLongitude=" + mLongitude +
                ", mDistance=" + mDistance +
                ", mElevation=" + mElevation +
                ", mTurn=" + mTurn +
                '}';
    }
}
