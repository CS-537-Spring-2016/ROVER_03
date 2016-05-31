package swarmBots;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import common.Coord;
import common.MapTile;
import common.ScanMap;
import communication.RoverServer;
import enums.Terrain;
import model.Rover;
import model.RoverQueue;
import movement.PathFinder;
import tasks.Task;
import trackingUtility.Tracker;

public class ROVER_03{

	/*--------------------------------------------- CONSTANTS AND VARIABLES ----------------------------------------------------*/

	private static final String ROVER_NAME = "ROVER_03";
	private static final int SLEEP_TIME = 1000; // The higher this number is the smaller the number of request sent to server

	// port and ip for swarm server we will be connecting to 
	private static final int PORT_ADDRESS = 9537;
	private static final String COMMAND_CENTER_IP = "127.0.0.1";
	private static final int COMMAND_CENTER_PORT = 53799;
	private String SERVER_ADDRESS;

	private Rover rover;

	// Buffered Reader used to receive responses from server, Print Writer used to send request to server
	private BufferedReader in;
	private PrintWriter out;

	private Map<String,Integer> cargo;
	private Tracker roverTracker;
	private ScanMap scanMap;

	private RoverServer server;

	/*--------------------------------------------------- ROVER_03 CONSTRUCTOR -----------------------------------------------*/

	public ROVER_03(String ip) throws IOException, InterruptedException {
		SERVER_ADDRESS = ip;
		rover = new Rover(ROVER_NAME);
		roverTracker = new Tracker();
		/* Sets cargo all cargo to 0 */
		cargo = new HashMap<>();
		resetCargo();
		server = new RoverServer(rover);
		new Thread(server).start();
	}

	/* Starts ROVER_03 */
	@SuppressWarnings("resource")
	public void start() throws IOException, InterruptedException {
		// Make connection and initialize streams
		Socket socket = new Socket(SERVER_ADDRESS, PORT_ADDRESS); 
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

		getLocation();
		setEquipment();
		setTargetLocationTasks();
		System.out.println(rover);

		/* Start ROVER_03 Tasks */ 
		while (true){ 
			roverTracker.lastVisited.clear();
			if(!server.getQueue().isEmpty()){
			startMission(server.getQueue().closestTask(roverTracker.getCurrentLocation()));
			}
		}
	}	


	/*----------------------------------------------- METHODS USED FOR MOVEMENT --------------------------------------------------------------*/

	/* ROVER_03 starts moving towars destination coordinate specified in Task */
	private void startMission(Task task) throws IOException, InterruptedException{
		System.out.println("\nCURRENT LOCATION: " + roverTracker.getCurrentLocation());

		roverTracker.setStartingPoint(roverTracker.getCurrentLocation());
		System.out.println("STARTING POINT: " + roverTracker.getStartingPoint());
		roverTracker.setDestination(task.getDestination());
		System.out.println("DESTINATION: " + task.getDestination());
		roverTracker.setDistanceTracker();
		System.out.println("DISTANCE: " + roverTracker.getDistanceTracker());
		
		
		PathFinder path = new PathFinder(scanMap, roverTracker, roverTracker.getCurrentLocation(), roverTracker.getDestination());

		while(!roverTracker.hasArrived()){
			ArrayList<String> moveList = path.generatePath();
			if(moveList == null){
				System.out.println("TARGET IS UNREACHABLE ... ABORTING MISSION");
				server.getQueue().removeCompletedJob();
				return;
			}
			//roverTracker.startedFrom = path.start;
			//Coord curr = null;
			for(String direction: moveList){
//				curr = c;
				accelerate(direction);
			}
			//System.exit(0);
			getLocation();
			path.setStart(roverTracker.getCurrentLocation());
			path.setMap(scanMap);
		}

		server.getQueue().removeCompletedJob();
		System.out.println("SENDING GATHER REQUEST ");
		out.println("GATHER");
		/* Send acknowledgement to command center */
		//System.out.println(task.getRoverName());
		//if(task.getRoverName().equals("ROVER"))
		//sendAcknowledgement(task + " GATHERED");
		System.out.println("JOB COMPLETED\n");
		setCargo();
	}


	private void accelerate(String direction) throws IOException, InterruptedException{
//		String direction = (xVelocity == 1)?"E":(xVelocity == -1)?"W":(yVelocity == 1)?"S":(yVelocity == -1)?"N":null;
		System.out.println("MOVE " + direction);
		Coord previousLocation = roverTracker.getCurrentLocation();
		while(previousLocation.equals(roverTracker.getCurrentLocation())){
			previousLocation = roverTracker.getCurrentLocation();
			//out.println("MOVE " + direction);
			move(direction);
			getLocation();
		}
	}

	/* Sends move request to server and updates distance tracker */
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
		}
			Thread.sleep(SLEEP_TIME);		/* Thread needs to sleep after every move so it does not send too many requests */
	}

	/*------------------------------------------- SWARM SERVER REQUESTS ----------------------------------------------------*/

	/* Sends request for either CARGO or EQUIPMENT to the swarm server*/
	@SuppressWarnings("unused")
	private ArrayList<String> request(String type) throws IOException{
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		out.println(type);

		String jsonEqListIn = in.readLine(); /* Grabs the string that was returned first */
		if(jsonEqListIn == null){
			jsonEqListIn = "";
		}
		StringBuilder jsonEqList = new StringBuilder();
		if(jsonEqListIn.startsWith(type)){
			while (!(jsonEqListIn = in.readLine()).equals(type + "_END")) {
				if(jsonEqListIn == null)
					break;
				jsonEqList.append(jsonEqListIn);
				jsonEqList.append("\n");
			}

		} else {
			/* in case the server call gives unexpected results */
			clearReadLineBuffer();
			return null; /* Server response did not start with the type that was requested */
		}
		String jsonEqListString = jsonEqList.toString();		
		ArrayList<String> returnList;		
		returnList = gson.fromJson(jsonEqListString, new TypeToken<ArrayList<String>>(){}.getType());		
		return returnList;
	}


	/* Returns coordinate object that represents rover's current location */
	private void getLocation() throws IOException, InterruptedException{
		Coord previousLocation = roverTracker.getCurrentLocation();
		out.println("LOC");
		String results = in.readLine();
		if (results == null) {
			System.exit(1);
			results = "";
		}
		if (results.startsWith("LOC"))
			roverTracker.setCurrentLocation(extractLOC(results));
		if(!roverTracker.getCurrentLocation().equals(previousLocation)){
			this.doScan();
		}
	}

	/* Sends a SCAN request to the server and puts the result in the scanMap array */
	public void doScan() throws IOException {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		out.println("SCAN");

		String jsonScanMapIn = in.readLine(); /* Grabs the string that was returned first */
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
			/* In case the server call gives unexpected results */
			clearReadLineBuffer();
			return; /* Server response did not start with "SCAN" */
		}

		String jsonScanMapString = jsonScanMap.toString();

		/* Convert from the json string back to a ScanMap object */
		scanMap = gson.fromJson(jsonScanMapString, ScanMap.class);		
	}

	/*-------------------------------------------------- SUPPORT METHODS -----------------------------------------------*/

	private void clearReadLineBuffer() throws IOException{
		while(in.ready()){
			in.readLine();	
		}
	}

	/* This takes the LOC response string, parses out the x and y values and returns a Coord object */
	public static Coord extractLOC(String response) {
		String[] coordinates = response.split(" ");
		return new Coord(Integer.parseInt(coordinates[1].trim()), Integer.parseInt(coordinates[2].trim()));
	}

	/* ----------------------------------------------------- GETTERS ----------------------------------------------------*/

	public ScanMap getScanMap(){
		return scanMap;
	}

	public RoverQueue getQueue(){
		return server.getQueue();
	}

	public Tracker getTracker(){
		return roverTracker;
	}

	public Rover getRover(){
		return rover;
	}

	public Map<String,Integer> getCargoList(){
		return cargo;
	}

	/*---------------------------------------------------- SETTERS ------------------------------------------------------*/

	private void setEquipment() throws IOException{
		/* Get equipment listing including drive type */			
		ArrayList<String> equipment = new ArrayList<String>();
		equipment = request("EQUIPMENT");
		rover.setTool(equipment.get(1));
		rover.setTool(equipment.get(2));
		rover.setDriveType(equipment.get(0));
	}

	private void setCargo() throws IOException{
		resetCargo();
		for(String s : request("CARGO")){
			if(cargo.containsKey(s)){
				cargo.put(s, cargo.get(s) + 1);
			}
		}
	}

	private void resetCargo(){
		cargo.put("ORGANIC", 0);
		cargo.put("MINERAL", 0);
		cargo.put("RADIOACTIVE", 0);
		cargo.put("CRYSTAL", 0);
	}

	private void setTargetLocationTasks() throws IOException{
		/* Target_location request */
		out.println("TARGET_LOC");
		Coord target = extractLOC(in.readLine());

		/* Will add every location in the target location to the queue regardless of the terrain it is on*/
		for(int x = -3; x < 4; x ++)
			for (int y = -3; y < 4; y++)
				server.getQueue().addTask(
						new Task(
								"TARGET_LOCATION", 
								"UNKNOWN", 				/* UNKNOWN TERRAIN */
								"UNKNOWN",				/* UNKNOWN SCIENCE */
								new Coord(x + target.xpos,y + target.ypos)));
	}


	/* --------------------------------- UNCOMMENT THIS TO RUN WITHOUT GUI -------------------------------------------------*/


	public static void main(String args[]) throws IOException, InterruptedException{
		ROVER_03 client;
		if(!(args.length == 0)){
			// 192.168.1.106
			client = new ROVER_03(args[0]);
		} else {
			client = new ROVER_03("127.0.0.1");
		}


		client.start();
	} 


}