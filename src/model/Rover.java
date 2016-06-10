package model;

import java.util.ArrayList;

/**
 * Rover class hold all properties of a rover such as it's tools and
 * drive type.
 * @author Carlos Galdamez
 */
public class Rover {
	
	private String name;
	private String driveType;
	private ArrayList<String> tools;
	
	/*----------------------------------------------- CONSTRUCTOR -------------------------------------------------------*/

	/**
	 * Constructor sets the name of the rover and instantiates the tool array list
	 * @param name - name of rover
	 * @see ArrayList
	 * @see String
	 */
	public Rover(String name){
		this.name = name;
		tools = new ArrayList<>();
	}
	
	/* ----------------------------------------------------- GETTERS ----------------------------------------------------*/

	/**
	 * Gets the name property of the rover
	 * @return string representation of rover name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the drive type property of the rover
	 * @return string representation of rover drive type
	 */
	public String getDriveType() {
		return driveType;
	}
	
	/**
	 * Get the list of tools that the rover has
	 * @return array list of tools
	 */
	public ArrayList<String> getTools() {
		return tools;
	}

	/*---------------------------------------------------- SETTERS ------------------------------------------------------*/

	/**
	 * Sets the name of the rover
	 * @param name - name of rover
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Sets the drive type of the rover
	 * @param driveType -  drive type of rover
	 */
	public void setDriveType(String driveType) {
		this.driveType = driveType;
	}
	
	/**
	 * Adds a specified tool to the array list of tools
	 * @param tool - rover tool that needs to be added to the tools list
	 */
	public void setTool(String tool){
		tools.add(tool);
	}
	
	/**
	 * Checks if rover has the specified tool
	 * @param toolType - tool the user wants to verify the rover has
	 * @return true or false
	 */
	public boolean hasTool(String toolType){
		return tools.contains(toolType);
	}
	
	/**
	 * Checks if rover has the specified drive type
	 * @param driveType - drive type the user wants to verify the rover has
	 * @return true or false
	 */
	public boolean hasDriveType(String driveType){
		return driveType.equals(this.driveType);
	}
	
	/*---------------------------------------------------- PRINT METHOD ------------------------------------------------------*/
	
	public String toString(){
		return "ROVER NAME : " + name + "\nTOOL 1 : " + tools.get(0) + "\nTOOL 2 : " + tools.get(1) + "\nDRIVE TYPE: " + driveType;
	}
}
