package com.krant.daniil.pet.gpxrallyparser;

public class RallyPoint {

    private final int mId;
    private final double mLatitude;
    private final double mLongitude;
    private final int mDistance;
    private final int mElevation;
    private String mName = "";
    private String mDescription = "";
    private final Turn mTurn;
    private String mHint;
    private boolean mIsFirst;
    private boolean mIsLast;

    public RallyPoint(int id, double latitude, double longitude, int distance, int elevation,
                      String descr, String name, Turn turn) {
        this.mId = id;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        this.mDistance = distance;
        this.mElevation = elevation;
        this.mTurn = turn;
        this.mDescription = descr;
        this.mName = name;
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

    public String getHint() {
        return mHint;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getName() {
        return mName;
    }


    public void setHint(String hint) {
        this.mHint = hint;
    }

    public boolean isFirst() {return mIsFirst;}

    public boolean isLast() {return mIsLast;}

    public void setIsFirst(boolean is) {
        this.mIsFirst = is;
    }

    public void setIsLast(boolean is) {
        this.mIsLast = is;
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
