package trackingUtility;

import java.util.ArrayList;
import java.util.Stack;

import common.Coord;
import movement.Coordinate;

public class Tracker {

	// Will have location coordinate and direction rover went in a string  
	private Coord currentLocation;
	private Stack<Coord> markers;
	
	private Coord startingPoint;
	private Coord destination;
	public Coord startedFrom;

	private Coord lastSuccessfulMove;
	// Keeps track of how may tiles are left to go
	private Coord distanceTracker;
	public ArrayList<Coordinate> lastVisited;
	

	public Tracker(){
		distanceTracker = new Coord(0,0);
		currentLocation = new Coord(0,0);
		markers = new Stack<>();
		lastVisited = new ArrayList<>();
	}
	
	public boolean hasVisited(Coordinate c){
		for(Coordinate t: lastVisited)
			if(c.equals(t))
				return true;
		return false;
	}

	public void setCurrentLocation(Coord currentLocation){
		this.currentLocation = currentLocation;
	}

	public Coord getCurrentLocation(){
		return currentLocation;
	}

	// It just peeks the last point where rover got stuck
	public Coord peekMarker(){
		return markers.peek();
	}

	public void updateDistanceTracker(){
		updateXPos(lastSuccessfulMove.xpos - currentLocation.xpos);
		updateYPos(lastSuccessfulMove.ypos - currentLocation.ypos);
	}

	public Coord removeMarker(){
		return markers.pop();
	}

	public void addMarker(Coord marker){
		markers.add(marker);
	}

	public Coord getStartingPoint() {
		return startingPoint;
	}

	public void setStartingPoint(Coord startingPoint) {
		this.startingPoint = startingPoint;
	}

	public Coord getDestination() {
		return destination;
	}

	public void setDestination(Coord destination) {
		this.destination = destination;
	}

	public Coord getDistanceTracker() {
		return distanceTracker;
	}

	public void setDistanceTracker() {
		distanceTracker.xpos = destination.xpos - startingPoint.xpos;
		distanceTracker.ypos = destination.ypos - startingPoint.ypos;
	}

	public void updateXPos(int x){
		distanceTracker.xpos += x; 
	}

	public void updateYPos(int y){
		distanceTracker.ypos += y; 
	}

	public boolean hasArrived(){
		return distanceTracker.xpos == 0 && distanceTracker.ypos == 0;
	}

	public void setLastSuccessfulMove(Coord location){
		lastSuccessfulMove = location;
	}

	public boolean targetInRange(){
		
		return (distanceTracker.xpos >= -3 && distanceTracker.xpos <= 3) && (distanceTracker.ypos >= -3 && distanceTracker.ypos <= 3);

	}
}
