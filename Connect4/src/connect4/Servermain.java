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
	
	//list of all active players, pull from that list
	private static CopyOnWriteArrayList<Player> players;
	//list of all usernames to check for uniqueness
	private static ConcurrentHashMap<String,Player> users;
	
	private static String dbUrl = "jdbc:mysql://localhost:3306/connectfour";
	private static String dbUser = "INSERT_YOUR_USER";
	private static String pwd = "INSERT_YOUR_PASS";
	
	private static ServerSocket ss;
	
	private static Connection conn;
	
	private static PreparedStatement st;
	
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
				
				BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
				PrintWriter pr = new PrintWriter(s.getOutputStream());
				
				String action = br.readLine().trim();
				
				boolean b;
				
				if(action.equals("login")) {
					b = logPlayer(br,pr);
				}
				else if(action.equals("register")) {
					b = createPlayer(br,pr,s);
				}
				else if(action.equals("guest")) {
					b = createGuest(br,pr,s);
				}
				else if(action.equals("quit")) {
					br.close();
					pr.close();
					s.close();
				}
				
			}
			
			
			
		}
		catch(Exception e) {
			e.printStackTrace();
			System.out.println("Connection error.");
		}
	}
	
	/* [COMPLETE] Create GUEST player object & add to database & player list.
	 * Write "success" to client, return true.
	 * If DB error, writer "error" to client, return false.
	 */
	public static boolean createGuest(BufferedReader br, PrintWriter pr, Socket s) {
		
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
				pr.write("success");
				pr.flush();
				return true;
			}
			else {
				pr.write("error");
				pr.flush();
				return false;
			}
			
		}
		catch(Exception e) {
			pr.write("error");
			pr.flush();
			e.printStackTrace();
			return false;
		}
	}
	
	
	/* [COMPLETE] Create player object & add to database & player list.
	 * Increment player count.
	 * Verify that username is unique. If so, write "success" to client, return true.
	 * If not, write "error" to client, return false.
	 */
	public static boolean createPlayer(BufferedReader br, PrintWriter pr, Socket s) {
		try {
			//check for username in DB
			String user = br.readLine().trim();
			String pass = hashPasscode(br.readLine().trim());
			
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
					pr.write(("success"));
					pr.flush();
					return true;
				}
			}
			else {
				pr.write("error");
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

		catch(Exception e) {
			pr.write("error");
			e.printStackTrace();
			return false;
		}
		
		return false;
		
	}
	
	/* [INCOMPLETE] Logs player in.
	 * Checks username, hashed password in DB.
	 * If match, write "success" to client.
	 * If not, write "error" to client.
	 */
	public static boolean logPlayer(BufferedReader br, PrintWriter pr) {
		
		try{
			
			String user = br.readLine().trim();
			String pass = br.readLine().trim();
			
			st = conn.prepareStatement("SELECT username,password FROM c4players WHERE username = ?");
			st.setString(1, user);
			ResultSet results = st.executeQuery();
			
			if(results.first()) {
				String hashPass = hashPasscode(pass);
				String resultPass = results.getString("password");
				
				if(hashPass.equals(resultPass)) {
					pr.write("success");
					pr.flush();
					return true;
				}
			}
				
		}
		catch(Exception e) {
			pr.write("error");
			pr.flush();
			return false;
		}
		
		pr.write("error");
		pr.flush();
		return false;
	}
	
	/*[COMPLETE] Hashes passed-in passcode.
	 * Returns a string in hexadecimal.
	 * If error, returns null.
	 */
	public static String hashPasscode(String pass) {
		
		try{
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] byteHash = md.digest(pass.getBytes(StandardCharsets.UTF_8));
			
			StringBuilder hashPass = new StringBuilder(byteHash.length * 2);
			for(int i=0; i<byteHash.length; i++) {
				String hexa = Integer.toHexString(0xff & byteHash[i]);
				if(hexa.length() == 1) {
					hashPass.append('0');
				}
				hashPass.append(hexa);
			}
			return hashPass.toString();
		}
		
		catch(Exception e) {
			e.printStackTrace();
			return null;
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
