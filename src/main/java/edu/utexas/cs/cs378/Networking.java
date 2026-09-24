package edu.utexas.cs.cs378;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

@UtilityClass
public class Networking {


    private static final Logger LOG = LoggerFactory.getLogger(Networking.class);
    // the client should send CLIENT_SEND on connection initialization
    // after the server has received that, it will send back SERVER_SEND
    public static final int CLIENT_SEND = 3;
    public static final int SERVER_SEND = 4;

    @SneakyThrows
    public Socket getClientSocket() {
        InetSocketAddress serverAddress = Parameters.getServerIp();

        int attempts = Parameters.getConnectAttempts();
        int retryDelayMs = Parameters.getConnectRetryDelayMs();
        int timeoutMs = Parameters.getConnectTimeoutMs();

        IOException lastFailure = null;
        for (int attempt = 1; attempt <= attempts; attempt++) {
            Socket socket = new Socket();
            try {
                socket.connect(serverAddress, timeoutMs);
                LOG.info("connected to server - {}:{}", socket.getInetAddress(), socket.getPort());
                // Setup output stream to send data to the server
                return socket;
            } catch (IOException e) {
                lastFailure = e;
                socket.close();
                LOG.warn("connection attempt {}/{} to {} failed: {}",
                        attempt, attempts, serverAddress, e.getMessage());
                if (attempt < attempts) {
                    Thread.sleep(retryDelayMs);
                }
            }
        }
        throw new IOException("failed to connect to " + serverAddress + " after "
                + attempts + " attempts", lastFailure);
    }

    @SneakyThrows
    public OutputStream outputStream(Socket socket) {
        // Setup output stream to send data to the server
        return new BufferedOutputStream(socket.getOutputStream());
    }


    @SneakyThrows
    public DataInputStream inputStream(Socket socket) {
        return new DataInputStream(new BufferedInputStream(socket.getInputStream()));
    }

    @SneakyThrows
    public ServerSocket getServerSocket(boolean copyToClipboard) {
        ServerSocket serverSocket = null;

        int port = Parameters.getPort();
        if (port == Parameters.NO_PORT_SPECIFIED) {
            port = 31001;
        }

        while (serverSocket == null) {
            try {
                serverSocket = new ServerSocket(port);
            } catch (IOException e) {
                LOG.warn("failed to bind to port {}", port);
                port++;
            }
        }
        LOG.info("server started - {}:{}", serverSocket.getInetAddress(),
                serverSocket.getLocalPort());

        if (copyToClipboard) {
            setClipboardToAddress(serverSocket);
        }

        return serverSocket;
    }

    @SneakyThrows
    public Socket accept(ServerSocket socket) {
        Socket clientSocket = socket.accept();
        LOG.info("connected to client - {}:{}", clientSocket.getInetAddress(),
                clientSocket.getLocalPort());
        return clientSocket;
    }

    @SneakyThrows
    public OutputStream getOutputStreamAndSendSignal(Socket clientSocket, int signal) {
        OutputStream stream = outputStream(clientSocket);
        stream.write(signal);
        stream.flush();
        return stream;
    }

    @SneakyThrows
    public DataInputStream getInputStreamAndWaitForSignal(Socket clientSocket, int signal) {
        DataInputStream stream = inputStream(clientSocket);
        int value = stream.readByte();
        if (value != signal)
            throw new IllegalStateException("Client isn't sending the correct value");
        return stream;
    }


    @SneakyThrows
    private void setClipboardToAddress(ServerSocket socket) {
        String address = InetAddress.getLocalHost().getHostAddress() + ":" + socket.getLocalPort();
        StringSelection selection = new StringSelection(address);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
    }
}
