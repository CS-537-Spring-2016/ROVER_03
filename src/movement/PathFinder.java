package movement;

import java.awt.geom.Point2D;
import java.util.ArrayList;
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
	private Stack<Coord> path; 

	public PathFinder(ScanMap map, Tracker tracker){
		System.out.println("----------------------------------NEW PATH---------------------------------- ");
		tiles = map.getScanMap();
		this.tracker = tracker;
		path = new Stack<Coord>();
		System.out.println("LOOK HERE: " + map.getScanMap().length);
	}

	public ArrayList<Coord> generatePath(){


		//if (tracker.getDistanceTracker().xpos > 0 && tracker.getDistanceTracker().ypos > 0){
			if(tracker.targetInRange()){
				System.out.println(tracker.getDistanceTracker());
				Coord curr = new Coord(tracker.getDistanceTracker().xpos + 3, tracker.getDistanceTracker().ypos + 3);
				path.push(curr);
				setPath(curr);
			}else{
				for(int i = tiles.length-1; i >= 0; i--){
					path.clear();
					for(int y = tiles.length-1; y >= 0; y--){
						if(pathFound){
							break;
						}
						path.clear();
						Coord curr = new Coord(i,y);
						System.out.println("COORDINATE: " + curr + "is blocked: " + blocked2(curr));
						if(blocked2(curr))continue;
						path.push(curr);
						setPath(curr);
					}
					if(pathFound){
						break;
					}
				}
			}
		//}

		ArrayList<Coord> moves = new ArrayList<>();

		Coord old = new Coord(3,3);  // middle
		Coord temp = null;
		while(!path.isEmpty()){
			temp = path.pop();
			moves.add(new Coord(temp.xpos - old.xpos,temp.ypos - old.ypos));
			old = temp;
		}

		return moves;
	}

	private boolean blocked2(Coord indexes){
		return tiles[indexes.xpos][indexes.ypos].getHasRover() 
				|| tiles[indexes.xpos][indexes.ypos].getTerrain() == Terrain.ROCK
				|| tiles[indexes.xpos][indexes.ypos].getTerrain() == Terrain.NONE;
	}


	private void setPath(Coord current){
		if(current == null){
			return;
		}
		else if((current.xpos == 4 && current.ypos == 3) || (current.xpos == 2 && current.ypos == 3) || (current.xpos == 3 && current.ypos == 4) || (current.xpos == 3 && current.ypos == 2)){
			pathFound = true;
			return;
		}
		else{
			setPath(nextBest(current));
		}
	}

	private Coord nextBest(Coord current){
		ArrayList<Coord> neighbors = new ArrayList<>();
		neighbors.add(new Coord(current.xpos, current.ypos + 1));  // South 
		neighbors.add(new Coord(current.xpos, current.ypos - 1));  // North
		neighbors.add(new Coord(current.xpos + 1, current.ypos));  // East
		neighbors.add(new Coord(current.xpos - 1, current.ypos));  // West

		for(int i = 0; i < neighbors.size(); i++){
			if(neighbors.get(i).xpos > 6 || neighbors.get(i).xpos < 0 ||neighbors.get(i).ypos > 6 ||neighbors.get(i).ypos < 0 || blocked2(neighbors.get(i)) || contains(neighbors.get(i))){
				neighbors.remove(neighbors.get(i));
				i--;
			}
		}

		/* Makes current location into a Point2D object */
		Point2D point = new Point2D.Double(3,3);
		Double closest = Double.POSITIVE_INFINITY; 


		Coord next = null;

		for(Coord tile: neighbors){
			//System.out.println("NEIGHBOR: " + tile);
			Point2D pt = new Point2D.Double(tile.xpos, tile.ypos);
			Double distanceFromCurr = point.distance(pt);
			if(distanceFromCurr < closest) {
				closest = distanceFromCurr;
				next = tile;
			}
		}
		System.out.println(next);
		path.push(next);
		return next;
	}

	private boolean contains(Coord c){
		for(Coord t: path)
			if(t.equals(c))
				return true;
		return false;

	}

}
