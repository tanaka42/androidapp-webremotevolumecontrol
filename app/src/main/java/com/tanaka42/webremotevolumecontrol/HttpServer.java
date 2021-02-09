package com.tanaka42.webremotevolumecontrol;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.os.Bundle;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;


public class HttpServer extends Thread {
    private static ServerSocket serverSocket;
    private AudioManager audioManager;
    private Context context;
    private static String server_ip;
    private static int server_port = 9000;
    private static boolean is_a_class_c_ip_address = false;
    private static boolean isStart = false;

    public HttpServer(final AudioManager audio, final Context ctx) {
        try {
            this.audioManager = audio;
            this.context = ctx;
        } catch (Exception er) {
            er.printStackTrace();
        }
    }

    private boolean isClassCAddress(String ip) {
        if (ip != null && !ip.isEmpty()) {
            Pattern pattern = Pattern.compile("(\\d+)\\.\\d+\\.\\d+\\.\\d+");
            Matcher matcher = pattern.matcher(ip);
            if (matcher.find()) {
                if (matcher.groupCount() > 0) {
                    String firstMemberString = matcher.group(1);
                    if (firstMemberString != null) {
                        int firstMember = parseInt(firstMemberString);
                        return (192 <= firstMember && firstMember <= 223);
                    }
                }
            }
        }
        return false;
    }

    private void notifiy_observers() {
        Intent urlUpdatedIntent = new Intent("com.tanaka42.webremotevolumecontrol.urlupdated");
        Bundle extras = new Bundle();
        extras.putString("url", "http://" + server_ip + ":" + server_port);
        extras.putString("ip", server_ip);
        extras.putInt("port", server_port);
        extras.putBoolean("is_a_class_c_ip", is_a_class_c_ip_address);
        urlUpdatedIntent.putExtras(extras);
        context.sendBroadcast(urlUpdatedIntent);
    }

    @Override
    public void run() {
        //System.out.println("Starting server ...");

        // Determine local network IP address
        try {
            final DatagramSocket socket = new DatagramSocket();
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            server_ip = socket.getLocalAddress().getHostAddress();
            socket.disconnect();
        } catch (SocketException ignored) {
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        // Check if determined IP address is a Class C one (a local network one, not an internet one).
        is_a_class_c_ip_address = isClassCAddress(server_ip);

        // update Activity
        notifiy_observers();

        // Start accepting request and responding
        if (server_ip != null && is_a_class_c_ip_address) {
            try {
                InetAddress addr = InetAddress.getByName(server_ip);
                serverSocket = new ServerSocket(server_port, 100, addr);
                serverSocket.setSoTimeout(5000);
                isStart = true;
                //System.out.println("Server started : listening.");
                while (isStart) {
                    try {
                        Socket newSocket = serverSocket.accept();
                        Thread newClient = new ClientThread(newSocket);
                        newClient.start();
                    } catch (SocketTimeoutException ignored) {
                    } catch (IOException ignored) {
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        isStart = false;
        context.stopService(new Intent(context, ForegroundService.class));
        // update Activity
        notifiy_observers();
        //System.out.println("Server stopped");
    }



    public class ClientThread extends Thread {
        protected Socket socket;
        private String content_type = "";
        private String status_code;

        public ClientThread(Socket clientSocket) {
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

                if (in != null) {
                    while (in.read(data) != -1) {
                        String recData = new String(data).trim();
                        String[] header = recData.split("\\r?\\n");
                        String[] h1 = header[0].split(" ");

                        final String requestLocation = h1[1];

                        status_code = "200";
                        String requestedFile = "";
                        String currentVolume = "";

                        switch (requestLocation) {
                            case "/volume-up":
                                audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
                                currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toString();
                                content_type = "text/html";
                                break;
                            case "/volume-down":
                                audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
                                currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toString();
                                content_type = "text/html";
                                break;
                            case "/volume-up.png":
                            case "/volume-down.png":
                                requestedFile = requestLocation.substring(1);
                                content_type = "image/png";
                                break;
                            case "/":
                                requestedFile = "webremotevolumecontrol_spa.html";
                                content_type = "text/html";
                                break;
                            default:
                                status_code = "404";
                                break;
                        }

                        byte[] buffer = new byte[0];
                        if (!requestedFile.isEmpty()) {
                            InputStream fileStream = context.getAssets().open(requestedFile, AssetManager.ACCESS_BUFFER);
                            int size = fileStream.available();
                            buffer = new byte[size];
                            int readResult = fileStream.read(buffer);
                        }
                        if (!currentVolume.isEmpty()) {
                            buffer = currentVolume.getBytes();
                        }
                        writeResponse(out, buffer.length + "", buffer, status_code, content_type);
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

    private void writeResponse(DataOutputStream output, String size, byte[] data, String status_code, String content_type) {
        try {
            SimpleDateFormat gmtFrmt = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
            gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));
            PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(output)), false);
            pw.append("HTTP/1.1 ").append(status_code).append(" \r\n");
            if (!content_type.isEmpty()) {
                printHeader(pw, "Content-Type", content_type);
            }
            printHeader(pw, "Date", gmtFrmt.format(new Date()));
            printHeader(pw, "Connection", "close");
            printHeader(pw, "Content-Length", size);
            printHeader(pw, "Server", server_ip);
            pw.append("\r\n");
            pw.flush();
            switch (content_type) {
                case "text/html":
                    pw.append(new String(data));
                    break;
                case "image/png":
                    output.write(data);
                    output.flush();
                    break;
            }
            pw.flush();
            //pw.close();
        } catch (Exception er) {
            er.printStackTrace();
        }
    }

    public static void stopServer() {
        if (isStart) {
            try {
                //System.out.println("Stopping server ...");
                isStart = false;
                serverSocket.close();
            } catch (IOException er) {
                er.printStackTrace();
            }
        }
    }

    public static boolean isStarted() {
        return isStart;
    }
}
