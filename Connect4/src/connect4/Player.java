package connect4;

import java.io.*;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.*;

public class Player extends Thread {
	private String username;
	private Token token;
	private boolean registered;
	private boolean inGame;
	private GameBoard board;
	private Integer playerNum;
	
	private Socket socket;
	private BufferedReader input;
	private PrintWriter output;
	
	public Player(Socket socket, String username, Token token, boolean registered) throws IOException {
		//customize player and token used
		this.socket = socket;
		this.username = username;
		this.token = token;
		this.registered = registered;
		input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		output = new ObjectOutputStream(socket.getOutputStream());    //remember to flush!
	}
	
	public boolean isPlaying() {
		return inGame;
	}
	
	public boolean isRegistered() {
		return registered;
	}
	
	public void assign(Integer playerNum, GameBoard board) {
		this.board = board;
		this.playerNum = playerNum;
	}
	
	public void write(Object o) throws IOException {
		output.writeObject(o);
		output.flush();
	}
	
	public void shutdown() throws IOException {
		write(null);
		input.close();
		output.close();
		socket.close();
	}
	
	public boolean insert(int col) {
		return board.add(playerNum, col);
	}
	
	@Override
	public void run() {
		while (true) {    //or clicks quit    //isPlaying
			
		}
	}
}
