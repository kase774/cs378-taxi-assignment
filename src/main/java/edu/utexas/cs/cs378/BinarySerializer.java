// modified in week 2
package edu.utexas.cs.cs378;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

@UtilityClass
public class BinarySerializer {

    @SneakyThrows
    public void writeTaxiData(DataOutputStream out, TripData data) {
        out.write(data.carHash);
        out.write(data.driverHash);
        out.writeInt(data.pickUpDate);
        out.writeInt(data.dropOffDate);
        out.writeShort(data.durationSeconds);
        out.writeShort(data.distanceInMiles);
        out.writeInt(data.pickUpLong);
        out.writeInt(data.pickUpLat);
        out.writeInt(data.dropOffLong);
        out.writeInt(data.dropOffLat);
        out.writeByte(data.method.ordinal());
        out.writeShort(encodeAsShort(data.fare));
        out.writeShort(data.surcharge);
        out.writeByte(data.mtaTax);
        out.writeShort(data.tip);
        out.writeShort(data.tolls);
        out.writeShort(encodeAsShort(data.total));
    }

    @SneakyThrows
    public TripData readTaxiData(DataInputStream in) {
        return new TripData(readMd5(in), readMd5(in), in.readInt(), in.readInt(), in.readShort(),
                in.readShort(), in.readInt(), in.readInt(), in.readInt(), in.readInt(),
                readPaymentMethod(in), decodeFromShort(in.readShort()), in.readShort(), in.readByte(),
                in.readShort(), in.readShort(), decodeFromShort(in.readShort()));
    }

    @SneakyThrows
    public byte[] convertToByteArray(TripDriverCarTotalData data) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(34);
        byteBuffer.put(data.carHash);
        byteBuffer.put(data.driverHash);
        byteBuffer.putShort(encodeAsShort(data.total));
        return byteBuffer.array();
    }

    // returns null when it can't read anymore
    @SneakyThrows
    public TripDriverCarTotalData readDriverCarEarningsData(DataInputStream in) {
        try {
            return new TripDriverCarTotalData(readMd5(in), readMd5(in), decodeFromShort(in.readShort()));
        } catch (IOException e) {
            return null;
        }
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
