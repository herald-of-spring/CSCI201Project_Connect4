package connect4;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class Player extends Thread {
	private String username;
	private boolean registered;
	private boolean inGame;
	private boolean inQueue;
	private boolean inviteFlag;
	private Board board;
	private Integer playerNum;
	private Player opponent;
	
	private Socket socket;
	private BufferedReader input;
	private PrintWriter output;
	
	private Lock playerLock = new ReentrantLock();
	private Condition turnCond = playerLock.newCondition();
	
	public Player(Socket socket, String username, boolean registered) throws IOException {
		//customize player and token used
		this.socket = socket;
		this.username = username;
		this.registered = registered;
		input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		output = new PrintWriter(socket.getOutputStream());    //remember to flush!
		this.inGame = false;
		this.inQueue = false;
	}
	
	public String getUsername() {
		return username;
	}
	
	//for use in servermain find()
	public boolean isPlaying() {
		return inGame;
	}
	
	//for finding a random opponent
	public boolean isWaiting() {
		return inQueue;
	}
	
	public boolean isRegistered() {
		return registered;
	}
	
	//instantiates board, player priority, and links 2 players together
	public void assign(Integer playerNum, Board board, Player opponent) {
		this.board = board;
		this.playerNum = playerNum;
		this.opponent = opponent;
	}
	
	//messages Clientmain
	public void write(String o) throws IOException {
		output.write(o);
		output.flush();
	}
	
	//tells Clientmain to shutdown
	public void shutdown() throws IOException {
		write(null);
		input.close();
		output.close();
		socket.close();
	}
	
	//only 1 player can invite at a time
	public synchronized boolean invite(String user) throws IOException {
		if (inGame) {
			return false;
		}
		write("invite");
		write(user);
		String response = input.readLine().trim();
		if (response == "accept") {
			inviteFlag = true;
			return true;
		}
		return false;
	}
	
	private Integer insert(Integer col) {
		return board.add(playerNum, col);
	}
	
	//returns false when quit/forfeit before taking turn, otherwise true
	private boolean takeTurn() throws IOException {
		write("move");
		String col;
		Integer valid;
		do {
			col = input.readLine().trim();
			if (col.equals("quit")) {
				opponent.relay("quit");
				return false;
			}
			valid = insert(Integer.parseInt(col));
		} while (valid == 0);
		opponent.relay(col);	
		return true;
	}
	
	//relays message with signaling
	public void relay(String message) throws IOException {
		playerLock.lock();
		turnCond.signal();
		write(message);
		playerLock.unlock();
	}
	
	@Override
	public void run() {    //remember to reset inGame, inQueue, inviteFlag to false and opponent, board to null!
		while (true) {    //or clicks quit
			try {
				if (inviteFlag) {
					String action = input.readLine().trim();    //main lobby side
					if (action.equals("play")) {
						inQueue = true;
						for (int i=0; i<30; ++i) {    //30 second timer
							opponent = Servermain.randomPlayer(username);
							if (opponent == null) {
								try {
									Thread.sleep(1000);    //no one else in queue yet
								}
								catch (InterruptedException ie) {    //match found while sleeping
									break;
								}
							}
							else {
								write("match");
								write(opponent.getUsername());
								opponent.write("match");
								opponent.write(getUsername());
								board = new Board(7, 6, 4);
								playerNum = 1;
								opponent.assign(2, board, this);
								break;
							}
							if (i == 29) {    //last iteration
								write("timeout");
							}
						}
						inQueue = false;
					}
					else if (action.equals("find")) {
						if (!registered) {
							write("invalid");
							continue;
						}
						do {
							String user = input.readLine().trim();
							if (user.equals("back")) {
								break;
							}
							opponent = Servermain.findPlayer(user);
							if (opponent == null) {
								write("invalid");
							}
							else if (opponent.invite(username) == false) {
								write("denied");
								opponent = null;
							}
						} while (opponent == null);
						if (opponent != null) {
							board = new Board(7, 6, 4);
							playerNum = 1;
							opponent.assign(2, board, this);
						}
					}
					else if (action.equals("quit")) {
						shutdown();
						break;
					}
				}
				if (opponent == null) {    //cannot start game at this point, query again
					continue;
				}
				inGame = true;    //game side
				Integer winner = 0;
				int turnNum = 1;
				while (winner == 0) {    //take turns until winner found
					if (turnNum+playerNum % 2 == 0) {    //playerNum 1 goes first always
						if (!takeTurn()) {    //player forfeits
							opponent.write("win");
							break;
						}
					}
					else {
						try {
							playerLock.lock();
							turnCond.await();    //waits for opponent to take turn (no time limit)
							playerLock.unlock();
						}
						catch (InterruptedException ie) {    //only happens when player forfeits on opponent's turn
							opponent.write("win");
							break;
						}
					}
					winner = board.checkWinner();
					++turnNum;
				}
				if (winner == playerNum) {
					write("win");
				}
				else {
					write("lose");
				}
				inGame = false;
				inviteFlag = false;
				opponent = null;
				board = null;
				playerNum = null;
			}
			catch (SocketException se) {    //client drops connection
				if (inviteFlag) {
					continue;
				}
				else {
					if (inGame) {
						opponent.write("quit");
					}
					input.close();
					output.close();
					socket.close();
					break;
				}
			}
		}
	}
}
