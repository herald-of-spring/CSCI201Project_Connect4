package connect4;

import java.io.*;
import java.net.*;
import java.util.concurrent.locks.*;

public class Player extends Thread {
	private String username;
	private boolean registered;
	private boolean inGame;
	private boolean inQueue;
	private boolean inviteFlag;
	private boolean endFlag;
	private Board board;
	private Integer playerNum;
	private Player opponent;
	
	private Socket socket;
	private BufferedReader input;
	private PrintWriter output;
	
	private Lock playerLock = new ReentrantLock();
	private Condition turnCond = playerLock.newCondition();
	
	public Player(Socket socket, BufferedReader input, PrintWriter output, String username, boolean registered) throws IOException {
		//customize player
		this.socket = socket;
		this.username = username;
		this.registered = registered;
		this.input = input;
		this.output = output;    //remember to flush!
		this.inGame = false;
		this.inQueue = false;
		inviteFlag = false;
		endFlag = false;
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
	private void assign(Integer playerNum, Board board, Player opponent) {
		this.board = board;
		this.playerNum = playerNum;
		this.opponent = opponent;
	}
	
	//messages Clientmain
	private void write(String s) throws IOException {
		output.println(s);
		output.flush();
	}
	
	//only 1 player can invite at a time
	private synchronized boolean invite(String user) throws SocketException, IOException {
		if (inGame) {
			return false;
		}
		else if (inQueue) {
			inQueue = false;
		}
		write("invite");
		write(user);
		String response = Servermain.readInput(input);
		if (response.equals("accept")) {
			inviteFlag = true;
			return true;
		}
		else if (response.equals("quit")) {
			throw new SocketException("");
		}
		return false;
	}
	
	private Integer insert(Integer col) {
		return board.add(playerNum, col);
	}
	
	//returns false when quit/forfeit before taking turn, otherwise true
	private boolean takeTurn() throws SocketException, IOException {
		String col;
		Integer valid;
		do {
			write("move");
			col = Servermain.readInput(input);
			
			if (col.equals("forfeit")) {
				opponent.relay("forfeit");
				endFlag = true;
				return false;
			}
			else if (col.equals("quit")) {
				opponent.relay("forfeit");
				endFlag = true;
				throw new SocketException("");
			}
			valid = insert(Integer.parseInt(col));
		} while (valid == 0);
		board.print();
		write("success");
		opponent.relay(col);
		return true;
	}
	
	//relays message with signaling
	private void relay(String message) throws IOException {
		playerLock.lock();
		turnCond.signal();
		write(message);
		playerLock.unlock();
	}
	
	//quality assurance by Clientmain
	//Lobby options: play find quit
	/*user can give 'back' command anytime to go back to lobby*/
		//play response: (match [opponentname]) (timeout)
			//(match [opponentname]) enters game state (see below)
			//(timeout) returns to lobby
		//find response: (unregistered) (invalid) (denied) (accepted [opponentname])
			//(accepted [opponentname]) enters game state
			//all others return to lobby
		//quit has no response, just shuts down
	
	//Game responses: (move) (win) (lose) ([number])
	/*user can give 'quit' command anytime to forfeit*/
		//(move) expects a column number reply
			//relays the column number to opponent
		//([number]) is the column the opponent chose as their move
		//all others return to lobby
	
	//If player gets invited via (invite [name]) command: expects accept reply
	/*user can give 'quit' command anytime to exit program*/
		//any other reply is assumed denial
	
	@Override
	public void run() {    //remember to reset inGame, inQueue, inviteFlag to false and opponent, board to null!
		while (true) {    //or clicks quit
			try {
				if (!inviteFlag) {    //if invited skip straight to game side (inviter uses assign() to populate invitee's data members)
					write("no");    //no invites
					String action = Servermain.readInput(input);    //main lobby side
					if (action.equals("play")) {
						inQueue = true;
						for (int i=0; i<300; ++i) {    //30 second timer
							if(opponent!=null) {
								break;
							}
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
							if (i == 299) {    //last iteration
								write("timeout");
							}
						}
						inQueue = false;
					}
					else if (action.equals("find")) {
						if (!registered) {
							write("unregistered");
							continue;
						}
						do {
							String user = Servermain.readInput(input);
							if (user.equals("back")) {    //clicks back to main lobby
								break;
							}
							else if (user.equals("quit")) {
								throw new SocketException("");
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
							write("accepted");
							//write(opponent.getUsername());
							board = new Board(7, 6, 4);
							playerNum = 1;
							opponent.assign(2, board, this);
						}
					}
					else if (action.equals("quit")) {
						throw new SocketException("");
					}
				}
				if (opponent == null) {    //cannot start game at this point, query again
					continue;
				}
				inGame = true;    //game side
				Integer winner = 0;
				int turnNum = 1;
				while (winner == 0 && endFlag == false) {    //take turns until winner found
					if ((turnNum+playerNum) % 2 == 0) {    //playerNum 1 goes first always
						boolean turnTaken = takeTurn();
						if (!turnTaken) {    //player forfeits
							opponent.write("win");
							write("lose");
							opponent.endFlag = true;
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
							write("lose");
							opponent.endFlag = true;
							break;
						}
					}
					winner = board.checkWinner();
					++turnNum;    //player takes turn every other iteration
				}
				if (endFlag == true) {
					if (winner == playerNum) {
						write("win");
					}
					else {
						write("lose");
					}
				}
				inGame = false;    //reset values
				inviteFlag = false;
				opponent = null;
				board = null;
				playerNum = null;
				endFlag = false;
			}
			catch (SocketException se) {    //client drops connection
				try {
					input.close();
					output.close();
					socket.close();
					if (inGame) {
						opponent.write("quit");
					}
					System.out.println("Client " + username + " dropped connection.");
					break;
				}
				catch (IOException io) {
					break;
				}
			}
			catch (IOException io) {    //default catchall return to lobby
				continue;
			}
		}
	}
}
