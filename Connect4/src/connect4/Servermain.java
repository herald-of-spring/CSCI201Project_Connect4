package connect4;
import java.sql.*;
import java.util.concurrent.*;
import java.net.*;
import java.io.*;



public class Servermain {
	
	
	private int playerCount;
	//list of all active players, pull from that list
	private CopyOnWriteArrayList<Player> players;
	//list of all usernames to check for uniqueness
	private CopyOnWriteArraySet<String> users;
	
	//where we store boards, call player function assign & pass board to there
	
	
	public static void main(String[] args) {
		try{
			ServerSocket ss = new ServerSocket(10000);
			ss.accept();
		}
		catch(IOException i) {
			
		}
	}
	
	/* [INCOMPLETE] Create GUEST player object & add to database & player list
	 * Increment player count.
	 * If DB error, return false.
	 */
	public boolean createGuest() {
		return false;
	}
	
	
	/* [INCOMPLETE] Create player object & add to database & player list.
	 * Increment player count.
	 * Verify that username is unique. If not, return false.
	 * If DB error, return false.
	 */
	public boolean createPlayer(String user, String pass) {
		
		
		//SQL
		
		String dbUrl = "jdbc:mysql://localhost:3306/connectfour";
		String dbUser = "INSERT_YOUR_USER";
		String pwd = "INSERT_YOUR_PASS";
	
		try {
			Connection conn = DriverManager.getConnection(dbUrl,dbUser,pwd);
			Statement st = conn.createStatement();
			String query = "INSERT INTO c4players VALUES(" + user + ", " + pass + "0, 0, 0, 1);";
			ResultSet result = st.executeQuery(query);
		}
		catch(SQLException s) {
			
			
			return false;
		}
		
		
		
		
		
		
		return false;
		
	}
	
	/* [INCOMPLETE] Called by a player seeking to play game.
	 * Search for player & check if they're available for a game.
	 * If they're available, return player2 object.
	 * If not available, return null
	 */
	public static Player findPlayer(String p1, String p2) {
		
		
		
		return null;
	}
}
