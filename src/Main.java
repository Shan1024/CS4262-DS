
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Shan
 */
public class Main {

    private final String SERVER_HOST = "sid.projects.mrt.ac.lk";
    private final int SERVER_PORT = 3000;

    private int count = 2;

    private String MY_IP = "localhost";
    private int MY_PORT;
//            = 300 + count;
    private String MY_USERNAME;
//    = "Shan123" + count;
    private Socket clientSocket;

    private DataOutputStream outToServer;
    private BufferedReader inFromServer;

    private LinkedList<Node> nodes = new LinkedList<>();

    public Main() {
        MY_PORT = Integer.parseInt("300" + count);
        MY_USERNAME = "Shan123" + count;
    }

    private void start() {

        boolean connected = createConnection();

        if (connected) {

            register();

            try {

//            InputStream inFromServer = clientSocket.getInputStream();
//            DataInputStream in = new DataInputStream(inFromServer);
//            byte[] reply = new byte[100];
//            in.read(reply);
//            System.out.println("Server says: " + new String(reply));
                String reply = inFromServer.readLine();
                System.out.println("REPLY FROM SERVER: " + reply);

                String[] temp = reply.split(" ");

                Node node;

                try {
//                    switch (temp[2]) {
//                        case "0":
//                            break;
//                        case "1":
//                            node = new Node(temp[3], Integer.parseInt(temp[4]), temp[5]);
//                            nodes.add(node);
//                            System.out.println("Node: " + node);
//                            break;
//                        case "2":
//                            node = new Node(temp[3], Integer.parseInt(temp[4]), temp[5]);
//                            nodes.add(node);
//                            System.out.println("Node1: " + node);
//
//                            node = new Node(temp[6], Integer.parseInt(temp[7]), temp[8]);
//                            nodes.add(node);
//                            System.out.println("Node2: " + node);
//                            break;
//                        default:
//                            System.out.println("Error occurred: " + reply);
//                            break;
//                    }

                    int n = Integer.parseInt(temp[2]);

                    if (n > 0) {
                        for (int i = 0; i < n; i++) {
                            node = new Node(temp[3 * (i + 1)], Integer.parseInt(temp[3 * (i + 1) + 1]), temp[3 * (i + 1) + 2]);
                            nodes.add(node);
                            System.out.println("Node: " + node);
                        }
                    }

                } catch (Exception e) {
                    System.out.println("Ex: " + e);
                }

                joinDistributedSystem();

                createUDPListener();

            } catch (Exception ex) {
                System.out.println("Ex: " + ex);
            }

        }

    }

    private boolean createConnection() {
        try {
//            MY_IP = Inet4Address.getLocalHost().getHostAddress();
            System.out.println("My IP: " + MY_IP);

            clientSocket = new Socket(SERVER_HOST, SERVER_PORT);

            outToServer = new DataOutputStream(clientSocket.getOutputStream());
            inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            return true;

        } catch (Exception e) {
            System.out.println("Ex: " + e);
            return false;
        }
    }

    private void register() {
        try {
            String msg = " REG " + MY_IP + " " + MY_PORT + " " + MY_USERNAME;
//            System.out.println("MSG: " + msg + " , LENGTH: " + msg.length());
//            System.out.println("COMPLETE MSG: " + "00" + (msg.length() + 4) + msg);
            outToServer.write(("00" + (msg.length() + 4) + msg).getBytes());
        } catch (Exception e) {
            System.out.println("Ex: " + e);
        }
    }

    private void createUDPListener() {

        System.out.println("Creating listener on port: " + MY_PORT);

        try {
            DatagramSocket datagramSocket = new DatagramSocket(MY_PORT);

            byte[] buffer = new byte[1024];
            DatagramPacket incomingPacket = new DatagramPacket(buffer, buffer.length);

            new Thread(new Runnable() {

                @Override
                public void run() {
                    while (true) {
                        System.out.println("Running");
                        try {
                            datagramSocket.receive(incomingPacket);
                            String msg = new String(buffer);
                            System.out.println("Received: " + msg);

                            boolean ok = processReceivedMsg(msg);

                            InetAddress IPAddress = incomingPacket.getAddress();
                            System.out.println("IPAddress: " + IPAddress);
                            int port = incomingPacket.getPort();
                            System.out.println("port: " + port);
                            String reply;

                            if (ok) {
                                reply = "0013 JOINOK 0";
                            } else {
                                reply = "0016 JOINOK 9999";
                            }

                            byte[] data = reply.getBytes();
                            DatagramPacket replyPacket = new DatagramPacket(data, data.length, IPAddress, port);
                            datagramSocket.send(replyPacket);

                        } catch (Exception ex) {
                            System.out.println("Ex: " + ex);
                        }

                    }
                }
            }).start();

        } catch (Exception ex) {
            System.out.println("Ex: " + ex);
        }
    }

    private boolean processReceivedMsg(String msg) {

        return true;
    }

    private void joinDistributedSystem() {

        System.out.println("joinDistributedSystem()");
        try {

            DatagramSocket datagramSocket = new DatagramSocket();

            String message = "JOIN " + MY_IP + " " + MY_PORT;

            message = "00" + (message.length() + 5) + " " + message;

            byte[] incomingData = new byte[1024];
            byte[] buffer = message.getBytes();

            for (Node node : nodes) {
                System.out.println("Node: " + node);
                InetAddress address = InetAddress.getByName(node.getIp());
                System.out.println("InetAddress: " + address + ":" + node.getPort());
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, node.getPort());
                datagramSocket.send(packet);
                System.out.println("message sent");

                DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
                datagramSocket.receive(incomingPacket);
                String response = new String(incomingPacket.getData());
                System.out.println("Response from server:" + response);
            }

        } catch (Exception e) {
            System.out.println("Ex: " + e);
        }
    }

    public static void main(String[] args) {
        new Main().start();
    }

}
