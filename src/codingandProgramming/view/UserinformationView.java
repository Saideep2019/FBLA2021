package codingandProgramming.view;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.NumberFormatter;



import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

/**
 * This is the UI for the log in screen that initially applies, when the user first opens the application.
 * @author Saideep Ambari
 * Date: 2/1/2020
 */
public class UserinformationView extends JFrame {

	private JPanel contentPane; // These are all naming panels and textfields, that are private which means that
	public static QuizView2 currentView = null;
	private JTextField lastNameField;
	private JTextField firstNameField;
	public static String firstName;
	public static String lastName;





	

	
	
	public static void main(String[] args) {
		JWindow splash = new JWindow();
		java.net.URL imgURL = JWindow.class.getResource("/splashvts.gif");

		if (imgURL != null) {
			ImageIcon icon = new ImageIcon(imgURL);
			splash.getContentPane().add(new JLabel("", icon, SwingConstants.CENTER));// centering the splash screen
			splash.setBounds(550, 160, 400, 400);// setting the size and coordinates of the splash screen.
			splash.setVisible(true);
			try {
				Thread.sleep(9000);// duration of the splash screen
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			splash.setVisible(false);// setting the visibility of the splash screen to false, after 3000 milliseconds. 

		}

		
		
		UserinformationView frame = null;
		;
		try {
			frame = new UserinformationView();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		frame.setVisible(true);
		

	}
	
	public static UserinformationView displayFrame() {
		
		UserinformationView frame = null;
		try {
			 frame = new UserinformationView();
			 frame.setVisible(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		return frame;
		
	}
	/**
	 * Create the frame.
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public UserinformationView() throws ClassNotFoundException, IOException {// This throws out the exceptions so that it shows
																	// in the console.
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setBounds(100, 100, 578, 251);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(238, 232, 170));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		NumberFormat gradeFormat = NumberFormat.getInstance(); // This is creating a single instance of the numberformat
																// class allowing it to be used.
		gradeFormat.setMinimumIntegerDigits(1);
		gradeFormat.setMaximumIntegerDigits(2);

		NumberFormatter textFormatter = new NumberFormatter(gradeFormat);

		lastNameField = new JTextField();
		lastNameField.setBounds(369, 43, 117, 22);
		contentPane.add(lastNameField);
		lastNameField.setColumns(10);

		firstNameField = new JTextField();
		firstNameField.setBounds(77, 43, 116, 22);
		contentPane.add(firstNameField);
		firstNameField.setColumns(10);

		JLabel categorylabel = new JLabel("FirstName:");
		categorylabel.setBounds(7, 46, 77, 17);
		contentPane.add(categorylabel);

		JButton enterButton = new JButton("Enter");
		enterButton.setAction(new LoginAction());
		enterButton.setBounds(191, 119, 169, 25);
		contentPane.add(enterButton);

		JMenuBar menuBar_1 = new JMenuBar();
		menuBar_1.setBounds(622, 76, 119, 26);
		contentPane.add(menuBar_1);
		
		JLabel lblPassword = new JLabel("LastName:");
		lblPassword.setBounds(295, 49, 108, 11);
		contentPane.add(lblPassword);

		this.setTitle("Login View");
	


	}
	

	/**
	 * This method will be executed when the log in button is clicked, and the method wil check if the password that the user has entered
	 * matches with what is in the Database.
	 * @author Saideep Ambari
	 *
	 */
	private class LoginAction extends AbstractAction {
		public LoginAction() {
			putValue(NAME, "Enter User Information");
			putValue(SHORT_DESCRIPTION, "Press to Enter");
		}

		public void actionPerformed(ActionEvent e) {
				QuizView2 frame = null;
			
				dispose();//close the login view		
				try {
					frame = new QuizView2();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		
			
			
			firstName = firstNameField.getText();
			lastName = lastNameField.getText();
		}
		
	
	
}
}

