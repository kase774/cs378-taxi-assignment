package edu.utexas.cs.cs378.week2;

import edu.utexas.cs.cs378.*;
import edu.utexas.cs.cs378.drivers.DriverEarnRateData;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;

import static edu.utexas.cs.cs378.Networking.*;

public class DriverEarningsMapService {

    private static final Logger LOG = LoggerFactory.getLogger(DriverEarningsMapService.class);

    @SneakyThrows
    public static void main(String[] args) {

        // used this as testing for whether the signals communication works
        Socket clientSocket = getClientSocket();
        OutputStream stream = getOutputStreamAndSendSignal(clientSocket, CLIENT_SEND);

        getInputStreamAndWaitForSignal(clientSocket, SERVER_SEND);

        LOG.info("finished exchanging verification messages");

        BinaryRotatingBufferStream buffer = new BinaryRotatingBufferStream(5, 20, 100000, stream);

        //noinspection resource
        int counter = buffer.takeIn(
                Files.lines(new File(Parameters.getDataset()).toPath(), StandardCharsets.UTF_8)
                .parallel()
                .map(StringSerializer::parseLine)
                .filter(Objects::nonNull) // parseLine returns null for bad lines
                .filter(data -> data.getTotal() < 30000 && data.getTip() < 30000 && data.getTip() > 500 && data.getTotal() > 500)
                .map(DriverEarnRateData::new)
                .map(BinarySerializer::convertToByteArray)
        );

        LOG.info("finished sending data - {} elements sent", counter);
    }

}
