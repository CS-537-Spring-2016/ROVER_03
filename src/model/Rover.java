package model;

import java.util.ArrayList;

/* Rover class different from Rover class that the swarm server uses */
public class Rover {
	
	private String name;
	private String driveType;
	private ArrayList<String> tools;
	
	/*----------------------------------------------- CONSTRUCTOR -------------------------------------------------------*/

	public Rover(String name){
		this.name = name;
		tools = new ArrayList<>();
	}
	
	/* ----------------------------------------------------- GETTERS ----------------------------------------------------*/

	public String getName() {
		return name;
	}

	public String getDriveType() {
		return driveType;
	}
	
	public ArrayList<String> getTools() {
		return tools;
	}

	/*---------------------------------------------------- SETTERS ------------------------------------------------------*/

	public void setName(String name) {
		this.name = name;
	}
	
	public void setDriveType(String driveType) {
		this.driveType = driveType;
	}
	
	public void setTool(String tool){
		tools.add(tool);
	}
	
	// Checks if rover has a certain tool
	public boolean hasTool(String toolType){
		return tools.contains(toolType);
	}
	
	// Checks if rover has certain drive type
	public boolean hasDriveType(String driveType){
		return driveType.equals(this.driveType);
	}
	
	/*---------------------------------------------------- PRINT METHOD ------------------------------------------------------*/
	
	public String toString(){
		return "ROVER NAME : " + name + "\nTOOL 1 : " + tools.get(0) + "\nTOOL 2 : " + tools.get(1) + "\nDRIVE TYPE: " + driveType;
	}
}
