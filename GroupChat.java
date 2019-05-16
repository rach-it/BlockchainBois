
import java.net.*;
import java.io.*;
import java.util.*;

public class GroupChat {
    private static final String TERMINATE = "Exit";
    static String name;
    static volatile boolean finished = false;

    public static void main(String[] args) {
        if (args.length != 2)
            System.out.println("Two arguments required: <multicast-host> <port-number>");
        else {
            try {
                InetAddress group = InetAddress.getByName(args[0]);
                int port = Integer.parseInt(args[1]);
                Scanner sc = new Scanner(System.in);
                System.out.print("Enter your name: ");
                name = sc.nextLine();
                MulticastSocket socket = new MulticastSocket(port);
                
                NetworkInterface ni = NetworkInterface.getByName("wlan3");
               
                socket.setNetworkInterface(ni);
                socket.setLoopbackMode(false);
                // Since we are deploying
                socket.setTimeToLive(1);
                // this on localhost only (For a subnet set it as 1)

                socket.joinGroup(group);
                Thread t = new Thread(new ReadThread(socket, group, port));
                
                Thread br = new Thread(new Blockreader(socket, group, port));
                // Spawn a thread for reading messages
                //t.start();
                br.start();
                Block b =new Block("Rachit","A");
                
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(b);
                oos.flush();
                System.out.println("THIS:"+bos.toString());
                
                byte[] bufblock=bos.toByteArray();
                DatagramPacket data = new DatagramPacket(bufblock, bufblock.length, group, port);
                socket.send(data);
                
                // sent to the current group
                System.out.println("Start typing messages...\n");
                while (true) {
                    String message;
                    message = sc.nextLine();
                    if (message.equalsIgnoreCase(GroupChat.TERMINATE)) {
                        finished = true;
                        socket.leaveGroup(group);
                        socket.close();
                        break;
                    }
                    message = name + ": " + message;
                    byte[] buffer = message.getBytes();
                    DatagramPacket datagram = new DatagramPacket(buffer, buffer.length, group, port);
                    socket.send(datagram);
                }
                sc.close();
            } catch (SocketException se) {
                System.out.println("Error creating socket");
                se.printStackTrace();
            } catch (IOException ie) {
                System.out.println("Error reading/writing from/to socket");
                ie.printStackTrace();
            }
           
        }
    }
}

class ReadThread implements Runnable {
    private MulticastSocket socket;
    private InetAddress group;
    private int port;
    private static final int MAX_LEN = 1000;

    ReadThread(MulticastSocket socket, InetAddress group, int port) {
        this.socket = socket;
        this.group = group;
        this.port = port;
    }

    @Override
    public void run() {
        while (!GroupChat.finished) {
            byte[] buffer = new byte[ReadThread.MAX_LEN];
            DatagramPacket datagram = new DatagramPacket(buffer, buffer.length, group, port);
            String message;
            try {
                socket.receive(datagram);
                message = new String(buffer, 0, datagram.getLength(), "UTF-8");
                if (!message.startsWith(GroupChat.name))
                    System.out.println(message);
            } catch (IOException e) {
                System.out.println("Socket closed!");
            }
        }
    }
}

 class Blockreader implements Runnable{
	 String serializedObject;
	 private MulticastSocket socket;
	 private InetAddress group;
	 private int port;
	 private static final int MAX_LEN = 1000;
	 
	 Blockreader(MulticastSocket socket, InetAddress group, int port) {
	        this.socket = socket;
	        this.group = group;
	        this.port = port;
	 }
	 
	 @Override
	 public void run() {
	        while (!GroupChat.finished) {
	            byte[] buffer = new byte[Blockreader.MAX_LEN];
	            DatagramPacket datagram = new DatagramPacket(buffer, buffer.length, group, port);
	            String message;
	            try {
	                socket.receive(datagram);
	                ByteArrayInputStream bi = new ByteArrayInputStream(buffer);
	                ObjectInputStream si = new ObjectInputStream(bi);
	                Block obj = (Block) si.readObject();
	                obj.Blockout();
	            } catch (Exception e) {
	                System.out.println("Socket closed!");
	            }
	        }
	    }
 }

 class Block implements Serializable{
	 int number;
	 String phash;
	 String voter;
	 String vote;
	 String sign;
	 String pkey;
	 String hash;
	 
	 Block(String voter, String vote){
		 this.voter=voter;
		 this.vote=vote;
		 number=0;
	 }
	 void Blockout() {
		 System.out.println("Reading the Block");
		 System.out.println(voter+":"+vote);
	 }
 }
