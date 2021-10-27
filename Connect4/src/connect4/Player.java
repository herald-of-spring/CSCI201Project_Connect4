package connect4;

import java.util.concurrent.locks.*;
import java.util.concurrent.*;

public class Player extends Thread {
	private String username;
	private Token token;
	
	private Lock playerLock = new ReentrantLock();
	private Condition timeLimit = playerLock.newCondition();
	
	public Player() {
		//customize player and token used
	}
	
	public void run() {
		
	}
}
