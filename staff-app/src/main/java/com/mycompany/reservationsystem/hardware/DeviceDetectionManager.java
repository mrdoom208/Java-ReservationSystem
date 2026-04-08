package com.mycompany.reservationsystem.hardware;

import com.fazecast.jSerialComm.SerialPort;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class DeviceDetectionManager {

    private SerialPort port;
    private BufferedReader in;
    private OutputStream out;

    /* ================= PORT LIFECYCLE ================= */

    public synchronized void openPort(String portName, int baudRate) throws Exception {
        if (portName == null || portName.isBlank()) {
            throw new IllegalArgumentException("Port name is null or empty");
        }

        if (isPortOpen()) return;

        port = SerialPort.getCommPort(portName);
        port.setBaudRate(baudRate);
        port.setComPortTimeouts(
                SerialPort.TIMEOUT_READ_SEMI_BLOCKING,
                500,
                0
        );

        if (!port.openPort()) {
            port = null;
            throw new IllegalStateException("Unable to open serial port: " + portName);
        }

        in = new BufferedReader(
                new InputStreamReader(port.getInputStream(), StandardCharsets.US_ASCII)
        );
        out = port.getOutputStream();

        // minimal boot delay (Pico / MCU reset)
        Thread.sleep(300);
        flushInput();
    }

    public synchronized boolean isPortOpen() {
        return port != null && port.isOpen();
    }

    public synchronized void closePort() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (port != null && port.isOpen()) port.closePort();
        } catch (Exception ignored) {
        } finally {
            in = null;
            out = null;
            port = null;
        }
    }

    /* ================= DEVICE DETECTION ================= */

    public DeviceResult detectDevice() throws Exception {
        ensureOpen();
        flushInput();

        // ---- HANDSHAKE ----
        String boot = readResponse(800);
        if (boot == null || !boot.contains("READY")) {
            sendCommand("PING");
            String pong = readResponse(800);
            if (pong == null || !pong.contains("PONG")) {
                throw new IllegalStateException("Handshake failed: device not responding");
            }
        }

        String controller = sendAndRead("GET_CONTROLLER", 400);
        String module = sendAndRead("GET_MODULE", 800);
        String phone = sendAndRead("GET_PHONE", 400);

        return new DeviceResult(
                clean(module),
                clean(phone),
                clean(controller)
        );
    }

    /* ================= SMS ================= */

    public synchronized void sendMessage(String phone, String message) throws Exception {
        ensureOpen();

        if (phone == null || phone.isBlank())
            throw new IllegalArgumentException("Invalid phone number");

        if (message == null || message.isBlank())
            throw new IllegalArgumentException("Message cannot be empty");

        message = message.replace("\n", " ").replace("\r", "");

        flushInput();
        sendCommand("SMS " + phone + " " + message);

        String response = readResponse(5000);

        if (response == null) {
            throw new IllegalStateException("No response from device");
        }

        if (response.contains("ERROR")) {
            throw new IllegalStateException("Device error: " + response);
        }

        if (!response.contains("OK") && !response.contains("+CMGS")) {
            throw new IllegalStateException("Unexpected response: " + response);
        }
    }

    /* ================= LOW LEVEL ================= */

    private String sendAndRead(String cmd, long timeout) throws Exception {
        flushInput();
        sendCommand(cmd);
        return readResponse(timeout);
    }

    private void sendCommand(String cmd) throws Exception {
        if (out == null)
            throw new IllegalStateException("Serial output not initialized");

        out.write((cmd + "\r\n").getBytes(StandardCharsets.US_ASCII));
        out.flush();
    }

    private String readResponse(long timeoutMs) throws Exception {
        long start = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();

        while (System.currentTimeMillis() - start < timeoutMs) {
            while (in != null && in.ready()) {
                String line = in.readLine();
                if (line != null) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        sb.append(line).append('\n');

                        if (line.equals("OK")
                                || line.startsWith("ERROR")
                                || line.startsWith("+CMGS")) {
                            return sb.toString().trim();
                        }
                    }
                }
            }
            Thread.sleep(50);
        }
        return sb.length() == 0 ? null : sb.toString().trim();
    }

    private void flushInput() throws Exception {
        while (in != null && in.ready()) {
            in.readLine();
        }
    }

    private void ensureOpen() {
        if (!isPortOpen()) {
            throw new IllegalStateException("Serial port not open");
        }
    }

    private String clean(String input) {
        if (input == null) return null;   // <- important!
        input = input.replace("\0", "").trim();
        return input.isEmpty() ? null : input;}

    /* ================= RESULT ================= */

    public static class DeviceResult {
        public final String module;
        public final String phone;
        public final String controller;

        public DeviceResult(String module, String phone, String controller) {
            this.module = module;
            this.phone = phone;
            this.controller = controller;
        }

        @Override
        public String toString() {
            return "DeviceResult{" +
                    "module='" + module + '\'' +
                    ", phone='" + phone + '\'' +
                    ", controller='" + controller + '\'' +
                    '}';
        }
    }
}
