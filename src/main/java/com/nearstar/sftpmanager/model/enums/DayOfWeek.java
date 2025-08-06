package com.nearstar.sftpmanager.model.enums;

public enum DayOfWeek {
    MON("Monday", 1),
    TUE("Tuesday", 2),
    WED("Wednesday", 3),
    THU("Thursday", 4),
    FRI("Friday", 5),
    SAT("Saturday", 6),
    SUN("Sunday", 7);

    private final String displayName;
    private final int dayNumber;

    DayOfWeek(String displayName, int dayNumber) {
        this.displayName = displayName;
        this.dayNumber = dayNumber;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getDayNumber() {
        return dayNumber;
    }

    public static DayOfWeek fromDayNumber(int dayNumber) {
        for (DayOfWeek day : values()) {
            if (day.dayNumber == dayNumber) {
                return day;
            }
        }
        throw new IllegalArgumentException("Invalid day number: " + dayNumber);
    }
}