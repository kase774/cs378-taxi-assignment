package edu.utexas.cs.cs378.hours;

import edu.utexas.cs.cs378.Archetypes;
import edu.utexas.cs.cs378.BinarySerializer;
import edu.utexas.cs.cs378.Parameters;

import java.util.Arrays;

public class HourFinalReductionService {

    private static final int TAKE_HIGHEST = 3;

    private static final HoursEarningsAggregateData aggregateData =
            new HoursEarningsAggregateData();


    // usage:
    // launch; this will output the socket address
    public static void main(String[] args) {
        Archetypes.finalReductionService(Parameters.getNumberOfClients(), inputStream -> {
            synchronized (aggregateData) {
                aggregateData.merge(BinarySerializer.readHoursEarningsAggregateData(inputStream));
            }
        });

        HourFinalData[] data = aggregateData.getDataPoints();
        Arrays.sort(data);

        for (int i = 0; i < TAKE_HIGHEST; i++) {
            System.out.println(data[data.length - i - 1]);
        }
    }
}
