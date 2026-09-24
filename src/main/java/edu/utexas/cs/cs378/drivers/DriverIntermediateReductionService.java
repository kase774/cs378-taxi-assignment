package edu.utexas.cs.cs378.drivers;

import edu.utexas.cs.cs378.Archetypes;
import edu.utexas.cs.cs378.BinarySerializer;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;

public class DriverIntermediateReductionService {

    private static final Logger LOG = LoggerFactory.getLogger(DriverIntermediateReductionService.class);
    private static final DriversEarningsAggregateData aggregateData = new DriversEarningsAggregateData();

    private static void registerAllTripData(DataInputStream inputStream) {
        // this part isn't parallelized because we are limited by reading anyway (the cost
        // of register is basically 0)

        int counter = 0;
        try {
            //noinspection InfiniteLoopStatement
            while (true) {
                aggregateData.addData(BinarySerializer.readDriverEarnRateData(inputStream));
                counter++;
            }
        } catch (Exception ignored) {}

        LOG.info("finished storing all data: received {} data", counter);
    }

    @SneakyThrows
    public static void main(String[] args) {
        Archetypes.intermediateReductionService(
                DriverIntermediateReductionService::registerAllTripData,
                () -> BinarySerializer.convertToByteArray(aggregateData)
        );
    }
}
