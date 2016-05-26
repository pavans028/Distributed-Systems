package p2p;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The IndexServerClientCon program is a thread program which handles each
 * clients lookup part in P2P Napster style File transferring application.
 *
 * This is specific to each connected client.
 *
 * @author Pavankumar Shetty
 * @version 1.0
 * @since 2015-09-21
 */

public class IndexServerClientCon implements Runnable {

	private Socket clientSocket; // socket to communicate between client and
									// server
	private BufferedReader in = null; // input reader for Client
	// private BufferedReader stdin = null; // input reader for server
	private ConcurrentHashMap<String, ArrayList<String>> mapOfPeers; // map
																		// globally
																		// declared
																		// to
																		// use
																		// it
																		// throughout
																		// the
																		// class

	// Constructor to initialize client socket info and map, passed from
	// IndexServer.java
	public IndexServerClientCon(Socket client, ConcurrentHashMap<String, ArrayList<String>> mapOfPeers) {
		this.clientSocket = client;
		this.mapOfPeers = mapOfPeers;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {

		try {
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			ObjectInputStream ios = new ObjectInputStream(clientSocket.getInputStream());
			ArrayList<String> listOfFiles = new ArrayList<String>();
			Object obj = ios.readObject();
			String ipAddress = in.readLine();
			// String ipAddress = clientSocket.getInetAddress().toString();
			listOfFiles = (ArrayList<String>) obj;

			// Iterate through list of files sent by client to register in
			// IndexServer Repository
			Iterator<String> listOfFilesIterator = listOfFiles.iterator();
			while (listOfFilesIterator.hasNext()) {
				String fileNameToStore = listOfFilesIterator.next();
				boolean fileAlreadyExist = false; // Boolean value, to check
													// whether the filename is
													// already registered, to
													// avoid duplicacy of the
													// filename in the map
				boolean newFile = false; // Boolean value, to check if its new
											// entry.

				// First entry in the map
				if (mapOfPeers.size() < 1) {
					ArrayList<String> listOfPeersFirst = new ArrayList<String>();
					listOfPeersFirst.add(ipAddress);
					mapOfPeers.put(fileNameToStore, listOfPeersFirst);
				} else {
					//Iterate in the map to check filename existence.
					Iterator<Entry<String, ArrayList<String>>> it = mapOfPeers.entrySet().iterator();
					while (it.hasNext()) {
						Entry<String, ArrayList<String>> pair = (Entry<String, ArrayList<String>>) it.next();
						String fileNameAlreadyIn = pair.getKey();
						if (fileNameAlreadyIn.equals(fileNameToStore)) {
							fileAlreadyExist = true;
						} else {
							newFile = true;
						}
					}
				}
				// if filename already exist, then add new ipaddress to it, retaining earlier value
				if (newFile == true && fileAlreadyExist == true) {
					// if ipaddress doesnt already exist in value field, then add it.
					if (!mapOfPeers.get(fileNameToStore).contains(ipAddress)) {
						mapOfPeers.get(fileNameToStore).add(ipAddress);
					}
				} else { // Add it straight-away if its new file
					ArrayList<String> listOfPeers = new ArrayList<String>();
					listOfPeers.add(ipAddress);
					mapOfPeers.put(fileNameToStore, listOfPeers);
					listOfPeers = null;
				}
			}
			System.out.println(ipAddress + " client registeration has been done and no of files registered are "
					+ listOfFiles.size());
			System.out.println("Total no of files registered till now : " + mapOfPeers.size());
			
		} catch (Exception e) {			
			System.err.println("Couldnot load files in the registry.");
		}
		// wait till client requests a file name.
		try {
			while (true) {
				String fileNameEntered = null;
				fileNameEntered = in.readLine();
				if (fileNameEntered != null)
					searchFiles(fileNameEntered); // Search the filename in the map repository.
			}

		} catch (Exception e) {
			System.err.println("Couldnt search the file.");
		}
	}
	/**
	* The searchFile(String) function accepts 1 parameter,
	* 1. String variable, which contains filename to be searched.
	* 
	* It receives filename from the client and provides the peer info who has this file.
	* 
	* @param fileNameEntered
	* 
	* @return Nothing
	*/
	private void searchFiles(String fileNameEntered) throws IOException {
		// TODO Auto-generated method stub
		try {
			// Initialize an Arraylist to send all the client ipaddress, which has the requested file.
			ArrayList<String> peerKey = new ArrayList<String>();

			// Iterate through the map to find the ipaddress
			Iterator<Entry<String, ArrayList<String>>> it = mapOfPeers.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, ArrayList<String>> pair = (Entry<String, ArrayList<String>>) it.next();
				if (pair.getKey().equals(fileNameEntered)) {
					peerKey = (ArrayList<String>) pair.getValue();
					break;
				}
			}
			// print in the server console about the lookup
			String searchResultMsg = null;
			//System.out.println(peerKey);
			if (peerKey.size() != 0) {
				searchResultMsg = "'" + fileNameEntered + "'- File is found with " + peerKey.size() + " peer/s";
			} else
				searchResultMsg = "'" + fileNameEntered + "'- No such file";

			System.out.println(searchResultMsg);
			
			// Create a Object Output stream to send the queried list, back to client.
			ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
			oos.writeObject(peerKey);
			oos.flush();
		} catch (Exception e) {
			System.err.println("Could not send the peer list to the client.");
		}
	}
}