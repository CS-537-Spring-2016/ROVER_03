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
import communication.RoverServer;
import enums.Terrain;
import model.Rover;
import trackingUtility.Tracker;

public class ROVER_03{

	/********************************************* Rover Constants ************************************************************/

	private static final String ROVER_NAME = "ROVER_03";
	private static final int SLEEP_TIME = 10000; // The higher this number is the smaller the number of request sent to server
	private static final int WAIT_FOR_ROVERS = 20000; 


	// port and ip for swarm server we will be connecting to ... change here if necessary 
	private static final String SERVER_ADDRESS = "localhost";
	//private static final String SERVER_ADDRESS = "192.168.1.206";
	private static final int PORT_ADDRESS = 9537;

	/*************************************************************************************************************************/

	// Buffered Reader used to receive responses from server, Print Writer used to send request to server
	private BufferedReader in;
	private PrintWriter out;

	private Tracker roverTracker;

	private ScanMap scanMap;

	private Rover rover;

	private Coord previousLoc;
	private Coord currentLoc;
	private String results = "";

	private RoverServer server;

	public ROVER_03() throws IOException, InterruptedException {
		rover = new Rover(ROVER_NAME);
		server = new RoverServer(rover);
		roverTracker = new Tracker();
		new Thread(server).start();
		Thread.sleep(WAIT_FOR_ROVERS);    // Make thread sleep until all rovers have connected
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

		System.out.println("ROVER_03 equipment list " + equipment + "\n");
		getLocation(rover.getName() + " currentLoc at start: ");
		
		//target_location request
		
		out.println("TARGET_LOC");
		server.getQueue().addLocation(in.readLine());

		/******** Rover logic *********/		
		// Cardinals directions will be used when rover starts moving
		String[] cardinals = {"N", "E", "S", "W"};

		// start Rover controller process
		while (true) {	
			getLocation(rover.getName() + " currentLoc: ");

			Thread.sleep(SLEEP_TIME); 
			if(!server.getQueue().isEmpty()){
				System.out.print("Going to this location: ");

				/************************* MOVEMENT FOR TESTING OF COMMUNICATION*************************************************/
				// pull the MapTile array out of the ScanMap object
				server.getQueue().displayLocation();
				roverTracker.setStartingPoint(currentLoc);
				roverTracker.setDestination(extractLOC(server.getQueue().closestTargetLocation("LOC" + " " + currentLoc.xpos + " " + currentLoc.ypos)));
				roverTracker.setDistanceTracker();
				String direction = (roverTracker.getDistanceTracker().xpos > 0)?"E":"W";
				if(direction.equals("E"))roverTracker.goingEast = true;
				else roverTracker.goingWest = true;

				while(!roverTracker.hasArrived()){
					//System.out.println("New iteration");
					//System.out.println(direction);
					if(roverTracker.goingEast){
						System.out.println("going east");
						while(roverTracker.getDistanceTracker().xpos != 0){
							getLocation(rover.getName() + " currentLoc: ");
							if(!blockedEast()){
								out.println("MOVE " + direction);
								roverTracker.updateXPos((roverTracker.getDistanceTracker().xpos >= 0)?-1:1);
								Thread.sleep(1100);
							}
							else{
								while(blockedEast()){
									getLocation(rover.getName() + " currentLoc: ");
									out.println("MOVE S");
									roverTracker.updateYPos(-1);
									Thread.sleep(1100);
								}
							}
						}

						if(roverTracker.getDistanceTracker().xpos == 0){
							roverTracker.goingEast = false;
							direction = (roverTracker.getDistanceTracker().ypos > 0)?"S":"N";
							if(direction.equals("S"))roverTracker.goingSouth = true;
							else roverTracker.goingNorth = true;
							System.out.println("leaving east loop");
							//System.out.println(roverTracker.toString());
						}
					}
					//System.out.println(direction);

					if(roverTracker.goingWest){
						System.out.println("Going west");
						while(roverTracker.getDistanceTracker().xpos != 0){
							getLocation(rover.getName() + " currentLoc: ");
							if(!blockedWest()){
								out.println("MOVE " + direction);
								roverTracker.updateXPos((roverTracker.getDistanceTracker().xpos >= 0)?-1:1);
								//System.out.println(roverTracker.toString());
								Thread.sleep(1100);
							}
							else{
								while(blockedWest()){
									getLocation(rover.getName() + " currentLoc: ");
									out.println("MOVE S");
									roverTracker.updateYPos(-1);
									Thread.sleep(1100);
								}
							}
						}
						if(roverTracker.getDistanceTracker().xpos == 0){
							roverTracker.goingWest = false;
							direction = (roverTracker.getDistanceTracker().ypos > 0)?"S":"N";
							if(direction.equals("S"))roverTracker.goingEast = true;
							else roverTracker.goingNorth = true;
							//System.out.println(roverTracker.toString());
						}
					}
					//System.out.println(direction);

					//going south
					if(roverTracker.goingSouth){
						System.out.println("Going south");
						while(roverTracker.getDistanceTracker().ypos != 0){
							getLocation(rover.getName() + " currentLoc: ");
							if(!blockedSouth()){
								out.println("MOVE " + direction);
								roverTracker.updateYPos((roverTracker.getDistanceTracker().ypos >= 0)?-1:1);
								Thread.sleep(1100);
							}
							else{
								while(blockedSouth()){
									getLocation(rover.getName() + " currentLoc: ");
									out.println("MOVE E");
									roverTracker.updateXPos(-1);
									Thread.sleep(1100);
								}
							}
						}
						if(roverTracker.getDistanceTracker().ypos == 0){
							roverTracker.goingSouth = false;
							direction = (roverTracker.getDistanceTracker().xpos > 0)?"E":"W";
							if(direction.equals("E"))roverTracker.goingEast = true;
							else roverTracker.goingWest = true;
							//System.out.println(roverTracker.toString());
						}
					}
					//System.out.println(direction);

					//going north
					if(roverTracker.goingNorth){
						System.out.println("Going north");
						while(roverTracker.getDistanceTracker().ypos != 0){
							getLocation(rover.getName() + " currentLoc: ");
							if(!blockedNorth()){
								out.println("MOVE " + direction);
								roverTracker.updateYPos((roverTracker.getDistanceTracker().ypos >= 0)?-1:1);
								Thread.sleep(1100);
							}
							else{
								while(blockedNorth()){
									getLocation(rover.getName() + " currentLoc: ");
									out.println("MOVE E");
									roverTracker.updateXPos(-1);
									Thread.sleep(1100);
								}
							}
						}
						if(roverTracker.getDistanceTracker().ypos == 0){
							roverTracker.goingNorth = false;
							direction = (roverTracker.getDistanceTracker().xpos > 0)?"E":"W";
							if(direction.equals("E"))roverTracker.goingEast = true;
							else roverTracker.goingWest = true;
							//System.out.println(roverTracker.toString());
						}
					}
				}
				server.getQueue().removeCompletedJob();
				System.out.println("ROVER_03 request GATHER");
				out.println("GATHER");
			}
			/************************* MOVEMENT FOR TESTING OF COMMUNICATION*************************************************/

		}



	}

	private boolean blockedEast(){
		MapTile[][] scanMapTiles = scanMap.getScanMap();
		int centerIndex = (scanMap.getEdgeSize() - 1)/2;
		return scanMapTiles[centerIndex+1][centerIndex].getHasRover() 
				|| scanMapTiles[centerIndex+1][centerIndex].getTerrain() == Terrain.ROCK
				|| scanMapTiles[centerIndex+1][centerIndex].getTerrain() == Terrain.NONE;
	}

	private boolean blockedWest(){
		MapTile[][] scanMapTiles = scanMap.getScanMap();
		int centerIndex = (scanMap.getEdgeSize() - 1)/2;
		return scanMapTiles[centerIndex-1][centerIndex].getHasRover() 
				|| scanMapTiles[centerIndex-1][centerIndex].getTerrain() == Terrain.ROCK
				|| scanMapTiles[centerIndex-1][centerIndex].getTerrain() == Terrain.NONE;
	}

	private boolean blockedNorth(){
		MapTile[][] scanMapTiles = scanMap.getScanMap();
		int centerIndex = (scanMap.getEdgeSize() - 1)/2;
		return scanMapTiles[centerIndex][centerIndex-1].getHasRover() 
				|| scanMapTiles[centerIndex][centerIndex-1].getTerrain() == Terrain.ROCK
				|| scanMapTiles[centerIndex][centerIndex-1].getTerrain() == Terrain.NONE;
	}

	private boolean blockedSouth(){
		MapTile[][] scanMapTiles = scanMap.getScanMap();
		int centerIndex = (scanMap.getEdgeSize() - 1)/2;
		return scanMapTiles[centerIndex][centerIndex+1].getHasRover() 
				|| scanMapTiles[centerIndex][centerIndex+1].getTerrain() == Terrain.ROCK
				|| scanMapTiles[centerIndex][centerIndex+1].getTerrain() == Terrain.NONE;
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
		//System.out.println(rover.getName() + " incomming SCAN result - first readline: " + jsonScanMapIn);

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
		ROVER_03 client = new ROVER_03();
		client.start();
	}
}