package FlightSimulator;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/*Name: 		Duffy Anderson
	Assignment: All Pairs Shortest Paths
	Course: 	CS 371
	Semester: 	Fall 2022
	Date: 		11/7/22
	Description:	
		In this assignment you will write a Java program that allows a user to search 
		for flights based on certain criteria. To do this you should implement the 
		Floyd-Warshall dynamic programming algorithm for the All-Pairs Shortest Paths 
		graph problem.
	Sources consulted: 
		- https://stackoverflow.com/questions/19872881/how-to-convert-scientific-notation
		into-normal-double-expression
		- https://www.geeksforgeeks.org/the-suppresswarnings-annotation-in-java/#:~:text=
		Use%20of%20%40SuppressWarnings%20is%20to,warnings%20coming%20from%20that%20class.
		- https://stackoverflow.com/questions/22506331/simple-dropdown-menu-in-java
		- https://stackoverflow.com/questions/718554/how-to-convert-an-arraylist-containing
		integers-to-primitive-int-array
		- https://alvinalexander.com/java/java-set-jframe-size/
		- https://www.geeksforgeeks.org/convert-an-arraylist-of-string-to-a-string-array-in-java/
	Known Bugs: The GUI doesn't show the comboboxes till the size is changed, s just grab 
	an edge of the box and drag :)
	Creativity: Wacky little broken GUI
*/
public class FlightSimulator {
	//Global Arrays
	static ArrayList<String> locations = new ArrayList<>();
	static double[][] costMatrix = null;
	static double[][] distanceMatrix = null;
	static double[][] timeMatrix = null;
	static int[][] pMCost = null;
	static int[][] pMDistance = null;
	static int[][] pMTime= null;
	static JComboBox<String> myDropDown;
	static JComboBox<String> myDD;
	static JComboBox<String> myChoiceDD;
	static String place1 = "";
	static String place2 = "";
	static String mychoice = "";
	static JLabel myLabel;
	
	public static void main(String[]args) throws FileNotFoundException {
		//scanFile, scans in the file, filling the matrices and arrayList
		scanFile("Flights.txt");
		
		//run the floydWarshaw algorithm on our data
		floydWarshaw(costMatrix, pMCost);
		floydWarshaw(distanceMatrix, pMDistance);
		floydWarshaw(timeMatrix, pMTime);
		
		//Gui Stuff, plus from there everything else is run
		guiStuff();
	}
	
	/**
	 * description: method in order to create a user interface. creating a frame and 
	 * three panels containing three comboboxes(dropdowns) and label
	 * @param none 
	 * @return none
	 * */
	public static void guiStuff() {
		JFrame myFrame = new JFrame("Welcome");
		myFrame.setVisible(true);
	    myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    myFrame.setPreferredSize(new Dimension(600,600));
	    myFrame.pack();
		
		JPanel myPanel = new JPanel();
		myPanel.setVisible(true);
		myPanel.setPreferredSize(new Dimension(450,200));
		myFrame.add(BorderLayout.NORTH, myPanel);
		
		JPanel myPanel2 = new JPanel();
		myPanel2.setVisible(true);
		myPanel2.setPreferredSize(new Dimension(250,200));
		myFrame.add(BorderLayout.CENTER, myPanel2);
		
		JPanel myPanel3 = new JPanel();
		myPanel3.setVisible(true);
		myPanel3.setPreferredSize(new Dimension(250,200));
		myFrame.add(BorderLayout.SOUTH, myPanel3);
		
		myLabel = new JLabel();
		myLabel.setText("This is the interactive flight simulator. Please select your flights");
		myLabel.setVisible(true);
		myLabel.setPreferredSize(new Dimension(450,150));
		myPanel.add(BorderLayout.NORTH, myLabel);
		
		//setting up out comboBox starting with 
		String[] locationsArr = (String[])locations.toArray(new String[locations.size()]);
		String[] sarr = {"cost", "distance", "time"};
		
		//JComboBox stuff
		myDropDown = new JComboBox<String>(locationsArr);
		myDD = new JComboBox<String>(locationsArr);
		myChoiceDD = new JComboBox<String>(sarr);
		myDropDown.setVisible(true);
		myDD.setVisible(true);
		myChoiceDD.setVisible(true);
		myDropDown.setEditable(false);
		myDD.setEditable(false);
		myChoiceDD.setEditable(false);
		myPanel2.add(BorderLayout.SOUTH, myDropDown);
		myPanel3.add(BorderLayout.NORTH, myDD);
		myPanel3.add(BorderLayout.SOUTH, myChoiceDD); 
		
		//from each of the following actionListeners we can see a print,
		//and a set text so the user sees that there actions are registering
		myDropDown.addActionListener(new ActionListener() {
			@SuppressWarnings("unchecked")
			public void actionPerformed(ActionEvent event) {
		        JComboBox<String> combo = (JComboBox<String>) event.getSource();
		        String place = (String) combo.getSelectedItem();
	        	place1 = place;
	        	System.out.println("place1 = " + place1);   
	        	myLabel.setText("place1 = " + place1 +"place2 = " + place2);
			}
		});
				
		myDD.addActionListener(new ActionListener() {
			@SuppressWarnings("unchecked")
			public void actionPerformed(ActionEvent event) {
		        JComboBox<String> combo = (JComboBox<String>) event.getSource();
		        String place = (String) combo.getSelectedItem();
		        place2 = place;
		        System.out.println("place2 = " + place2); 
		        myLabel.setText("place1 = " + place1 +"place2 = " + place2);
			}
		});
		
		//in this action listener the user has answered all available questions 
		//findPath is called in order to show the user the answer to the question
		myChoiceDD.addActionListener(new ActionListener() {
			@SuppressWarnings("unchecked")
			public void actionPerformed(ActionEvent event) {
		        JComboBox<String> combo = (JComboBox<String>) event.getSource();
		        String choice = (String) combo.getSelectedItem();
		        mychoice = choice;
		        System.out.println("choice = " + mychoice); 
		        myLabel.setText(place1 +" to " + place2 + "minimizing" + choice);
		        findPath();
			}
		});
	}
	
//------------mandatory printy stuff ------------------------------------------------------
	/**
	 * Description: This method utilizes the mychoice value and assesses which 
	 *    array to run the create the createPath value on. Finally it takes that output of
	 *    that method give and calls printPath
	 * */
	public static void findPath() {
		double[][] arr = new double[locations.size()][locations.size()];
		ArrayList<Integer> path = new ArrayList<>();
		if(mychoice.equals("distance")) {
			arr = distanceMatrix;
			path = createPath(distanceMatrix, pMDistance);
		} else if(mychoice.equals("time")) {
			arr = timeMatrix;
			path = createPath(timeMatrix, pMTime);
		} else {//cost
			arr = costMatrix;
			path = createPath(costMatrix, pMCost);
		}
		printPath(path, arr, mychoice);
	}
	
	/**
	 * Description: takes in the matrix specified by the user chosen trait and finds the 
	 *     ideal flight path
	 * @param myArr is the array that store the value of the minimization of each of the flights 
	 *     for a specified characteristic
	 * @param parentArr is the 2d int array that holds the value of each locations "parent" row
	 * */
	public static ArrayList<Integer> createPath(double[][] myArr, int[][] parentArr) {
		int one = locations.indexOf(place1);
		int two = locations.indexOf(place2);
		ArrayList<Integer> cityPath = new ArrayList<>();
		if(parentArr[one][two] != -1) {
			while(one != two && !cityPath.contains(two)) {
				cityPath.add(0, two);
				two = parentArr[one][two];
				//System.out.println(two);
			}
			cityPath.add(0, one);
		}
		//System.out.println(cityPath.toString());
		return cityPath;
	}
	
	/**
	 * Description: prints out the locations on a flight path and its minimization by characteristic
	 * @param arrList is a list that stores all the locations on the flight path
	 * @param arr is the specified matrix for the trait the user is looking for 
	 * @param str is the name of the specified trait being searched for
	 * */
	public static void printPath(ArrayList<Integer> arrList, double[][] arr, String str) {
		String tot = "";
		System.out.print("\nYour minimized " + str + " is:");
		
		System.out.println(arr[locations.indexOf(place1)][locations.indexOf(place2)]);
		tot +=("<html>Your minimized " + str + " is:" + (arr[locations.indexOf(place1)][locations.indexOf(place2)]));
		System.out.println("Your flight path will appear as such");
		tot += ("<br/>Your flight path will appear as such<br/>");
		for(Integer a: arrList){
			System.out.print(locations.get(a) + " -> ");
			tot +=(locations.get(a) + " -> ");
		}
		System.out.println("Greatness!");
		tot += ("Greatness!<html>");
		myLabel.setText(tot);
		
	}
//_____________________floydWarshaw Algorithm__________________________________________	
	
	/**
	 * Description: updates the matrices, finding the shortest path. 
	 * @param arr is the two dimensional array/matrix used to hold the shortest path 
	 * @param arrP is a two dimensional array/matrix holding the parents of each 
	 * 		shortest path
	 * */
	public static void floydWarshaw(double[][] arr, int[][] arrP) {
		int loc = locations.size();
		for(int i=0; i<loc; i++) {
			for(int j=0; j<loc; j++) {
				if(arr[i][j] != Integer.MAX_VALUE) {
					arrP[i][j] = i;
				} else {
					arrP[i][j] = -1;
				}
			}
		}
		
		for(int x=0; x<loc; x++) {
			for(int mid=0; mid<loc; mid++) {
				for(int y=0; y<loc; y++){
					if(arr[x][y] > arr[x][mid] + arr[mid][y]) {
						arr[x][y] = arr[x][mid] + arr[mid][y];
						arrP[mid][y] = arrP[x][y];
					}//end of if
					if(x==y) {
						arr[x][y] = 0;
					}
				}//end of y
			}//end of mid
		}//end of x
		
		printArrP(arrP);
	}
	
	/**
	 * Description: is used to print a two dimensional array of type double
	 * @param arr is a twoD double array that is passed and printed
	 * */
	public static void printArr(double[][] arr) {
		for(double[] i : arr) {
			for(double j: i) {
				System.out.print(j + ", ");
			}
			System.out.println();
		}
	}
	
	/**
	 * Description: is used to print a two dimensional array of type int
	 * @param arr is a twoD int array that is passed and printed
	 * */
	public static void printArrP(int[][] arr) {
		for(int[] i : arr) {
			for(int j: i) {
				System.out.print(j + ", ");
			}
			System.out.println();
		}
	}
//_________________________File Scan Portion________________________________________	
	
	/**
	 * Description:Will work as a mini main that is primarily for calling other methods
	 * and initializing the scanner
	 * @param fileName is the name of the file which is set in main
	 * @throws FileNotFoundException 
	 * */
	public static void scanFile(String fileName) throws FileNotFoundException {
		File myFile = new File(fileName);
		Scanner scan = new Scanner(myFile);
		locations = getLocations(scan);
		getMatrices(scan);
	}//end of scan file
	
	/**
	 * Description: Scans the file for the first set of information, the nodes, and
	 * 	 places them in an arrayList<String>
	 * @param scan is a variable with the ability to pull in the information from the 
	 *   file the scanner was created with 
	 * @return locations2 the arrayList<String> containing each of the nodes, that are 
	 * 	 locations.
	 * */
	public static ArrayList<String> getLocations(Scanner scan) {
		ArrayList<String> locations2 = new ArrayList<>();
		while(scan.hasNext()) {
			String line = scan.nextLine();
			if(line.equals("EDGES")) {
				break;
			} else if(line.indexOf("NODES") == -1) {
				locations2.add(line);
			}//end of if
		}//end of while
		return locations2;
	}//end of getLocations
	
	/**
	 * Description: initializes the values of the 6 major arrays that will
	 * be used later in the floyd-warshaw algorithm
	 * @param scan, the scanner that was created in the scan file method
	 * */
	public static void getMatrices(Scanner scan) {
		int v = locations.size();
		costMatrix = new double[v][v];
		timeMatrix = new double[v][v];
		distanceMatrix = new double[v][v];
		pMDistance = new int[v][v];
		pMTime = new int[v][v];
		pMCost = new int[v][v];
		
		for(int i = 0; i < v; i++) {
			for(int j = 0; j < v; j++) {
				costMatrix[i][j] = Double.MAX_VALUE;
				timeMatrix[i][j] = Double.MAX_VALUE;
				distanceMatrix[i][j] =  Double.MAX_VALUE;
			}
		}
		
		while(scan.hasNextLine()) {
			
			//capture the first line, put it in its own scanner, to allow for sub 
			//partitioning without damage to the original scanner or read in file. 
			String line = scan.nextLine();
			Scanner lineScanner = new Scanner(line);
			
			//set delimiter to " seperate out the locations
			lineScanner.useDelimiter("\" ");
			
			//create the row and column variables to store the to locations
			//for each [x][y] in our array.
			String row = lineScanner.next();
			String column = lineScanner.next();
			row = row.substring(row.indexOf('"')+1);
			column = column.substring(column.indexOf('"')+1);
			
			//update the delimiter in order to break up distance,time,and cost
			lineScanner.useDelimiter(":");
			lineScanner.next();
			String distanceS = lineScanner.next();
			String timeS = lineScanner.next();
			String costS = lineScanner.next();
			distanceS = distanceS.substring(0, distanceS.indexOf(' '));
			timeS = timeS.substring(0, timeS.indexOf(' '));	
			
			double cost = 0,time = 0,distance = 0;
			distance = Double.parseDouble(distanceS);
			cost = Double.parseDouble(costS);
			time = Double.parseDouble(timeS);
		
			//convert row/column to indexes
			int r = locations.indexOf(row);
			int c = locations.indexOf(column);
			
			//build matrices
			costMatrix[r][c] = cost;
			timeMatrix[r][c] = time;
			distanceMatrix[r][c] = distance;
			
			pMDistance[r][c] = r;
			pMTime[r][c] = r;
			pMCost[r][c] = r;
			
		}//end of while has next line	
		
		//set diagonals 0
		for(int i=0; i<v; i++) {
			costMatrix[i][i] = 0;
			distanceMatrix[i][i] = 0;
			timeMatrix[i][i] = 0;
			pMCost[i][i] = 0;
			pMDistance[i][i] = 0;
			pMTime[i][i]= 0;
		}//end of diagonal zeros
	}//end of getMatrices
//_____________________________________end of file scan portion______________________________________________
}//end of class
