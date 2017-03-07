package dht;
import java.io.*;
import java.net.*;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

/**
* The ClientAsServerConDHT program is a thread program which handles each clients
* PUT GET DELETE operations on Distributed hash table.
*
* This helps in performing the PUT GET DELETE operations on Distributed hash table.
*
* @author  Pavankumar Shetty
* @version 1.0
* @since   2015-10-08
*/

public class ClientAsServerConDHT implements Runnable {

    private Socket clientSocket; // CLient socket used to communicate.
    private static ObjectInputStream peerIn = null; // Input Stream to read values from client Socket
    private static ObjectOutputStream peerOut = null; // Output Stream to send values to client
    
	private ConcurrentHashMap<String, String> distHashMapOfPeers; // map
	// globally
	// declared
	// to
	// use
	// it
	// throughout
	// the
	// class

	// Constructor to initialize client socket info and map, passed from
	// Client's server
	public ClientAsServerConDHT(Socket client, ConcurrentHashMap<String, String> distHashMapOfPeers) {
		this.clientSocket = client;
		this.distHashMapOfPeers = distHashMapOfPeers;
	}
    
	@Override
    public void run() {
        
		try {
			peerIn = new ObjectInputStream(clientSocket.getInputStream());
			peerOut = new ObjectOutputStream(clientSocket.getOutputStream());
			while(true){
	        String completeObject =(String)peerIn.readObject();
			//System.out.println(operation);	
			StringTokenizer keyMsg = new StringTokenizer(completeObject, "~");
			//String operation = completeObject.valueOf(completeObject.charAt(0));
			String operation = (String)keyMsg.nextElement();
			String key = null;
				String msg = null;				
				switch(operation){
				case "1" :
					key = (String)keyMsg.nextElement();
					msg = (String)keyMsg.nextElement();
					boolean putResult = putTheKeyIntoServer(key, msg);
					peerOut.writeObject(String.valueOf(putResult));
					break;
				case "2" :
					key = (String)keyMsg.nextElement(); 
					String getResult = getTheKeyFromServer(key);
					peerOut.writeObject(getResult);
					break;
				case "3" :
					key = (String)keyMsg.nextElement();
					boolean delResult = delTheKeyFromServer(key);
					peerOut.writeObject(String.valueOf(delResult));
					break;
				default:
					System.out.println("Incorrect command received.");
					break;
				}
			
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	/**
	* The delTheKeyFromServer() function accepts accepts 1 parameter.
	* It deletes the entered key from the Server.
	* 
	* @param String, contains the key to be deleted in the server
	* @param String, contains the socket created to communicate with the server.
	* 
	* @return Boolean, which gives the success or failure state of the deletion
	*/
	private boolean delTheKeyFromServer(String key) {
		
		try {
			if (distHashMapOfPeers.containsKey(key)) {
				distHashMapOfPeers.remove(key);	
				return true;
			}
			
		} catch (Exception e) {
			System.err.println("Could not delete the key");
		}		
		return false;
	}
	
	/**
	* The getTheKeyFromServer() function accepts 1 parameter.
	* It gets the message associated with the key from the Server.
	* 
	* @param String, contains the key to be searched in the server
	* 
	* @return String, Msg which gives out the message value associated with the key.
	*/
	private String getTheKeyFromServer(String key) {
		// TODO Auto-generated method stub
		String message = "";
		try {
			message = distHashMapOfPeers.get(key);
				//String searchResultMsg = null;
				if (message == null){
					return "";					
				}							
				return message;
			
	
		} catch (Exception e) {
			System.err.println("Could not send the key list to the client.");
		}		
		return message;
	}

	/**
	* The putTheKeyIntoServer() function accepts 2 parameters.
	* It inserts key into the Distributed hash table.
	* 
	* @param String, contains the key to be entered into server
	* @param String, contains the msg to be entered into server
	* 
	* @return Boolean, which gives the success or failure state of the insertion
	*/
	
	private boolean putTheKeyIntoServer(String key, String msg) {
		// TODO Auto-generated method stub
		try {
			
			distHashMapOfPeers.put(key, msg);	
			return true;
			
		} catch (Exception e) {			
			System.err.println("Couldnot store the key in the registry.");
		}
		return true;
	}
}