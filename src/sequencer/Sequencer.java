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

            String s = "{\n" +
                    "    \"replica_id\" : \"karl/waqar/nick\",\n" +
                    "    \"sequence_id\" : -1,\n" +
                    "    \"store\" : \"QC\",\n" +
                    "    \"response_details\" : {\n" +
                    "        \"method_name\" : \"returnItem\",\n" +
                    "        \"message\" : \"customerID return of itemID on dateOfReturn\",\n" +
                    "        \"status_code\" : \"successful/failed\",\n" +
                    "        \"parameters\" : {\n" +
                    "            \"customerID\" : \"QCU1001\",\n" +
                    "            \"itemID\" : \"QC1023\",\n" +
                    "            \"dateOfReturn\" : \"20112020\"\n" +
                    "        }\n" +
                    "    }\n" +
                    "}";

            Thread.sleep(250);
            int i = 0;
            while (true) {
//                forwardRequest();
                if (i==3){
                    multicastRequest("KILL");
                    Thread.sleep(41000);
                }
                i++;
                System.out.println("Sent");
                multicastRequest(s);
                Thread.sleep(500);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void forwardRequest() throws Exception {
        String request = receiveFrontEnd();
        String seqRequest = attachSequenceId(request);
        multicastRequest(seqRequest);
    }

    private static String receiveFrontEnd() throws Exception {
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

    private static void receiveRM() throws Exception {
        DatagramSocket socket = new DatagramSocket(4200);

        byte[] buffer = new byte[1000];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        socket.receive(packet);

        String seqString = new String(packet.getData(), 0, packet.getLength());
        int seqId = Integer.parseInt(seqString);

        String request = requestList.get(seqId);

        byte[] requestBuffer = request.getBytes();
        DatagramPacket resend = new DatagramPacket(requestBuffer, requestBuffer.length, packet.getAddress(), packet.getPort());

        socket.send(resend);
    }
}
