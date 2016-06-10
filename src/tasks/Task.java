package tasks;

import movement.Coordinate;

/**
 * Task class contains information regarding a task a rover has to fulfill.
 * Rover can set this task or it can also be tasks sent by other rovers.
 * @author Carlos Galdamez
 */
public class Task {
	
	private String recvFrom;
	private String terrain;
	private String scienceType;
	private Coordinate destination;
	
	/*----------------------------------------------- CONSTRUCTOR -------------------------------------------------------*/
	
	/**
	 * Constructor instantiates a task and sets values of task to the values specified in the arguments
	 * @param roverName
	 * @param terrain
	 * @param scienceType
	 * @param destination
	 */
	public Task(String roverName, String terrain, String scienceType, Coordinate destination){
		this.recvFrom = roverName;
		this.terrain = terrain;
		this.scienceType = scienceType;
		this.destination = destination;
	}
	
	/*--------------------------------------------------- GETTERS -------------------------------------------------------*/
	
	/**
	 * Gets the rover name that sent this task
	 * @return name of rover that task was received from
	 */
	public String getRoverName(){
		return recvFrom;
	}

	/**
	 * Gets the terrain that the destination is on
	 * @return string representation of the terrain the destination is on
	 */
	public String getTerrain(){
		return terrain;
	}
	
	/**
	 * Gets type of science at destination
	 * @return string representation of the type of science at the destination
	 */
	public String getScienceType(){
		return scienceType;
	}
	
	/**
	 * Gets destination coordinate object
	 * @return coordinate object of destination
	 */
	public Coordinate getDestination(){
		return destination;
	}
	
	/*-------------------------------------------------------- TO STRING METHOD -------------------------------------------*/
	
	public String toString(){
		return terrain + " " + scienceType + " " + destination.getAbsoluteX() + " " + destination.getAbsoluteY();
	}
}
