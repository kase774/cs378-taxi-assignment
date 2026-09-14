package edu.utexas.cs.cs378;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringSerializer {

    static class Cursor {
        int index;
        String line;
        boolean errorFlag = false;

        public Cursor(String line, int index) {
            this.line = line;
            this.index = index;
        }

        public char nextChar() {

            return line.charAt(index++);
        }

        public char peek() {
            return line.charAt(index);
        }

        public boolean expect(char c) {
            if (nextChar() != c) {
                errorFlag = true;
            }
            return errorFlag;
        }

        public byte[] parseMd5() {
            byte[] bytes = new byte[16];
            for (int i = 0; i < 16; i++) {
                int high = hex();
                int low = hex();
                if ((high | low) < 0) {
                    errorFlag = true;
                    return null;
                }
                bytes[i] = (byte) ((high << 4) | low);
            }
            return bytes;
        }

        private static final int digitOffset = '0';
        private static final int letterOffset = 'A' - 10;

        private int hex() {
            char c = nextChar();
            if (c >= '0' && c <= '9') return c - digitOffset;
            if (c >= 'A' && c <= 'F') return c - letterOffset;
            return -1;
        }

        // parse date from YYYY-MM-DD HH:mm:SS
        // returned int should be as:
        // (((YYYY - 2013) * 12 + MM) * 31 + DD) + ...
        public int parseDate() {
            int year = number(4);
            expect('-');
            int month = number(2);
            expect('-');
            int day = number(2);
            expect(' ');
            int hour = number(2);
            expect(':');
            int minute = number(2);
            expect(':');
            int second = number(2);
            int date = ((year - 2013) * 12 + month) * 31 + day;
            return date * 86400 + hour * 3600 + minute * 60 + second;
        }

        private int decimal() {
            char c = nextChar();
            if (c >= '0' && c <= '9') return c - digitOffset;
            return -1;
        }

        private int number(int digits) {
            int value = 0;
            for (int i = 0; i < digits; i++) {
                value *= 10;
                int decimal = decimal();
                if (decimal < 0) {
                    errorFlag = true;
                    return -1;
                }
                value += decimal;
            }
            return value;
        }

        public int parseNumber() {
            int value = 0;
            while (true) {
                int decimal = decimal();
                if (decimal < 0) {
                    index--;
                    break;
                }
                value = value * 10 + decimal;
            }
            return value;
        }

        public int parse6Dec() {
            boolean negative = peek() == '-';
            if (negative) {
                nextChar();
            }
            int value = parseNumber() * 1000000;
            expect('.');
            value += number(6);
            return negative ? -value : value;
        }

        public TripData.PaymentMethod parsePaymentMethod() {
            String next3Chars = line.substring(index, index + 3);
            index += 3;
            for (TripData.PaymentMethod method : TripData.PaymentMethod.values()) {
                if (method.name().equals(next3Chars)) {
                    return method;
                }
            }
            errorFlag = true;
            return null;
        }

        public int parse2Dec() {
            int value = 100;
            value *= parseNumber();
            expect('.');
            value += number(2);
            return value;
        }

        public TripData parseTaxiData() {
            byte[] taxiIdMd5 = parseMd5();
            if (expect(',')) return null;
            byte[] taxiLicenseMd5 = parseMd5();
            if (expect(',')) return null;
            int pickUpDate = parseDate();
            if (expect(',')) return null;
            int dropOffDate = parseDate();
            if (expect(',')) return null;
            short durationSeconds = (short) parseNumber();
            if (expect(',')) return null;
            short distanceInMiles = (short) parse2Dec();
            if (expect(',')) return null;
            int pickUpLong = parse6Dec();
            if (expect(',')) return null;
            int pickUpLat = parse6Dec();
            if (expect(',')) return null;
            int dropOffLong = parse6Dec();
            if (expect(',')) return null;
            int dropOffLat = parse6Dec();
            if (expect(',')) return null;
            TripData.PaymentMethod method = parsePaymentMethod();
            if (expect(',')) return null;
            int fare = parse2Dec();
            if (expect(',')) return null;
            short surcharge = (short) parse2Dec();
            if (expect(',')) return null;
            byte mtaTax = (byte) parse2Dec();
            if (expect(',')) return null;
            short tip = (short) parse2Dec();
            if (expect(',')) return null;
            short tolls = (short) parse2Dec();
            if (expect(',')) return null;
            int total = parse2Dec();
            if (errorFlag) return null;
            return new TripData(taxiIdMd5, taxiLicenseMd5, pickUpDate, dropOffDate,
                    durationSeconds, distanceInMiles, pickUpLong, pickUpLat,
                    dropOffLong, dropOffLat, method, fare, surcharge, mtaTax, tip,
                    tolls, total);
        }
    }

    public TripData parseLine(String line) {
        return new Cursor(line, 0).parseTaxiData();
    }

    private static final char[] HEX = "0123456789ABCDEF".toCharArray();
    private static final char[] DIGITS = "0123456789".toCharArray();

    private static void appendMd5(StringBuilder sb, byte[] md5) {
        for (byte b : md5) {
            sb.append(HEX[(b >> 4) & 0xF]).append(HEX[b & 0xF]);
        }
    }

    private static void appendMd5(StringBuilder sb, Md5Wrapper md5) {
        appendMd5(sb, md5.toArray());
    }

    private static void appendTwo(StringBuilder sb, int value) {
        sb.append(DIGITS[value / 10]).append(DIGITS[value % 10]);
    }

    // inverse of parseDate: value = (((YYYY - 2013) * 12 + MM) * 31 + DD) * 86400 + h*3600 + m*60 + s
    private static void appendDate(StringBuilder sb, int value) {
        int secondsOfDay = value % 86400;
        int date = value / 86400;
        int day = date % 31;
        int month = date / 31;
        if (day == 0) {
            day = 31;
            month--;
        }
        int year = 2013 + (month - 1) / 12;
        month = (month - 1) % 12 + 1;
        sb.append(year).append('-');
        appendTwo(sb, month);
        sb.append('-');
        appendTwo(sb, day);
        sb.append(' ');
        appendTwo(sb, secondsOfDay / 3600);
        sb.append(':');
        appendTwo(sb, secondsOfDay / 60 % 60);
        sb.append(':');
        appendTwo(sb, secondsOfDay % 60);
    }

    private static void append2Dec(StringBuilder sb, int value) {
        if (value < 0) {
            sb.append('-');
            value = -value;
        }
        sb.append(value / 100).append('.');
        appendTwo(sb, value % 100);
    }

    private static void append6Dec(StringBuilder sb, int value) {
        if (value < 0) {
            sb.append('-');
            value = -value;
        }
        sb.append(value / 1000000).append('.');
        int fraction = value % 1000000;
        appendTwo(sb, fraction / 10000);
        appendTwo(sb, fraction / 100 % 100);
        appendTwo(sb, fraction % 100);
    }


    public String toString(TripData input) {
        StringBuilder sb = new StringBuilder(160);

        appendMd5(sb, input.carHash);
        sb.append(',');
        appendMd5(sb, input.driverHash);

        sb.append(',');
        appendDate(sb, input.pickUpDate);
        sb.append(',');
        appendDate(sb, input.dropOffDate);
        sb.append(',').append(input.durationSeconds);

        sb.append(',');
        append2Dec(sb, input.distanceInMiles);

        sb.append(',');
        append6Dec(sb, input.pickUpLong);
        sb.append(',');
        append6Dec(sb, input.pickUpLat);
        sb.append(',');
        append6Dec(sb, input.dropOffLong);
        sb.append(',');
        append6Dec(sb, input.dropOffLat);

        sb.append(',').append(input.method);

        sb.append(',');
        append2Dec(sb, input.fare);
        sb.append(',');
        append2Dec(sb, input.surcharge);
        sb.append(',');
        append2Dec(sb, input.mtaTax);
        sb.append(',');
        append2Dec(sb, input.tip);
        sb.append(',');
        append2Dec(sb, input.tolls);
        sb.append(',');
        append2Dec(sb, input.total);

        return sb.toString();
    }

    // looks like (3FF2709163DE7036FCAA4E5A3324E4BF, 10, 3239458.19)
    public String toString(DriverCarEarnings earnings) {
        StringBuilder sb = new StringBuilder(64);

        sb.append('(');
        appendMd5(sb, earnings.getDriver());
        sb.append(", ").append(earnings.getCars().size());
        sb.append(", ");
        append2Dec(sb, earnings.getTotalEarnings());
        sb.append(')');

        return sb.toString();
    }

}
