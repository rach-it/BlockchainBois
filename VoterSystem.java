import java.io.*;
import java.util.*;
import java.net.*;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;

public class VoterSystem {
	static volatile boolean finished = false; 
	static volatile boolean verifieduser = false;
	static volatile String previoushash="init";
	static volatile String user=null;
	static volatile String fileName=null;
	static final int COMPLEXITY = 4;
	
	static void readBlockchain() {
		
	}
	
	static void createGenesisBlock() {
		
	}
	
	static boolean verifyUser(String ID) {
		//Check exists in hashmap and has not voted
		return true;
	}
	
	static boolean verifyBlock(Block b) {
		return verifySign(b);
	}
	
	static boolean verifySign(Block b) {
		try {
			Signature dsa = Signature.getInstance("SHA256withECDSA");
			
			dsa.initVerify(b.pkey);
			String str = b.voter + b.vote;
			byte[] strByte = str.getBytes("UTF-8");
	        
	        dsa.update(strByte);
	        System.out.println("Sign Matched: " + dsa.verify(b.sign));
	        return dsa.verify(b.sign);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	
	static void showCandidates() {
		System.out.println("A,B,C\n");
	}
	
	static void vote(String user,String vote) {
		Block b = new Block();
		b.voter=user;
		b.vote = vote;
		b.phash=VoterSystem.previoushash;
		b.sign=null;
		b.pkey=null;
		b.salt = null;
		
		try {
	        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
	        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

	        keyGen.initialize(256, random);

	        KeyPair pair = keyGen.generateKeyPair();
	        PrivateKey priv = pair.getPrivate();
	        PublicKey pub = pair.getPublic();
	        b.pkey = pub;
	        
	        /*
	         * Create a Signature object and initialize it with the private key
	         */

	        Signature dsa = Signature.getInstance("SHA256withECDSA");

	        dsa.initSign(priv);
	        
	        String str = b.voter+b.vote;
	        
	        byte[] strByte = str.getBytes("UTF-8");
	        
	        dsa.update(strByte);

	        /*
	         * Now that all the data to be signed has been read in, generate a
	         * signature for it
	         */

	        b.sign = dsa.sign();


		} catch(Exception e) {
			e.printStackTrace();
		} 
		
		b.hash=computeHash(b);
	
		sendBlock(b);
	}

	// Pad a string, salt here, with 0s to make it 4 bit
	static String leftPadWithZeros(String s) {
        String answer = s;
        while (answer.length() < 4) {
            answer = "0" + answer;
        }
        return answer;
	}
	
	// Checks whether hash(data + salt) has atleast COMPLEXITY number of f's
	static boolean complexityChecker(String hashPlusSalt) {
		int cnt = 0;
		hashPlusSalt = hashPlusSalt.toLowerCase();
		for (int i = 0; i < hashPlusSalt.length(); i++) {
			char ch = hashPlusSalt.charAt(i);
			if (ch == 'f')
				cnt++;
		}
		if (cnt >= COMPLEXITY)
			return true;
		return false;
	}
		
	// Support Function for computing md5 segmented hash
	static String getMd5(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] messageDigest = md.digest(input.getBytes());

			BigInteger no = new BigInteger(1, messageDigest);

			String hashtext = no.toString(16);
			while (hashtext.length() < 32) {
				hashtext = "0" + hashtext;
			}
			return hashtext;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	// Find the correct 4 bit salt for a given data hash string
	static String findSalt(String datahash) {
		int lowLim = 0, upLim = 65535;
		for (int i = lowLim; i <= upLim; i++) {
			String saltHex = Integer.toHexString(i);
			saltHex = leftPadWithZeros(saltHex); // To keep salt 4 bit for all nos
			String hashPlusSalt = datahash + saltHex;
			if (complexityChecker(hashPlusSalt))
				return saltHex;
		}
		return "ffff";
	}
	
	static String computeHash(Block b) {
		String hash = null;
		ArrayList<String> hashableAttributes = new ArrayList<>();

		// Add or remove hashable attributes from here
		hashableAttributes.add(b.phash);
		hashableAttributes.add(b.voter);
		hashableAttributes.add(b.vote);

		StringBuilder str = new StringBuilder();
		for (String s : hashableAttributes) {
			String segmentHash = getMd5(s);
			// Chain all the segmented hashes
			str.append(segmentHash);
		}

		hash = getMd5(str.toString());
		String saltForHash = findSalt(hash);

		b.salt = saltForHash; // Setting the value for salt in the block

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
				//System.out.println("THIS IS IT: " + choice);
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
                if(VoterSystem.verifyBlock(obj))
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
	byte[] sign;
	PublicKey pkey;
	String salt;
	String hash;
	
	void Blockout() {
		 System.out.println("Reading the Block");
		 System.out.println(voter+"\n" +vote + "\n" + hash);
	 }
}