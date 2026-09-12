package edu.utexas.cs.cs378;

import lombok.Getter;
import lombok.SneakyThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class TaxiDataPool extends ByteArrayOutputStream {
    static final int SIZE_PER_TAXI_DATA = 72;

    private final DataOutputStream dataOutputStream = new DataOutputStream(this);
    static final List<TaxiDataPool> list = Collections.synchronizedList(new ArrayList<>());
    static final Map<Long, Integer> keyToIndex = new ConcurrentHashMap<>();

    public static int getPoolId() {
        Integer value = keyToIndex.get(Thread.currentThread().threadId());
        if (value == null) {
            synchronized (list) {
                value = list.size();
                list.add(new TaxiDataPool());
                synchronized (keyToIndex) {
                    keyToIndex.put(Thread.currentThread().threadId(), value);
                }
            }
        }
        return value;
    }

    public static long getSortingKey(TaxiData data) {
        int id = getPoolId();
        TaxiDataPool pool = list.get(id);
        return ((long) data.fare) << 40 | (long) id << 32 | (long) pool.addTaxiAndGetIndex(data);
    }

    private static final long ID_MASK = (1L << 32) - 1;
    private static final long MASK = ((1L << 40) - 1) ^ (ID_MASK);

    public static TaxiData getFromPools(long key) {
        int id = (int) ((key & MASK) >> 32);
        int index = (int) (key & ID_MASK);
        return list.get(id).read(index);
    }

    private static short fromUShort(int input) {
        return (short) input;
    }

    private static int toUShort(short input) {
        if (input < 0) return input + (1 << 16);
        return input;
    }

    TaxiDataPool() {
        super(144);
    }

    @SneakyThrows
    public int addTaxiAndGetIndex(TaxiData data) {
        int index = count / SIZE_PER_TAXI_DATA;
        dataOutputStream.write(data.taxiIdMd5);
        dataOutputStream.write(data.taxiLicenseMd5);
        dataOutputStream.writeInt(data.pickUpDate);
        dataOutputStream.writeInt(data.dropOffDate);
        dataOutputStream.writeShort(data.durationSeconds);
        dataOutputStream.writeShort(data.distanceInMiles);
        dataOutputStream.writeInt(data.pickUpLong);
        dataOutputStream.writeInt(data.pickUpLat);
        dataOutputStream.writeInt(data.dropOffLong);
        dataOutputStream.writeInt(data.dropOffLat);
        dataOutputStream.writeByte(data.method.ordinal());
        dataOutputStream.writeShort(fromUShort(data.fare));
        dataOutputStream.writeShort(data.surcharge);
        dataOutputStream.writeByte(data.mtaTax);
        dataOutputStream.writeShort(data.tip);
        dataOutputStream.writeShort(data.tolls);
        dataOutputStream.writeShort(fromUShort(data.total));
        return index;
    }

    @SneakyThrows
    public TaxiData read(int index) {
        int offset = index * SIZE_PER_TAXI_DATA;
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(buf, offset,
                SIZE_PER_TAXI_DATA));
        byte[] taxiIdMd5 = readMd5(in);
        byte[] taxiLicenseMd5 = readMd5(in);
        return new TaxiData(
                taxiIdMd5,
                taxiLicenseMd5,
                in.readInt(),
                in.readInt(),
                in.readShort(),
                in.readShort(),
                in.readInt(),
                in.readInt(),
                in.readInt(),
                in.readInt(),
                TaxiData.PaymentMethod.values()[in.readUnsignedByte()],
                toUShort(in.readShort()),
                in.readShort(),
                in.readByte(),
                in.readShort(),
                in.readShort(),
                toUShort(in.readShort()));
    }

    @SneakyThrows
    private static byte[] readMd5(DataInputStream in) {
        byte[] md5 = new byte[16];
        in.readFully(md5);
        return md5;
    }

}
