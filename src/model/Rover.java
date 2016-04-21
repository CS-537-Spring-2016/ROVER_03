package model;

import java.util.ArrayList;

/* Rover class different from Rover class that the server uses */
public class Rover {
	
	private String name;
	private String driveType;
	private ArrayList<String> tools;
	
	public Rover(String name){
		this.name = name;
		tools = new ArrayList<>();
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getDriveType() {
		return driveType;
	}

	public void setDriveType(String driveType) {
		this.driveType = driveType;
	}
	
	public ArrayList<String> getTools() {
		return tools;
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
}
