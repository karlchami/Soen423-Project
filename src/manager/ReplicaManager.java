package manager;


import Models.request.Request;
import replica.replica_waqar.server_waqar.BCServer;
import replica.replica_waqar.server_waqar.ONServer;
import replica.replica_waqar.server_waqar.QCServer;


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;


public class ReplicaManager {
    public static ArrayList<String> stashed_commands;
    public static ArrayList<String> received_commands;
    public static Thread QC;
    public static Thread ON;
    public static Thread BC;
    public static int sequence_count;

    public static void main(String args[]) {

        try {
            sequence_count = 0;
            startAll();
            stashed_commands = new ArrayList<String>();
            received_commands = new ArrayList<String>();
            Runnable task = () -> {
                receive_multicast();
            };
            Thread thread = new Thread(task);
            thread.start();

            Runnable task2 = () -> {
                receive();
            };
            Thread thread2 = new Thread(task2);
            thread2.start();


        } catch (Exception e) {
        }


    }


    private static void startBC() {
        Runnable task = () -> {
            try {
                String[] args = new String[1];
                BCServer.main(args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        BC = new Thread(task);
        BC.start();

    }

    private static void startON() {
        Runnable task = () -> {
            try {
                String[] args = new String[1];
                ONServer.main(args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        ON = new Thread(task);
        ON.start();
    }

    private static void startQC() {
        Runnable task = () -> {
            try {
                String[] args = new String[1];
                QCServer.main(args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        QC = new Thread(task);
        QC.start();
    }

    public static void endQCThread() {
        EndServerUDP(3003, "Exit");
        try {
            while (!QC.isInterrupted() && QC.isAlive()) {
                QC.interrupt();
            }
        } catch (Exception consumed) {

        }
    }

    public static void endBCThread() {
        EndServerUDP(3002, "Exit");
        try {
            while (!BC.isInterrupted() && BC.isAlive()) {
                BC.interrupt();
            }
        } catch (Exception consumed) {
            consumed.printStackTrace();
        }
    }

    public static void endONThread() {
        EndServerUDP(3001, "Exit");
        try {
            while (!ON.isInterrupted() && ON.isAlive()) {
                ON.interrupt();
            }
        } catch (Exception consumed) {

        }
    }

    public static void startAll() throws InterruptedException {
        Thread.sleep(1500);
        startBC();
        Thread.sleep(1500);
        startON();
        Thread.sleep(1500);
        startQC();
    }

    public static void restartAll() throws InterruptedException {
        Thread.sleep(1500);
        endQCThread();
        Thread.sleep(1500);
        endBCThread();
        Thread.sleep(1500);
        endONThread();
        startAll();
        Thread.sleep(3500);
        System.out.println("RESTARTED");
    }

    public static boolean heartbeat(String StorePrefix) {
        String status = "";
        System.out.println("Prefix check :" + StorePrefix.equals("QC"));
        if (StorePrefix.equals("ON"))
            status = sendUDP(2001, "Heartbeat").trim();
        if (StorePrefix.equals("BC"))
            status = sendUDP(2002, "Heartbeat").trim();
        if (StorePrefix.equals("QC"))
            status = sendUDP(2003, "Heartbeat").trim();
        if (status.equals("TRUE"))
            return true;
        return false;

    }

    public static void parseJSON() {
        try {
            String testString = "{\n" +
                    "    \"replica_id\" : \"karl/waqar/nick\",\n" +
                    "    \"sequence_id\" : \"5\",\n" +
                    "    \"response_details\" : {\n" +
                    "        \"method_name\" : \"returnItem\",\n" +
                    "        \"message\" : \"customerID return of itemID on dateOfReturn\",\n" +
                    "        \"status_code\" : \"successful/failed\",\n" +
                    "        \"parameters\" : {\n" +
                    "            \"customerID\" : \"QCU1001\",\n" +
                    "            \"itemID\" : \"QC6000\",\n" +
                    "            \"dateOfReturn\" : \"20-11-2020\"\n" +
                    "        }\n" +
                    "    }\n" +
                    "}";


            testSeperate(testString);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void testSeperate(String JSONString) {
        try {
            Request request = new Request(JSONString);
            System.out.println(request.getRequest_details().getMethod_name());


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void resendMessages(){
        for(int i=0; i<received_commands.size();i++) {
            String sentence = received_commands.get(i);
            Request dumbo = new Request(sentence);
            sentence = "R-" + sentence;
            if (dumbo.getStore().equals("QC")) {
                System.out.println("Resent");;
                System.out.println(sendNoReply(3003, sentence, "localhost"));
            }
            if (dumbo.getStore().equals("BC")) {
                sendNoReply(3002, sentence, "localhost");
            }
            if (dumbo.getStore().equals("ON")) {
                sendNoReply(3001, sentence, "localhost");
            }
        }
    }

    public static void handleRequest(String sentence){
        Request dumbo = new Request(sentence);

        if(sequence_count != dumbo.getSequence_id()){
            stashed_commands.add(sentence);
            System.out.println("Current Count : " + sequence_count + "\n Received Count : " + dumbo.getSequence_id());
            System.out.println("Added to queue");
            requestMissedPackets(sequence_count, dumbo.getSequence_id());
            return;
        }
        received_commands.add(sentence);
        System.out.println("Current Count : " + sequence_count + "\n Received Count : " + dumbo.getSequence_id());
        System.out.println("Executing : " + dumbo.getSequence_id());
        sequence_count++;

        if(dumbo.getStore().equals("QC")){
            System.out.println(sendNoReply(3003, sentence, "localhost"));
        }
        if(dumbo.getStore().equals("BC")){
            sendNoReply(3002, sentence, "localhost");
        }
        if(dumbo.getStore().equals("ON")){
            sendNoReply(3001, sentence, "localhost");
        }
    }

    private static void requestMissedPackets(int seq, int received_message_count) {
        for(int i=seq; i<received_message_count; i++){
            String message = requestResend(i,"132.205.95.146").trim();
            if(message.equals("none")){
                return;
            }
            handleRequest(message);
        }
        handleOutOfOrder();

    }

    private static String requestResend(int seq, String host) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(2500);

            byte[] bytes = String.valueOf(seq).getBytes();
            InetAddress hostName = InetAddress.getByName(host); // REPLACE WITH ADDRESS OF SEQUENCER (WILL BE DIFFERENT HOST)
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

    public static void handleOutOfOrder(){
        String command = "";
        for(int i=0; i<stashed_commands.size(); i++){
            Request request = new Request(stashed_commands.get(i));
            if(request.getSequence_id() == sequence_count){
                command = stashed_commands.get(i);
                stashed_commands.remove(i);
                handleRequest(command);
            }
        }
        if(!command.equals(""))
            handleOutOfOrder();

    }


    private static void receive_multicast() {
        MulticastSocket socket = null;
        String returnMessage = "";
        try {
            socket = new MulticastSocket(4444);
            socket.joinGroup(InetAddress.getByName("230.4.4.5"));
            byte[] buffer = new byte[1000];
            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);
                String sentence = new String(request.getData(), request.getOffset(), request.getLength()).trim();
                System.out.println("RM received : " + sentence);
                handleRequest(sentence);

            }
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        } finally {
            if (socket != null)
                socket.close();
        }
    }


    private static void receiveFE() {
        try (DatagramSocket socket = new MulticastSocket(6000);) {
            while (true) {
                byte[] buffer = new byte[1000];
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);

                String sentence = new String(request.getData(), request.getOffset(), request.getLength()).trim();

                if (sentence.equals("restart")) {
                    restartAll();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void receive() {
        DatagramSocket socket = null;
        String returnMessage = "";
        try {
            socket = new DatagramSocket(2020);
            byte[] buffer = new byte[1000];
            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);
                String sentence = new String(request.getData(), request.getOffset(), request.getLength()).trim();
                System.out.println("Function Received " + sentence);
                if (sentence.equals("restart")) {
                    restartAll();
                    resendMessages();
                    returnMessage = "restarted";
                }

                byte[] sendData = returnMessage.getBytes();
                DatagramPacket reply = new DatagramPacket(sendData, returnMessage.length(), request.getAddress(),
                        request.getPort());
                socket.send(reply);
            }
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        } finally {
            if (socket != null)
                socket.close();
        }
    }


    private static String sendUDP(int port, String UDPMessage) {
        DatagramSocket socket = null;
        String result = "";
        try {
            result = "";
            socket = new DatagramSocket();
            socket.setSoTimeout(27000);
            byte[] messageToSend = UDPMessage.getBytes();
            InetAddress hostName = InetAddress.getByName("localhost");
            DatagramPacket request = new DatagramPacket(messageToSend, UDPMessage.length(), hostName, port);
            socket.send(request);
            byte[] bf = new byte[256];
            DatagramPacket reply = new DatagramPacket(bf, bf.length);
            socket.receive(reply);
            result = new String(reply.getData());
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            if (socket != null)
                socket.close();
        }
        return result.trim();

    }

    private static String sendNoReply(int port, String UDPMessage, String host) {
        DatagramSocket socket = null;
        String result = "";
        try {
            result = "";
            socket = new DatagramSocket();
            socket.setSoTimeout(2700);
            byte[] messageToSend = UDPMessage.getBytes();
            InetAddress hostName = InetAddress.getByName(host);
            DatagramPacket request = new DatagramPacket(messageToSend, UDPMessage.length(), hostName, port);
            socket.send(request);
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            if (socket != null)
                socket.close();
        }
        return result.trim();

    }


    private static void EndServerUDP(int port, String UDPMessage) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            byte[] messageToSend = UDPMessage.getBytes();
            InetAddress hostName = InetAddress.getByName("localhost");
            DatagramPacket request = new DatagramPacket(messageToSend, UDPMessage.length(), hostName, port);
            socket.send(request);
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            if (socket != null)
                socket.close();
        }
    }


}
