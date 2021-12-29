package manager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class Killer {
    private static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            try {
                String address = acquireTarget();
                int port = aim();
                kill(address, port);
            } catch (Exception e) {
                System.out.println("Target missed.");
            }
        }
    }

    private static void kill(String address, int port) throws Exception {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(3000);
            InetAddress hostName = InetAddress.getByName(address);

            byte[] bytes = "Exit".getBytes();
            DatagramPacket request = new DatagramPacket(bytes, bytes.length, hostName, port);
            socket.send(request);

            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length, request.getAddress(), request.getPort());
            socket.receive(reply);

            System.out.println(new String(reply.getData(), 0, reply.getLength()));
        }
    }

    private static String acquireTarget() {
        System.out.println("Enter address:");
        return sc.nextLine();
    }

    private static int aim() {
        System.out.println("Enter port:");
        return Integer.parseInt(sc.nextLine());
    }
}
