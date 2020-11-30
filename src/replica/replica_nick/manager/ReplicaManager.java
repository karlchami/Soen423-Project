package replica.replica_nick.manager;

import Models.request.Request;
import replica.replica_nick.servers.BCServer;
import replica.replica_nick.servers.ONServer;
import replica.replica_nick.servers.QCServer;

import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

public class ReplicaManager {
    private static HashMap<Integer, String> requestQueue = new HashMap<>(50);
    private static ArrayList<String> received_commands = new ArrayList<>(50);
    private static int nextSeq = 0;

    public static void main(String[] args) {
        try {
            startAll();

            Runnable seqTask = ReplicaManager::receiveMulticast;
            Runnable feTask = ReplicaManager::receiveFE;

            Thread seqThread = new Thread(seqTask);
            Thread feThread = new Thread(feTask);

            seqThread.start();
            feThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void startAll() throws InterruptedException {
        startQC();
        startON();
        startBC();
    }

    private static void startQC() {
        Runnable task = () -> QCServer.main(null);
        Thread QC = new Thread(task);
        QC.start();
    }

    private static void startON() {
        Runnable task = () -> ONServer.main(null);
        Thread ON = new Thread(task);
        ON.start();
    }

    private static void startBC() {
        Runnable task = () -> BCServer.main(null);
        Thread BC = new Thread(task);
        BC.start();
    }

    private static void restartAll() throws InterruptedException {
        endQC();
        endON();
        endBC();
        Thread.sleep(200);
        startAll();
        Thread.sleep(500);
        System.out.println("All servers restarted.");
        resendMessages();
    }

    private static void endQC() {
        endServerUDP(5551);
        endServerUDP(5001);
    }

    private static void endON() {
        endServerUDP(5552);
        endServerUDP(5002);
    }

    private static void endBC() {
        endServerUDP(5553);
        endServerUDP(5003);
    }

    private static boolean heartbeat(String storePrefix) {
        String message = "Heartbeat";
        String alive = "TRUE";

        switch (storePrefix) {
            case "QC":
                return sendUDP(5551, message).equals(alive);
            case "ON":
                return sendUDP(5552, message).equals(alive);
            case "BC":
                return sendUDP(5553, message).equals(alive);
            default:
                return false;
        }
    }

    private static void resendMessages() throws InterruptedException {
        for (String request : received_commands) {
            sendRequest("x" + request);
            Thread.sleep(100);
        }
    }

    private static void receiveMulticast() {
        try (MulticastSocket socket = new MulticastSocket(4444);) {
            socket.joinGroup(InetAddress.getByName("230.4.4.5"));
            while (true) {
                byte[] buffer = new byte[1000];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String message = new String(packet.getData(), packet.getOffset(), packet.getLength()).trim();
                System.out.println(message);
                int seq = new Request(message).getSequence_id();

                if (seq == nextSeq) {
                    sendRequest(message);
                    received_commands.add(message);
                    nextSeq++;
                } else if (seq > nextSeq) {
                    requestQueue.put(seq, message);
                    requestMissedPackets(nextSeq);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void requestMissedPackets(int seq) {
        String message = requestResend(seq);

        while (!message.equals("none")) {
            Request request = new Request(message);

            if (request.getSequence_id() == nextSeq) {
                sendRequest(message);
                received_commands.add(message);
                nextSeq++;

                String nextRequest = requestQueue.get(nextSeq);

                while (nextRequest != null) {
                    sendRequest(nextRequest);
                    received_commands.add(nextRequest);
                    nextRequest = requestQueue.get(++nextSeq);
                }

                message = requestResend(nextSeq);
            } else {
                break;
            }
        }
    }

    private static void sendRequest(String message) {
    	Request request = null;
    	if (message.charAt(0) == 'x') {
    		request = new Request(message.substring(1));
    	}
    	else {
    		request = new Request(message);
    	}
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

    private static void receiveFE() {
        try (DatagramSocket socket = new DatagramSocket(6000)) {
            while (true) {
                byte[] buffer = new byte[1000];
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);

                String message = new String(request.getData(), request.getOffset(), request.getLength()).trim();
                System.out.println(message);

                if (message.contains("nick") && message.contains("CRASHED")) {
                    if (!heartbeat("QC") || !heartbeat("ON") || !heartbeat("BC")) {
                        restartAll();
                    }
                } else if (message.contains("nick") && message.contains("FAILED") && message.contains("restart")) {
                    restartAll();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String sendUDP(int port, String message) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(2500);

            byte[] bytes = message.getBytes();
            InetAddress hostName = InetAddress.getByName("localhost");
            DatagramPacket request = new DatagramPacket(bytes, bytes.length, hostName, port);
            socket.send(request);

            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            socket.receive(reply);

            return new String(reply.getData(), 0, reply.getLength());

        } catch (SocketTimeoutException e) {
            System.out.println(e.getMessage());
            return "";
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

    private static String requestResend(int seq) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(2500);

            byte[] bytes = String.valueOf(seq).getBytes();
            InetAddress hostName = InetAddress.getByName("localhost"); // REPLACE WITH ADDRESS OF SEQUENCER (WILL BE DIFFERENT HOST)
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, hostName, 4200);
            socket.send(packet);

            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            socket.receive(reply);

            return new String(reply.getData(), 0, reply.getLength());

        } catch (Exception e) {
            e.printStackTrace();
            return "none";
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
