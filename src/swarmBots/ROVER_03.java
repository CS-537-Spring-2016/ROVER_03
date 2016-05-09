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
import trackingUtility.State;
import trackingUtility.Tracker;

public class ROVER_03{

	/********************************************* Rover Constants ************************************************************/

	private static final String ROVER_NAME = "ROVER_03";
	private static final int SLEEP_TIME = 1000; // The higher this number is the smaller the number of request sent to server
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
	private RoverServer server;

	public ROVER_03() throws IOException, InterruptedException {
		rover = new Rover(ROVER_NAME);
		server = new RoverServer(rover);
		roverTracker = new Tracker();
		new Thread(server).start();
		//		Thread.sleep(WAIT_FOR_ROVERS);    // Make thread sleep until all rovers have connected
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

		/* Get equipment listing including drive type */			
		ArrayList<String> equipment = new ArrayList<String>();
		equipment = getEquipment();
		rover.setTool(equipment.get(1));
		rover.setTool(equipment.get(2));
		rover.setDriveType(equipment.get(0));

		System.out.println("ROVER_03 equipment list " + equipment + "\n");

		//target_location request
		out.println("TARGET_LOC");
		Coord target = extractLOC(in.readLine());
		roverTracker.setTargetLocation(target);
		getLocation();
		startMission(target);
		
		/* Puts all the tiles in the target location in the job queue */
		if(roverTracker.atTargetLocation(roverTracker.getCurrentLocation())){
			for(int x = -3; x < 4; x ++)
				for (int y = -3; y < 4; y++)
					if (!blocked(x,y))
						server.getQueue().addLocation(new Coord(x + roverTracker.getCurrentLocation().xpos,y + roverTracker.getCurrentLocation().ypos));
		}
		
		// Start Rover controller process 
		while (true) 
			if(!server.getQueue().isEmpty()){
				getLocation();
				startMission(server.getQueue().closestTargetLocation(roverTracker.getCurrentLocation()));
			}
	}	

	private void startMission(Coord destination) throws IOException, InterruptedException{
		System.out.println("\nCURRENT LOCATION: " + roverTracker.getCurrentLocation());
		roverTracker.setStartingPoint(roverTracker.getCurrentLocation());
		System.out.println("STARTING POINT: " + roverTracker.getStartingPoint());
		roverTracker.setDestination(destination);
		System.out.println("DESTINATION: " + destination);
		roverTracker.setDistanceTracker();
		System.out.println("DISTANCE: " + roverTracker.getDistanceTracker());


		String direction = null;

		while(!roverTracker.hasArrived()){
			direction = resolveDirection();
			if(direction.equals("E")){
				System.out.println("HEADED EAST");
				accelerate(1,0);
			}
			if(direction.equals("W")){
				System.out.println("HEADED WEST");
				accelerate(-1,0);
			}
			if(direction.equals("S")){
				System.out.println("HEADED SOUTH");
				accelerate(0,1);
			}
			if(direction.equals("N")){
				System.out.println("HEADED NORTH");
				accelerate(0,-1);
			}
		}

		server.getQueue().removeCompletedJob();
		System.out.println(rover.getName() + " request GATHER");
		out.println("GATHER");
		System.out.println("JOB COMPLETED\n");
	}

	/* This method is used to decide what direction the rover will go next */
	private String resolveDirection(){
		if(roverTracker.getDistanceTracker().xpos > 0)
			return "E";
		if(roverTracker.getDistanceTracker().xpos < 0)
			return "W";
		if(roverTracker.getDistanceTracker().ypos > 0)
			return "S";
		if(roverTracker.getDistanceTracker().ypos < 0)
			return "N";
		return null;
	}

	private void accelerate(int xVelocity, int yVelocity) throws IOException, InterruptedException{

		String direction = (xVelocity == 1)?"E":(xVelocity == -1)?"W":(yVelocity == 1)?"S":(yVelocity == -1)?"N":null;
		while((xVelocity != 0)?roverTracker.getDistanceTracker().xpos != 0:roverTracker.getDistanceTracker().ypos != 0){
			if(!blocked(xVelocity,yVelocity)) move(direction);
			else {
				roverTracker.addMarker(new State(new Coord(roverTracker.getCurrentLocation().xpos + xVelocity, roverTracker.getCurrentLocation().ypos + yVelocity)));
				roverTracker.setLastSuccessfulMove(roverTracker.getCurrentLocation());
				goAround(direction);
			}
			getLocation();
		}
	}

	private void goAround(String direction) throws InterruptedException, IOException{

		String previousDirection = "";
		String direction1 = previousDirection;
		while((roverTracker.getCurrentLocation().ypos > roverTracker.peekMarker().getY() && direction.equals("N")) ||
				(roverTracker.getCurrentLocation().xpos > roverTracker.peekMarker().getX() && direction.equals("W")) ||
				(roverTracker.getCurrentLocation().ypos < roverTracker.peekMarker().getY() && direction.equals("S")) ||
				(roverTracker.getCurrentLocation().xpos < roverTracker.peekMarker().getX() && direction.equals("E"))){
			getLocation();
			int centerIndex = (scanMap.getEdgeSize() - 1)/2;
			direction1 = previousDirection;

			if((!blocked(0,1) && (blocked(1,-1, centerIndex, centerIndex + 1) || blocked(1,1))) && !previousDirection.equals("N")){
				move("S");
				previousDirection = "S";
				continue;
			}

			if((!blocked(0,-1) && (blocked(-1,1, centerIndex, centerIndex - 1) || blocked(-1,-1))) && !previousDirection.equals("S")){
				move("N");
				previousDirection = "N";
				continue;
			}

			if((!blocked(-1,0) && (blocked(1,1, centerIndex - 1, centerIndex) || blocked(-1,1))) && !previousDirection.equals("E")){
				move("W");
				previousDirection = "W";
				continue;
			}

			if((!blocked(1,0) && (blocked(-1,-1, centerIndex + 1, centerIndex) || blocked(1,-1))) && !previousDirection.equals("W")){
				move("E");
				previousDirection = "E";
				continue;
			}

			if(direction1.equals(previousDirection)){
				previousDirection = "";
			}
		}
	}


	private void move(String direction) throws IOException, InterruptedException{
		Coord previousLocation = roverTracker.getCurrentLocation();
		out.println("MOVE " + direction);
		getLocation();
		if(!previousLocation.equals(roverTracker.getCurrentLocation())){		
		switch(direction.charAt(0)){
		case('N'):
			roverTracker.updateYPos(1);
		break;
		case('W'):
			roverTracker.updateXPos(1);
		break;
		case('S'):
			roverTracker.updateYPos(-1);
		break;
		case('E'):
			roverTracker.updateXPos(-1);
		break;
		}
		System.out.println("Distance Left = " + roverTracker.getDistanceTracker().xpos + "," + roverTracker.getDistanceTracker().ypos);
		}
		Thread.sleep(SLEEP_TIME);
	}

	private boolean blocked(int xOffset, int yOffset){
		MapTile[][] map = scanMap.getScanMap();
		int centerIndex = (scanMap.getEdgeSize() - 1)/2;
		return map[centerIndex + xOffset][centerIndex + yOffset].getHasRover() 
				|| map[centerIndex + xOffset][centerIndex + yOffset].getTerrain() == Terrain.ROCK
				|| map[centerIndex + xOffset][centerIndex + yOffset].getTerrain() == Terrain.NONE;
	}

	private boolean blocked(int xOffset, int yOffset, int roverX, int roverY){
		MapTile[][] map = scanMap.getScanMap();
		return map[roverX + xOffset][roverY + yOffset].getHasRover() 
				|| map[roverX + xOffset][roverY + yOffset].getTerrain() == Terrain.ROCK
				|| map[roverX + xOffset][roverY + yOffset].getTerrain() == Terrain.NONE;
	}

	/* Returns coordinate object that represents rover's current location */
	private void getLocation() throws IOException, InterruptedException{
		Coord previousLocation = roverTracker.getCurrentLocation();
		out.println("LOC");
		String results = in.readLine();
		if (results == null) {
			System.out.println(rover.getName() + " check connection to server");
			results = "";
		}
		if (results.startsWith("LOC"))
			roverTracker.setCurrentLocation(extractLOC(results));
		if(!roverTracker.getCurrentLocation().equals(previousLocation)){
			this.doScan();
			//scanMap.debugPrintMap();
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