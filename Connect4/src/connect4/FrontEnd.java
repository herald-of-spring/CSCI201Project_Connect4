package connect4;
import java.awt.*;

/*
 * This class will hadle all of the functionality
 * for front end rendering
 */

/*
 * The following tutorial was followed along with for
 * this implementation: 
 * Name: Java tic tac toe game
 * Author: Bro Code
 * Link: https://www.youtube.com/watch?v=rA7tfvpkw0I
 * */
public class FrontEnd {
	JFrame frame = new JFrame();
	JPanel player_panel = new JPanel();
	JPanel gameboard_panel = new Jpanel();
	JLabel textfield = new JLabel();
	JButton[] buttons = new JButton[];
	
	FrontEnd()
	{
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 800);
		frame.getContentPane().setBackground(new Colors(50, 50, 50));
		frame.setLayout(new BorderLayout());
		frame.setVisible(true);
		
		textfield.setBackground(new Color(25,25,25));
		texxtfield.setForeground(new Color(25, 255, 0));
		textfield.setFont(new Font("Ink Free", Font.BOLD, 75));
		textfield.setHorizontalAlignment(JLabel.Center);
		textfield.setText("Ya'll f'in with a real one";)
	}
	
	public static void main(String[] args)
	{
		
	}
}
