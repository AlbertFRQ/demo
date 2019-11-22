package com.richard.demo.cache.util;

public interface CacheTime {
    int FIVE_MINUTES = 300;
    int TEN_MINUTES = 600;
    int QUARTER = 900;
    int HALF_AN_HOUR = 1800;
    int ONE_HOUR = 3600;
    int TWO_HOURS = ONE_HOUR * 2;
    int FOUR_HOURS = ONE_HOUR * 4;
    int SIX_HOURS = ONE_HOUR * 6;
    int ONE_DAY = ONE_HOUR * 24;
    int TWO_DAYS = ONE_DAY * 2;
    int ONE_WEEK = ONE_DAY * 7;
    int HALF_MONTH = ONE_DAY * 15;
    int ONE_MONTH = ONE_DAY * 30;
    int THREE_MONTHS = ONE_DAY * 90;

    /**
     * Half and hour
     */
    int DEFAULT = HALF_AN_HOUR;
    /**
     * Minimum TTL allowed
     */
    int MIN = 30;
}
