package com.nightout.model;

public class PriceFilter {

    public enum Mode { FREE, RANGE, ANY}

    private final Mode mode;
    private final double minPrice;
    private final double maxPrice;

    private PriceFilter(Mode mode, Double minPrice, Double maxPrice) {
        this.mode = mode;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }

    public static PriceFilter free() {
        return new PriceFilter(Mode.FREE, 0.0, 0.0);
    }

    public static PriceFilter range(double min, double max) {
        return new PriceFilter(Mode.RANGE, min, max);
    }

    public static PriceFilter any() {
        return new PriceFilter(Mode.ANY, null, null);
    }

    public Mode getMode() {
        return mode;
    }
    public double getMinPrice() {
        return minPrice;
    }
    public double getMaxPrice() {
        return maxPrice;
    }

    public boolean isFree() {
        return mode == Mode.FREE;
    }
    public boolean isRange() {
        return mode == Mode.RANGE;
    }
    public boolean isAny() {
        return mode == Mode.ANY;
    }

    public String toString() {
        switch (mode) {
            case FREE:
                return "Free events only";
            case RANGE:
                return String.format("Price range: £%.0f – £%.0f", minPrice, maxPrice);
            case ANY:
                return "Any price";
        }
        throw new IllegalStateException("Unexpected value: " + mode);
    }
}