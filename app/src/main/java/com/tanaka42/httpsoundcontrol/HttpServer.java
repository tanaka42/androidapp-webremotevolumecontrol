package com.tanaka42.httpsoundcontrol;

import android.content.Context;
import android.content.res.AssetManager;
import android.media.AudioManager;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.AccessMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import java.net.DatagramSocket;


public class HttpServer extends Thread {
    private static ServerSocket serverSocket;
    private AudioManager audioManager;
    private Context context;
    private static String server_ip;
    private static int server_port = 9000;
    private static boolean isStart = true;

    public HttpServer(final AudioManager audio, final Context ctx) {
        try {
            this.audioManager = audio;
            this.context = ctx;
        } catch (Exception er) {
            er.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            try(final DatagramSocket socket = new DatagramSocket()){
                socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
                server_ip = socket.getLocalAddress().getHostAddress();
                socket.disconnect();
            }

            InetAddress addr = InetAddress.getByName(server_ip);
            serverSocket = new ServerSocket(server_port, 100, addr);
            serverSocket.setSoTimeout(5000);
            while (isStart) {
                try {
                    Socket newSocket = serverSocket.accept();
                    Thread newClient = new EchoThread(newSocket);
                    newClient.start();
                } catch (SocketTimeoutException s) {
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getURL() {
        return "http://" + server_ip + ":" + server_port;
    }

    public class EchoThread extends Thread {
        protected Socket socket;
        private Context context;

        public EchoThread(Socket clientSocket) {
            this.socket = clientSocket;
        }

        @Override
        public void run() {
            try {
                DataInputStream in = null;
                DataOutputStream out = null;

                if (this.socket.isConnected()) {
                    try {
                        in = new DataInputStream(this.socket.getInputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        out = new DataOutputStream(this.socket.getOutputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                byte[] data = new byte[1500];

                while (in.read(data) != -1) {
                    String recData = new String(data).trim();
                    String[] header = recData.split("\\r?\\n");
                    String[] h1 = header[0].split(" ");

                    final String requestLocation = h1[1];

                    int responseStatus = 200;
                    String requestedFile = "";

                    switch (requestLocation) {
                        case "/volume-up":
                            audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
                            break;
                        case "/volume-down":
                            audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
                            break;
                        case "/volume-up.png":
                        case "/volume-down.png":
                            requestedFile = requestLocation.substring(1);
                            break;
                        case "/":
                            requestedFile = "httpsoundcontrol_spa.html";
                            break;
                        default:
                            responseStatus = 404;
                            break;
                    }

                    if (requestedFile.isEmpty()) {
                        constructHeader(out, "0", "", responseStatus);
                    } else {
                        InputStream fileStream = context.getAssets().open(requestedFile, AssetManager.ACCESS_BUFFER);
                        int size = fileStream.available();
                        byte[] buffer = new byte[size];
                        fileStream.read(buffer);
                        if (requestedFile.matches(".*\\.png$")) {
                            constructHeaderPng(out, buffer.length + "", buffer, responseStatus);
                        } else {
                            String responseData = new String(buffer);
                            constructHeader(out, responseData.length() + "", responseData, responseStatus);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void printHeader(PrintWriter pw, String key, String value) {
        pw.append(key).append(": ").append(value).append("\r\n");
    }

    private void constructHeaderPng(DataOutputStream output, String size, byte[] data, int code) {
        try{
            SimpleDateFormat gmtFrmt = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
            gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));
            PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(output)), false);
            pw.append("HTTP/1.1 ").append(code + "").append(" \r\n");
            printHeader(pw, "Content-Type", "image/png");
            printHeader(pw, "Date", gmtFrmt.format(new Date()));
            printHeader(pw, "Connection", "close");
            printHeader(pw, "Content-Length", size);
            printHeader(pw, "Server", "192.168.1.47");
            pw.append("\r\n");
            pw.flush();
            output.write(data);
            output.flush();
            pw.close();
        } catch (Exception er) {
            er.printStackTrace();
        }
    }

    private void constructHeader(DataOutputStream output, String size, String data, int code) {
        SimpleDateFormat gmtFrmt = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(output)), false);
        pw.append("HTTP/1.1 ").append(code + "").append(" \r\n");
        printHeader(pw, "Date", gmtFrmt.format(new Date()));
        printHeader(pw, "Connection", "close");
        printHeader(pw, "Content-Length", size);
        printHeader(pw, "Server", "192.168.1.47");
        pw.append("\r\n");
        pw.append(data);
        pw.flush();
        pw.close();
    }

    public static void stopServer() {
        if (isStart) {
            try {
                isStart = false;
                serverSocket.close();
                System.out.println("Server stopped !");
            } catch (IOException er) {
                er.printStackTrace();
            }
        }
    }
}