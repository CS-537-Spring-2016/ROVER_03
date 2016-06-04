package swarmBots;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import common.MapTile;
import common.ScanMap;
import communication.RoverServer;
import enums.Terrain;
import model.Rover;
import model.RoverQueue;
import movement.Coordinate;
import movement.PathFinder;
import tasks.Task;
import trackingUtility.Tracker;

public class ROVER_03{

	/*--------------------------------------------- CONSTANTS AND VARIABLES ----------------------------------------------------*/

	private static final String ROVER_NAME = "ROVER_03";
	private static final int SLEEP_TIME = 100; // The higher this number is the smaller the number of request sent to server

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

		roverTracker.setOrigin(roverTracker.getCurrentLocation().getAbsoluteX(), roverTracker.getCurrentLocation().getAbsoluteY());
		roverTracker.setDestination(task.getDestination().getAbsoluteX(), task.getDestination().getAbsoluteY());
		roverTracker.setDistance();


		PathFinder path = new PathFinder(roverTracker);

		while(!roverTracker.hasArrived()){
			ArrayList<String> moveList = path.generatePath(scanMap);
			if(moveList == null){
				System.out.println("TARGET IS UNREACHABLE ... ABORTING MISSION");
				server.getQueue().removeCompletedJob();
				return;
			}
			System.out.println("MOVES: " + moveList.toString());



			for(String direction: moveList){
				accelerate(direction);
				if((roverTracker.getYDistance() >= -6 && roverTracker.getYDistance() <= 6) || (roverTracker.getXDistance() >= 6 && roverTracker.getXDistance() <= 6))
					continue;
				if(blocked(roverTracker.getCurrentLocation().compareTo(roverTracker.getDestination())))
					break;
			}
			getLocation();
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
		Coordinate previousLocation = new Coordinate(roverTracker.getCurrentLocation().getAbsoluteX(), roverTracker.getCurrentLocation().getAbsoluteY(), Coordinate.TYPE.ABSOLUTE);
		while(previousLocation.equals(roverTracker.getCurrentLocation())){
			previousLocation.setAbsolute(roverTracker.getCurrentLocation().getAbsoluteX(), roverTracker.getCurrentLocation().getAbsoluteY());
			move(direction);
			getLocation();
		}
	}

	/* Sends move request to server and updates distance tracker */
	private void move(String direction) throws IOException, InterruptedException{
		Coordinate previousLocation = new Coordinate(roverTracker.getCurrentLocation().getAbsoluteX(), roverTracker.getCurrentLocation().getAbsoluteY(), Coordinate.TYPE.ABSOLUTE);
		out.println("MOVE " + direction);
		getLocation();
		if(!previousLocation.equals(roverTracker.getCurrentLocation())){
			switch(direction.charAt(0)){
			case('N'):
				roverTracker.updateYDistance(1);
			break;
			case('W'):
				roverTracker.updateXDistance(1);
			break;
			case('S'):
				roverTracker.updateYDistance(-1);
			break;
			case('E'):
				roverTracker.updateXDistance(-1);
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
		//Coordinate previousLocation = roverTracker.getCurrentLocation();
		out.println("LOC");
		String results = in.readLine();
		if (results == null) {
			System.exit(1);
			results = "";
		}
		if (results.startsWith("LOC")){
			int[] coordinate = extractLOC(results);
			roverTracker.setCurrentLocation(coordinate[0], coordinate[1]);
		}
		//if(!roverTracker.getCurrentLocation().equals(previousLocation)){
		this.doScan();
		//}
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

	private boolean blocked(int quadrant){
		MapTile[][] tiles = scanMap.getScanMap();
		if(quadrant == 4){
			//	return tiles[6][6].getHasRover() || tiles[6][6].getTerrain() == Terrain.ROCK || tiles[6][6].getTerrain() == Terrain.NONE;
			for(int i = 5; i >= 3; i--){
				if(tiles[5][i].getHasRover() || tiles[5][i].getTerrain() == Terrain.ROCK || tiles[5][i].getTerrain() == Terrain.NONE)
					return true;
			}

			for(int i = 5; i >= 3; i--){
				if(tiles[i][5].getHasRover() || tiles[i][5].getTerrain() == Terrain.ROCK || tiles[i][5].getTerrain() == Terrain.NONE)
					return true;
			}
			return false;
		}
		if(quadrant == 3){
			//				return tiles[6][0].getHasRover() || tiles[6][0].getTerrain() == Terrain.ROCK || tiles[6][0].getTerrain() == Terrain.NONE;
			for(int i = 1; i <= 3; i++){
				if(tiles[5][i].getHasRover() || tiles[5][i].getTerrain() == Terrain.ROCK || tiles[5][i].getTerrain() == Terrain.NONE)
					return true;
			}

			for(int i = 1; i >= 3; i--){
				if(tiles[i][1].getHasRover() || tiles[i][1].getTerrain() == Terrain.ROCK || tiles[i][1].getTerrain() == Terrain.NONE)
					return true;
			}
			return false;
		}		
		if(quadrant == 2){
			//return tiles[0][6].getHasRover() || tiles[0][6].getTerrain() == Terrain.ROCK || tiles[0][6].getTerrain() == Terrain.NONE;
			for(int i = 1; i <= 3; i++){
				if(tiles[i][5].getHasRover() || tiles[i][5].getTerrain() == Terrain.ROCK || tiles[i][5].getTerrain() == Terrain.NONE)
					return true;
			}
			for(int i = 5; i >= 3; i--){
				if(tiles[1][i].getHasRover() || tiles[1][i].getTerrain() == Terrain.ROCK || tiles[1][i].getTerrain() == Terrain.NONE)
					return true;
			}
			return false;
		}		
		else if(quadrant == 1){
			//return tiles[0][0].getHasRover() || tiles[0][0].getTerrain() == Terrain.ROCK || tiles[0][0].getTerrain() == Terrain.NONE;
			for(int i = 1; i <= 3; i++){
				if(tiles[i][1].getHasRover() || tiles[i][1].getTerrain() == Terrain.ROCK || tiles[i][1].getTerrain() == Terrain.NONE)
					return true;
			}

			for(int i = 1; i <= 3; i++){
				if(tiles[1][i].getHasRover() || tiles[1][i].getTerrain() == Terrain.ROCK || tiles[1][i].getTerrain() == Terrain.NONE)
					return true;
			}
			return false;
		}

		return false;
	}

	/* This takes the LOC response string, parses out the x and y values and returns a Coord object */
	public static int[] extractLOC(String response) {
		String[] coordinates = response.split(" ");
		int[] coord = {Integer.parseInt(coordinates[1].trim()), Integer.parseInt(coordinates[2].trim())};
		return coord; 
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
		int[] target = extractLOC(in.readLine());

		/* Will add every location in the target location to the queue regardless of the terrain it is on*/
		for(int x = -3; x < 4; x ++)
			for (int y = -3; y < 4; y++)
				server.getQueue().addTask(
						new Task(
								"TARGET_LOCATION", 
								"UNKNOWN", 				/* UNKNOWN TERRAIN */
								"UNKNOWN",				/* UNKNOWN SCIENCE */
								new Coordinate(x + target[0],y + target[1], Coordinate.TYPE.ABSOLUTE)));
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