package connect4;
import java.sql.*;
import java.util.Random;
import java.util.concurrent.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.Map;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;


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
	
	private static BufferedReader br;
	
	private static PrintWriter pr;
	
	private static Connection conn;
	
	private PreparedStatement st;
	
	//where we store boards, call player function assign & pass board to there
	
	
	/* Creates the server socket that waits 
	 * for clientmain to connect & send player info.
	 */
	public static void main(String[] args) {
		try{
			players = new CopyOnWriteArrayList<Player>();
			users = new ConcurrentHashMap<String,Player>();
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(dbUrl,dbUser,pwd);
			ss = new ServerSocket(10000);
			Socket s;
			while(true) {
				s = ss.accept();
				
				/* Check if guest or registered user or login
				 * Run either createGuest or createPlayer, pass in socket
				 */ 
				br = new BufferedReader(new InputStreamReader(s.getInputStream()));
				pr = new PrintWriter(s.getOutputStream());
				
				String action = br.readLine().trim();
				if(action.equals("login")) {
					
				}
				else if(action.equals("register")) {
					
				}
				else if(action.equals("guest")) {
					
				}
				
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
	public boolean createGuest(BufferedReader br, PrintWriter pr, Socket s) {
		
		try {
			
			
			
			Random rng = new Random(0);
			String user = "Guest#" + Integer.toString(rng.nextInt(Integer.MAX_VALUE)%10000);
			
			while(users.containsKey(user)) { //if we already have this username, re-randomize
				user = "Guest#" + Integer.toString(rng.nextInt(Integer.MAX_VALUE)%10000);
			}
			
			st = conn.prepareStatement("INSERT INTO c4players VALUES(?,null,0,0,0,0)");
			st.setString(1, user);
			int success = st.executeUpdate();
			
			if(success>=1) {
				Player p = new Player(s,br,pr,user,false);
				players.add(p);
				users.put(user,p);
				playerCount++;
				pr.write("Guest created successfully!");
				pr.flush();
				return true;
			}
			else {
				pr.write("Error creating guest account.");
				pr.flush();
				return false;
			}
			
		}
		catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	/* [COMPLETE] Create player object & add to database & player list.
	 * Increment player count.
	 * Verify that username is unique. If not, return false.
	 * If DB error, return false.
	 */
	public boolean createPlayer(String user, String pass, Socket s) {
		try {
			//check for username in DB
			
			
			if(!users.containsKey(user) && user.length()<=50 && pass.length()<=64) { 
				//if DB does not contain username & user/pass is valid, add to DB 
				st = conn.prepareStatement("INSERT INTO c4players VALUES(?,?,0,0,0,1");
				st.setString(1, user);
				st.setString(2, pass);
				int success = st.executeUpdate();
				
				if(success >= 1) { //update successful
					//create player, add to list
					Player p = new Player(s,br,pr,user,true);
					//TODO: p.setUser(user) //etc etc
					players.add(p);
					users.put(user,p);
					playerCount++;
					pr.write(("Registered successfully!"));
					pr.flush();
					return true;
				}
			}
			else {
				pr.write("Error registering new user.");
				pr.flush();
				return false;
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
	
	/* [INCOMPLETE] Logs player in.
	 * 
	 */
	public boolean logPlayer(String user, String pass) {
		return false;
	}
	
	
	public String hashPasscode(String pass) {
		
		try{
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] byteHash = md.digest(pass.getBytes(StandardCharsets.UTF_8));
			
			StringBuilder hashPass = new StringBuilder(byteHash.length * 2);
			for(int i=0; i<byteHash.length; i++) {
				
			}
		}
		
		catch(Exception e) {
			e.printStackTrace();
		}
		
		
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
		
		for(Map.Entry<String,Player> entry : users.entrySet()) {
			if(!((entry.getKey()).equals(p1)) && entry.getValue().isWaiting()) {
				return entry.getValue();
			}
		}
		
		return null;
	}
	
	
	
}
