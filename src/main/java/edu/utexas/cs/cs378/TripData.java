package edu.utexas.cs.cs378;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.UtilityClass;


// all models
// the order in which they are stored is equal to the order in which they
// are going to be serialized

// memory usage
// serialized (packed) layout, bytes per record:
//   taxiIdMd5          16
//   taxiLicenseMd5     16
//   pickUpDate          4
//   dropOffDate         4
//   durationSeconds     2  (max 10800 fits 14 bits)
//   distanceInMiles     2  (max 100.00 mi => 10000 hundredths)
//   pickUpLong          4
//   pickUpLat           4
//   dropOffLong         4
//   dropOffLat          4
//   method              1
//   fare                2  (ushort; max 50000 > Short.MAX_VALUE)
//   surcharge           2
//   mtaTax              1
//   tip                 2
//   tolls               2
//   total               2  (ushort; max 65000 > Short.MAX_VALUE)
//   ----------------------------------
//   total              72 bytes/record
// 87m records => ~6.3 GB packed, + long[87m] sort keys ~0.7 GB => ~7.0 GB
//
// further packing options: derive dropOffDate from pickUpDate + duration (-4 B)
// if that invariant holds, or dictionary-encode the two md5s (-24 B)
//

@Data
@AllArgsConstructor
public class TripData {

    // each one of these is 16 bytes
    private byte[] carHash;
    private byte[] driverHash;

    // from 2013-01-01 to 2013-06-28
    // except for 3: 2 of 2013-07-18
    // one of 2013-12-12, 2014-01-03
    // these are actually all errored inputs:
    // the 2 2013-07-18 ones have lat and long 0.000000
    private int pickUpDate;
    private int dropOffDate;
    // max 10800
    private short durationSeconds;
    private short distanceInMiles;
    // 8 digits, always 6 dec places
    private int pickUpLong;
    private int pickUpLat;
    private int dropOffLong;
    private int dropOffLat;
    // encode index as 1 byte
    private PaymentMethod method;
    // 50k max
    // encode as ushort
    private int fare;
    private short surcharge;
    private byte mtaTax;
    private short tip;
    private short tolls;
    // 65k max, encode as ushort
    private int total;

    public enum PaymentMethod {
        CSH, CRD, NOC, DIS, UNK;
    }

    public boolean correctTotalSum() {
        int calculatedTotal = getFare() + getSurcharge() + getMtaTax() + getTip() + getTolls();
        return calculatedTotal == getTotal();
    }

    @UtilityClass
    public static class Dates {

        private int monthValue(int timestamp) {
            int date = timestamp / 86400;
            int month = date / 31;
            if (date % 31 == 0) {
                month--;
            }
            return month;
        }

        public int getYear(int timestamp) {
            return 2013 + (monthValue(timestamp) - 1) / 12;
        }

        public byte getMonth(int timestamp) {
            return (byte) ((monthValue(timestamp) - 1) % 12 + 1);
        }

        public byte getDay(int timestamp) {
            int day = timestamp / 86400 % 31;
            return (byte) (day == 0 ? 31 : day);
        }

        public byte getHour(int timestamp) {
            return (byte) (timestamp / 3600 % 24);
        }

        public byte getMinute(int timestamp) {
            return (byte) (timestamp / 60 % 60);
        }

        public byte getSecond(int timestamp) {
            return (byte) (timestamp % 60);
        }
    }
}
