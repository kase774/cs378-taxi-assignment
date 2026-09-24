package edu.utexas.cs.cs378.drivers;

import edu.utexas.cs.cs378.Md5Wrapper;
import lombok.Data;

import java.util.*;

@Data
public class DriversEarningsAggregateData {
    // one entry per driver
    private final Map<Md5Wrapper, DriverFinalData> drivers = new HashMap<>();

    public void addData(DriverEarnRateData data) {
        drivers.computeIfAbsent(new Md5Wrapper(data.getDriverHash()), DriverFinalData::new)
                .addData(data);
    }

    public void put(DriverFinalData data) {
        drivers.merge(data.getDriver(), data, DriverFinalData::merge);
    }

    public List<DriverFinalData> getTop(int count) {
        PriorityQueue<DriverFinalData> queue = new PriorityQueue<>(Comparator.reverseOrder());
        queue.addAll(drivers.values());
        List<DriverFinalData> result = new ArrayList<>();
        for (int i = 0; i < count && !queue.isEmpty(); i++) {
            result.add(queue.poll());
        }
        return result;
    }

    public void merge(DriversEarningsAggregateData data) {
        for (DriverFinalData point : data.drivers.values()) {
            put(point);
        }
    }
}
