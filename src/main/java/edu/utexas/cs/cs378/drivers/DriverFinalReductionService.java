package edu.utexas.cs.cs378.drivers;

import edu.utexas.cs.cs378.Archetypes;
import edu.utexas.cs.cs378.BinarySerializer;

import java.util.Arrays;

public class DriverFinalReductionService {

    private static final int NUMBER_OF_CLIENTS = 2;
    private static final int TAKE_HIGHEST = 10;

    private static final DriversEarningsAggregateData aggregateData =
            new DriversEarningsAggregateData();


    // usage:
    // launch; this will output the socket address
    public static void main(String[] args) {
        Archetypes.finalReductionService(NUMBER_OF_CLIENTS, inputStream -> {
            synchronized (aggregateData) {
                aggregateData.merge(BinarySerializer.readDriversEarningsAggregateData(inputStream));
            }
        });

        aggregateData.getTop(TAKE_HIGHEST).forEach(System.out::println);
    }
}
