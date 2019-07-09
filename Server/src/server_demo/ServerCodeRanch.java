package server_demo;

import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ServerCodeRanch {

	//QUE SE GUARDE EN la carpeta correspondiente de Music Catcher y tome de la carpeta que deba ser lo (incluyendo python) el png que corresponda
	
	private static String STORAGE_RECORDINGS = "/Users/solange/Desktop/MusicCatcher/recordings/";
	private static String STORAGE_IMAGES = "/Users/solange/Desktop/MusicCatcher/img/scores/";
	private static String PYTHON_PATH = "/Users/solange/anaconda3/bin/python";
	private static String PYTHON_FILE = "/Users/solange/PycharmProjects/music_processor";
	private static String[] command = { PYTHON_PATH, PYTHON_FILE, STORAGE_RECORDINGS, STORAGE_IMAGES, "", "","" };

	private static Socket clientSocket = null;
	private static int bytesRead;
	private static String fileToSend = null;
	private static boolean finished = false;

	private static DataInputStream clientData;

	private static int userid;
	private static int exerid;
	private static String fileName;
	private static String filePath;

	private static OutputStream outputFile;
	private static long size;
	private static byte[] buffer;

	//private static String url = "jdbc:mysql://127.0.0.1:3306/music_catcher";
	static String url = "jdbc:mysql://127.0.0.1:3306/music_catcher";
	private static String user = "admin1";
	private static String ps = "Sol900907";
	private static int compositionRow;

	public static void main(String[] args) throws IOException {

		try {

			ServerSocket serverSocket = null;
			serverSocket = new ServerSocket(8080);

			while (!finished) {

				System.out.println("wait for connection on port 8080");

				clientSocket = serverSocket.accept();
				System.out.println("got connection on port 8080");

				InputStream in = clientSocket.getInputStream();
				clientData = new DataInputStream(in);

				int instruction = clientData.readInt();
				System.out.println("Received instruction");

				receiver(instruction);
				DBinsert(instruction);
				procesor();
				sender();
			}

			serverSocket.close();

		} catch (IOException ioe) {
			ioe.printStackTrace();

		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	/**
	 * Receives the data file from the TCP Client and stores in the proper format
	 * and directory. If it is a student, it receives additionally the exercise id.
	 * 
	 * @param profile. int. Profile of the user that sends the information.
	 * @throws IOException
	 */
	private static void receiver(int profile) throws IOException {

		userid = clientData.readInt();

		if (profile == 1) {
			exerid = clientData.readInt();
			System.out.println("Exercise id: " + exerid);
		}

		fileName = clientData.readUTF();
		filePath = STORAGE_RECORDINGS + fileName + ".wav";

		System.out.println("User id: " + userid);
		System.out.println("User profile: " + profile);
		System.out.println("File name: " + fileName);
		System.out.println("File storage: " + filePath);

		outputFile = new FileOutputStream(filePath);
		size = clientData.readLong();
		buffer = new byte[1024];
		while (size > 0 && (bytesRead = clientData.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
			outputFile.write(buffer, 0, bytesRead);
			size -= bytesRead;
		}

	}

	/**
	 * Processes the file received and runs the music_processor.py for generating
	 * the corresponding image file for the file received.
	 * 
	 * @throws IOException
	 */
	private static void procesor() throws IOException {

		command[4] = filePath;
		command[5] = fileName;
		command[6] = Integer.toString(compositionRow);

		
		Process p = Runtime.getRuntime().exec(command); BufferedReader pyFile = new
		BufferedReader(new InputStreamReader(p.getInputStream())); String pyLine;
		  
		 // Iterate through all the lines in the script 
		 while ((pyLine =pyFile.readLine()) != null) { System.out.println(pyLine); }
		  
		 fileToSend = STORAGE_IMAGES + fileName + ".png";
		 
		//fileToSend = "/Users/solange/Desktop/lily/Sol.png";

	}

	/**
	 * Sends the back to the TCP client the generated file.
	 * 
	 * @throws IOException
	 */
	private static void sender() throws IOException {

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

	/**
	 * Inserts a row into the music_catcher database the compositions table the
	 * information about the author , .wav file and .png file.
	 * 
	 * @throws SQLException
	 */
	private static void DBinsert(int profile) throws SQLException {

		String wavFile = fileName + ".wav";
		String scores = fileName + ".png";

		ResultSet resultSet = null;

		try {
			Connection myconn = DriverManager.getConnection(url, user, ps);

			String query = "INSERT INTO music_catcher.compositions (author, recording, scores) VALUES (?,?,?)";

			PreparedStatement insertComposition = myconn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

			insertComposition.setInt(1, userid);
			insertComposition.setString(2, wavFile);
			insertComposition.setString(3, scores);

			insertComposition.executeUpdate();

			System.out.println(
					"Author, recording and scores have been added to the music_catcher DB. Table: compositions ");

			resultSet = insertComposition.getGeneratedKeys();

			if (resultSet.next()) {
				compositionRow = resultSet.getInt(1);
				System.out.println("Inserted id: " + compositionRow);
				if (profile == 1) {
					DBexercise(exerid, compositionRow);
					DBnotifier(exerid);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
		}
	}

	/**
	 * Inserts a row into the music_catcher database the studentExer_compositions table is the instruction comes from a student.
	 * @param studentExercise int. Student_exercise id passed to the server if the TCP client was a student.
	 * @param composition int. Last row inserted on the compositions table which is to be linked to an exercise assigned to a student.
	 * @throws SQLException
	 */
	private static void DBexercise(int studentExercise, int composition) throws SQLException {

		ResultSet resultSet = null;

		try {
			Connection myconn = DriverManager.getConnection(url, user, ps);

			String query = "INSERT INTO music_catcher.studentExer_compositions (student_exercise, composition) VALUES (?,?)";

			PreparedStatement insertStudentExer = myconn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

			insertStudentExer.setInt(1, studentExercise);
			insertStudentExer.setInt(2, composition);

			insertStudentExer.executeUpdate();

			System.out.println("Composition linked to student_exercise for student in music_catcher DB. "
					+ "Table: studentExer_compositions");

			resultSet = insertStudentExer.getGeneratedKeys();

			if (resultSet.next()) {
				int lastRow = resultSet.getInt(1);
				System.out.println("Inserted id: " + lastRow);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
		}

	}
	
	/**
	 * Adds a notification for the teacher for the recording that has been upload by the student for the exercise.
	 * @param exercise int.
	 * @throws SQLException
	 */
	private static void DBnotifier (int exercise) throws SQLException {
		
		int teacher = 0;
		
		ResultSet resultSet = null;
		
		try {
			Connection myconn = DriverManager.getConnection(url, user, ps);
			
			String queryTeacher = " SELECT * FROM music_catcher.student_exercises "
					+ "INNER JOIN music_catcher.exercise "
					+ "ON music_catcher.student_exercises.exercise = music_catcher.exercise.id "
					+ "WHERE music_catcher.student_exercises.id = ? ";
			
			PreparedStatement selectTeacher= myconn.prepareStatement(queryTeacher);
			
			selectTeacher.setInt(1, exercise);
				
			resultSet = selectTeacher.executeQuery();
			
			while (resultSet.next()) {
				teacher = resultSet.getInt("teacher");
			}
			
			String query = "INSERT INTO music_catcher.notifications (activity, recipient, student_exercises) VALUES (?,?,?)";
			
			PreparedStatement insertNotifcaition = myconn.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);
			
			insertNotifcaition.setInt(1, 4);
			insertNotifcaition.setInt(2, teacher);
			insertNotifcaition.setInt(3, exercise);
			
			insertNotifcaition.executeUpdate();
			
			System.out.println("Activity, recipient and student_exercises have been added to the music_catcher DB."
					+ "Table: notifications ");	
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
		}		
	}
}
