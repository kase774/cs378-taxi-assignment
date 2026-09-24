package edu.utexas.cs.cs378;

import edu.utexas.cs.cs378.drivers.DriverEarnRateData;
import edu.utexas.cs.cs378.drivers.DriverFinalData;
import edu.utexas.cs.cs378.drivers.DriversEarningsAggregateData;
import edu.utexas.cs.cs378.hours.HoursEarningsAggregateData;
import edu.utexas.cs.cs378.hours.TripHourData;
import edu.utexas.cs.cs378.week2.TripDriverCarTotalData;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.ByteBuffer;

@UtilityClass
public class BinarySerializer {

    @SneakyThrows
    public void writeTaxiData(DataOutputStream out, TripData data) {
        out.write(data.getCarHash());
        out.write(data.getDriverHash());
        out.writeInt(data.getPickUpDate());
        out.writeInt(data.getDropOffDate());
        out.writeShort(data.getDurationSeconds());
        out.writeShort(data.getDistanceInMiles());
        out.writeInt(data.getPickUpLong());
        out.writeInt(data.getPickUpLat());
        out.writeInt(data.getDropOffLong());
        out.writeInt(data.getDropOffLat());
        out.writeByte(data.getMethod().ordinal());
        out.writeShort(encodeAsShort(data.getFare()));
        out.writeShort(data.getSurcharge());
        out.writeByte(data.getMtaTax());
        out.writeShort(data.getTip());
        out.writeShort(data.getTolls());
        out.writeShort(encodeAsShort(data.getTotal()));
    }

    @SneakyThrows
    public TripData readTaxiData(DataInputStream in) {
        return new TripData(readMd5(in), readMd5(in), in.readInt(), in.readInt(), in.readShort(),
                in.readShort(), in.readInt(), in.readInt(), in.readInt(), in.readInt(),
                readPaymentMethod(in), decodeFromShort(in.readShort()), in.readShort(),
                in.readByte(), in.readShort(), in.readShort(), decodeFromShort(in.readShort()));
    }

    @SneakyThrows
    public byte[] convertToByteArray(TripDriverCarTotalData data) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(34);
        byteBuffer.put(data.getCarHash());
        byteBuffer.put(data.getDriverHash());
        byteBuffer.putShort(encodeAsShort(data.getTotal()));
        return byteBuffer.array();
    }

    @SneakyThrows
    public byte[] convertToByteArray(DriverEarnRateData data) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(20);
        byteBuffer.put(data.getDriverHash());
        byteBuffer.putShort(data.getDuration());
        byteBuffer.putShort(data.getEarnings());
        return byteBuffer.array();
    }

    // int count, then per driver: 16 byte hash + long totalEarnings + long totalDuration
    public byte[] convertToByteArray(DriversEarningsAggregateData data) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES + 32 * data.getDrivers().size());
        byteBuffer.putInt(data.getDrivers().size());
        for (DriverFinalData point : data.getDrivers().values()) {
            write(byteBuffer, point);
        }
        return byteBuffer.array();
    }

    public void write(ByteBuffer buffer, DriverFinalData data) {
        buffer.put(data.getDriver().toArray());
        buffer.putLong(data.getTotalEarnings());
        buffer.putLong(data.getTotalDuration());
    }

    @SneakyThrows
    public byte[] convertToByteArray(TripHourData data) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(3);
        byteBuffer.put(data.getHour());
        byteBuffer.putShort(data.getEarnings());
        return byteBuffer.array();
    }

    public byte[] convertToByteArray(HoursEarningsAggregateData data) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(24 * 8 + 24 * 4);
        long[] totalEarnings = data.getTotalEarnings();
        int[] rideCount = data.getRideCount();
        for (int i = 0; i < 24; i++) {
            byteBuffer.putLong(totalEarnings[i]);
        }
        for (int i = 0; i < 24; i++) {
            byteBuffer.putInt(rideCount[i]);
        }
        return byteBuffer.array();
    }

    @SneakyThrows
    public TripHourData readTripHourData(DataInputStream in) {
        return new TripHourData(in.readByte(), in.readShort());
    }

    @SneakyThrows
    public DriverEarnRateData readDriverEarnRateData(DataInputStream in) {
        return new DriverEarnRateData(readMd5(in), in.readShort(), in.readShort());
    }

    @SneakyThrows
    public DriversEarningsAggregateData readDriversEarningsAggregateData(DataInputStream in) {
        int count = in.readInt();
        DriversEarningsAggregateData data = new DriversEarningsAggregateData();
        for (int i = 0; i < count; i++) {
            DriverFinalData point = new DriverFinalData(new Md5Wrapper(readMd5(in)));
            point.setTotalEarnings(in.readLong());
            point.setTotalDuration(in.readLong());
            data.put(point);
        }
        return data;
    }

    @SneakyThrows
    public TripDriverCarTotalData readDriverCarEarningsData(DataInputStream in) {
        return new TripDriverCarTotalData(readMd5(in), readMd5(in),
                decodeFromShort(in.readShort()));
    }

    @SneakyThrows
    public HoursEarningsAggregateData readHoursEarningsAggregateData(DataInputStream in) {
        HoursEarningsAggregateData data = new HoursEarningsAggregateData();
        long[] totalEarnings = data.getTotalEarnings();
        int[] rideCount = data.getRideCount();
        for (int i = 0; i < 24; i++) {
            totalEarnings[i] = in.readLong();
        }
        for (int i = 0; i < 24; i++) {
            rideCount[i] = in.readInt();
        }
        return data;
    }

    // these functions all throw exceptions if they fail at reading (rather than return -1 like
    // the native stream implementation
    @SneakyThrows
    private static TripData.PaymentMethod readPaymentMethod(DataInputStream in) {
        return TripData.PaymentMethod.values()[in.readUnsignedByte()];
    }

    @SneakyThrows
    private static byte[] readMd5(DataInputStream in) {
        byte[] md5 = new byte[16];
        in.readFully(md5);
        return md5;
    }

    // encodes as ushort
    short encodeAsShort(int input) {
        return (short) input;
    }

    // decodes reading as if it were a ushort. this allows us to store values
    // up to 65535 in 32 bits (Short.MAX_VALUE is 32767)
    int decodeFromShort(short input) {
        if (input < 0) return input + (1 << 16);
        return input;
    }

}
