import java.io.*;
import java.util.*;
import java.net.*;

public class VoterSystem {
	static volatile boolean finished = false; 
	static volatile boolean verifieduser = false;
	static volatile String previoushash="init";
	static volatile String user;
	static volatile String fileName=null;
	
	static void readBlockchain() {
		
	}
	
	static void createGenesisBlock() {
		
	}
	
	static boolean verifyUser(String ID) {
		//Check exists in hashmap and has not voted
		return true;
	}
	
	static boolean verifyBlock() {
		return true;
	}
	
	static void showCandidates() {
		System.out.println("A,B,C\n");
	}
	
	static void vote(String user,String vote) {
		showCandidates();
		Block b = new Block();
		b.voter=user;
		b.vote = vote;
		b.phash=VoterSystem.previoushash;
		b.sign=null;
		b.pkey=null;
		b.hash=computeHash(b);
	
		sendBlock(b);
	}
	
	static String computeHash(Block b) {
		String hash=null;
		return hash;
	}
	
	static void countVotes() {
		
	}
	
	
	static void sendBlock(Block b) {
		try {
			InetAddress group = InetAddress.getByName("239.1.2.3");
            int port = Integer.parseInt("1234");
            
            MulticastSocket socket = new MulticastSocket(port);
            NetworkInterface ni = NetworkInterface.getByName("wlan3");           
            socket.setNetworkInterface(ni);
            socket.setTimeToLive(1);
            socket.setLoopbackMode(false);
            socket.joinGroup(group);
            
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(b);
            oos.flush();
            //System.out.println("THIS:"+bos.toString());
            
            byte[] bufblock=bos.toByteArray();
            DatagramPacket data = new DatagramPacket(bufblock, bufblock.length, group, port);
            socket.send(data);
            socket.close();
            
		} catch (SocketException se) {
            System.out.println("Error creating socket");
            se.printStackTrace();
        } catch (IOException ie) {
            System.out.println("Error reading/writing from/to socket");
            ie.printStackTrace();
        }
	}
	
	static void addBlock(Block b) {

	}
		
	public static void main(String args[]) {
		try {
			InetAddress group = InetAddress.getByName("239.1.2.3");
            int port = Integer.parseInt("1234");
            
            MulticastSocket socket = new MulticastSocket(port);
            NetworkInterface ni = NetworkInterface.getByName("wlan3");
            socket.setNetworkInterface(ni);
            socket.setTimeToLive(1);         
            socket.joinGroup(group);
            
            Thread t = new Thread(new Receive(socket,group, port));
            t.start();
            
            
		} catch (SocketException se) {
            System.out.println("Error creating socket");
            se.printStackTrace();
        } catch (IOException ie) {
            System.out.println("Error reading/writing from/to socket");
            ie.printStackTrace();
        }
		
		
		Scanner sc = new Scanner(System.in);
		String details=null;
		String choice= null;
		while(!VoterSystem.finished) {
			System.out.println("Voter System Running\n");
			
			while(!VoterSystem.verifieduser) {
				System.out.println("Enter Details\n");
				
				details = sc.nextLine();
				if(VoterSystem.verifyUser(details))
					VoterSystem.verifieduser = true;
			}
			
			VoterSystem.user=details;
			
			while(true) {
				System.out.println("1.Vote 2.Count 3.View Chain 4.Exit");
				
				choice = sc.nextLine();
				System.out.println("THIS IS IT: " + choice);
				if(choice.equals("vote")) {
					showCandidates();
					String votedfor=sc.nextLine();
					vote(VoterSystem.user,votedfor);
				}
				else if(choice.equals("Count"))
					VoterSystem.countVotes();
				else if(choice.equals("view"))
					VoterSystem.readBlockchain();
				else if(choice.equals("exit")) {
					VoterSystem.finished = true;
					System.out.println("Going Bye");
					break;
				}
				else
					System.out.println("Wrong input\n");
			}
		}
		sc.close();
	}
}

class Receive implements Runnable{
	private MulticastSocket socket;
    private InetAddress group;
    private int port;
    private static final int MAX_LEN = 1000;

    Receive(MulticastSocket socket, InetAddress group, int port) {
        this.socket = socket;
        this.group = group;
        this.port = port;
    }
    
	@Override
	public void run() {
		while (!VoterSystem.finished) {
            byte[] buffer = new byte[Receive.MAX_LEN];
            DatagramPacket datagram = new DatagramPacket(buffer, buffer.length, group, port);
               
            try {
                socket.receive(datagram);
                ByteArrayInputStream bi = new ByteArrayInputStream(buffer);
                ObjectInputStream si = new ObjectInputStream(bi);
                Block obj = (Block) si.readObject();
                obj.Blockout();
                if(VoterSystem.verifyBlock())
                	VoterSystem.addBlock(obj);
            } catch (Exception e) {
                System.out.println("Socket closed!");
            }
        }
	}
}

class Block implements Serializable{
	final static long serialVersionUID=0;
	int number;
	String phash;
	String voter;
	String vote;
	String sign;
	String pkey;
	String hash;
	
	void Blockout() {
		 System.out.println("Reading the Block");
		 System.out.println(voter+":"+vote);
	 }
}