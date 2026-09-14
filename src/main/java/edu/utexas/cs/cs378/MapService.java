package edu.utexas.cs.cs378;

import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class MapService {

    private static final Logger LOG = LoggerFactory.getLogger(MapService.class);

    public static final String BIG_CSV = "taxi-data-sorted-large-n.csv";
    public static final String SMALL_CSV = "taxi-data-sorted-small.csv";

    @SneakyThrows
    private static InetSocketAddress readInInetSocketAddress() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String dataString = reader.readLine();
        String[] parts = dataString.split(":");
        String host = parts[0];
        int port = Integer.parseInt(parts[1]);

        return new InetSocketAddress(host, port);
    }

    @SneakyThrows
    public static void main(String[] args) {
        // create a socket to connect to the server running on localhost at port number 9090
        InetSocketAddress serverAddress = readInInetSocketAddress();
        Socket socket = new Socket(serverAddress.getAddress(), serverAddress.getPort());
        LOG.info("connected to server - {}:{}", socket.getInetAddress(), socket.getPort());

        // Setup output stream to send data to the server
        BufferedOutputStream stream = new BufferedOutputStream(socket.getOutputStream());
        AtomicInteger sentCounter = new AtomicInteger();
        //noinspection resource
        Files.lines(new File(BIG_CSV).toPath(), StandardCharsets.UTF_8)
                .parallel()
                .map(StringSerializer::parseLine)
                .filter(Objects::nonNull)
                .filter(TripData::correctTotalSum)
                .filter(data -> data.total < 50000)
                .map(TripDriverCarTotalData::new)
                .map(BinarySerializer::convertToByteArray)
                .peek(data -> sentCounter.incrementAndGet())
                .forEachOrdered(data -> {
                    try {
                        stream.write(data);
                    }catch (Exception ohNoThisIsVeryBad) {
                        TripDriverCarTotalData deserializedData =
                                BinarySerializer.readDriverCarEarningsData(
                                        new DataInputStream(new ByteArrayInputStream(data))
                                );
                        LOG.error("while sending data {}", deserializedData, ohNoThisIsVeryBad);
                    }
                });

        stream.flush();
        LOG.info("finished sending data - {} elements sent", sentCounter.get());

        socket.close();

    }

}
