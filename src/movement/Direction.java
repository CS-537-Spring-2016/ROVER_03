package movement;

public class Direction {
	
//	private String cardinal;
//	
//	public Direction(int xVelocity, int yVelocity){
//		cardinal = (xVelocity == 1)?"E":(xVelocity == -1)?"W":(yVelocity == 1)?"S":(yVelocity == -1)?"N":null;
//	}
	
	public static String getCardinalDirection(int xVelocity, int yVelocity){
		//cardinal = (xVelocity == 1)?"E":(xVelocity == -1)?"W":(yVelocity == 1)?"S":(yVelocity == -1)?"N":null;
		return (xVelocity == 1)?"E":(xVelocity == -1)?"W":(yVelocity == 1)?"S":(yVelocity == -1)?"N":null;
	}
}
