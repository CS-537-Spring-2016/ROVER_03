package trackingUtility;

import java.util.ArrayList;

import movement.Coordinate;

/**
 * Tracker keeps track of rover position through completion of a task
 * @author Carlos Galdamez
 * @see Coordinate
 */
public class Tracker {

	private Coordinate currentLocation;
	
	private Coordinate origin;
	private Coordinate destination;

	// Keeps track of how may tiles are left to go
	private int xDistance;
	private int yDistance;
	
	public ArrayList<Coordinate> lastVisited;
	
	/*---------------------------------------------- CONSTRUCTOR ----------------------------------------------------*/
	
	/**
	 * Constructor instantiates coordinates that will be used and array list of vissited coordinates.
	 */
	public Tracker(){
		currentLocation = new Coordinate(0,0,Coordinate.TYPE.ABSOLUTE);
		origin = new Coordinate(0,0,Coordinate.TYPE.ABSOLUTE);
		destination = new Coordinate(0,0,Coordinate.TYPE.ABSOLUTE);
		lastVisited = new ArrayList<>();
	}
	
	/*--------------------------------------------------- SETTERS ------------------------------------------------------*/

	/**
	 * Sets absolute position of current location
	 * @param xPos - x position
	 * @param yPos - y position
	 */
	public void setCurrentLocation(int xPos, int yPos){
		currentLocation.setAbsolute(xPos, yPos);
	}
	
	/**
	 * Sets absolute position of origin location at beginning of the mission
	 * @param xPos - x position
	 * @param yPos - y position
	 */
	public void setOrigin(int xPos, int yPos) {
		origin.setAbsolute(xPos, yPos);
	}

	/**
	 * Sets absolute position of destination location
	 * @param xPos - x position
	 * @param yPos - y position
	 */
	public void setDestination(int xPos, int yPos) {
		destination.setAbsolute(xPos, yPos);
	}
	
	/**
	 * Sets distance from origin to destination
	 */
	public void setDistance(){
		xDistance = destination.getAbsoluteX() - origin.getAbsoluteX();
		yDistance = destination.getAbsoluteY() - origin.getAbsoluteY();
	}
	
	/*--------------------------------------------------- GETTERS ------------------------------------------------------*/
	
	/**
	 * Gets current location coordinate object
	 * @return coordinate object
	 */
	public Coordinate getCurrentLocation(){
		return currentLocation;
	}
	
	/**
	 * Gets origin location coordinate object
	 * @return coordinate object
	 */
	public Coordinate getOrigin() {
		return origin;
	}
	
	/**
	 * Gets destination location coordinate object
	 * @return coordinate object
	 */
	public Coordinate getDestination() {
		return destination;
	}
	
	/**
	 * Gets distance left in the x direction
	 * @return integer representing distance left
	 */
	public int getXDistance(){
		return xDistance;
	}
	
	/**
	 * Gets distance left in the y direction
	 * @return integer representing distance left
	 */
	public int getYDistance(){
		return yDistance;
	}

	/**
	 * Updates distance left in the x direction
	 * @param x - amount to update distance by
	 */
	public void updateXDistance(int x){
		xDistance += x; 
	}

	/**
	 * Updates distance left in the y direction
	 * @param y - amount to update distance by
	 */
	public void updateYDistance(int y){
		yDistance += y; 
	}

	/*--------------------------------------------------- BOOLEAN METHODS ----------------------------------------------*/
	
	/**
	 * Checks if rover has arrived to its destination
	 * @return true or false
	 */
	public boolean hasArrived(){
		return xDistance == 0 && yDistance == 0;
	}

	/**
	 * Checks if rover has visited a certain coordinate
	 * @param c coordinate object user wishes to check if the rover has visited
	 * @return true or false
	 */
	public boolean hasVisited(Coordinate c){
		for(Coordinate t: lastVisited)
			if(c.equals(t))
				return true;
		return false;
	}

	/**
	 * Checks if destination is in the local scan, meaning that it is in range. Values being
	 * check can change depending on how big your scan map is. This is for a 7 x 7 scan map.
	 * @return
	 */
	public boolean targetInRange(){
		return (xDistance >= -3 && xDistance <= 3) && (yDistance >= -3 && yDistance <= 3);
	}
}
