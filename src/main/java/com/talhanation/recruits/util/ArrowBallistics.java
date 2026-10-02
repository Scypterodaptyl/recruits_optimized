package com.talhanation.recruits.util;

// Vanilla arrow flight per tick: move, drag 0.99, gravity 0.05.
public final class ArrowBallistics {
    private static final double GRAVITY = 0.05D;
    private static final double DRAG = 0.99D;
    private static final double MIN_SPEED = 0.3D;
    private static final double MAX_SPEED = 2.75D;
    private static final int MAX_TICKS = 1000;

    private ArrowBallistics() {
    }

    // Distance at which the arrow comes down to targetY (relative to the shot), -1 if it never does.
    public static double landingDistance(double speed, double pitch, double targetY) {
        double vx = speed * Math.cos(pitch);
        double vy = speed * Math.sin(pitch);
        double x = 0.0D;
        double y = 0.0D;
        boolean wasAbove = false;

        for (int tick = 0; tick < MAX_TICKS; tick++) {
            double prevX = x;
            double prevY = y;
            x += vx;
            y += vy;
            vx *= DRAG;
            vy = vy * DRAG - GRAVITY;

            if (y > targetY) wasAbove = true;
            if (wasAbove && vy < 0 && y <= targetY) {
                double t = (prevY - targetY) / (prevY - y);
                return prevX + (x - prevX) * t;
            }
        }
        return -1.0D;
    }

    // Speed that lands the arrow at distance/targetY; the maximum speed if out of range.
    public static float speedForDistance(double distance, double targetY, double pitch) {
        if (landingDistance(MAX_SPEED, pitch, targetY) < distance) return (float) MAX_SPEED;

        double lo = MIN_SPEED;
        double hi = MAX_SPEED;
        for (int i = 0; i < 24; i++) {
            double mid = (lo + hi) / 2.0D;
            if (landingDistance(mid, pitch, targetY) < distance) lo = mid;
            else hi = mid;
        }
        return (float) hi;
    }
}
