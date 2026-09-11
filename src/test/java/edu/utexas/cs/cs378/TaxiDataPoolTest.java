package edu.utexas.cs.cs378;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TaxiDataPoolTest {

    @Test
    void roundTripsSingleRecordWithBoundaryValues() {
        TaxiDataPool pool = new TaxiDataPool();
        TaxiData data = new TaxiData();
        data.taxiIdMd5 = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
        data.taxiLicenseMd5 = new byte[]{15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0};
        data.pickUpDate = 2764800;
        data.dropOffDate = 2764920;
        data.durationSeconds = 10800;
        data.distanceInMiles = 9585;
        data.pickUpLong = -73956528;
        data.pickUpLat = 40716976;
        data.dropOffLong = -73962440;
        data.dropOffLat = 40715008;
        data.method = TaxiData.PaymentMethod.CRD;
        data.fare = 50000;   // > Short.MAX_VALUE
        data.surcharge = 1250;
        data.mtaTax = 50;
        data.tip = 17700;
        data.tolls = 2000;
        data.total = 65000;  // > Short.MAX_VALUE

        int index = pool.addTaxiAndGetIndex(data);
        assertEquals(0, index);
        assertEquals(TaxiDataPool.SIZE_PER_TAXI_DATA, pool.size());

        TaxiData read = pool.read(index);
        assertArrayEquals(data.taxiIdMd5, read.taxiIdMd5);
        assertArrayEquals(data.taxiLicenseMd5, read.taxiLicenseMd5);
        assertEquals(data.pickUpDate, read.pickUpDate);
        assertEquals(data.dropOffDate, read.dropOffDate);
        assertEquals(data.durationSeconds, read.durationSeconds);
        assertEquals(data.distanceInMiles, read.distanceInMiles);
        assertEquals(data.pickUpLong, read.pickUpLong);
        assertEquals(data.pickUpLat, read.pickUpLat);
        assertEquals(data.dropOffLong, read.dropOffLong);
        assertEquals(data.dropOffLat, read.dropOffLat);
        assertEquals(data.method, read.method);
        assertEquals(data.fare, read.fare);
        assertEquals(data.surcharge, read.surcharge);
        assertEquals(data.mtaTax, read.mtaTax);
        assertEquals(data.tip, read.tip);
        assertEquals(data.tolls, read.tolls);
        assertEquals(data.total, read.total);
    }

    @Test
    void roundTripsSampleOfSmallCsv() throws Exception {
        List<TaxiData> originals = Files.lines(Path.of("taxi-data-sorted-small.csv"), StandardCharsets.UTF_8)
                .limit(10_000)
                .map(StringSerialization::parseLine)
                .collect(Collectors.toList());

        TaxiDataPool pool = new TaxiDataPool();
        int[] indices = new int[originals.size()];
        for (int i = 0; i < originals.size(); i++) {
            indices[i] = pool.addTaxiAndGetIndex(originals.get(i));
            assertEquals(i, indices[i]);
        }

        for (int i = 0; i < originals.size(); i++) {
            assertEquals(originals.get(i), pool.read(indices[i]));
        }
    }

    @Test
    void appendsConsecutiveRecordsAtConsecutiveIndices() {
        TaxiDataPool pool = new TaxiDataPool();
        TaxiData first = sample(1);
        TaxiData second = sample(2);

        assertEquals(0, pool.addTaxiAndGetIndex(first));
        assertEquals(1, pool.addTaxiAndGetIndex(second));
        assertEquals(2 * TaxiDataPool.SIZE_PER_TAXI_DATA, pool.size());
        assertEquals(first, pool.read(0));
        assertEquals(second, pool.read(1));
    }

    @Test
    void encodesFareIdAndIndexInKey() {
        TaxiData data = sample(42);
        long key = TaxiDataPool.getKeyAndAddToPool(data);

        int id = (int) ((key >>> 32) & 0xFF);
        int index = (int) (key & 0xFFFFFFFFL);
        int fare = (int) (key >>> 40);

        assertEquals(TaxiDataPool.getPoolId(), id);
        assertEquals(data.fare, fare);
        assertEquals(data, TaxiDataPool.getFromPools(key));

        long second = TaxiDataPool.getKeyAndAddToPool(sample(43));
        assertEquals(index + 1, (int) (second & 0xFFFFFFFFL));
    }

    private static TaxiData sample(int seed) {
        TaxiData data = new TaxiData();
        data.taxiIdMd5 = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, (byte) seed};
        data.taxiLicenseMd5 = new byte[]{15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, (byte) seed};
        data.pickUpDate = 2764800;
        data.dropOffDate = 2764920;
        data.durationSeconds = (short) (120 + seed);
        data.distanceInMiles = (short) (44 + seed);
        data.pickUpLong = -73956528;
        data.pickUpLat = 40716976;
        data.dropOffLong = -73962440;
        data.dropOffLat = 40715008;
        data.method = TaxiData.PaymentMethod.CRD;
        data.fare = 350 + seed;
        data.surcharge = 50;
        data.mtaTax = 50;
        data.tip = 0;
        data.tolls = 0;
        data.total = 500 + seed;
        return data;
    }
}
