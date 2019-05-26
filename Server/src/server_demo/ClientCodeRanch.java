package server_demo;
import java.net.*;
import java.io.*;

public class ClientCodeRanch {
	
	public static void main(String[] args) throws IOException {
		 
        Socket sock = new Socket("127.0.0.1", 13267);
 
        //Send file
        File myFile = new File("E7060v1.2.zip");
        byte[] mybytearray = new byte[(int) myFile.length()];
         
        FileInputStream fis = new FileInputStream(myFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        //bis.read(mybytearray, 0, mybytearray.length);
         
        DataInputStream dis = new DataInputStream(bis);   
        dis.readFully(mybytearray, 0, mybytearray.length);
         
        OutputStream os = sock.getOutputStream();
         
        //Sending file name and file size to the server
        DataOutputStream dos = new DataOutputStream(os);   
        dos.writeUTF(myFile.getName());   
        dos.writeLong(mybytearray.length);   
        dos.write(mybytearray, 0, mybytearray.length);   
        dos.flush();
      
         
        //Closing socket
        os.close();
        dos.close();
        sock.close();
    }

}
