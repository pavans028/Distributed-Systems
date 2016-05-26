package p2p;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The IndexingServer program implements main server in P2P Napster style File
 * transferring application, to which each clients connect and register their
 * filenames from directory.
 * 
 * It also provides a lookup module where in a client can search filenames, and
 * gets in return the ipaddress of the peer which has the file.
 *
 * This is one main server for all clients.
 *
 * @author Pavankumar Shetty
 * @version 1.0
 * @since 2015-09-21
 */

public class IndexingServer {

	private static ServerSocket serverSocket; // Server socket to host server and keep ready for client request
	private static Socket clientSocket = null; // Client socket to store connected client socket 
	private static ConcurrentHashMap<String, ArrayList<String>> mapOfPeers; // COncurrentHashMap to hold all filenames, which acts a repository for lookup model.

	public static void main(String[] args) throws IOException {

		try {
			//Read values from property file
			FileReader readServerIP = new FileReader("config.properties"); //"config.properties" file has all ipaddress and directory path of each client.
			Properties properties = new Properties();
			properties.load(readServerIP);
			String ipAddress = properties.getProperty("ipAddressIndexServer"); // This variable is will fetch ipaddress of the indexServer from the property file
			
			//Host the index server with ServerSocket
			InetAddress inet = InetAddress.getByName(ipAddress); // InetAddress	is used to host server with required ipaddress
			serverSocket = new ServerSocket(4444, 0, inet);
			System.out.println("Indexing Server Ready.");
			
			//Instantiate a new map variable to store all filenames registered by the clients
			mapOfPeers = new ConcurrentHashMap<String, ArrayList<String>>();
			
		} catch (Exception e) {
			System.err.println("Port already in use.");
			System.exit(1);
		}
		
		// Make server ready to listen to all clients lookup request
		while (true) {
			try {
				//Store the accepted client connection socket.
				clientSocket = serverSocket.accept();
				System.out.println("Accepted connection : " + clientSocket);

				// Start a thread for each client and pass its socket
				Thread t = new Thread(new IndexServerClientCon(clientSocket, mapOfPeers));
				t.start();

			} catch (Exception e) {
				System.err.println("Error in connection attempt.");
			}
		}
	}

}
