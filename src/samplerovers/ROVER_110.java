package samplerovers;

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
import common.ScanMap;
import communication.RoverServer2;
import model.Rover;

public class ROVER_110{

	/********************************************* Rover Constants ************************************************************/

	private static final String ROVER_NAME = "ROVER_110";
	private static final int SLEEP_TIME = 10000; // The higher this number is the smaller the number of request sent to server
	private static final int WAIT_FOR_ROVERS = 20000; 


	// port and ip for swarm server we will be connecting to ... change here if necessary 
	private static final String SERVER_ADDRESS = "localhost";
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
	
	private RoverServer2 server;

	public ROVER_110() throws IOException, InterruptedException {
		rover = new Rover(ROVER_NAME);
		server = new RoverServer2(rover);
		new Thread(server).start();
		server.connectTo("127.0.0.1", 9000);
		/****** Testing to see if other rover gets there locations *************/
//		server.sendLOC("LOC 30 21 0");
//		server.sendLOC("LOC 5 32 0");
//		server.sendLOC("LOC 15 36 0");
//		server.sendLOC("LOC 17 36 0");
//		server.sendLOC("LOC 19 0 0");
//		server.sendLOC("LOC 24 13 0");
//		server.sendLOC("LOC 17 12 0");
//		server.sendLOC("LOC 4 20 0");
//		server.sendLOC("LOC 10 23 0");


		/**********************************************************************/
		
		Thread.sleep(WAIT_FOR_ROVERS);    // Make thread sleep until all rovers have connected
		// this should be a safe but slow timer value
	}

	// Starts rover
	@SuppressWarnings("resource")
	private void start() throws IOException, InterruptedException {
		// Make connection and initialize streams
		Socket socket = new Socket(SERVER_ADDRESS, PORT_ADDRESS); // set port here
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);


		// Process all messages from server, wait until server requests Rover ID name
		while (true) {
			String line = in.readLine();
			if (line.startsWith("SUBMITNAME")) {
				out.println(ROVER_NAME); /* This sets the name of this instance of a swarmBot for identifying the thread to the server*/
				break;
			}
		}

		/**** get equipment listing including drive type ****/			
		ArrayList<String> equipment = new ArrayList<String>();
		equipment = getEquipment();
		rover.setTool(equipment.get(1));
		rover.setTool(equipment.get(2));
		rover.setDriveType(equipment.get(0));
		
		System.out.println("ROVER_110 equipment list " + equipment + "\n");
		getLocation(rover.getName() + " currentLoc at start: ");

		/******** Rover logic *********/		
		// Cardinals directions will be used when rover starts moving
		String[] cardinals = {"N", "E", "S", "W"};

		// start Rover controller process
		while (true) {	
			getLocation(rover.getName() + " currentLoc: ");
			Thread.sleep(SLEEP_TIME); // We need to have thread sleep until signal is received from another rover ****
			server.roverQueue.displayLocation();
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
	
	
	// Add get LOC from Server and set it on queue **************************************/


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
			}
		} else {
			// in case the server call gives unexpected results
			clearReadLineBuffer();
			return; // server response did not start with "SCAN"
		}

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
		ROVER_110 client = new ROVER_110();
		client.start();
	}
}