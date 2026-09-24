package edu.utexas.cs.cs378.drivers;

import edu.utexas.cs.cs378.TripData;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DriverEarnRateData {
    private byte[] driverHash;
    private short duration;
    private short earnings;

    public DriverEarnRateData(TripData data) {
        // we're going to apply the same filters as hours, so this should fit.
        this(data.getDriverHash(), data.getDurationSeconds(), (short) data.getTotal());
    }
}
