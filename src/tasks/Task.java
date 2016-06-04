package tasks;

import movement.Coordinate;

public class Task {
	
	/* Name of rover that the task was received from */
	private String recvFrom;
	private String terrain;
	private String scienceType;
	private Coordinate destination;
	
	/*----------------------------------------------- CONSTRUCTOR -------------------------------------------------------*/
	
	public Task(String roverName, String terrain, String scienceType, Coordinate destination){
		this.recvFrom = roverName;
		this.terrain = terrain;
		this.scienceType = scienceType;
		this.destination = destination;
	}
	
	
	/*--------------------------------------------------- GETTERS -------------------------------------------------------*/
	
	/* Returns name of rover that task was received from */
	public String getRoverName(){
		return recvFrom;
	}
	
	/* Returns the type of terrain the destination is on */
	public String getTerrain(){
		return terrain;
	}
	
	/* Returns the type of science that will be collected at the destination */
	public String getScienceType(){
		return scienceType;
	}
	
	/* Returns the Coord representation of the destination */
	public Coordinate getDestination(){
		return destination;
	}
	
	public String toString(){
		return terrain + " " + scienceType + " " + destination.getAbsoluteX() + " " + destination.getAbsoluteY();
	}
}
