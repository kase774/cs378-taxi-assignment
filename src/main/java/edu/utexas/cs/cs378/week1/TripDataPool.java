package edu.utexas.cs.cs378.week1;

import edu.utexas.cs.cs378.BinarySerializer;
import edu.utexas.cs.cs378.TripData;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
@Getter
public class TripDataPool extends ByteArrayOutputStream {

    // Week 1: Per thread pooling implementation

    private final DataOutputStream dataOutputStream = new DataOutputStream(this);
    private static final List<TripDataPool> list = Collections.synchronizedList(new ArrayList<>());
    private static final Map<Long, Integer> keyToIndex = new ConcurrentHashMap<>();

    public static int getPoolId() {
        Integer value = keyToIndex.get(Thread.currentThread().threadId());
        if (value == null) {
            synchronized (list) {
                value = list.size();
                list.add(new TripDataPool());
                synchronized (keyToIndex) {
                    keyToIndex.put(Thread.currentThread().threadId(), value);
                }
            }
        }
        return value;
    }

    public static long getSortingKey(TripData data) {
        int id = getPoolId();
        TripDataPool pool = list.get(id);
        return ((long) data.getFare()) << 40 | (long) id << 32 | (long) pool.addTaxiAndGetIndex(data);
    }

    private static final long ID_MASK = (1L << 32) - 1;
    private static final long MASK = ((1L << 40) - 1) ^ (ID_MASK);

    public static TripData getFromPools(long key) {
        int id = (int) ((key & MASK) >> 32);
        int index = (int) (key & ID_MASK);
        return list.get(id).read(index);
    }

    // taxi data pool object

    public static final int SIZE_PER_TAXI_DATA = 72;

    // growable buffer for memory
    public TripDataPool() {
        super(144);
    }

    @SneakyThrows
    public int addTaxiAndGetIndex(TripData data) {
        int index = count / SIZE_PER_TAXI_DATA;
        BinarySerializer.writeTaxiData(dataOutputStream, data);
        return index;
    }

    @SneakyThrows
    public TripData read(int index) {
        int offset = index * SIZE_PER_TAXI_DATA;
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(buf, offset,
                SIZE_PER_TAXI_DATA));
        return BinarySerializer.readTaxiData(in);
    }

}
