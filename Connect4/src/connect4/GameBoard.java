package connect4;

import java.util.concurrent.locks.*;
import java.util.List;
import java.util.concurrent.*;

public class GameBoard extends Thread{
	private int size;
	private List<List<Token>> board = new CopyOnWriteArrayList<List<Token>>();
	
	private Lock boardLock = new ReentrantLock();
	private Condition playerTurn = boardLock.newCondition();
	
	public GameBoard() {
		//customize board
	}
	
	public void run() {
		
	}
	
}
