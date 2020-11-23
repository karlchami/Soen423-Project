package replica.replica_nick.manager;

import Models.request.Request;
import replica.replica_nick.servers.BCServer;
import replica.replica_nick.servers.ONServer;
import replica.replica_nick.servers.QCServer;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.PriorityQueue;

public class ReplicaManager {
    public static PriorityQueue<String> pq = new PriorityQueue<>(50);
    public static ArrayList<String> received_commands = new ArrayList<>(50);
    public static Thread QC;
    public static Thread ON;
    public static Thread BC;

    public static void main(String[] args) {
        try {
            startAll();

            Runnable task = ReplicaManager::receiveMulticast;
            Thread thread = new Thread(task);
            thread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void startAll() throws InterruptedException {
        Thread.sleep(200);
        startQC();
        Thread.sleep(200);
        startON();
        Thread.sleep(200);
        startBC();
    }

    private static void startQC() {
        Runnable task = () -> QCServer.main(null);
        QC = new Thread(task);
        QC.start();
    }

    private static void startON() {
        Runnable task = () -> ONServer.main(null);
        ON = new Thread(task);
        ON.start();
    }

    private static void startBC() {
        Runnable task = () -> BCServer.main(null);
        BC = new Thread(task);
        BC.start();
    }

    public static void restartAll() throws InterruptedException {
        Thread.sleep(500);
        endQC();
        Thread.sleep(500);
        endON();
        Thread.sleep(500);
        endBC();
        startAll();
        Thread.sleep(200);
        System.out.println("Restarted");
    }

    public static void endQC() {
        endServerUDP(5001);
        endServerUDP(5551);
    }

    public static void endON() {
        endServerUDP(5002);
        endServerUDP(5552);
    }

    public static void endBC() {
        endServerUDP(5003);
        endServerUDP(5553);
    }

    public static boolean heartbeat(String storePrefix) {
        String message = "Heartbeat";
        String alive = "TRUE";

        switch (storePrefix) {
            case "QC":
                return sendUDP(5551, message).trim().equals(alive);
            case "ON":
                return sendUDP(5552, message).trim().equals(alive);
            case "BC":
                return sendUDP(5553, message).trim().equals(alive);
            default:
                return false;
        }
    }

    public static void resendMessages() {
        for (String request : received_commands) {
            sendRequest(request);
        }
    }

    private static void receiveMulticast() {
        try (MulticastSocket socket = new MulticastSocket(4444);) {
            socket.joinGroup(InetAddress.getByName("230.4.4.5"));
            while (true) {
                byte[] buffer = new byte[1000];
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);

                String sentence = new String(request.getData(), request.getOffset(), request.getLength()).trim();

                if (sentence.equals("KILL")) {
                    restartAll();
                    resendMessages();
                } else {
                    received_commands.add(sentence);
                    sendRequest(sentence);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendRequest(String message) {
        Request request = new Request(message);
        switch (request.getStore()) {
            case "QC":
                sendNoReply(5551, message);
                break;
            case "ON":
                sendNoReply(5552, message);
                break;
            case "BC":
                sendNoReply(5553, message);
                break;
        }
    }

//    private static void receive() {
//        DatagramSocket socket = null;
//        String returnMessage = "";
//        try {
//            socket = new DatagramSocket(2004);
//            byte[] buffer = new byte[1000];
//            while (true) {
//                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
//                socket.receive(request);
//                String sentence = new String(request.getData(), request.getOffset(), request.getLength()).trim();
//                String[] split = sentence.split("-");
//                //action+"-"+username+"-"+itemId+"-"+cost"-"+oldItem
//                System.out.println("Function Received " + split[0]);
//                if (split[0].equals("Restart")) {
//
//                }
//                if (split[0].equals("SendForward")) {
//
//                }
//                if (split[0].equals("GetMessage")) {
//
//                }
//                byte[] sendData = returnMessage.getBytes();
//                DatagramPacket reply = new DatagramPacket(sendData, returnMessage.length(), request.getAddress(),
//                        request.getPort());
//                socket.send(reply);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (socket != null)
//                socket.close();
//        }
//    }

    private static String sendUDP(int port, String message) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(5000);

            byte[] bytes = message.getBytes();
            InetAddress hostName = InetAddress.getByName("localhost");
            DatagramPacket request = new DatagramPacket(bytes, bytes.length, hostName, port);
            socket.send(request);

            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            socket.receive(reply);

            return new String(reply.getData(), 0, reply.getLength());

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static void sendNoReply(int port, String message) {
        try (DatagramSocket socket = new DatagramSocket()) {
            byte[] bytes = message.getBytes();
            InetAddress hostName = InetAddress.getByName("localhost");
            DatagramPacket request = new DatagramPacket(bytes, bytes.length, hostName, port);
            socket.send(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void endServerUDP(int port) {
        try (DatagramSocket socket = new DatagramSocket()) {
            byte[] bytes = "Exit".getBytes();
            InetAddress hostName = InetAddress.getByName("localhost");

            DatagramPacket request = new DatagramPacket(bytes, bytes.length, hostName, port);
            socket.send(request);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
