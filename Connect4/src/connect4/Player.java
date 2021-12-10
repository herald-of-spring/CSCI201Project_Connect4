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
	private boolean quitFlag;
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
		quitFlag = false;
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
	
	//tells Clientmain to shutdown
	private void shutdown() throws IOException {
		input.close();
		output.close();
		socket.close();
	}
	
	//only 1 player can invite at a time
	private synchronized boolean invite(String user) throws IOException {
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
			shutdown();
			quitFlag = true;
			return false;
		}
		return false;
	}
	
	private Integer insert(Integer col) {
		return board.add(playerNum, col);
	}
	
	//returns false when quit/forfeit before taking turn, otherwise true
	private boolean takeTurn() throws IOException {
		String col;
		Integer valid;
		System.out.println(username + " taking turn");
		do {
			write("move");
			System.out.println(username+" move sent");
			col = Servermain.readInput(input);
			
			if (col.equals("forfeit")) {
				System.out.println(username+" PLAYER FORFEIT RAN!");
				opponent.relay("forfeit");
				return false;
			}
			else if (col.equals("quit")) {
				opponent.relay("forfeit");
				quitFlag = true;
				return false;
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
		System.out.println("player running!");
		while (true) {    //or clicks quit
			if (quitFlag == true) {    //only for quitting directly from find
				break;
			}
			try {
				if (!inviteFlag) {    //if invited skip straight to game side (inviter uses assign() to populate invitee's data members)
					String action = Servermain.readInput(input);    //main lobby side
					System.out.println(username+ "action = "+action);
					if (action.equals("play")) {
						inQueue = true;
						for (int i=0; i<30; ++i) {    //30 second timer
							if(opponent!=null) {
								System.out.println(username+ " playing opponent "+opponent.getUsername());
								break;
							}
							else {
								System.out.println(username + " playing NULLopponent null");
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
								//System.out.println(username+"about to break from 30 second timer opponent "+opponent.getUsername());
								break;
							}
							if (i == 29) {    //last iteration
								write("timeout");
							}
						}
						System.out.println("assigning queue to be false");
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
								quitFlag = true;
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
							write("accepted");
							write(opponent.getUsername());
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
				System.out.println(username+" broke free from 30 second timer");
				if (opponent == null) {    //cannot start game at this point, query again
					
					continue;
				}
				System.out.println("opponent: "+opponent.getUsername());
				inGame = true;    //game side
				System.out.println(username+ " In game = "+inGame+" in queue = "+inQueue);
				Integer winner = 0;
				int turnNum = 1;
				while (winner == 0) {    //take turns until winner found
					
					if ( (turnNum+playerNum) % 2 == 0) {    //playerNum 1 goes first always
						//System.out.println(username+"'s turn now!");
						boolean turnTaken = takeTurn();
						if (!turnTaken) {    //player forfeits
							opponent.write("win");
							break;
						}
					}
					else {
						try {
							//System.out.println(username+" waiting for "+opponent.getUsername() + " player num = "+playerNum);
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
					System.out.println(username+" is winner "+winner);
					++turnNum;    //player takes turn every other iteration
				}
				if (winner == playerNum) {
					write("win");
				}
				else {
					write("lose");
				}
				System.out.println("values reset");
				inGame = false;    //reset values
				inviteFlag = false;
				opponent = null;
				board = null;
				playerNum = null;
			}
			catch (SocketException se) {    //client drops connection
				try {
					if (inGame) {
						opponent.write("quit");
					}
					shutdown();
					break;
				}
				catch (IOException io) {
					continue;
				}
			}
			catch (IOException io) {    //default catchall return to lobby
				continue;
			}
		}
	}
}
