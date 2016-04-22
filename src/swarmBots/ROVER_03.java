package swarmBots;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import common.Coord;
import common.MapTile;
import common.ScanMap;
<<<<<<< HEAD
import communication.RoverServer;
import model.Rover;

public class ROVER_03{

	/********************************************* Rover Constants ************************************************************/

	private static final String ROVER_NAME = "ROVER_03";
	private static final int SLEEP_TIME = 10000; // The higher this number is the smaller the number of request sent to server
	private static final int WAIT_FOR_ROVERS = 20000; 


	// port and ip for swarm server we will be connecting to ... change here if necessary 
//	private static final String SERVER_ADDRESS = "localhost";
	private static final String SERVER_ADDRESS = "192.168.1.206";
	private static final int PORT_ADDRESS = 9537;

	/*************************************************************************************************************************/

	// Buffered Reader used to receive responses from server, Print Writer used to send request to server
	private BufferedReader in;
	private PrintWriter out;

	private ScanMap scanMap;

	private Rover rover;

	private Coord previousLoc;
	private Coord currentLoc;
	private String results = "";
	
	private RoverServer server;

	public ROVER_03() throws IOException, InterruptedException {
		rover = new Rover(ROVER_NAME);
		server = new RoverServer(rover);
		new Thread(server).start();
		Thread.sleep(WAIT_FOR_ROVERS);    // Make thread sleep until all rovers have connected
	}
=======
import enums.Terrain;

/*
 * This is just an idea for the moment but ROVER_03 might have to implement
 * runnable in order to allow for multithreading. The reason we might need this 
 * is because in order to communicate with the other rovers in blue corporation 
 * we will most likely need to create a P2P network connection between the rovers
 * */
public class ROVER_03  {

	// Line 16 to 32 don't need to be changed, got these from sample rovers
	BufferedReader in;
	PrintWriter out;
	String rovername;
	ScanMap scanMap;
	int sleepTime;
	String SERVER_ADDRESS = "localhost";
	static final int PORT_ADDRESS = 9537;

	public ROVER_03() {
		System.out.println("ROVER_03 rover object constructed");
		rovername = "ROVER_03";
		SERVER_ADDRESS = "localhost";
		sleepTime = 10000; // Changed sleep time to decrease number of request
							// sent to server but will have to modify this
	}

	private void start() throws IOException, InterruptedException {
>>>>>>> ea62f47f5669585bc7f969059e0e5d887bafa331

	// Starts rover
	@SuppressWarnings("resource")
	private void start() throws IOException, InterruptedException {
		// Make connection and initialize streams
		// TODO - need to close this socket
		Socket socket = new Socket(SERVER_ADDRESS, PORT_ADDRESS); // set port
																	// here
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);

		// Gson gson = new GsonBuilder().setPrettyPrinting().create();

		// Process all messages from server, wait until server requests Rover ID
		// name
		while (true) {
			String line = in.readLine();
			if (line.startsWith("SUBMITNAME")) {
<<<<<<< HEAD
				out.println(ROVER_NAME); /* This sets the name of this instance of a swarmBot for identifying the thread to the server*/
=======
				out.println(rovername); // This sets the name of this instance
										// of a swarmBot for identifying the
										// thread to the server
>>>>>>> ea62f47f5669585bc7f969059e0e5d887bafa331
				break;
			}
		}

<<<<<<< HEAD
		/**** get equipment listing including drive type ****/			
		ArrayList<String> equipment = new ArrayList<String>();
		equipment = getEquipment();
		rover.setTool(equipment.get(1));
		rover.setTool(equipment.get(2));
		rover.setDriveType(equipment.get(0));
		
		System.out.println("ROVER_03 equipment list " + equipment + "\n");
		getLocation(rover.getName() + " currentLoc at start: ");

		/******** Rover logic *********/		
		// Cardinals directions will be used when rover starts moving
		//String[] cardinals = {"N", "E", "S", "W"};

		// start Rover controller process
		while (true) {	
			getLocation(rover.getName() + " currentLoc: ");
			Thread.sleep(SLEEP_TIME); // We need to have thread sleep until signal is received from another rover ****
		}



	}

	// displays current location and map on console only if location has changed
	private void getLocation(String displayMessage) throws IOException, InterruptedException{
		// **** location call ****
		out.println("LOC");
		results = in.readLine();
		if (results == null) {
			System.out.println(rover.getName() + " check connection to server");
			results = "";
		}
		if (results.startsWith("LOC")) {
			currentLoc = extractLOC(results);
		}
		if(!currentLoc.equals(previousLoc)){
			System.out.println(displayMessage + currentLoc);

			// after getting location set previous equal current to be able to check for stuckness and blocked later
			previousLoc = currentLoc;

			// ***** do a SCAN *****
			this.doScan();
			scanMap.debugPrintMap();
			System.out.println(rover.getName() + " ------------ bottom process control --------------"); 
		}
	}

	/************************* Support Methods *****************************/

	private void clearReadLineBuffer() throws IOException{
		while(in.ready()){
			in.readLine();	
		}
	}


	// method to retrieve a list of the rover's equipment from the server
	private ArrayList<String> getEquipment() throws IOException {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		out.println("EQUIPMENT");

		String jsonEqListIn = in.readLine(); //grabs the string that was returned first
		if(jsonEqListIn == null){
			jsonEqListIn = "";
		}
		StringBuilder jsonEqList = new StringBuilder();
		if(jsonEqListIn.startsWith("EQUIPMENT")){
				while (!(jsonEqListIn = in.readLine()).equals("EQUIPMENT_END")) {
					if(jsonEqListIn == null)
						break;
					jsonEqList.append(jsonEqListIn);
					jsonEqList.append("\n");
				}

		} else {
			// in case the server call gives unexpected results
			clearReadLineBuffer();
			return null; // server response did not start with "EQUIPMENT"
		}
		String jsonEqListString = jsonEqList.toString();		
		ArrayList<String> returnList;		
		returnList = gson.fromJson(jsonEqListString, new TypeToken<ArrayList<String>>(){}.getType());		

		return returnList;
	}


	// sends a SCAN request to the server and puts the result in the scanMap array
	public void doScan() throws IOException {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		out.println("SCAN");

		String jsonScanMapIn = in.readLine(); //grabs the string that was returned first
		if(jsonScanMapIn == null){
			System.out.println(rover.getName() + " check connection to server");
			jsonScanMapIn = "";
		}
		StringBuilder jsonScanMap = new StringBuilder();
		System.out.println(rover.getName() + " incomming SCAN result - first readline: " + jsonScanMapIn);

		if(jsonScanMapIn.startsWith("SCAN")){	
			while (!(jsonScanMapIn = in.readLine()).equals("SCAN_END")) {
				jsonScanMap.append(jsonScanMapIn);
				jsonScanMap.append("\n");
=======
		// ******** Rover logic *********
		// int cnt=0;
		String line = "";

		boolean goingSouth = false;
		boolean stuck = false; // just means it did not change locations between
								// requests,
								// could be velocity limit or obstruction etc.
		boolean blocked = false;

		String[] cardinals = new String[4];
		cardinals[0] = "N";
		cardinals[1] = "E";
		cardinals[2] = "S";
		cardinals[3] = "W";

		String currentDir = cardinals[0];
		Coord currentLoc = null;
		Coord previousLoc = null;

		// start Rover controller process
		while (true) {

			// currently the requirements allow sensor calls to be made with no
			// simulated resource cost

			// **** location call ****
			out.println("LOC");
			line = in.readLine();
			if (line == null) {
				System.out.println("ROVER_00 check connection to server");
				line = "";
			}
			if (line.startsWith("LOC")) {
				// loc = line.substring(4);
				currentLoc = extractLOC(line);
			}
			System.out.println("ROVER_03 currentLoc at start: " + currentLoc);

			// after getting location set previous equal current to be able to
			// check for stuckness and blocked later
			previousLoc = currentLoc;

			// **** get equipment listing ****
			ArrayList<String> equipment = new ArrayList<String>();
			equipment = getEquipment();
			// System.out.println("ROVER_00 equipment list results drive " +
			// equipment.get(0));
			System.out.println("ROVER_03 equipment list results " + equipment + "\n");

			// ***** do a SCAN *****
			// System.out.println("ROVER_00 sending SCAN request");
			this.doScan();
			scanMap.debugPrintMap();

			// ***** MOVING *****
			// try moving east 5 block if blocked
			if (blocked) {
				for (int i = 0; i < 5; i++) {
					out.println("MOVE E");
					// System.out.println("ROVER_00 request move E");
					Thread.sleep(300);
				}
				blocked = false;
				// reverses direction after being blocked
				goingSouth = !goingSouth;
			} else {

				// pull the MapTile array out of the ScanMap object
				MapTile[][] scanMapTiles = scanMap.getScanMap();
				int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
				// tile S = y + 1; N = y - 1; E = x + 1; W = x - 1

				if (goingSouth) {
					// check scanMap to see if path is blocked to the south
					// (scanMap may be old data by now)
					if (scanMapTiles[centerIndex][centerIndex + 1].getHasRover()
							|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.ROCK
							|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.NONE) {
						blocked = true;
					} else {
						// request to server to move
						out.println("MOVE S");
						// System.out.println("ROVER_00 request move S");
					}

				} else {
					// check scanMap to see if path is blocked to the north
					// (scanMap may be old data by now)
					// System.out.println("ROVER_00
					// scanMapTiles[2][1].getHasRover() " +
					// scanMapTiles[2][1].getHasRover());
					// System.out.println("ROVER_00
					// scanMapTiles[2][1].getTerrain() " +
					// scanMapTiles[2][1].getTerrain().toString());

					if (scanMapTiles[centerIndex][centerIndex - 1].getHasRover()
							|| scanMapTiles[centerIndex][centerIndex - 1].getTerrain() == Terrain.ROCK
							|| scanMapTiles[centerIndex][centerIndex - 1].getTerrain() == Terrain.NONE) {
						blocked = true;
					} else {
						// request to server to move
						out.println("MOVE N");
						// System.out.println("ROVER_00 request move N");
					}
				}
			}

			// start Rover controller process
			while (true) {

				// **** location call ****
				out.println("LOC");
				line = in.readLine();
				if (line == null) {
					System.out.println("ROVER_03 check connection to server");
					line = "";
				}
				if (line.startsWith("LOC")) {
					// loc = line.substring(4);
					currentLoc = extractLOC(line);
				}
				if (!currentLoc.equals(previousLoc)) {
					System.out.println("ROVER_03 currentLoc at start: " + currentLoc);

					// after getting location set previous equal current to be
					// able to check for stuckness and blocked later
					previousLoc = currentLoc;

				}
			}
		}

	}

	// ################ Support Methods ###########################

	private void clearReadLineBuffer() throws IOException {
		while (in.ready()) {
			// System.out.println("ROVER_03 clearing readLine()");
			String garbage = in.readLine();
		}
	}

	// method to retrieve a list of the rover's equipment from the server
	private ArrayList<String> getEquipment() throws IOException {
		// System.out.println("ROVER_03 method getEquipment()");
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		out.println("EQUIPMENT");

		String jsonEqListIn = in.readLine(); // grabs the string that was
												// returned first
		if (jsonEqListIn == null) {
			jsonEqListIn = "";
		}
		StringBuilder jsonEqList = new StringBuilder();
		// System.out.println("ROVER_03 incomming EQUIPMENT result - first
		// readline: " + jsonEqListIn);

		if (jsonEqListIn.startsWith("EQUIPMENT")) {
			while (!(jsonEqListIn = in.readLine()).equals("EQUIPMENT_END")) {
				if (jsonEqListIn == null) {
					break;
				}
				// System.out.println("ROVER_03 incomming EQUIPMENT result: " +
				// jsonEqListIn);
				jsonEqList.append(jsonEqListIn);
				jsonEqList.append("\n");
				// System.out.println("ROVER_03 doScan() bottom of while");
			}
		} else {
			// in case the server call gives unexpected results
			clearReadLineBuffer();
			return null; // server response did not start with "EQUIPMENT"
		}

		String jsonEqListString = jsonEqList.toString();
		ArrayList<String> returnList;
		returnList = gson.fromJson(jsonEqListString, new TypeToken<ArrayList<String>>() {
		}.getType());
		// System.out.println("ROVER_03 returnList " + returnList);

		return returnList;
	}

	// sends a SCAN request to the server and puts the result in the scanMap
	// array
	public void doScan() throws IOException {
		// System.out.println("ROVER_03 method doScan()");
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		out.println("SCAN");

		String jsonScanMapIn = in.readLine(); // grabs the string that was
												// returned first
		if (jsonScanMapIn == null) {
			System.out.println("ROVER_03 check connection to server");
			jsonScanMapIn = "";
		}
		StringBuilder jsonScanMap = new StringBuilder();
		System.out.println("ROVER_03 incomming SCAN result - first readline: " + jsonScanMapIn);

		if (jsonScanMapIn.startsWith("SCAN")) {
			while (!(jsonScanMapIn = in.readLine()).equals("SCAN_END")) {
				// System.out.println("ROVER_03 incomming SCAN result: " +
				// jsonScanMapIn);
				jsonScanMap.append(jsonScanMapIn);
				jsonScanMap.append("\n");
				// System.out.println("ROVER_03 doScan() bottom of while");
>>>>>>> ea62f47f5669585bc7f969059e0e5d887bafa331
			}
		} else {
			// in case the server call gives unexpected results
			clearReadLineBuffer();
			return; // server response did not start with "SCAN"
		}
<<<<<<< HEAD

		String jsonScanMapString = jsonScanMap.toString();
		// debug print json object to a file
		//new MyWriter( jsonScanMapString, 0);  //gives a strange result - prints the \n instead of newline character in the file

		// convert from the json string back to a ScanMap object
		scanMap = gson.fromJson(jsonScanMapString, ScanMap.class);		
	}


	/* this takes the LOC response string, parses out the x and x values and returns a Coord 
	 * object */
	public static Coord extractLOC(String sStr) {
		String[] coordinates = sStr.split(" ");
		return new Coord(Integer.parseInt(coordinates[1].trim()), Integer.parseInt(coordinates[2].trim()));
	}

	public static void main(String args[]) throws IOException, InterruptedException{
		ROVER_03 client = new ROVER_03();
		client.start();
	}
=======
		// System.out.println("ROVER_03 finished scan while");

		String jsonScanMapString = jsonScanMap.toString();
		// debug print json object to a file
		// new MyWriter( jsonScanMapString, 0); //gives a strange result -
		// prints the \n instead of newline character in the file

		// System.out.println("ROVER_03 convert from json back to ScanMap
		// class");
		// convert from the json string back to a ScanMap object
		scanMap = gson.fromJson(jsonScanMapString, ScanMap.class);
	}

	// this takes the LOC response string, parses out the x and x values and
	// returns a Coord object
	
	public static Coord extractLOC(String sStr) {
		sStr = sStr.substring(4);
		if (sStr.lastIndexOf(" ") != -1) {
			String xStr = sStr.substring(0, sStr.lastIndexOf(" "));
			// System.out.println("extracted xStr " + xStr);

			String yStr = sStr.substring(sStr.lastIndexOf(" ") + 1);
			// System.out.println("extracted yStr " + yStr);
			return new Coord(Integer.parseInt(xStr), Integer.parseInt(yStr));
		}
		return null;
	}

	public static void main(String args[]) throws IOException, InterruptedException {
		ROVER_03 client = new ROVER_03();
		client.start();
	}

	
>>>>>>> ea62f47f5669585bc7f969059e0e5d887bafa331
}
