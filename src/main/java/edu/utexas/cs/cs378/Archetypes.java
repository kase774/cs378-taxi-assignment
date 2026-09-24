package edu.utexas.cs.cs378;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.File;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static edu.utexas.cs.cs378.Networking.*;

@UtilityClass
public class Archetypes {
    // most of the core logic, extracted out
    // so that it's easier to use for multiple different classes

    private static final Logger LOG = LoggerFactory.getLogger(Archetypes.class);

    @SneakyThrows
    public <T> void mappingService(int elementSize, Function<TripData, T> transformer, Function<T
            , byte[]> toByteArray, Predicate<TripData> filter) {
        Path datasetFile = new File(Parameters.getDataset()).toPath();

        Socket clientSocket = getClientSocket();
        OutputStream stream = getOutputStreamAndSendSignal(clientSocket, CLIENT_SEND);

        getInputStreamAndWaitForSignal(clientSocket, SERVER_SEND);

        LOG.info("received server ready confirmation");

        BinaryRotatingBufferStream buffer = new BinaryRotatingBufferStream(3, elementSize, 100000, stream);

        //noinspection resource
        int counter =
                buffer.takeIn(
                        Files.lines(datasetFile, StandardCharsets.UTF_8)
                                .parallel()
                                .map(StringSerializer::parseLine)
                                .filter(Objects::nonNull) // parseLine returns null for bad lines
                                .filter(filter)
                                .map(transformer)
                                .map(toByteArray));

        clientSocket.close();

        LOG.info("finished sending data - {} elements sent", counter);
    }

    @SneakyThrows
    public void fromFileMappingService(String suffix) {
        Path datasetFile = new File(Parameters.getDataset() + suffix).toPath();

        Socket clientSocket = getClientSocket();
        OutputStream stream = getOutputStreamAndSendSignal(clientSocket, CLIENT_SEND);

        getInputStreamAndWaitForSignal(clientSocket, SERVER_SEND);

        // datasetFile is the concatenation of the fixed-size records produced by
        // fileMappingService, so forward it verbatim to the reduction service
        long copied = Files.copy(datasetFile, stream);
        stream.flush();

        clientSocket.close();

        LOG.info("finished sending data - {} bytes from {}", copied, datasetFile);
    }

    @SneakyThrows
    public <T> void fileMappingService(int elementSize, String suffix,
                                       Function<TripData, T> transformer, Function<T, byte[]> toByteArray,
                                       Predicate<TripData> filter) {
        String dataset = Parameters.getDataset();
        Path datasetFile = new File(dataset).toPath();
        Path outputFile = new File(dataset + suffix).toPath();

        BinaryRotatingBufferStream buffer = new BinaryRotatingBufferStream(3, elementSize, 100000,
                Files.newOutputStream(outputFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING));

        //noinspection resource
        int counter =
                buffer.takeIn(
                        Files.lines(datasetFile, StandardCharsets.UTF_8)
                                .parallel()
                                .map(StringSerializer::parseLine)
                                .filter(Objects::nonNull) // parseLine returns null for bad lines
                                .filter(filter)
                                .map(transformer)
                                .map(toByteArray));


        LOG.info("finished writing data - {} elements written", counter);
    }



    // usage:
    // launch; then enter IP of final reduction server
    // then, this will open its own socket and display.

    // the client must connect and send the verification msg.
    // this will then trigger its verification message by asking the final server
    // after the server responds, this will respond to the client; then the client
    // should send data to this service.
    @SneakyThrows
    public void intermediateReductionService(Consumer<DataInputStream> dataConsumer,
                                             Supplier<byte[]> result) {

        Socket toFinalServerSocket = getClientSocket();

        ServerSocket serverSocket = getServerSocket(false);
        Socket clientSocket = accept(serverSocket);

        // input stream to receive from client

        DataInputStream inputStream = getInputStreamAndWaitForSignal(clientSocket, CLIENT_SEND);

        LOG.info("finished exchanging client messages");

        OutputStream toFinalServerOutputStream = getOutputStreamAndSendSignal(toFinalServerSocket
                , CLIENT_SEND);
        getInputStreamAndWaitForSignal(toFinalServerSocket, SERVER_SEND);

        // wait for the final server to be ready before telling the client that we're ready
        getOutputStreamAndSendSignal(clientSocket, SERVER_SEND);

        LOG.info("finished exchanging verification messages");
        dataConsumer.accept(inputStream);

        clientSocket.close();
        serverSocket.close();

        toFinalServerOutputStream.write(result.get());
        toFinalServerOutputStream.flush();

        toFinalServerSocket.close();
    }

    private static byte[] results = new byte[0];

    // there shouldn't ever be an instance where 2 of these are called, so
    // global state, while a massive code smell, is fine here.
    public void intermediateReductionService(Function<DataInputStream, byte[]> reduction) {
        intermediateReductionService(inputStream -> results = reduction.apply(inputStream), () -> results);
    }

    // usage:
    // launch; this will display the socket address
    // waits for `numberOfClients` intermediates, reads one partial aggregate from
    // each and merges them into `aggregate`, then hands the result to `onComplete`.
    @SneakyThrows
    public void finalReductionService(int numberOfClients,
                                          Consumer<DataInputStream> perClientHandler) {
        ServerSocket serverSocket = getServerSocket(false);
        CountDownLatch clientsConnected = new CountDownLatch(numberOfClients);

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < numberOfClients; i++) {
            Thread thread = new Thread(() ->
                    finalReductionClient(serverSocket, clientsConnected, perClientHandler));
            threads.add(thread);
            thread.start();
        }

        clientsConnected.await();
        //noinspection LoggingSimilarMessage
        LOG.info("finished exchanging verification messages");

        for (Thread thread : threads) {
            thread.join();
        }

        serverSocket.close();
    }

    @SneakyThrows
    private static <A> void finalReductionClient(ServerSocket serverSocket,
                                                 CountDownLatch clientsConnected,
                                                 Consumer<DataInputStream> perClientHandler) {
        Socket clientSocket = accept(serverSocket);

        // input stream to receive from client
        DataInputStream inputStream = getInputStreamAndWaitForSignal(clientSocket, CLIENT_SEND);
        LOG.info("received message from {}, on thread {}", clientSocket.getInetAddress(),
                Thread.currentThread().getName());

        clientsConnected.countDown();
        // wait until every client has connected before letting them send
        clientsConnected.await();

        // tell the intermediate server that we're reading
        getOutputStreamAndSendSignal(clientSocket, SERVER_SEND);

        perClientHandler.accept(inputStream);
        LOG.info("finished processing data from {}, on thread {}", clientSocket.getInetAddress(),
                Thread.currentThread().getName());
    }

}
