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
	private String opponent = null;
	
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
	public void contactPlayer() throws IOException {
		System.out.println("Please choose what you would like to do today:\n"
				+ "1) \"play\" to play a random opponent\n"
				+ "2) \"find\" to search for an opponent to play\n");
		String input = scanner.nextLine().toLowerCase();
		isQuit(input);
		socketOutput.println(input);
		String response;
		try {
			switch(input) {
				case "play":
					response = socketInput.readLine();
					switch (response) {
						case "timeout":
							System.out.println("Timeout error. Please try again.");
							contactPlayer();
							break;
						case "match":
							opponent = socketInput.readLine();
							System.out.println("Match found with opponent: " + opponent);
							break;
					}
					break;
				case "find":
					System.out.println("Please enter the username of the opponent you are searching for.");
					input = scanner.nextLine();
					isQuit(input);
					opponent = input;
					socketOutput.println(input);
					response = socketInput.readLine();
					switch (response) {
						case "unregistered":
							System.out.println("You need to be registered to search for opponents.");
							contactPlayer();
							break;
						case "denied":
							System.out.println("Play request denied.");
							contactPlayer();
							break;
						case "invalid":
							System.out.println("Invalid opponent.");
							contactPlayer();
							break;
						case "accepted":
							System.out.println("Play request accepted by your opponent: " + opponent);
							break;
					}
					break;
				default:
					throw new IllegalArgumentException();
			}
		} catch (IllegalArgumentException e) {
			System.out.println("You can only choose the following inputs...");
			contactPlayer();
		}
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
	public void authorize() throws IOException {
		System.out.println("Please choose an authorization method. You can type:\n"
				+ "1) \"register\" to Register\n"
				+ "2) \"login\" to Log in\n"
				+ "3) \"guest\" to play as Guest");
		try {
			String input = scanner.nextLine().toLowerCase();
			isQuit(input);
			socketOutput.println(input);
			switch (input) {
				case "register":
				case "login":
					String response;
					do {
						System.out.println("Please enter your username");
						input = scanner.nextLine();
						isQuit(input);
						socketOutput.println(input);
						response = socketInput.readLine();
						if (response.equals("error")) {
							System.out.println("Username already in use or database error."
									+ "\nPlease try again.");
						}
					} while(response.equals("error"));
					do {
						System.out.println("Please enter your password");
						input = scanner.nextLine();
						isQuit(input);
						socketOutput.println(input);
						response = socketInput.readLine();
						if (response.equals("error")) {
							System.out.println("Database error. Please try again");
						}
					} while(response.equals("error"));
					break;
				case "guest":
					break;
				default:
					throw new IllegalArgumentException();
			}
		} catch (IllegalArgumentException e) {
			System.out.println("Illegal argument, please choose one of the options below...");
			authorize();
		}
	}
	/*
	 * Test main class
	 */
	public void isQuit(String quit) {
		if (quit.toLowerCase().equals("quit")) {
			socketOutput.println("quit");
			close();
			System.exit(0);
		}
	}
	public void startGame() {
		Board board = new Board(7,6,4);
		
	}
	public static void main(String [] args) {
		Clientmain solution = null;
		try {
			solution = new Clientmain();
			System.out.println("Welcome to Connect 4 by Saleem, Matthew, Maia, Dylan, Yoon Jung and Anh!");
			System.out.println("You can quit anytime by typing \"quit\"");
			solution.authorize();
			while (true) {
				solution.contactPlayer();
				solution.startGame();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (solution != null) {
			solution.close();
		}
	}
}
