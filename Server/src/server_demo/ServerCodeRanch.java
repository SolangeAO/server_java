package server_demo;

import java.io.*;
import java.net.*;

public class ServerCodeRanch {

	public static void main(String[] args) throws IOException {

		final String STORAGE = "/Users/solange/Desktop/lily/";
		final String PYTHON_PATH = "/Users/solange/anaconda3/bin/python";
		final String PYTHON_FILE = "/Users/solange/PycharmProjects/music_processor";
		final String[] command = { PYTHON_PATH, PYTHON_FILE, "", ""};

		int bytesRead;
		String fileToSend = null;
		boolean finished = false;
		
		int userid;
		int userProfile;
		String fileName;
		String filePath;
		
		OutputStream outputFile;
		long size;
		byte[] buffer;
		

		try {

			ServerSocket serverSocket = null;
			serverSocket = new ServerSocket(8080);

			while (!finished) {

				System.out.println("wait for connection on port 8080");

				Socket clientSocket = null;
				clientSocket = serverSocket.accept();
				System.out.println("got connection on port 8080");

				InputStream in = clientSocket.getInputStream();
				DataInputStream clientData = new DataInputStream(in);

				int instruction = clientData.readInt();
				System.out.println(instruction);

				switch (instruction) {
				case 1:		
					userid = clientData.readInt();
					int exerid =  clientData.readInt();
					fileName = clientData.readUTF();
					filePath = STORAGE + fileName + ".wav";
					System.out.println("User id: "+ userid);
					System.out.println("User profile: "+ instruction);
					System.out.println("Exercise id: "+ exerid);
					System.out.println("File name: "+fileName);
					System.out.println("File storage: "+ filePath);
					outputFile = new FileOutputStream(
							filePath);
					size = clientData.readLong();
					buffer = new byte[1024];
					while (size > 0
							&& (bytesRead = clientData.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
						outputFile.write(buffer, 0, bytesRead);
						size -= bytesRead;
					}

					command[2]= filePath;
					command[3]= fileName;
					
					
							
					fileToSend = "/Users/solange/Desktop/lily/scale_c_minor_midi.png";
					break;

				case 2:
					userid = clientData.readInt();
					fileName = clientData.readUTF();
					filePath = STORAGE + fileName + ".wav";
					System.out.println("User id: "+ userid);
					System.out.println("User profile: "+ instruction);
					System.out.println("File name: "+fileName);
					System.out.println("File storage: "+ filePath);
					outputFile = new FileOutputStream(
							filePath);
					size = clientData.readLong();
					buffer = new byte[1024];
					while (size > 0
							&& (bytesRead = clientData.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
						outputFile.write(buffer, 0, bytesRead);
						size -= bytesRead;
					}

					command[2]= filePath;
					command[3]= fileName;
					
					/*
					Process p = Runtime.getRuntime().exec(command);
					BufferedReader pyFile = new BufferedReader(new InputStreamReader(p.getInputStream()));
					String pyLine;
					
					//Iterate through all the lines in the script
					while ((pyLine = pyFile.readLine()) != null) {
						System.out.println(pyLine);
					}

					fileToSend = STORAGE + fileName + ".png";
					*/
					fileToSend = "/Users/solange/Desktop/lily/Sol.png";
					
					break;

				default:
					fileToSend = "/Users/solange/Desktop/lily/thanos.png";

				}

				File myFile = new File(fileToSend);
				byte[] mybytearray = new byte[(int) myFile.length()];

				FileInputStream fis = null;
				fis = new FileInputStream(myFile);
				BufferedInputStream bis = new BufferedInputStream(fis);

				BufferedOutputStream outToClient = null;
				OutputStream os = clientSocket.getOutputStream();
				outToClient = new BufferedOutputStream(os);

				bis.read(mybytearray, 0, mybytearray.length);
				outToClient.write(mybytearray, 0, mybytearray.length);
				outToClient.flush();

				bis.close();

				System.out.println("File sent: " + fileToSend);

			}

			serverSocket.close();

		} catch (IOException ioe) {
			ioe.printStackTrace();

		} catch (Exception e) {
			e.printStackTrace();

		}
	}
}
