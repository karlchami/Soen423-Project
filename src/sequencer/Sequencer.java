package sequencer;

import Models.request.Request;
import com.google.gson.Gson;

import java.io.IOException;

import java.net.*;
import java.util.ArrayList;

public class Sequencer {
    private static int seq = 0;
    private static ArrayList<String> requestList = new ArrayList<>(50);
    private static MulticastSocket multicastSocket;
    private static InetAddress group;
    private static Gson gson = new Gson();

    public static void main(String[] args) {
        try {
            group = InetAddress.getByName("230.4.4.5");
            multicastSocket = new MulticastSocket(4444);

            Runnable feTask = Sequencer::forwardRequest;
            Thread feThread = new Thread(feTask);
            feThread.start();

            Runnable rmTask = Sequencer::receiveRM;
            Thread rmThread = new Thread(rmTask);
            rmThread.start();

            // For testing - TO BE DELETED
            for (int i = 0; i < 30; i++) {
                String s = "{\n" +
                        "    \"replica_id\" : \"karl/waqar/nick\",\n" +
                        "    \"sequence_id\" : " + i + ",\n" +
                        "    \"store\" : \"QC\",\n" +
                        "    \"request_details\" : {\n" +
                        "        \"method_name\" : \"returnItem\",\n" +
                        "        \"parameters\" : {\n" +
                        "            \"customerID\" : \"QCU1001\",\n" +
                        "            \"itemID\" : \"QC200" + i + "\",\n" +
                        "            \"dateOfReturn\" : \"20112020\"\n" +
                        "        }\n" +
                        "    }\n" +
                        "}";

                requestList.add(s);

                if (i != 2 && i != 7 && i != 8 && i != 9 && i != 13 && i != 21 && i != 27 && i != 28) {
                    multicastRequest(s);
                    System.out.println("sent: " + i);
                }
//                if (i == 4 || i == 14 || i == 23) {
//                    multicastRequest("restart");
//                    System.out.println("sent: restart");
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void forwardRequest() {
        try {
            while (true) {
                String request = receiveFE();
                String seqRequest = attachSequenceId(request);
                multicastRequest(seqRequest);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String receiveFE() throws IOException {
        DatagramSocket sock = new DatagramSocket(4100);

        byte[] buffer = new byte[1000];
        DatagramPacket request = new DatagramPacket(buffer, buffer.length);

        sock.receive(request);

        return new String(request.getData(), 0, request.getLength());
    }

    private static String attachSequenceId(String message) {
        Request request = new Request(message);
        request.setSequence_id(seq++);
        String seqRequest = gson.toJson(request);
        requestList.add(seqRequest);
        return seqRequest;
    }

    private static void multicastRequest(String message) throws IOException {
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, 4444);
        multicastSocket.send(packet);
    }

    private static void receiveRM() {
        DatagramSocket socket = null;
        DatagramPacket packet = null;
        while (true) {
            try {
                socket = new DatagramSocket(4200);

                byte[] buffer = new byte[1000];
                packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String seqString = new String(packet.getData(), 0, packet.getLength());
                int seqId = Integer.parseInt(seqString);

                String request = requestList.get(seqId);

                byte[] bytes = request != null ? request.getBytes() : "none".getBytes();
                DatagramPacket resend = new DatagramPacket(bytes, bytes.length, packet.getAddress(), packet.getPort());
                socket.send(resend);

            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                byte[] bytes = "none".getBytes();
                DatagramPacket resend = new DatagramPacket(bytes, bytes.length, packet.getAddress(), packet.getPort());
                try {
                    socket.send(resend);
                } catch (IOException io) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                assert socket != null;
                socket.close();
            }
        }
    }
}
