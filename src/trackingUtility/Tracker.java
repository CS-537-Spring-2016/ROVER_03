package trackingUtility;

import java.util.ArrayList;
import java.util.Stack;

import common.Coord;
import movement.Coordinate;

public class Tracker {

	// Will have location coordinate and direction rover went in a string  
	private Coordinate currentLocation;
	
	private Coordinate origin;
	private Coordinate destination;

	// Keeps track of how may tiles are left to go
	private int xDistance;
	private int yDistance;
	
	public ArrayList<Coordinate> lastVisited;
	
	public Tracker(){
		currentLocation = new Coordinate(0,0,Coordinate.TYPE.ABSOLUTE);
		origin = new Coordinate(0,0,Coordinate.TYPE.ABSOLUTE);
		destination = new Coordinate(0,0,Coordinate.TYPE.ABSOLUTE);
		lastVisited = new ArrayList<>();
	}
	
	/*--------------------------------------------------- SETTERS ------------------------------------------------------*/

	public void setCurrentLocation(int xPos, int yPos){
		currentLocation.setAbsolute(xPos, yPos);
	}
	
	public void setOrigin(int xPos, int yPos) {
		origin.setAbsolute(xPos, yPos);
	}

	public void setDestination(int xPos, int yPos) {
		destination.setAbsolute(xPos, yPos);
	}
	
	public void setDistance(){
		xDistance = destination.getAbsoluteX() - origin.getAbsoluteX();
		yDistance = destination.getAbsoluteY() - origin.getAbsoluteY();
	}
	
	/*--------------------------------------------------- GETTERS ------------------------------------------------------*/
	
	public Coordinate getCurrentLocation(){
		return currentLocation;
	}
	
	public Coordinate getOrigin() {
		return origin;
	}
	
	public Coordinate getDestination() {
		return destination;
	}
	
	public int getXDistance(){
		return xDistance;
	}
	
	public int getYDistance(){
		return yDistance;
	}

	public void updateXDistance(int x){
		xDistance += x; 
	}

	public void updateYDistance(int y){
		yDistance += y; 
	}

	/*--------------------------------------------------- BOOLEAN METHODS ----------------------------------------------*/
	
	public boolean hasArrived(){
		return xDistance == 0 && yDistance == 0;
	}

	public boolean hasVisited(Coordinate c){
		for(Coordinate t: lastVisited)
			if(c.equals(t))
				return true;
		return false;
	}

	public boolean targetInRange(){
		return (xDistance >= -3 && xDistance <= 3) && (yDistance >= -3 && yDistance <= 3);
	}
}
