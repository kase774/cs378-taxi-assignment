package edu.utexas.cs.cs378.week2;

import edu.utexas.cs.cs378.BinarySerializer;
import edu.utexas.cs.cs378.Md5Wrapper;
import edu.utexas.cs.cs378.StringSerializer;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import static edu.utexas.cs.cs378.Networking.*;

public class DriverEarningsReductionService {

    private static final Logger LOG = LoggerFactory.getLogger(DriverEarningsReductionService.class);
    private static final Map<Md5Wrapper, DriverCarEarnings> driversMap = new HashMap<>();
    private static final int PRINT_TOP_K = 10;

    private static void registerTripData(TripDriverCarTotalData data) {
        Md5Wrapper key = new Md5Wrapper(data.getDriverHash());
        DriverCarEarnings earnings = driversMap.get(key);
        if (earnings == null) {
            earnings = new DriverCarEarnings(key);
            driversMap.put(key, earnings);
        }
        earnings.registerNewData(data);
    }

    private static void registerAllTripData(DataInputStream inputStream) {
        // this part isn't parallelized because we are limited by reading anyway (the cost
        // of register is basically 0)
        int receivedCounter = 0;

        try {
            TripDriverCarTotalData data;
            while ((data = BinarySerializer.readDriverCarEarningsData(inputStream)) != null) {
                registerTripData(data);
                receivedCounter++;
            }
        } catch (Exception ignored) {}

        LOG.info("finished storing all data: received {} data, created {} entries",
                receivedCounter, driversMap.size());
    }

    // usage:
    // launch reduction service; network IP + port are printed to the stdout
    // then, launch map service and input the ip and port in, which will connect
    @SneakyThrows
    public static void main(String[] args) {
        // WARNING! Only do this for faster startup, will erase your clipboard

        ServerSocket serverSocket = getServerSocket(true);
        Socket clientSocket = accept(serverSocket);

        // input stream to receive from client

        DataInputStream inputStream = getInputStreamAndWaitForSignal(clientSocket, CLIENT_SEND);
        getOutputStreamAndSendSignal(clientSocket, SERVER_SEND);

        LOG.info("finished exchanging verification messages");

        registerAllTripData(inputStream);

        PriorityQueue<DriverCarEarnings> driverEarningsQueue =
                new PriorityQueue<>(Comparator.comparing(DriverCarEarnings::getTotalEarnings).reversed());
        driverEarningsQueue.addAll(driversMap.values());

        driversMap.clear();

        for (int i = 0; i < PRINT_TOP_K; i++) {
            if (driverEarningsQueue.isEmpty()) {
                break;
            }
            DriverCarEarnings earnings = driverEarningsQueue.poll();
            System.out.println(StringSerializer.toString(earnings));
        }

        clientSocket.close();
        serverSocket.close();
    }
}
