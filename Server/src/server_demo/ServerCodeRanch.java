package server_demo;

import java.io.*;
import java.net.*;

public class ServerCodeRanch {

	public static void main(String[] args) throws IOException {

		int bytesRead;
		//int current = 0;
		String fileToSend = null;
		boolean finished = false;

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


				//if (outToClient != null) {

					// Switch statement que dependiendo el int enviado por el cliente, envia un
					// diferente file

					// AGREGAR EN UNO DE LOS CASES EL: 
					// 3.MANDAR A ABRIR PYTHON PARA PROCESAR

					switch (instruction) {
					case 1:
						fileToSend = "/Users/solange/Desktop/lily/otra.png";
						break;
						
					case 2:
						String fileName = clientData.readUTF();
						System.out.println(fileName);
						OutputStream outputFile = new FileOutputStream("/Users/solange/Desktop/MusicCatcher/"+fileName+".wav");
						long size = clientData.readLong();
						byte[] buffer = new byte[1024];
						while (size > 0 && (bytesRead = clientData.read(buffer, 0,
								(int) Math.min(buffer.length, size))) != -1) {
							outputFile.write(buffer, 0, bytesRead);
							size -= bytesRead;
						}
					
						//outputFile.close();
					
						fileToSend = "/Users/solange/Desktop/lily/Sol.png";
						break;
						
					default:
						fileToSend = "/Users/solange/Desktop/lily/thanos.png";

					}
					
					// Para enviar al cliente un file
					
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
					// connectionSocket.close();
					
					bis.close();

					System.out.println("File sent");

				//}
				// Para mandar al cliente un string, dependiendo del int que haya recibido
				/*
				 * try {
				 * 
				 * PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
				 * 
				 * switch (size) { case 1 : pw.write("Escogiste el 1. Hola desde Java"); break;
				 * case 2 : pw.write("Hola. Se mandó 2"); break; default:
				 * pw.write("No escogiste ninguna opcion valida");
				 * 
				 * }
				 * 
				 * pw.flush();
				 * 
				 * } catch (IOException io) { io.printStackTrace(); }
				 */

				/*
				 * byte[] buffer = new byte[1024]; while (size > 0 && (bytesRead =
				 * clientData.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
				 * output.write(buffer, 0, bytesRead); size -= bytesRead; }
				 */

				// Closing the FileOutputStream handle
			
				//in.close(); - NO SE PUEDE CERRAR PORQUE SINO, SE INTERRUMPE LA TRANSMISIÓN DE LA IMAGEN
				//clientData.close();- NO SE PUEDE CERRAR PORQUE SINO, SE INTERRUMPE LA TRANSMISIÓN DE LA IMAGEN
				//outToClient.close();
				//clientSocket.close();

			}

			serverSocket.close();

		} catch (IOException ioe) {
			ioe.printStackTrace();

		} catch (Exception e) {
			e.printStackTrace();

		}
	}
}
