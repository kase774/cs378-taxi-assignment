package edu.utexas.cs.cs378.hours;

import lombok.Data;

@Data
public class HourFinalData implements Comparable<HourFinalData> {

    int hour;
    long totalEarnings;
    int rideCount;
    double average;

    public HourFinalData(int hour, long totalEarnings, int rideCount) {
        this.hour = hour;
        this.totalEarnings = totalEarnings;
        this.rideCount = rideCount;
        this.average = (double) totalEarnings / rideCount;
    }

    @Override
    public String toString() {
        return "(" + hour + ", (" + totalEarnings + ", " + rideCount + "))";
    }

    @Override
    public int compareTo(HourFinalData o) {
        return Double.compare(average, o.average);
    }
}
