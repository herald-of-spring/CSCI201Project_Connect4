package connect4;
import java.sql.*;
import java.util.Random;
import java.util.concurrent.*;
import java.net.*;
import java.io.*;


/* Accesses DB
 * Holds list of player objects
 */
public class Servermain {
	
	
	private static int playerCount;
	//list of all active players, pull from that list
	private static CopyOnWriteArrayList<Player> players;
	//list of all usernames to check for uniqueness
	private static ConcurrentHashMap<String,Player> users;
	
	private static String dbUrl = "jdbc:mysql://localhost:3306/connectfour";
	private static String dbUser = "INSERT_YOUR_USER";
	private static String pwd = "INSERT_YOUR_PASS";
	
	private static ServerSocket ss;
	
	private OutputStream ostream;
	
	private InputStream istream;
	
	private BufferedReader br;
	
	private PrintWriter pr;
	
	private static Connection conn;
	
	private PreparedStatement st;
	
	//where we store boards, call player function assign & pass board to there
	
	public Servermain() {
		try {
			
		}
		catch(Exception e) {
			e.printStackTrace();
			
		}
	}
	
	/* Creates the server socket that waits 
	 * for clientmain to connect & send player info.
	 */
	public static void main(String[] args) {
		try{
			players = new CopyOnWriteArrayList<Player>();
			users = new ConcurrentHashMap<String,String>();
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(dbUrl,dbUser,pwd);
			ss = new ServerSocket(10000);
			Socket s;
			while(true) {
				s = ss.accept();
				/* Check if guest or registered user being created
				 * Run either createGuest or createPlayer, pass in socket
				 * 
				 */ 
			}
			
			
			
		}
		catch(Exception e) {
			e.printStackTrace();
			System.out.println("Connection error.");
		}
	}
	
	/* [INCOMPLETE] Create GUEST player object & add to database & player list
	 * Increment player count.
	 * If DB error, return false.
	 */
	public boolean createGuest(Socket s) {
		
		try {
			ostream = s.getOutputStream();
			istream = s.getInputStream();
			br = new BufferedReader(new InputStreamReader(istream));
			pr = new PrintWriter(ostream);
			
			Random rng = new Random(0);
			String user = "Guest#" + Integer.toString(rng.nextInt(Integer.MAX_VALUE)%10000);
			
			while(users.containsKey(user)) { //if we already have this username, re-randomize
				user = "Guest#" + Integer.toString(rng.nextInt(Integer.MAX_VALUE)%10000);
			}
			
			st = conn.prepareStatement("INSERT INTO c4players VALUES(?,null,0,0,0,0)");
			st.setString(1, user);
			int success = st.executeUpdate();
			
			if(success>=1) {
				Player p = new Player(s,br,pr,user,0);
				players.add(p);
				users.put(user,p);
				playerCount++;
			}
			
		}
		catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		
		
		return false;
	}
	
	
	/* [INCOMPLETE] Create player object & add to database & player list.
	 * Increment player count.
	 * Verify that username is unique. If not, return false.
	 * If DB error, return false.
	 */
	public boolean createPlayer(String user, String pass, Socket s) {
	
		try {
			//check for username in DB
			ostream = s.getOutputStream();
			istream = s.getInputStream();
			br = new BufferedReader(new InputStreamReader(istream));
			pr = new PrintWriter(ostream);
			
			if(!users.containsKey(user) && user.length()<=50 && pass.length()<=64) { 
				//if DB does not contain username & user/pass is valid, add to DB 
				st = conn.prepareStatement("INSERT INTO c4players VALUES(?,?,0,0,0,1");
				st.setString(1, user);
				st.setString(2, pass);
				int success = st.executeUpdate();
				
				if(success >= 1) { //update successful
					BufferedReader br = new BufferedReader(new InputStreamReader(istream));
					PrintWriter pr = new PrintWriter(ostream);
					//create player, add to list
					Player p = new Player(s,br,pr,user,1);
					//TODO: p.setUser(user) //etc etc
					players.add(p);
					users.put(user,p);
					playerCount++;
					ostream.write(("Registered successfully!").getBytes());
					ostream.flush();
					return true;
				}
			}
			
			else {
				
			}
			
			
//				st = conn.prepareStatement("SELECT * FROM c4players WHERE username = ?");
//				st.setString(1, user);
//				ResultSet result = st.executeQuery();
//				
//				if(result.next() || users.find(user)) { //if we get a match, invalid - 
//					//write response to socket
//					ostream.write(("Username taken - enter a unique username.").getBytes());
//					ostream.flush();
//					System.out.println("Enter a unique username.");
//					return false;
//				}
			
			
			
			
					
			
			
		}
		catch(SQLException e) {
			System.out.println("DB Error");
			e.printStackTrace();
			return false;
		}
		catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return false;
		
	}
	
	/* [COMPLETE] Called by a player seeking to play game.
	 * Search for player & check if they're available for a game.
	 * If they're available, return player2 object.
	 * If not available, return null.
	 */
	public static Player findPlayer(String p2) {
		
		Player playerTwo = users.get(p2);
		
		if(playerTwo != null && !playerTwo.isPlaying()) {
			return playerTwo;
		}

		return null;
	}
	
	/* [COMPLETE] Called by a player looking for a random
	 *  opponent, to find another player looking for a 
	 *  random opponent.
	 *  If they're looking for a random opponent, return player2 object.
	 *  If they aren't, return null.
	 */
	public static Player randomPlayer(String p1) {
		
		Random rand = new Random(36);
		int r_index = (rand.nextInt(Integer.MAX_VALUE)) % playerCount;
		Player playerTwo = players.get(r_index);
		
		if(playerTwo!=null && playerTwo.isWaiting()) {
			return playerTwo;
		}
		
		return null;
	}
	
	
	
}
