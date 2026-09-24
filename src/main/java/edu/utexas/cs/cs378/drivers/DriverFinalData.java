package edu.utexas.cs.cs378.drivers;

import edu.utexas.cs.cs378.Md5Wrapper;
import lombok.Data;

import java.util.Locale;

// 32 bytes per object
@Data
public class DriverFinalData implements Comparable<DriverFinalData> {

    // 16
    private final Md5Wrapper driver;
    private long totalEarnings;
    private long totalDuration;

    public void addData(DriverEarnRateData data) {
        totalEarnings += data.getEarnings();
        totalDuration += data.getDuration();
    }

    public DriverFinalData merge(DriverFinalData data) {
        totalEarnings += data.totalEarnings;
        totalDuration += data.totalDuration;
        return this;
    }

    private double getDollarsPerMinute() {
        double minutes = totalDuration / 60.0;
        double dollars = totalEarnings / 100.0;
        return minutes == 0 ? 0 : dollars / minutes;
    }

    @Override
    public String toString() {
        return "(" + driver + ", " + String.format(Locale.US, "%.2f", getDollarsPerMinute()) + ")";
    }

    @Override
    public int compareTo(DriverFinalData o) {
        return Double.compare(getDollarsPerMinute(), o.getDollarsPerMinute());
    }
}
