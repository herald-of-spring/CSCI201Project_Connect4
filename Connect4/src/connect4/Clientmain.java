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
	private int lastMove = 0;
	
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
	}

	/*
	 * 
	 */
	public void contactPlayer() throws IOException {
		System.out.println("Please choose what you would like to do today:\n"
				+ "1) \"play\" to play a random opponent or check for invites\n"
				+ "2) \"find\" to search for an opponent to play\n");
		String input = scanner.nextLine().toLowerCase().trim();
		isQuit(input);
		socketOutput.println(input);
		String response;
		try {
			switch(input) {
				case "play":
					response = readInput(socketInput);
					switch (response) {
						case "timeout":
							System.out.println("Timeout error. Please try again.");
							contactPlayer();
							break;
						case "match":
							opponent = readInput(socketInput);
							System.out.println("Match found with opponent: " + opponent);
							break;
						case "invite":
							opponent = readInput(socketInput);
							System.out.println("Invite received from: " + opponent);
							System.out.println("\"accept\" to accept or \"deny\" to deny.");
							input = scanner.nextLine().toLowerCase();
							isQuit(input);
							if (input.equals("accept")) {
								socketOutput.println("accept");
							}
							else {
								contactPlayer(); // any other response means denial and goes back to lobby
							}
							break;
					}
					break;
				case "find":
					System.out.println("Please enter the username of the opponent you are searching for.");
					System.out.println("Otherwise, you can do \"back\" to go back to the lobby.");
					input = scanner.nextLine();
					isQuit(input);
					switch(input) {
					case "back":
						break;
					default:
						socketOutput.println(input);
						System.out.println("default running");
						response = readInput(socketInput);
						System.out.println("response obtained: "+response);
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
								opponent = input;
								System.out.println("Play request accepted by your opponent: " + opponent);
								break;
						}
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
			String input = scanner.nextLine().toLowerCase().trim();
			isQuit(input);
			if(input!=null && (input.equals("register")||input.equals("login")||input.equals("guest"))) {
				socketOutput.println(input);
			}
			
			switch (input) {
				case "register":
				case "login":
					String response;
					do {
						System.out.println("Please enter your username");
						input = scanner.nextLine();
						isQuit(input);
						socketOutput.println(input);
						System.out.println("Please enter your password");
						input = scanner.nextLine();
						isQuit(input);
						socketOutput.println(input);
						//System.out.println(socketInput.toString());
						response = readInput(socketInput);
						//System.out.println(response);
//						while(true) {
//							response = socketInput.readLine().trim();
//							if(!response.isEmpty()) {
//								break;
//							}
//						}

						
						if (response.equals("error")) {
							System.out.println("Database error. Please try again");
						}
					} while(response.equals("error"));
					break;
				case "guest":
					
					do {
						response = readInput(socketInput);
					}while(response.equals("error"));
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
	public void startGame() throws IOException {
		Board board = new Board(7,6,4);
		System.out.println("Game started");
		board.print();
		boolean over = false;
		while (!over) {
			over = gameLoop(board);
		}
	}
	public boolean gameLoop(Board b) throws IOException {
		Board board = b;
		String response = readInput(socketInput);
		switch (response) {
			case "move":
				board.print();
				queryMove();
				return false;
			case "success": // you made a valid move
				System.out.println("Valid column picked!");
				board.add(1, lastMove);
				board.print();
				return false;
			case "forfeit":
				System.out.println("Your opponent forfeited the game");
				return false;
			case "win":
				System.out.println("You won!");
				board.print();
				return true;
			case "lose":
				System.out.println("Game over!");
				board.print();
				return true;
			default: // opponent made a move with column col
				int col = Integer.parseInt(response);
				System.out.println("Your opponent picked column " + col);
				board.add(2, col);
				board.print();
				return false;
		}
	}
	public void queryMove() throws IOException {
		String input = "";
		int col = 0; // no column picked
		while (col < 1 || col > 7) {
		    System.out.println("Pick your move by typing a valid column from 1 to 7.");
		    System.out.println("To forfeit, type \"forfeit\".");
		    input = scanner.nextLine().toLowerCase();
		    //System.out.println("input: "+input);
		    isQuit(input);
		    //System.out.println(input+" equals forfeit is "+ input.equals("forfeit"));
		    if (input.equals("forfeit")) {
		    	//System.out.println("FORFEIT RAN!");
		    	socketOutput.println("forfeit");
		    }
		    try {
		        col = Integer.parseInt(input);
		    } catch (NumberFormatException e) {
		    	System.out.println("Error: Not a game command or invalid column.");
		        queryMove();
		    }
		    if (col < 1 || col > 7) {
		    	System.out.println("Error: Your input must range from 1 to 7.");
		    }
		}
		lastMove = col;
		socketOutput.println(input);
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
	public static String readInput(BufferedReader br) {
		String input = "";
		//System.out.println(br.toString());
		try {
			while(true) {
				//System.out.println("Eeee");
				input = br.readLine().trim();
				if((input!=null) && !input.isEmpty()) {
					//System.out.println("read!");
					return input;
				}
			}
		}
		catch(Exception e) {
			System.out.println("read exception");
			e.printStackTrace();
			return null;
		}	
	}
}
