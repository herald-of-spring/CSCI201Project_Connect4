package connect4;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/*
 * This class will handle all of the functionality
 * for front end rendering
 */

/*
 * The following tutorial was followed along with for
 * this implementation: 
 * Name: Java tic tac toe game
 * Author: Bro Code
 * Link: https://www.youtube.com/watch?v=rA7tfvpkw0I
 * */
public class FrontEnd implements ActionListener {
	JFrame frame = new JFrame();
	JPanel player_panel = new JPanel();
	JPanel gameboard_panel = new JPanel();
	JLabel textfield = new JLabel();
	
	/*
	 * [TO-DO] Change the value mapping for the buttons
	 * later. Need a getter in board class to get height and
	 * with value of board so this value can be calculated
	 * */
	JButton[] buttons;
	
	FrontEnd(int boardHeight, int boardWidth)
	{
		buttons = new JButton[boardHeight * boardWidth];
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 800);
		frame.getContentPane().setBackground(new Color(50, 50, 50));
		frame.setLayout(new BorderLayout());
		frame.setVisible(true);
		
		//setting up all data for text
		textfield.setBackground(new Color(25,25,25));
		textfield.setForeground(new Color(25, 255, 0));
		textfield.setFont(new Font("Times New Roman", Font.BOLD, 60));
		textfield.setHorizontalAlignment(JLabel.CENTER);
		textfield.setText("Player 1's Turn");
		textfield.setOpaque(true);
		
		//actually printing text using data we set earlier
		player_panel.setLayout(new BorderLayout());
		player_panel.setBounds(0,0,800,100);
		
		//Setting up panel for buttons to exist on
		gameboard_panel.setLayout(new GridLayout(boardHeight, boardWidth));
		gameboard_panel.setBackground(new Color(150,150,150));
		
		//Set up panel for buttons representing game board
		for(int i = 0; i < buttons.length; i++)
		{
			buttons[i] = new JButton();
			gameboard_panel.add(buttons[i]);
			buttons[i].setFont(new Font("Times New Roman", Font.BOLD, 120));
			buttons[i].setFocusable(false);
			buttons[i].addActionListener(this);
		}
		
		/*this text will display the current 
		player's turn and stick to the top of the frame*/
		player_panel.add(textfield);
		frame.add(player_panel, BorderLayout.NORTH);
		frame.add(gameboard_panel);
	}
	
	public static void main(String[] args)
	{
		FrontEnd fe = new FrontEnd(6, 5);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
}
