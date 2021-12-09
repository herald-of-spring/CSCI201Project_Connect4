package connect4;

import java.util.*;
import java.io.*;
import java.net.Socket;

/**
 * @author Saleem Bekkali
 *
 */
public class Clientmain {
	private static final String addr = "localhost";
	private static final int port = 10000;
	private Scanner scanner = null;
	private Socket socket = null;
	private BufferedReader socketInput = null;
	private PrintWriter socketOutput = null;
	
	/* 
	 * ClientMain constructor accepts no values. It initializes a Scanner to read user input and establishes a connection to the
	 * ServerMain via a Socket (will be retrieved from the front end in the future implementation). The Socket's ObjectInputStream and ObjectOutputStream are initialized. If an IOException is thrown,
	 * then it propagates to the front end and a new ClientMain needs to be instantiated to continue.
	 * 
	 * @throws IOException
	 */
	public Clientmain() throws IOException {
		scanner = new Scanner(System.in);
		socket = new Socket(addr, port);
		socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		socketOutput = new PrintWriter(socket.getOutputStream(), true);
		System.out.println("What is your username");
		String username = scanner.nextLine();
		System.out.println("What is your password");
		String password = scanner.nextLine();
		// Log in to server
	}
		
	/*
	 * Prints if the player is busy or pulls the chosen column from the Scanner (will be front end in the future) and sends it to ServerMain.
	 * If the column input is not an integer, then method is executed again recursively.
	 */
	/*public void contactPlayer(String action) {
		try {
			oos.writeObject("isPlaying");
			if ((Boolean) ois.readObject()) {
				System.out.println("Which column do you pick?");
				int column = Integer.parseInt(scanner.nextLine());
				oos.writeObject(Integer.valueOf(column));
			}
			else {
				System.out.println("Player is busy.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {

		}
	}*/
	
	/*
	 * 
	 */
	public String contactPlayer(String action ) {
		socketOutput.println(action);
		String response;
		try {
			if (action.equals("play")) {
				response = socketInput.readLine();
				if (response.equals("timeout")) {
					return "timeout";
				}
				else if (response.equals("match")) {
					return socketInput.readLine(); // opponent name
				}
			}
			else if (action.equals("find")) {
				response = socketInput.readLine();
				if (response.equals("unregistered")) {
					return "unregistered";
				}
			}
			else if (action.equals("quit")) {
				// quit
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/*
	 * Closes all resources. Cannot be called before other ClientMain method calls on the same instance.
	 */
	public void close() {
		try {
			if (scanner != null) {
				scanner.close();
			}
			if (socket != null) {
				socket.close();
			}
			if (socketInput != null) {
				socketInput.close();
			}
			if (socketOutput != null) {
				socketOutput.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/*
	 * Test main class
	 */
	public static void main(String [] args) {
		Clientmain solution = null;
		try {
			solution = new Clientmain();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (solution != null) {
			solution.close();
		}
	}
}
