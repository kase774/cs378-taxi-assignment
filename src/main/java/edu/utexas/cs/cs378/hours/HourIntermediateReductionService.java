package edu.utexas.cs.cs378.hours;

import edu.utexas.cs.cs378.Archetypes;
import edu.utexas.cs.cs378.BinarySerializer;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import static edu.utexas.cs.cs378.Networking.*;

public class HourIntermediateReductionService {

    private static final Logger LOG = LoggerFactory.getLogger(HourIntermediateReductionService.class);
    private static final HoursEarningsAggregateData aggregateData = new HoursEarningsAggregateData();

    private static void registerAllTripData(DataInputStream inputStream) {
        // this part isn't parallelized because we are limited by reading anyway (the cost
        // of register is basically 0)

        try {
            //noinspection InfiniteLoopStatement
            while (true) {
                TripHourData data = BinarySerializer.readTripHourData(inputStream);
                aggregateData.addData(data);
            }
        } catch (Exception ignored) {}

        LOG.info("finished storing all data: received {} data",
                aggregateData.getTotalRideCount());
    }

    @SneakyThrows
    public static void main(String[] args) {
        Archetypes.intermediateReductionService(
                HourIntermediateReductionService::registerAllTripData,
                () -> BinarySerializer.convertToByteArray(aggregateData)
        );
    }
}
