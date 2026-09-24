package edu.utexas.cs.cs378.hours;

import lombok.Data;

@Data
public class HoursEarningsAggregateData {
    // 24 entries; 1 for each hour

    long[] totalEarnings;
    int[] rideCount;

    public HoursEarningsAggregateData() {
        totalEarnings = new long[24];
        rideCount = new int[24];
    }

    public void addData(TripHourData data) {
        totalEarnings[data.getHour()] += data.getEarnings();
        rideCount[data.getHour()]++;
    }

    public int getTotalRideCount() {
        int total = 0;
        for (int count : rideCount) {
            total += count;
        }
        return total;
    }

    public HourFinalData[] getDataPoints() {
        HourFinalData[] points = new HourFinalData[24];
        for (int i = 0; i < 24; i++) {
            points[i] = new HourFinalData(i, totalEarnings[i], rideCount[i]);
        }
        return points;
    }

    public void merge(HoursEarningsAggregateData data) {
        for (int i = 0; i < 24; i++) {
            totalEarnings[i] += data.totalEarnings[i];
            rideCount[i] += data.rideCount[i];
        }
    }
}
