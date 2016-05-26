package p2p;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

/**
* The PeerOneAsServer program is a server part of a 1st client
* in P2P Napster style File transferring application.
*
* This transfers the file to other clients.
*
* @author  Pavankumar Shetty
* @version 1.0
* @since   2015-09-21 
*/

class PeerOneAsServer implements Runnable {

	Thread tClientOne; // Thread instance for every client file download request
	private static ServerSocket serverSocket; // Server socket to act as server for other clients.
	private static Socket clientSocket = null; // Client socket for incoming client request
	private static String directoryWindowsPeerOne; // Variable which stores the working directory path

	// Constructor to initialize each client and make it as a separate thread
	public PeerOneAsServer() {
		tClientOne = new Thread(this);
		tClientOne.start();
	}

	@Override
	public void run() {

		try {
			//Read values from property file
			FileReader readPeerOneDir = new FileReader("config.properties");
			Properties properties = new Properties();
			properties.load(readPeerOneDir);
			directoryWindowsPeerOne = properties.getProperty("directoryWindowsPeerOne"); //Peer One Working directory
			String ipAddress = properties.getProperty("ipAddressPeerOne"); //ipaddress to host client server
			InetAddress inet = InetAddress.getByName(ipAddress);
			serverSocket = new ServerSocket(4444,0,inet);
			System.out.println("Server available for other peers at IP : "+serverSocket.getInetAddress());
		} catch (Exception e) {
			System.err.println("Port already in use.");
			System.exit(1);
		}

		while (true) {
			try {
				clientSocket = serverSocket.accept();
				//System.out.println(clientSocket.getInetAddress() + " is connected and downloading a file.");

				Thread t = new Thread(new ClientAsServerCon(clientSocket, directoryWindowsPeerOne));
				t.start();

			} catch (Exception e) {
				System.err.println("Error in connection attempt.");
			}
		}

	}

}

/**
* The ClientOne program acts a 1st
* client in P2P Napster style File transferring application.
*
* @author  Pavankumar Shetty
* @version 1.0
* @since   2015-09-21 
*/

public class ClientOne {

	private static Socket sock; // socket to connect to IndexServer
	private static Socket sockToDownload; // a new socket which connects to client for file transfer
	private static BufferedReader stdin; // stdin-InputReader to read all console inputs
	private static PrintStream os, osDownload, osToSendIP; // os-OutputStream to send Filename to indexServer, osDownload-OutputStream to send Filename to other client's server, osToSendIP-OutputStream to send ipaddress to indexServer while registering
	private static String ipAddress; //ipaddress of this peer to send accross IndexServer
	private static String directoryWindowsPeerOne; // Variable which stores the working directory path

	public static void main(String[] args) throws IOException {
		try{
			//Read values from property file
			FileReader readPeerOneDir = new FileReader("config.properties");
			Properties properties = new Properties();
			properties.load(readPeerOneDir);
			directoryWindowsPeerOne = properties.getProperty("directoryWindowsPeerOne");
			ipAddress = properties.getProperty("ipAddressPeerOne");
			String ipAddrOfIndexServer = properties.getProperty("ipAddressIndexServer");
			
			// Start the server for other clients to connect for file sharing
			new PeerOneAsServer();
			
			// connect to indexServer with client socket named sock
			sock = new Socket(ipAddrOfIndexServer, 4444);
			
			//Register all the files, once the connection is established
			listFiles();
			
			stdin = new BufferedReader(new InputStreamReader(System.in));

		} catch (Exception e) {
			System.err.println("Cannot connect to the server, try again later.");
			System.exit(1);
		}

		System.out.println("You are connected to Index Server now.");
		
		//initiate lookup functionality
		selectAction();
		sock.close();
	}

	/**
	* The selectAction() function accepts no parameters.
	* It allows user to continue with lookup functionality and also to disconnect from the network.
	* 
	* @param No parameters
	* @return Nothing
	* @exception IOException on input error
	* @see IOException
	*/
	
	public static void selectAction() throws IOException {

		try {
			String action = "";
			while (true) {
				System.out.println("Do you want to Search file?\nEnter Y to continue\nEnter N to disconnect\n:");
				action = stdin.readLine();
				if ("Y".equals(action) || "y".equals(action) || "N".equals(action) || "n".equals(action)) {
					break;
				} else {
					System.out.println("Invalid Input. Please re-enter your choice.");
				}
			}
			switch (action) {
			case "y":
			case "Y":
				searchFiles(); // Calls searchFiles() method which allows user to search for file.
				break;
			case "n":
			case "N":
				closeTheConnection(); // Disconnects from the indexServer.
				break;
			default:
				System.out.println("Incorrect command received.");
				break;
			}
		} catch (Exception e) {
			System.err.println("Not a valid input");
			selectAction();
		}
	}

	/*
	 * final String directoryLinuxMac ="/Users/loiane/test"; final String
	 * directoryWindowsPeerOne ="C://test";
	 */
	
	/**
	* The closeTheConnection() function accepts no parameters.
	* It disconnects user from the indexServer.
	* 
	* @param No parameters
	* @return Nothing
	* @exception IOException on input error
	* @see IOException
	*/
	public static void closeTheConnection() throws IOException{
		System.out.println("Thank you for connecting.");
		osToSendIP.flush();
		os.flush();
		sock.close();
		System.exit(1);
	}
	
	/**
	* The listFiles() function accepts no parameters.
	* It lists all the files from the client directory and sends it to indexServer.
	* 
	* @param No parameters
	* @return Nothing
	*/
	public static void listFiles() {

		File directory = new File(directoryWindowsPeerOne);
		// To get all the files from a directory to register with server
		File[] fList = directory.listFiles();

		// Arraylist to store all filenames
		ArrayList<String> listOfFiles = new ArrayList<String>();
		for (File file : fList) {
			if (file.isFile()) {
				String fileName = file.getName();
				listOfFiles.add(fileName);
			}
		}
		// Pass the list to server for registering these in its repository
		try {
			ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
			oos.writeObject(listOfFiles);
			osToSendIP = new PrintStream(sock.getOutputStream());
			osToSendIP.println(ipAddress);
			oos.flush();
		} catch (IOException e) {
			System.err.println("Couldnt send the files to the index server");
		}
	}
	
	/**
	* The searchFiles() function accepts no parameters.
	* It provides interface to enter filename and also provides the search results in the console.
	* 
	* @param No parameters
	* @return Nothing
	*/
	
	@SuppressWarnings("unchecked")
	public static void searchFiles() {

		System.out.println("Please enter the file name");
		String fileNameEntered;
		try {
			fileNameEntered = stdin.readLine();
			System.out.println("Searching....");
			os = new PrintStream(sock.getOutputStream());
			os.println(fileNameEntered);
			// ObjectInputStream to get list of peers from the server
			ObjectInputStream ios = new ObjectInputStream(sock.getInputStream());

			//Arraylist to store all the peers
			ArrayList<String> peerList = new ArrayList<String>();
			Object obj = ios.readObject();
			peerList = (ArrayList<String>) obj;
			String searchResultMsg;
			if (peerList.size() != 0) {
				searchResultMsg = "File found with " + peerList.size() + " peers.\nEnter";
				int count = 1;
				Iterator<String> pLIterator = peerList.iterator();
				while (pLIterator.hasNext()) {
					searchResultMsg += " " + count + " to download from " + pLIterator.next() + "\n";
					count++;
				}
				System.out.println(searchResultMsg);
				iterateInPeerList(peerList,fileNameEntered); 	// Validation for peer selection
				
			} else {
				searchResultMsg = "No such file found.";
				System.out.println(searchResultMsg);
				selectAction(); 	// Invoke selectAction() for users to reenter different filename
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.err.println("Couldnt search the file in the server. No Such file found.");
		}
	}

	/**
	* The iterateInPeerList(ArrayList<String>,String) function accepts 2 parameters,
	* 1. ArrayList, which contains peers ipaddress &
	* 2. String, which contains filename to be downloaded.
	* 
	* It validates user input in selecting right peer from the list
	* and invokes connectToClientFileSharing() method which in turn connects to other client.
	* 
	* @param ArrayList, which contains peers ipaddress
	* @param String, which contains filename to be downloaded
	* @return Nothing

	*/
	
	public static void iterateInPeerList(ArrayList<String> peerList,String fileNameEntered){
		System.out.println("Enter here :");
		int i = 0;				
		//int i = (Integer.parseInt(stdin.readLine()));
		
		while (true) {
			try {
				i = (Integer.parseInt(stdin.readLine()));
				if (i > 0 && i<=peerList.size()) {
					int index = i - 1;
					System.out.println("Downloading file '" + fileNameEntered + "' from " + peerList.get(index));
					connectToClientFileSharing(peerList.get(index), fileNameEntered, peerList);
					break;
				} else {
					System.err.println("Invalid Input. Please re-enter your choice.\n Enter here :");
				}
			}
			catch (Exception e) {
				System.err.println("Not valid input!! Please enter only numbers");
				iterateInPeerList(peerList, fileNameEntered);
			}	
		}		
	}
	
	/**
	* The connectToClientFileSharing(String,String,ArrayList<String>) function accepts 3 parameters,
	* 1. String variable, which contains peer ipaddress to connect &
	* 2. String variable, which contains filename to be downloaded.
	* 3. ArrayList, which contains peers ipaddress, need this to invoke iterateInPeerList(ArrayList<String>,String) if not able to download from any peer 
	* 
	* It creates a new socket and connects to clients ipaddress which has the file.
	* And passes Filename through a printStream instance.
	* 
	* And invokes receiveFileAsAClient(String) to receive file.
	* 
	* @param String variable, which contains peer ipaddress to connect
	* @param String, which contains filename to be downloaded
	* @param ArrayList, which contains peers ipaddress
	* 
	* @param String, which contains filename to be downloaded
	* 
	* @return Nothing
	*/
	public static void connectToClientFileSharing(String peerAddress, String fileNameEntered, ArrayList<String> peerList) {
		// TODO Auto-generated method stub
		try {
			//sockToDownload = new Socket("localhost", 4444);
			sockToDownload = new Socket(peerAddress, 4444);
			osDownload = new PrintStream(sockToDownload.getOutputStream());
			osDownload.println(fileNameEntered);
			receiveFileAsAClient(fileNameEntered);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.err.println("Couldnot connect to client.\nPlease try downloading with different client.\n");
			iterateInPeerList(peerList, fileNameEntered);
		}

	}
	/**
	* The receiveFileAsAClient(String) function accepts 1 parameter,
	* 1. String variable, which contains filename to be downloaded.
	* 
	* It receives file from client.
	* 
	* Expects full path of the filename, so directoryWindows global variable is used here.
	* 
	* Doesnt return a value.
	*/
	
	public static void receiveFileAsAClient(String fileNameToBeReceived) {
		try {
			int bytesReadFromServer;
			InputStream in = sockToDownload.getInputStream(); // create a inputStream to read from clientSocket
			DataInputStream clientData = new DataInputStream(in); // reads data sent from DataOutputStream
			fileNameToBeReceived = clientData.readUTF(); // reads filename sent from the server
			
			// Enter the path where file has to be downloaded using FileOutputStream.
			OutputStream outputFS = new FileOutputStream((directoryWindowsPeerOne+"/"+fileNameToBeReceived));
			long sizeOfFile = clientData.readLong();
			byte[] buffer = new byte[1024];
			while (sizeOfFile > 0 && (bytesReadFromServer = clientData.read(buffer, 0, (int) Math.min(buffer.length, sizeOfFile))) != -1) {
				outputFS.write(buffer, 0, bytesReadFromServer);
				sizeOfFile -= bytesReadFromServer;
			}

			//Close all the input and output streams
			outputFS.close();
			in.close();

			System.out.println("File '" + fileNameToBeReceived + "' downloaded successfully.");
			selectAction();

		} catch (Exception e) {
			System.err.println("File cannot be downloaded. Please try later");
			searchFiles();
		}
	}
}
