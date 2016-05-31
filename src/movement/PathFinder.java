package movement;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Map;
import java.util.Stack;

import common.Coord;
import common.MapTile;
import common.ScanMap;
import enums.Terrain;
import trackingUtility.Tracker;

public class PathFinder {

	private MapTile[][] tiles;
	private Tracker tracker;
	private boolean pathFound = false;
	private Stack<Coordinate> path; 
	public Coordinate start;
	public Coordinate end;

	public PathFinder(ScanMap map, Tracker tracker, Coord start, Coord end){
		System.out.println("----------------------------------NEW PATH---------------------------------- ");
		setMap(map);
		this.tracker = tracker;
		path = new Stack<Coordinate>();
		setStart(start);
		this.end = new Coordinate(end.xpos,end.ypos,Coordinate.TYPE.ABSOLUTE);
		System.out.println("START: " + this.start);
		System.out.println("END: " + this.end);
		//System.exit(0);
	}
	
	public void setMap(ScanMap map){
		tiles = map.getScanMap();
	}
	
	public void setStart(Coord start){
		pathFound = false;
		this.start = new Coordinate(start.xpos,start.ypos,Coordinate.TYPE.ABSOLUTE);
		// These are specific to our rover because they have no range extender
		this.start.setLocalX(3);
		this.start.setLocalY(3);
		tracker.lastVisited.add(this.start);
		System.out.println("START: " + this.start);
		System.out.println("END: " + this.end);
	}

	public ArrayList<String> generatePath(){
		if(tracker.targetInRange()){
			Coordinate curr = new Coordinate(tracker.getDistanceTracker().xpos + 3, tracker.getDistanceTracker().ypos + 3, Coordinate.TYPE.LOCAL);
			System.out.println("TARGET IS IN RANGE, AVAILABLE ROUTE? : " + !blocked(curr));
			if(blocked(curr)) return null;
			path.push(curr);
			setPath(curr);
		}
		if(!pathFound){
			int[] order = {3,2,4,5,1,6,0};
			if(tracker.getDistanceTracker().ypos == 0 && tracker.getDistanceTracker().xpos > 0){
				for(int i = tiles.length-1; i >= 0; i--){
					for(int j = 0; j < order.length; j++){
						if(pathFound) break;
						search(i,order[j]);
					}
					if(pathFound) break;
				}
			}
			else if(tracker.getDistanceTracker().xpos == 0 && tracker.getDistanceTracker().ypos < 0){
				for(int i = tiles.length-1; i >= 0; i--){
					for(int j = 0; j < order.length; j++){
						if(pathFound) break;
						search(order[j],i);
					}
					if(pathFound) break;
				}
			}
			else if(tracker.getDistanceTracker().ypos == 0 && tracker.getDistanceTracker().xpos < 0){
				for(int i = tiles.length-1; i >= 0; i--){
					for(int j = 0; j < order.length; j++){
						if(pathFound) break;
						search(i,order[j]);
					}
					if(pathFound) break;
				}
			}
			else if(tracker.getDistanceTracker().xpos == 0 && tracker.getDistanceTracker().ypos > 0){
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
				}
			}
			else if(start.compareTo(end) == 3){
				for(int i = tiles.length-2; i >= 1; i--){ 
					for(int j = 1; j < tiles.length - 1; j++){
						if(pathFound) break;
						search(i,j);
					}
					if(pathFound) break;
				}
			}
			else if(start.compareTo(end) == 2){
				for(int i = 1; i < tiles.length - 1 ; i++){ 
					for(int j = tiles.length-2; j >= 1; j--){
						if(pathFound) break;
						search(i,j);
					}
					if(pathFound) break;
				}
			}
			else if(start.compareTo(end) == 1){
				for(int i = 1; i < tiles.length - 1; i++){ 
					for(int j = 1; j < tiles.length - 1; j++){
						if(pathFound) break;
						search(i,j);
					}
					if(pathFound) break;
				}
			}
		}



		ArrayList<String> moves = new ArrayList<>();

		Coordinate old = start;  // middle
		Coordinate temp = null;
		while(!path.isEmpty()){
			temp = path.pop();
			moves.add(Direction.getCardinalDirection(temp.getLocalX() - old.getLocalX(),temp.getLocalY() - old.getLocalY()));
			old = temp;
		}

		return moves;
	}

	private void search(int localX, int localY){
		
		Coordinate curr = new Coordinate(localX,localY,Coordinate.TYPE.LOCAL);
		int[] offsetFromStart = start.getOffset(curr);
		curr.setAbsolute(offsetFromStart[0] + start.getAbsoluteX(),offsetFromStart[1] + start.getAbsoluteY());
		System.out.println(curr);
		//System.exit(0);
		path.clear();
		System.out.println("ABOVE COORDINATE IS BLOCKED? : " + (blocked(curr) && getNeighbors(curr.getLocalX(),curr.getLocalY()).size() > 1));
		if(blocked(curr) || curr.equals(start)) return;
		//System.exit(0);
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

	//	private Coord getOffset(Coord c){
	//		return new Coord(c.xpos - 3, c.ypos - 3);
	//	}


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
//
//		for(Coordinate c: neighbors)
//			System.out.println("NEIGHBOR: " + c);

		//System.exit(0);

		/* Makes current location into a Point2D object */
		Double closest = Double.POSITIVE_INFINITY; 

		Coordinate next = null;

		for(Coordinate tile: neighbors){
			//System.out.println("NEIGHBOR: " + tile);
			//Point2D pt = new Point2D.Double(tile.xpos, tile.ypos);

			Double distanceFromCurr = start.getDistance(tile, Coordinate.TYPE.LOCAL);
			if(distanceFromCurr < closest) {
				closest = distanceFromCurr;
				next = tile;
			}
		}

		path.push(next);
		System.out.println("NEXT MOVE: " + next);
		//System.exit(0);
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


	//	private void setPath2(Coord current){
	//		if(current == null){
	//			return;
	//		}
	//		else if((current.xpos == 4 && current.ypos == 3) || (current.xpos == 2 && current.ypos == 3) || (current.xpos == 3 && current.ypos == 4) || (current.xpos == 3 && current.ypos == 2)){
	//			pathFound = true;
	//			return;
	//		}
	//		else{
	//			setPath(nextBest2(current));
	//		}
	//	}
	//
	//	private Coord nextBest2(Coord current){
	//		ArrayList<Coord> neighbors = new ArrayList<>();
	//		neighbors.add(new Coord(current.xpos, current.ypos + 1));  // South 
	//		neighbors.add(new Coord(current.xpos, current.ypos - 1));  // North
	//		neighbors.add(new Coord(current.xpos + 1, current.ypos));  // East
	//		neighbors.add(new Coord(current.xpos - 1, current.ypos));  // West
	//
	//		for(int i = 0; i < neighbors.size(); i++){
	//			if(neighbors.get(i).xpos > 6 || neighbors.get(i).xpos < 0 ||neighbors.get(i).ypos > 6 ||neighbors.get(i).ypos < 0 || blocked2(neighbors.get(i)) || contains(neighbors.get(i))){
	//				neighbors.remove(neighbors.get(i));
	//				i--;
	//			}
	//		}
	//		//		System.out.println("NEIGBORS SIZE : " + neighbors.size() );
	//		//		if(neighbors.size() <= 1) // might be a hall way
	//		//			return null;
	//
	//
	//		/* Makes current location into a Point2D object */
	//		Point2D point = new Point2D.Double(3,3);
	//		Double closest = Double.POSITIVE_INFINITY; 
	//
	//
	//		Coord next = null;
	//
	//		for(Coord tile: neighbors){
	//			//System.out.println("NEIGHBOR: " + tile);
	//			Point2D pt = new Point2D.Double(tile.xpos, tile.ypos);
	//			Double distanceFromCurr = point.distance(pt);
	//			if(distanceFromCurr < closest) {
	//				closest = distanceFromCurr;
	//				next = tile;
	//			}
	//		}
	//
	//		System.out.println(next);
	//		path.push(next);
	//		return next;
	//	}

	private boolean inStack(Coordinate c){
		for(Coordinate curr: path)
			if(c.equals(curr))
				return true;
		return false;

	}

}
