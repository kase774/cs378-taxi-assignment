package edu.utexas.cs.cs378;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class StringSerializationTest {

    private static final String FIRST_LINE =
            "07290D3599E7A0D62097A346EFCC1FB5,E7750A37CAB07D0DFF0AF7E3573AC141,"
                    + "2013-01-01 00:00:00,2013-01-01 00:02:00,120,0.44,"
                    + "-73.956528,40.716976,-73.962440,40.715008,CSH,"
                    + "3.50,0.50,0.50,0.00,0.00,4.50";

    private static final String HEAD =
            "07290D3599E7A0D62097A346EFCC1FB5,E7750A37CAB07D0DFF0AF7E3573AC141,";

    private static final String TAIL =
            ",120,0.44,-73.956528,40.716976,-73.962440,40.715008,CSH,"
                    + "3.50,0.50,0.50,0.00,0.00,4.50";

    private static String lineForDates(String pickUp, String dropOff) {
        return HEAD + pickUp + "," + dropOff + TAIL;
    }

    private static String lineForMethod(String method) {
        return HEAD + "2013-01-01 00:00:00,2013-01-01 00:02:00,120,0.44,"
                + "-73.956528,40.716976,-73.962440,40.715008," + method
                + ",3.50,0.50,0.50,0.00,0.00,4.50";
    }

    // ---------------------------------------------------------------- parsing

    @Test
    void parsesKnownLine() {
        TaxiData data = StringSerialization.parseLine(FIRST_LINE);

        assertNotNull(data);
        assertArrayEquals(
                new byte[]{7, 41, 13, 53, -103, -25, -96, -42, 32, -105, -93, 70, -17, -52, 31, -75},
                data.taxiIdMd5);
        assertArrayEquals(
                new byte[]{-25, 117, 10, 55, -54, -80, 125, 13, -1, 10, -9, -29, 87, 58, -63, 65},
                data.taxiLicenseMd5);
        assertEquals(2764800, data.pickUpDate);
        assertEquals(2764920, data.dropOffDate);
        assertEquals(120, data.durationSeconds);
        assertEquals(44, data.distanceInMiles);
        assertEquals(-73956528, data.pickUpLong);
        assertEquals(40716976, data.pickUpLat);
        assertEquals(-73962440, data.dropOffLong);
        assertEquals(40715008, data.dropOffLat);
        assertEquals(TaxiData.PaymentMethod.CSH, data.method);
        assertEquals(350, data.fare);
        assertEquals(50, data.surcharge);
        assertEquals(50, data.mtaTax);
        assertEquals(0, data.tip);
        assertEquals(0, data.tolls);
        assertEquals(450, data.total);
    }

    @Test
    void parsesEveryPaymentMethod() {
        for (TaxiData.PaymentMethod method : TaxiData.PaymentMethod.values()) {
            TaxiData data = StringSerialization.parseLine(lineForMethod(method.name()));
            assertNotNull(data, method.name());
            assertEquals(method, data.method);
        }
    }

    @Test
    void parsesEveryLineOfSmallCsv() throws IOException {
        long total = Files.lines(Path.of("taxi-data-sorted-small.csv"), StandardCharsets.UTF_8).count();
        assertEquals(1_999_999, total);

        long parsed = Files.lines(Path.of("taxi-data-sorted-small.csv"), StandardCharsets.UTF_8)
                .map(StringSerialization::parseLine)
                .filter(Objects::nonNull)
                .count();
        assertEquals(total, parsed);
    }

    @Test
    void returnsNullForMalformedMd5() {
        String bad = "G7290D3599E7A0D62097A346EFCC1FB5,E7750A37CAB07D0DFF0AF7E3573AC141,"
                + "2013-01-01 00:00:00,2013-01-01 00:02:00,120,0.44,"
                + "-73.956528,40.716976,-73.962440,40.715008,CSH,"
                + "3.50,0.50,0.50,0.00,0.00,4.50";
        assertNull(StringSerialization.parseLine(bad));
    }

    @Test
    void returnsNullForLowercaseMd5() {
        assertNull(StringSerialization.parseLine(FIRST_LINE.toLowerCase()));
    }

    @Test
    void returnsNullForWrongDelimiter() {
        String bad = FIRST_LINE.replaceFirst(",", ";");
        assertNull(StringSerialization.parseLine(bad));
    }

    @Test
    void returnsNullForExtraLeadingField() {
        String bad = "X" + FIRST_LINE;
        assertNull(StringSerialization.parseLine(bad));
    }

    // --------------------------------------------------------- serialize back

    @Test
    void serializesKnownLineExactly() {
        TaxiData data = StringSerialization.parseLine(FIRST_LINE);
        assertNotNull(data);
        assertEquals(FIRST_LINE, StringSerialization.toString(data));
    }

    @Test
    void roundTripsKnownLineThroughParser() {
        TaxiData data = StringSerialization.parseLine(FIRST_LINE);
        assertNotNull(data);

        TaxiData reparsed = StringSerialization.parseLine(StringSerialization.toString(data));
        assertEquals(data, reparsed);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "2013-01-01 00:00:00",
            "2013-01-31 23:59:59",
            "2013-02-28 12:34:56",
            "2013-03-31 00:00:01",
            "2013-04-30 09:08:07",
            "2013-05-31 18:00:00",
            "2013-06-28 06:07:08",
            "2014-01-01 00:00:00"
    })
    void roundTripsDatesIncludingDay31(String date) {
        String line = lineForDates(date, "2013-01-01 00:02:00");
        TaxiData data = StringSerialization.parseLine(line);

        assertNotNull(data);
        assertEquals(line, StringSerialization.toString(data));
        assertEquals(data, StringSerialization.parseLine(StringSerialization.toString(data)));
    }

    @Test
    void serializesMd5WithFullByteRange() {
        TaxiData data = new TaxiData();
        data.taxiIdMd5 = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
        data.taxiLicenseMd5 = new byte[]{16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31};
        data.pickUpDate = 2764800;
        data.dropOffDate = 2764920;
        data.durationSeconds = 120;
        data.distanceInMiles = 44;
        data.pickUpLong = -73956528;
        data.pickUpLat = 40716976;
        data.dropOffLong = -73962440;
        data.dropOffLat = 40715008;
        data.method = TaxiData.PaymentMethod.CSH;
        data.fare = 350;
        data.surcharge = 50;
        data.mtaTax = 50;
        data.tip = 0;
        data.tolls = 0;
        data.total = 450;

        String expected = "000102030405060708090A0B0C0D0E0F"
                + ",101112131415161718191A1B1C1D1E1F"
                + ",2013-01-01 00:00:00,2013-01-01 00:02:00,120,0.44,"
                + "-73.956528,40.716976,-73.962440,40.715008,CSH,"
                + "3.50,0.50,0.50,0.00,0.00,4.50";
        assertEquals(expected, StringSerialization.toString(data));
    }

    @Test
    void serializesZeroAndNegativeValues() {
        TaxiData data = new TaxiData();
        data.taxiIdMd5 = new byte[16];
        data.taxiLicenseMd5 = new byte[16];
        data.pickUpDate = 2764800;
        data.dropOffDate = 2764800;
        data.durationSeconds = 0;
        data.distanceInMiles = 0;
        data.pickUpLong = 0;
        data.pickUpLat = -4999;
        data.dropOffLong = 0;
        data.dropOffLat = -4999;
        data.method = TaxiData.PaymentMethod.NOC;
        data.fare = 0;
        data.surcharge = 0;
        data.mtaTax = 0;
        data.tip = 0;
        data.tolls = 0;
        data.total = 0;

        String expected = "00000000000000000000000000000000"
                + ",00000000000000000000000000000000"
                + ",2013-01-01 00:00:00,2013-01-01 00:00:00,0,0.00,"
                + "0.000000,-0.004999,0.000000,-0.004999,NOC,"
                + "0.00,0.00,0.00,0.00,0.00,0.00";
        assertEquals(expected, StringSerialization.toString(data));
    }

    @Test
    void roundTripsWholeSmallCsvThroughSerializeParse() throws IOException {
        Files.lines(Path.of("taxi-data-sorted-small.csv"), StandardCharsets.UTF_8)
                .limit(50_000)
                .forEach(line -> {
                    TaxiData data = StringSerialization.parseLine(line);
                    assertNotNull(data, line);
                    TaxiData reparsed = StringSerialization.parseLine(StringSerialization.toString(data));
                    assertEquals(data, reparsed, line);
                });
    }
}
