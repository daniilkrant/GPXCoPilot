package com.krant.daniil.pet.gpxrallyparser;

public class Turn {

    private final int mAngle;
    private final Direction mDirection;

    public Turn(int angle, Direction direction) {
        this.mAngle = angle;
        this.mDirection = direction;
    }

    public int getAngle() {
        return mAngle;
    }

    public Direction getDirection() {
        return mDirection;
    }

    @Override
    public String toString() {
        return "Turn{" +
                "mAngle=" + mAngle +
                ", mDirection=" + mDirection +
                '}';
    }

    enum Direction {
        LEFT,
        RIGHT
    }
}
