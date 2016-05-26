package p2p;
import java.io.*;
import java.net.*;

/**
* The ClientAsServerCon program is a thread program which handles each clients
* file download part in P2P Napster style File transferring application.
*
* This transfers the file to the specific connected client.
*
* @author  Pavankumar Shetty
* @version 1.0
* @since   2015-09-21 
*/

public class ClientAsServerCon implements Runnable {

    private Socket clientSocket; // CLient socket used to communicate and send the file.
    private String fullPath; // which contains the full path of the working directory
    private static BufferedReader peerIn = null; // Input Stream to read filename from client Socket
    
    // Constructor to initialize clientSocket and fullpath
    public ClientAsServerCon(Socket client, String fullPath) {
        this.clientSocket = client;
        this.fullPath = fullPath;
    }

    
	@Override
    public void run() {
        
		try {
			peerIn = new BufferedReader(new InputStreamReader(
			        clientSocket.getInputStream()));
			String fileName = peerIn.readLine();
			fileName = fullPath+"/"+fileName;
			sendFileAsAServer(fileName);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	/**
	* The sendFileAsAServer(String) function accepts 1 parameter,
	* 1. String variable, which contains filename to be sent.
	* 
	* It sends file to the client.
	* 
	* Expects full path of the filename, so fullpath global variable is used here.
	* 
	* @param String, which contains filename to be sent
	* @return Nothing
	*/
    public void sendFileAsAServer(String fileNameToBeSent) {
    	
        try {
            // creates a new file abstract instance
            File fileToBeSent = new File(fileNameToBeSent);
            byte[] byteArray = new byte[(int) fileToBeSent.length()];

            // create a fileInputStream to open a connection to actual file
            FileInputStream fis = new FileInputStream(fileToBeSent);
            BufferedInputStream bis = new BufferedInputStream(fis);

            @SuppressWarnings("resource")
			DataInputStream dis = new DataInputStream(bis);
            
            // reads bytes from inputStream(array, offset value, array's length)
            dis.readFully(byteArray, 0, byteArray.length); 

            //create a output stream to send file over client socket
            OutputStream os = clientSocket.getOutputStream();

            //Send file name using writeTUF(which can be read using readUTF) 
            // and also size of the file to the client
            DataOutputStream dos = new DataOutputStream(os);
            dos.writeUTF(fileToBeSent.getName());
            dos.writeLong(byteArray.length);
            dos.write(byteArray, 0, byteArray.length);
            dos.flush();
            //System.out.println("File "+fileNameToBeSent+" successfully delivered.");
        } catch (Exception e) {
            System.err.println("File not found.");
        } 
    }

}