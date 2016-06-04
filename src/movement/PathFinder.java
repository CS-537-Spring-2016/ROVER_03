package movement;

import java.util.ArrayList;
import java.util.Stack;

import common.Coord;
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
 */
public class PathFinder {

	private MapTile[][] tiles;				/* 7 x 7 scan of rover */
	private Tracker tracker;				/* This will be a reference to the tracker class being used by the rover */
	private boolean pathFound = false;		/* Boolean used to know when to stop searching for a path */
	private Stack<Coordinate> path; 		/* This stack is used to store coordinate objects that are possible points in the path the rover will take*/
	public Coordinate start;
	public Coordinate end;

	/*---------------------------------------------------- CONSTRUCTOR ----------------------------------------------------*/
	
	/**
	 * Initializes Pathfinder object.
	 * @param tracker -reference to rover's tracker object.
	 * @see Stack
	 * @see Tracker
	 * @see Coordinate
	 */
	public PathFinder(Tracker tracker){
		this.tracker = tracker;
		path = new Stack<Coordinate>();
		end = tracker.getDestination();
	}
		
	private void initialize(ScanMap map){
		pathFound = false;
		tiles = map.getScanMap();
		start = tracker.getCurrentLocation();
		start.setLocal(3,3);
		tracker.lastVisited.add(start.clone());
		System.out.println("START: " + start);
		System.out.println("END: " + end);
	}

	public ArrayList<String> generatePath(ScanMap map){
		initialize(map);
		if(tracker.targetInRange()){
			System.out.println("TARGET IS IN RANGE");
			Coordinate curr = new Coordinate(tracker.getXDistance() + start.getLocalX(), tracker.getYDistance() + start.getLocalX(), Coordinate.TYPE.LOCAL);
			if(blocked(curr)) return null;   /* If it is blocked it is on a tile we cannot access */
			path.push(curr);
			setPath(curr);
		}
		if(!pathFound){
			int[] order = {3,2,4,5,1,6,0};
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
			else if (start.compareTo(end) == 4){
				//System.exit(0);
				for(int i = tiles.length-2; i >= 1; i--){ // Give it some space to see so we start at 2 not 3
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



		ArrayList<String> moves = new ArrayList<>();
		if(moves.size() != 0 && moves.get(moves.size()-1) == null){
			tracker.lastVisited.clear();
			generatePath(map);
		}
		
		Coordinate old = start;
		Coordinate temp = null;
		while(!path.isEmpty()){
			temp = path.pop();
			moves.add(Direction.getCardinalDirection(temp.getLocalX() - old.getLocalX(),temp.getLocalY() - old.getLocalY()));
			old = temp;
			System.out.println("NEXT MOVE: " + old);
		}

		return moves;
	}

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
	
	private boolean blocked(Coordinate coord){
		return tiles[coord.getLocalX()][coord.getLocalY()].getHasRover() 
				|| tiles[coord.getLocalX()][coord.getLocalY()].getTerrain() == Terrain.ROCK
				|| tiles[coord.getLocalX()][coord.getLocalY()].getTerrain() == Terrain.NONE
				|| inStack(coord)
				|| tracker.hasVisited(coord);
	}


	private void setPath(Coordinate current){
		if(current == null){
			return;
		}

		int x = current.getLocalX();
		int y = current.getLocalY();
		if((x == 4 && y == 3) || (x == 2 && y == 3) || (x == 3 && y == 4) || (x == 3 && y == 2)){
			pathFound = true;
			return;
		}

		setPath(nextBest(current));
	}

	private Coordinate nextBest(Coordinate current){
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

		path.push(next);
		// System.out.println("NEXT MOVE: " + next);
		return next;
	}


	private ArrayList<Coordinate> getNeighbors(int x , int y){
		ArrayList<Coordinate> neighbors = new ArrayList<>();

		neighbors.add(new Coordinate(x, y + 1 ,Coordinate.TYPE.LOCAL));  // South 
		neighbors.add(new Coordinate(x, y - 1,Coordinate.TYPE.LOCAL));  // North
		neighbors.add(new Coordinate(x + 1, y,Coordinate.TYPE.LOCAL));  // East
		neighbors.add(new Coordinate(x - 1, y,Coordinate.TYPE.LOCAL));  // West

		for(Coordinate c: neighbors){
			int[] offsetFromStart = start.getOffset(c);
			c.setAbsolute(offsetFromStart[0] + start.getAbsoluteX(),offsetFromStart[1] + start.getAbsoluteY());
		}
		
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

	private boolean inStack(Coordinate c){
		for(Coordinate curr: path)
			if(c.equals(curr))
				return true;
		return false;

	}

}
