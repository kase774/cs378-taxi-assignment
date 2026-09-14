package edu.utexas.cs.cs378;

import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ReductionService {

    private static final Logger LOG = LoggerFactory.getLogger(ReductionService.class);
    private static final Map<Md5Wrapper, DriverCarEarnings> driversMap = new HashMap<>();
    private static final int PRINT_TOP_K = 10;

    private static void registerTripData(TripDriverCarTotalData data) {
        Md5Wrapper key = new Md5Wrapper(data.driverHash);
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
        TripDriverCarTotalData data;
        while ((data = BinarySerializer.readDriverCarEarningsData(inputStream)) != null) {
            registerTripData(data);
            receivedCounter++;
        }

        LOG.info("finished storing all data: received {} data, created {} entries",
                receivedCounter, driversMap.size());
    }

    @SneakyThrows
    private static void setClipboardToAddress(ServerSocket socket) {
        String address = InetAddress.getLocalHost().getHostAddress() + ":" + socket.getLocalPort();
        StringSelection selection = new StringSelection(address);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
    }

    // usage:
    // launch reduction service; network IP + port are printed to the stdout
    // then, launch map service and input the ip and port in, which will connect
    @SneakyThrows
    public static void main(String[] args) {
        ServerSocket serverSocket = new ServerSocket(31001);
        setClipboardToAddress(serverSocket);
        LOG.info("server started - {}:{}", serverSocket.getInetAddress(),
                serverSocket.getLocalPort());

        Socket clientSocket = serverSocket.accept();
        LOG.info("connected to client - {}:{}", clientSocket.getInetAddress(),
                clientSocket.getLocalPort());

        // input stream to receive from client
        DataInputStream inputStream =
                new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
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
