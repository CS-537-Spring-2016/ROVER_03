package movement;

import java.util.ArrayList;
import java.util.Stack;

import common.MapTile;
import common.ScanMap;
import enums.Terrain;
import trackingUtility.Tracker;

/**
 * Pathfinder is a class that uses various methods to facilitate the movement of the rover
 * and provide the shortest path to the destination. Pathfinder is instantiated once per mission. Each path
 * that is created by the generate path method is a max of 4 moves, the generate path should be called again for
 * the next 4 moves.
 * @author Carlos Galdamez
 * @see Stack
 * @see Tracker
 * @see Coordinate
 */
public class PathFinder {

	private MapTile[][] tiles;				/* 7 x 7 scan of rover in this case */
	private Tracker tracker;				/* This will be a reference to the tracker class being used by the rover */
	private boolean pathFound = false;		/* Boolean used to know when to stop searching for a path */
	private Stack<Coordinate> path; 		/* This stack is used to store coordinate objects that are possible points in the path the rover will take*/
	public Coordinate start;
	public Coordinate end;

	/*----------------------------------------------------------- CONSTRUCTOR ------------------------------------------------------*/
	
	/**
	 * Initializes path finder object.
	 * @param tracker -reference to rover's tracker object.
	 */
	public PathFinder(Tracker tracker){
		this.tracker = tracker;
		path = new Stack<Coordinate>();
		end = tracker.getDestination();
	}

	/*---------------------------------------------------------- PUBLIC METHODS ----------------------------------------------------*/
	
	/**
	 * Uses private methods to generate most efficient path to destination
	 * @param map - scan map object that represents current area scan
	 * @return array list of cardinal directions
	 */
	public ArrayList<String> generatePath(ScanMap map){
		initialize(map);								/* Always need to initialize in order to reset everything in the class to create a new path */
		if(tracker.targetInRange()){					/* Only executes if target is within the scan map */
			System.out.println("TARGET IS IN RANGE");
			Coordinate curr = new Coordinate(tracker.getXDistance() + start.getLocalX(), tracker.getYDistance() + start.getLocalX(), Coordinate.TYPE.LOCAL);
			if(blocked(curr)) return null;   /* If it is blocked it is on a tile we cannot access */
			path.push(curr);
			setPath(curr);
		}
		
		/* If not path was found from executing the code above then try the following */
		if(!pathFound){
			/* Lines 62 to 99 (everything inside the following 4 if and if else statements) are executed when 
			 * the rover is a distance a distance or -2,-1,0,1, or 2 for either x or y. The reason we have these
			 * is that the rover usually goes in a zigzag motion when it is not blocked. But we do not want it to do that if the
			 * destination is straight ahead. It is more efficient to tell it just to go straight instead of
			 * in a zigzag motion. */
			int[] order = {3,2,4,5,1,6,0}; /* This is the order in which the path finder will start searching for a path */
			
			/* It is very helpful to do a trace of how the following loops work. All of then are traversing 
			 * through the 7 X 7 array but in  a different fashion starting at different points in the array.
			 * I tried to refactor this part and make it simpler but it is not possible without some kind of 
			 * functional programming language and high order functions */
			if(tracker.getYDistance() >= -2 && tracker.getYDistance() <= 2 && tracker.getXDistance() > 0){
				for(int i = tiles.length-1; i >= 0; i--){
					for(int j = 0; j < order.length; j++){
						if(pathFound) break;
						search(i,order[j]);
					}
					if(pathFound) break;
				}
			}
			else if(tracker.getXDistance() >=-2 && tracker.getXDistance() <= 2 && tracker.getYDistance() < 0){
				for(int i = 0; i < tiles.length; i++){
					for(int j = 0; j < order.length; j++){
						if(pathFound) break;
						search(order[j],i);
					}
					if(pathFound) break;
				}
			}
			else if(tracker.getYDistance() >=-2 && tracker.getYDistance() <= 2 && tracker.getXDistance() < 0){
				for(int i = 0; i < tiles.length; i++){
					for(int j = 0; j < order.length; j++){
						if(pathFound) break;
						search(i,order[j]);
					}
					if(pathFound) break;
				}
			}
			else if(tracker.getXDistance() >=-2 && tracker.getXDistance() <= 2 && tracker.getYDistance() > 0){
				for(int i = tiles.length-1; i >= 0; i--){
					for(int j = 0; j < order.length; j++){
						if(pathFound) break;
						search(order[j],i);
					}
					if(pathFound) break;
				}
			}
			/* For the following statement we first have to check what quadrant the destination is at.
			 * Based of which quadrant it is at we will deside what tile in the 7 X 7 array we should start looking
			 * for a path to. All of the following if statements should generate a 4 move path. It is possible to
			 * make a 6 move path from each however it is safer to do a 4 move path in order to check if the tile
			 * that you are creating a path to is in a hallway. If so, we can just ignore that tile and choose 
			 * a different one to generate a path to */
			else if (start.compareTo(end) == 4){
				//System.exit(0);
				for(int i = tiles.length-2; i >= 1; i--){
					for(int j = tiles.length-2; j >= 1; j--){
						if(pathFound) break;
						search(i,j);
					}
					if(pathFound) break;
					if(i == 1) tracker.lastVisited.clear(); // means rover doesnt know where to go
				}
			}
			else if(start.compareTo(end) == 3){
				for(int i = tiles.length - 2; i >= 1; i--){ 
					for(int j = 1; j < tiles.length - 1; j++){
						if(pathFound) break;
						search(i,j);
					}
					if(pathFound) break;
					if(i == 1) tracker.lastVisited.clear(); // means rover doesnt know where to go

				}
			}
			else if(start.compareTo(end) == 2){
				for(int i = 1 ; i < tiles.length - 1 ; i++){ 
					for(int j = tiles.length - 2; j >= 1; j--){
						if(pathFound) break;
						search(i,j);
					}
					if(pathFound) break;
					if(i == 5) tracker.lastVisited.clear(); // means rover doesnt know where to go

				}
			}
			else if(start.compareTo(end) == 1){
				for(int i = 1; i < tiles.length - 1; i++){ 
					for(int j = 1; j < tiles.length - 1; j++){
						if(pathFound) break;
						search(i,j);
					}
					if(pathFound) break;
					if(i == 5) tracker.lastVisited.clear(); // means rover doesnt know where to go
				}				
			}
		}

		/* Need to check if path stack contains null if it does then it means that generator
		 * could not fins any other moves to make, so we can just reset everything and try
		 * to generate a path again */
		if(path.contains(null)){										
			tracker.lastVisited.clear();
			generatePath(map);
		}
		
		/* ArrayList of cardinal directions */
		ArrayList<String> moves = new ArrayList<>();
		Coordinate old = start;
		Coordinate temp = null;
		while(!path.isEmpty()){
			temp = path.pop();
			moves.add(Direction.getCardinalDirection(temp.getLocalX() - old.getLocalX(),temp.getLocalY() - old.getLocalY())); /* Use Direction class to get cardinal direction */
			old = temp;
			System.out.println("NEXT MOVE: " + old);
		}

		return moves;
	}

	/*---------------------------------------------------------------PRIVATE METHODS -------------------------------------------------------*/
	
	/**
	 * Used to reset path finder in order to genereate a new path from a new starting point
	 * @param map reference to current scan map
	 */
	private void initialize(ScanMap map){
		pathFound = false;
		tiles = map.getScanMap();
		start = tracker.getCurrentLocation();
		start.setLocal(3,3);
		tracker.lastVisited.add(start.clone());
		System.out.println("START: " + start);
		System.out.println("END: " + end);
	}
	
	/**
	 * Used to set up and start recursive serach for path from a certain point, which
	 * will be set by the loops in the generate path method.
	 * @param localX - local x position
	 * @param localY - local y position
	 */
	private void search(int localX, int localY){
		System.out.println("VISITED: " + tracker.lastVisited.toString());
		Coordinate curr = new Coordinate(localX,localY,Coordinate.TYPE.LOCAL);
		int[] offsetFromStart = start.getOffset(curr);
		curr.setAbsolute(offsetFromStart[0] + start.getAbsoluteX(),offsetFromStart[1] + start.getAbsoluteY());
		System.out.println(curr);
		path.clear();
		System.out.println("ABOVE COORDINATE IS BLOCKED? : " + (blocked(curr) && getNeighbors(curr.getLocalX(),curr.getLocalY()).size() > 1));
		if(blocked(curr) || curr.equals(start)) return;
		path.push(curr);
		setPath(curr);
	}
	
	/**
	 * Recursive method that keeps trying to find a path to the center of the local
	 * scan from the current coordinate. Recursive calls end when it decides that
	 * there is no path from the current coordinate to the center of local scan or when
	 * it finds a path. 
	 * @param current - coordinate object
	 */
	private void setPath(Coordinate current){
		/* If current is null then no path was found */
		if(current == null){
			return;
		}
		
		/* You know a path is found if the following cases are true. The cases may change depending on whether you scan
		 * is larger or smaller. Since my scan is 7 x 7 then the center of my scan map is (3,3) so if current
		 * is a coordinate one away in any direction then you know a path has been found */
		int x = current.getLocalX();
		int y = current.getLocalY();
		if((x == 4 && y == 3) || (x == 2 && y == 3) || (x == 3 && y == 4) || (x == 3 && y == 2)){
			pathFound = true;
			return;
		}

		/* Recursive call */
		setPath(nextBest(current));
	}
	
	/**
	 * Decides what the next best move is
	 * @param current - coordinate object
	 * @return new coordinate object
	 */
	private Coordinate nextBest(Coordinate current){
		/* Array list of coordinates, which are the neighbors of the current coordinate. There should not be
		 * more than 4 neighbors.*/
		ArrayList<Coordinate> neighbors = getNeighbors(current.getLocalX(),current.getLocalY());
		Double closest = Double.POSITIVE_INFINITY; 

		Coordinate next = null;

		for(Coordinate tile: neighbors){
			Double distanceFromCurr = start.getDistance(tile, Coordinate.TYPE.LOCAL);
			if(distanceFromCurr < closest) {
				closest = distanceFromCurr;
				next = tile;
			}
		}

		/*The coordinate that is pushed to the stack is the neighbor of the current
		 * coordinate that is closest to the center of scan map which in this case is (3,3)*/
		path.push(next);
		return next;
	}
	
	/**
	 * Creates an array list of all the neighbors relative to an x and y position
	 * @param x - x position
	 * @param y - y position
	 * @return list of neighbors which are coordinate objects
	 */
	private ArrayList<Coordinate> getNeighbors(int x , int y){
		ArrayList<Coordinate> neighbors = new ArrayList<>();

		neighbors.add(new Coordinate(x, y + 1 ,Coordinate.TYPE.LOCAL));  // South 
		neighbors.add(new Coordinate(x, y - 1,Coordinate.TYPE.LOCAL));  // North
		neighbors.add(new Coordinate(x + 1, y,Coordinate.TYPE.LOCAL));  // East
		neighbors.add(new Coordinate(x - 1, y,Coordinate.TYPE.LOCAL));  // West

		/* Used to set absolute x and y positions of neighbors */
		for(Coordinate c: neighbors){
			int[] offsetFromStart = start.getOffset(c);
			c.setAbsolute(offsetFromStart[0] + start.getAbsoluteX(),offsetFromStart[1] + start.getAbsoluteY());
		}
		
		/* Filters through the list and removes neighbors that cannot be a possible next move either because they are outside
		 * the bounds of the scan map , are on terrain rover can not traverse, is already part of the stack or has already previously visited. */
		for(int i = 0; i < neighbors.size(); i++){
			Coordinate curr = neighbors.get(i);
			int lX = curr.getLocalX();
			int lY = curr.getLocalY();
			if(lX > 6 || lX < 0 || lY > 6 || lY < 0 || blocked(neighbors.get(i))){
				neighbors.remove(neighbors.get(i));
				i--;
			}
		}

		return neighbors;
	}
	
	/**
	 * Checks if a coordinate is blocked
	 * @param coord coordinate object
	 * @return true or false
	 */
	private boolean blocked(Coordinate coord){
		return tiles[coord.getLocalX()][coord.getLocalY()].getHasRover() 
				|| tiles[coord.getLocalX()][coord.getLocalY()].getTerrain() == Terrain.ROCK
				|| tiles[coord.getLocalX()][coord.getLocalY()].getTerrain() == Terrain.NONE
				|| inStack(coord)
				|| tracker.hasVisited(coord);
	}

	/**
	 * Checks if a coordinate is already in the path stack
	 * @param c coordinate object
	 * @return true or false
	 */
	private boolean inStack(Coordinate c){
		for(Coordinate curr: path)
			if(c.equals(curr))
				return true;
		return false;

	}

}
