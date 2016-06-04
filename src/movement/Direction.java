package movement;

/**
 * Direction class has only one static method that takes in a velocity in the x direction and
 * a velocity in the y direction. The values for these velocities can be either -1, 1, or 0. 
 * NOTE: If x velocity is 1 or -1 then the velocity in the y direction has to be 0 and vise versa.
 * @author Carlos Galdamez
 * */
public class Direction {
	
	/**
	 * Generates a cardinal direction based on the velocity of the rover in the x direction and y direction.
	 * @param xVelocity	- velocity in the x direction, can be -1 ,1 or 0.
	 * @param yVelocity - velocity in the y direction, can be -1 ,1 or 0.
	 * @return Cardinal direction representation of the velocity that was entered as an argument.
	 */
	public static String getCardinalDirection(int xVelocity, int yVelocity){
		return (xVelocity == 1)?"E":(xVelocity == -1)?"W":(yVelocity == 1)?"S":(yVelocity == -1)?"N":null;
	}
}
