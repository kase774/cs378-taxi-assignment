package edu.utexas.cs.cs378.hours;

import edu.utexas.cs.cs378.TripData;
import lombok.AllArgsConstructor;
import lombok.Data;

// 3 bytes; 1 byte for hour, 2 for earnings
@Data
@AllArgsConstructor
public class TripHourData {
    private byte hour;
    // in filter we remove total > 300 (30k), so encoding as short is fine
    private short earnings;

    public TripHourData(TripData data) {
        this(TripData.Dates.getHour(data.getPickUpDate()), (short) data.getTotal());
    }
}
