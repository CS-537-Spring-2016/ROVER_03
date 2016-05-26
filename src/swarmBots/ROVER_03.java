package swarmBots;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
import tasks.Task;
import trackingUtility.State;
import trackingUtility.Tracker;

public class ROVER_03{

	/*--------------------------------------------- CONSTANTS AND VARIABLES ----------------------------------------------------*/

	private static final String ROVER_NAME = "ROVER_03";
	private static final int SLEEP_TIME = 1000; // The higher this number is the smaller the number of request sent to server

	// port and ip for swarm server we will be connecting to 
	private static final int PORT_ADDRESS = 9537;
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

		setEquipment();
		setTargetLocationTasks();
		System.out.println(rover);

		/* Start ROVER_03 Tasks */ 
		while (true){ 
			if(!server.getQueue().isEmpty())
				startMission(server.getQueue().closestTask(roverTracker.getCurrentLocation()));
		}
	}	

	
	/*----------------------------------------------- METHODS USED FOR MOVEMENT --------------------------------------------------------------*/
	
	/* ROVER_03 starts moving towars destination coordinate specified in Task */
	private void startMission(Task task) throws IOException, InterruptedException{
		// Requests current location from swarm server and sets current location in the rover tracker instance
		getLocation();
		
		System.out.println("\nCURRENT LOCATION: " + roverTracker.getCurrentLocation());
		
		roverTracker.setStartingPoint(roverTracker.getCurrentLocation());
		System.out.println("STARTING POINT: " + roverTracker.getStartingPoint());
		
		roverTracker.setDestination(task.getDestination());
		System.out.println("DESTINATION: " + task.getDestination());
		
		roverTracker.setDistanceTracker();
		System.out.println("DISTANCE: " + roverTracker.getDistanceTracker());

		/* Location is not se in the beginning */
		String direction = null;

		while(!roverTracker.hasArrived()){
			
			/* This if statement applies to the locations that were set for the target location when the rover went online since 
			 * the rover places them in the queue regardless of the terrain. What this if statement looks for is if the rover can see its
			 * destination in its scan, if it can it checks whether or not the destination is on rocky terrain or has a rover on it, if it does 
			 * the the mission is aborted */
			if(roverTracker.targetInRange() && blocked(roverTracker.getDistanceTracker().xpos, roverTracker.getDistanceTracker().ypos)){
				server.getQueue().removeCompletedJob();
				System.out.println("UNABLE TO REACH DESTINATION ... ABORTING MISSION!");
				return;
			}
			
			/* Call resolveDirection() which decides what direction rover should go first */
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
		System.out.println("SENDING GATHER REQUEST ");
		out.println("GATHER");
		/* Send awknowledgement to command center */
		if(!task.getRoverName().equals("TARGET_LOCATION"))
			sendAcknowledgement(task + " GATHERED");
		System.out.println("JOB COMPLETED\n");
		setCargo();
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

		/* The direction here is decided based on the entries for the x and y velocity, this is a shortened version of 
		 * if else statements */
		String direction = (xVelocity == 1)?"E":(xVelocity == -1)?"W":(yVelocity == 1)?"S":(yVelocity == -1)?"N":null;
		/* Needs to choose a condition for the while loop so there is a shorthand if statement in the while condition*/
		while((xVelocity != 0)?roverTracker.getDistanceTracker().xpos != 0:roverTracker.getDistanceTracker().ypos != 0){
			if(!blocked(xVelocity,yVelocity)) move(direction); /* If it is not blocked it moves in the direction that was decided */
			else {
				roverTracker.addMarker(new State(new Coord(roverTracker.getCurrentLocation().xpos + xVelocity, roverTracker.getCurrentLocation().ypos + yVelocity)));
				roverTracker.setLastSuccessfulMove(roverTracker.getCurrentLocation());
				goAround(direction); /* Direction is the direction the rover was headed when it got blocked */
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
		}
		Thread.sleep(SLEEP_TIME);		/* Thread needs to sleep after every move so it does not send too many requests */
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
	
	/* Used to send message to command center when job is completed */
	private void sendAcknowledgement(String acknowledgement) throws UnknownHostException, IOException{
		Socket commandCenter = new Socket("192.168.1.108", 53799); //Do not know if this is the correct ip
		PrintWriter out = new PrintWriter(commandCenter .getOutputStream());
		out.print(acknowledgement);
		commandCenter.close();
		out.close();
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
			System.out.println(rover.getName() + " check connection to server");
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

	/*	
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
	 */

}