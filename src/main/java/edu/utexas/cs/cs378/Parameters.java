package edu.utexas.cs.cs378;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

@UtilityClass
public class Parameters {

    private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static final String BIG_CSV_P1 = "taxi-data-sorted-large-p1.csv";
    public static final String BIG_CSV_P2 = "taxi-data-sorted-large-p2.csv";
    public static final String SMALL_CSV = "taxi-data-sorted-small.csv";

    public static final int NO_PORT_SPECIFIED = -1;

    @SneakyThrows
    public String getLine(String prompt) {
        System.out.println(prompt);
        return reader.readLine();
    }

    public InetSocketAddress getServerIp() {
        String dataString = System.getProperty("server.address");

        if (dataString == null || dataString.trim().isEmpty()) {
            dataString = getLine("enter server address:");
            System.setProperty("server.address", dataString);
        }

        int colon = dataString.lastIndexOf(':');
        if (colon < 0) {
            throw new IllegalArgumentException(
                    "server.address must be \"host:port\" but was \"" + dataString + "\""
                            + " (pass it as e.g. server.address=127.0.1.1:31001)");
        }
        String host = dataString.substring(0, colon);
        int port = Integer.parseInt(dataString.substring(colon + 1));
        return new InetSocketAddress(host, port);
    }

    public int getPort() {
        return intProperty("server.port", NO_PORT_SPECIFIED);
    }

    // retry settings for connecting to another service (which may still be starting up)
    public int getConnectAttempts() {
        return intProperty("connect.attempts", 60);
    }

    public int getConnectRetryDelayMs() {
        return intProperty("connect.retry.delay.ms", 1000);
    }

    public int getConnectTimeoutMs() {
        return intProperty("connect.timeout.ms", 2000);
    }

    // how many intermediate reductions the final reduction expects to connect
    public int getNumberOfClients() {
        return intProperty("clients", 2);
    }

    private int intProperty(String key, int defaultValue) {
        String value = System.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }

    public String getDataset() {
        String dataString = System.getProperty("dataset");
        if (dataString == null || dataString.trim().isEmpty()) {
            dataString = getLine("enter dataset path: (1 = p1, 2 = p2, 3 = small)");
            System.setProperty("dataset", dataString);
        }

        int value = Integer.parseInt(dataString);

        return switch (value) {
            case 1 -> BIG_CSV_P1;
            case 2 -> BIG_CSV_P2;
            case 3 -> SMALL_CSV;
            default -> throw new IllegalArgumentException("Invalid dataset path");
        };
    }

}
