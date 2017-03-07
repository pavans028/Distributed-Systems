package dht;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
* The PeerTwoAsServerDHT program is a server part of a 2nd client
* in Distributed hash table scenario
*
* @author  Pavankumar Shetty
* @version 1.0
* @since   2015-10-08
*/

class PeerTwoAsServerDHT implements Runnable {

	Thread tClient; // Thread instance for every client file download request
	private static ServerSocket serverSocket; // Server socket to act as server for other clients.
	private static Socket clientSocket = null; // Client socket for incoming client request
	private static ConcurrentHashMap<String, String> distHashMapOfPeers; // COncurrentHashMap to hold all key value pairs, which acts a Distributed hash table.
	// Constructor to initialize each client and make it as a separate thread
	public PeerTwoAsServerDHT() {
		tClient = new Thread(this);
		tClient.start();
	}

	@Override
	public void run() {

		try {
			//Read values from property file
			FileReader readPeerDir = new FileReader("config.properties");
			Properties properties = new Properties();
			properties.load(readPeerDir);
			String ipAddress = properties.getProperty("ipAddressPeerTwo"); //ipaddress to host client server
						
			InetAddress inet = InetAddress.getByName(ipAddress);
			serverSocket = new ServerSocket(4444,0,inet);
			System.out.println("Server available for other peers at IP : "+serverSocket.getInetAddress());
			distHashMapOfPeers = new ConcurrentHashMap<String, String>();
			
		} catch (Exception e) {
			System.err.println("Port already in use.");
			System.exit(1);
		}

		while (true) {
			try {
				clientSocket = serverSocket.accept();
				Thread t = new Thread(new ClientAsServerConDHT(clientSocket, distHashMapOfPeers));
				t.start();

			} catch (Exception e) {
				System.err.println("Error in connection attempt.");
			}
		}

	}

}

/**
* The ClientTwoDHT program acts a 2nd
* client in Distributed hash table scenario
*
* @author  Pavankumar Shetty
* @version 1.0
* @since   2015-10-08
*/

public class ClientTwoDHT {

	//private static Socket sockToOperateOnDHT; // a new socket which handles client communication with other clients server.
	private static BufferedReader stdin; // stdin-InputReader to read all console inputs
	private static ObjectInputStream in; // in-InputReader to read inputs from different server.
	private static ObjectOutputStream osToOperateOnDHT; // osToOperateOnDHT-OutputStream to send values to other client's server
	private static String ipAddress; //ipaddress of the peer to create socket for each client
	private static int noOfServers; // Variable which stores the no of servers in the network
	private static ArrayList<Socket> availablePeers = null; // List to store all the sockets created
	private static ArrayList<String> availablePeersIPAddr = null; // List to store all the sockets IP address created
	private static ArrayList<ObjectOutputStream> availableOStream = null; //List to store outputstreams for all servers
	private static ArrayList<ObjectInputStream> availableIStream = null; //List to store inputstreams for all servers

	public static void main(String[] args) throws IOException {
		try{
			//Read values from property file
			FileReader readPeerDir = new FileReader("config.properties");
			Properties properties = new Properties();
			properties.load(readPeerDir);
			
			availablePeers = new ArrayList<Socket>();
			availableOStream = new ArrayList<ObjectOutputStream>();
			availableIStream = new ArrayList<ObjectInputStream>();
			for(int i=0;i<8;i++){
				availablePeers.add(null);
			}
			for(int j=0;j<8;j++){
				availableOStream.add(null);
			}
			for(int k=0;k<8;k++){
				availableIStream.add(null);
			}
			availablePeersIPAddr = new ArrayList<String>();
			
			ipAddress = properties.getProperty("ipAddressPeerOne");
			availablePeersIPAddr.add(ipAddress);
			
			ipAddress = properties.getProperty("ipAddressPeerTwo");
			availablePeersIPAddr.add(ipAddress);
			
			ipAddress = properties.getProperty("ipAddressPeerThree");
			availablePeersIPAddr.add(ipAddress);
			
			ipAddress = properties.getProperty("ipAddressPeerFour");
			availablePeersIPAddr.add(ipAddress);
			
			ipAddress = properties.getProperty("ipAddressPeerFive");
			availablePeersIPAddr.add(ipAddress);
			
			ipAddress = properties.getProperty("ipAddressPeerSix");
			availablePeersIPAddr.add(ipAddress);
			
			ipAddress = properties.getProperty("ipAddressPeerSeven");
			availablePeersIPAddr.add(ipAddress);
			
			ipAddress = properties.getProperty("ipAddressPeerEight");
			availablePeersIPAddr.add(ipAddress);
			ipAddress = null;
			
			// Start the server for other clients to use DHT
			new PeerTwoAsServerDHT();
			noOfServers = 8;
			stdin = new BufferedReader(new InputStreamReader(System.in));
			
			//initiate lookup functionality
			selectActionDHT();			
			

		} catch (Exception e) {
			System.err.println("Cannot connect to the server, try again later.");
			System.exit(1);
		}		
	}

	/**
	* The selectActionDHT() function accepts no parameters.
	* It allows user to select the operations(PUT, GET, DELETE).
	* 
	* @param No parameters
	* @return Nothing
	* @exception NO Exceptions thrown
	* 
	*/
	
	public static void selectActionDHT() {

		try {
			String action = "";
			while (true) {
				System.out.println("Hello !! Please select an option\n:");
				System.out.println("1. Put a key\n2. Get a key\n3. Delete a key\n:");
				action = stdin.readLine();
				if ("1".equals(action) || "2".equals(action) || "3".equals(action)) {
					break;
				} else {
					System.out.println("Invalid Input. Please re-enter your choice.");
				}
			}
			switch (action) {
			case "1":
				System.out.println("Enter the key to be saved:");
				String keyToBeSaved;
				while(true){
					keyToBeSaved = stdin.readLine();
					byte[] b = keyToBeSaved.getBytes();
					if(b.length >24)
						System.err.println("Key size is more. Please re-enter:");
					else
						break;
				}
				System.out.println("Enter Message:");
				String msgToBeSaved;
				while(true){
					msgToBeSaved = stdin.readLine();
					byte[] b = msgToBeSaved.getBytes();
					if(b.length >1000)
						System.err.println("Key size is more. Please re-enter:");
					else
						break;
				}
				int indexOfServerPut = generateHashKey(keyToBeSaved);
				Socket serverToPut = availablePeers.get((indexOfServerPut-1));
				//Call put function
				boolean putResult = putTheKeyIntoServer(keyToBeSaved, msgToBeSaved, serverToPut, (indexOfServerPut-1));
				if(putResult){
					System.out.println("Successfully put the key: "+keyToBeSaved);
					System.out.println("----------------------------------------");
					selectActionDHT();
				}else{
					System.err.println("Please try again after sometime.");
					selectActionDHT();
				}
				
				break;
			case "2":				
				System.out.println("Enter the key to retrieve:");
				String keyToBeRetrieved;
				while(true){
					keyToBeRetrieved = stdin.readLine();
					byte[] b = keyToBeRetrieved.getBytes();
					if(b.length >24)
						System.err.println("Key size is more. Please re-enter:");
					else
						break;
				}
				int indexOfServerGet = generateHashKey(keyToBeRetrieved);
				Socket serverToGet = availablePeers.get((indexOfServerGet-1));
				//Call get function
				String getResult = getTheKeyFromServer(keyToBeRetrieved, serverToGet, (indexOfServerGet-1));
				if(getResult.equals("")){
					System.err.println("No such key found.\n");
					selectActionDHT();
				}else{
					System.out.println("Message : "+getResult);
					System.out.println("-------------------------------------");
					selectActionDHT();
				}				
				break;
			case "3":
				System.out.println("Enter the key to delete:");
				String keyToBeDeleted;
				while(true){
					keyToBeDeleted = stdin.readLine();
					byte[] b = keyToBeDeleted.getBytes();
					if(b.length >24)
						System.err.println("Key size is more. Please re-enter:");
					else
						break;
				}
				int indexOfServerDel = generateHashKey(keyToBeDeleted);	
				//Call delete function
				
				Socket serverToDel = availablePeers.get((indexOfServerDel-1));
				boolean delResult = delTheKeyFromServer(keyToBeDeleted, serverToDel, (indexOfServerDel-1));
				if(delResult){
					System.out.println("Successfully deleted the key: "+keyToBeDeleted);
					System.out.println("-----------------------------------------------");
					selectActionDHT();
				}else{
					System.err.println("No such key found");
					selectActionDHT();
				}								
				break;
			default:
				System.out.println("Incorrect command received.");
				break;
			}
		} catch (Exception e) {
			System.err.println("Not a valid input");
			selectActionDHT();
		}
	}

	/**
	* The putTheKeyIntoServer() function accepts 4 parameters.
	* It inserts key into the Server.
	* 
	* @param String, contains the key to be entered into server
	* @param String, contains the msg to be entered into server
	* @param String, contains the socket created to communicate with the server.
	* @param Integer, contains the index of the socket in the list.
	* 
	* @return Boolean, which gives the success or failure state of the insertion
	* @exception 
	* @see
	*/

	public static boolean putTheKeyIntoServer(String key, String msg, Socket serverNo, int indexOfServerPut){
		boolean result = false;
		try {			
			if(serverNo == null){
				String ipAddress = availablePeersIPAddr.get(indexOfServerPut);
				serverNo = new Socket(ipAddress, 4444);
				availablePeers.set(indexOfServerPut, serverNo);
				osToOperateOnDHT = new ObjectOutputStream(serverNo.getOutputStream());
				in = new ObjectInputStream(serverNo.getInputStream());
				availableOStream.set(indexOfServerPut, osToOperateOnDHT);
				availableIStream.set(indexOfServerPut, in);
			}		
			else{
				osToOperateOnDHT = availableOStream.get(indexOfServerPut);
				in = availableIStream.get(indexOfServerPut);	
			}	
			String stringToSendServer = null;
			stringToSendServer = "1"+"~"+key+"~"+msg;
			osToOperateOnDHT.writeObject(stringToSendServer);
			result = Boolean.parseBoolean((String)in.readObject());
			return result;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.err.println("Couldnot INSERT into server.\nPlease try again.\n");
			selectActionDHT();
		}
		return result;
	}
	
	/**
	* The getTheKeyFromServer() function accepts 3 parameters.
	* It gets the message associated with the key from the Server.
	* 
	* @param String, contains the key to be searched in the server
	* @param String, contains the socket created to communicate with the server.
	* @param Integer, contains the index of the socket in the list.
	* 
	* @return String, Msg which gives out the message value associated with the key.
	* @exception IOException on input error
	* @see IOException
	*/
	public static String getTheKeyFromServer(String key, Socket serverNo, int indexOfServerGet) throws IOException{
		
		String result = "";
		try {
			if(serverNo == null){
				String ipAddress = availablePeersIPAddr.get(indexOfServerGet);
				serverNo = new Socket(ipAddress, 4444);
				availablePeers.set(indexOfServerGet, serverNo);
				in = new ObjectInputStream(serverNo.getInputStream());
				osToOperateOnDHT = new ObjectOutputStream(serverNo.getOutputStream());
				availableOStream.set(indexOfServerGet, osToOperateOnDHT);
				availableIStream.set(indexOfServerGet, in);
			}		
			else{
				osToOperateOnDHT = availableOStream.get(indexOfServerGet);
				in = availableIStream.get(indexOfServerGet);	
			}

			String stringToServerToGet = null;
			stringToServerToGet = "2"+"~"+key;
			osToOperateOnDHT.writeObject(stringToServerToGet);
			result = (String)in.readObject();
			return result;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.err.println("Couldnot retrieve the value.\nPlease try again.\n");
			selectActionDHT();
		}
		return "";
	}
	/**
	* The delTheKeyFromServer() function accepts accepts 3 parameters.
	* It deletes the entered key from the Server.
	* 
	* @param String, contains the key to be deleted in the server
	* @param String, contains the socket created to communicate with the server.
	* @param Integer, contains the index of the socket in the list.
	* 
	* @return Boolean, which gives the success or failure state of the deletion
	* @exception IOException on input error
	* @see IOException
	*/
	public static boolean delTheKeyFromServer(String key, Socket serverNo, int indexOfServerDel) throws IOException{
		boolean result = false;
		try {
			
			if(serverNo == null){
				String ipAddress = availablePeersIPAddr.get(indexOfServerDel);
				serverNo = new Socket(ipAddress, 4444);
				availablePeers.set(indexOfServerDel, serverNo);
				in = new ObjectInputStream(serverNo.getInputStream());
				osToOperateOnDHT = new ObjectOutputStream(serverNo.getOutputStream());
				availableOStream.set(indexOfServerDel, osToOperateOnDHT);
				availableIStream.set(indexOfServerDel, in);
			}		
			else{
				osToOperateOnDHT = availableOStream.get(indexOfServerDel);
				in = availableIStream.get(indexOfServerDel);	
			}
			String stringToServerToDel = null;
			stringToServerToDel = "3"+"~"+key;
			osToOperateOnDHT.writeObject(stringToServerToDel);
			result = Boolean.parseBoolean((String)in.readObject());
			return result;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.err.println("Couldnot delete.\nPlease try again\n");
			selectActionDHT();
		}
		return false;
	}
	
	/**
	* The generateHashKey() function accepts key name as a parameter.
	* It generates hash value for each key entered. And then multipied by prime number.
	* Modulus of the obtained hash value with total no of server is done to identify the server to add the key.
	* 
	* @param String, Key name to be stored in server.
	* @return integer, server number
	*/
	public static int generateHashKey(String keyEntered) {

		int hashValueOfServer = 0;
		
		hashValueOfServer = keyEntered.hashCode()*11;
		hashValueOfServer %= noOfServers;		
		if(hashValueOfServer<=0){
			hashValueOfServer += noOfServers;;
		}
		//System.out.println("hashValue-->"+hashValueOfServer);
		return hashValueOfServer;
	}
	
}
