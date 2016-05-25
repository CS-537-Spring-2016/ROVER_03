package trackingUtility;

import java.util.Stack;

import common.Coord;

public class Tracker {

	// Will have location coordinate and direction rover went in a string  
	private Coord currentLocation;
	private Stack<State> markers;
	
	private Coord startingPoint;
	private Coord destination;

	private Coord lastSuccessfulMove;
	// Keeps track of how may tiles are left to go
	private Coord distanceTracker;

	public Tracker(){
		distanceTracker = new Coord(0,0);
		currentLocation = new Coord(0,0);
		markers = new Stack<>();
	}

	public void setCurrentLocation(Coord currentLocation){
		this.currentLocation = currentLocation;
	}

	public Coord getCurrentLocation(){
		return currentLocation;
	}

	// It just peeks the last point where rover got stuck
	public State peekMarker(){
		return markers.peek();
	}

	public void updateDistanceTracker(){
		updateXPos(lastSuccessfulMove.xpos - currentLocation.xpos);
		updateYPos(lastSuccessfulMove.ypos - currentLocation.ypos);
	}

	public State removeMarker(){
		return markers.pop();
	}

	public void addMarker(State marker){
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
		return currentLocation.xpos <= (destination.xpos + 3) && 
				currentLocation.xpos >= (destination.xpos - 3) && 
				currentLocation.ypos <= (destination.ypos + 3) && 
				currentLocation.ypos >= (destination.ypos -3); 
	}
}
