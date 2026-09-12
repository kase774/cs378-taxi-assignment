package edu.utexas.cs.cs378;

import lombok.SneakyThrows;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class Main {

    public static final String BIG_CSV = "taxi-data-sorted-large-n.csv";
    public static final String SMALL_CSV = "taxi-data-sorted-small.csv";

    @SneakyThrows
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        BufferedWriter writer =
                Files.newBufferedWriter(new File("SORTED-FILE-RESULT.txt").toPath());
        //noinspection resource
        long[] keys =
                Files.lines(new File(BIG_CSV).toPath(), StandardCharsets.UTF_8).parallel().map(StringSerialization::parseLine).filter(Objects::nonNull).mapToLong(TaxiDataPool::getSortingKey)
                                .sorted().toArray();
        System.out.println(System.currentTimeMillis() - start);
        StringBuffer buffer = new StringBuffer(3, 1000);

        new Thread(() -> {
            //noinspection StatementWithEmptyBody
            while (buffer.moveDataTo(writer));
            try {
                writer.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();

        AtomicInteger curIndex = new AtomicInteger(0);
        // this way, parallelization only takes from the beginning, vs an IntStream with
        // range would divide the work among the threads in a more equal fashion (which would
        // undermine the whole purpose of using the buffer). this way, the threads divide the
        // work from the base together, so multiple threads can work on the same thing at the
        // same time
        IntStream.generate(curIndex::getAndIncrement).limit(keys.length).parallel().forEach(index -> buffer.writeTo(index, StringSerialization.toString(TaxiDataPool.getFromPools(keys[index]))));

        buffer.writingFinished();
        System.out.println(System.currentTimeMillis() - start);
    }

}
