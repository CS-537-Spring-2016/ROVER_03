package missionControl;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import common.MapTile;
import common.ScanMap;
import enums.Terrain;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import model.RoverQueue;
import swarmBots.ROVER_03;
import trackingUtility.Tracker;

public class MissionControl extends Application{
	private Label[][] labels = new Label[7][7];
	private ROVER_03 client;
	private RoverQueue queue;
	private Tracker tracker;
	private ScanMap map = null;
	private Map<String,Integer> cargoList;

	@Override
	public void start(Stage primaryStage) throws IOException, InterruptedException{
		List<String> parameters = getParameters().getRaw();
		if(parameters.isEmpty())
			client = new ROVER_03("127.0.0.1");				/* Default port is local host */
		else 
			client = new ROVER_03(parameters.get(0));		/* Allows user to enter any IP on console */

		new Thread (new Runnable(){

			@Override
			public void run() {
				try {
					client.start();					
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}			
			}

		}).start();
		
		Thread.sleep(2000);						// Thread need to sleep to allow ROVER_03 to get set up

		/* Since objects are passed by reference, we get the reference to the following objects from
		 * the ROVER_03 class in order to keep the GUI updated in case of any changes in any of these objects */
		queue = client.getQueue();
		tracker = client.getTracker();
		cargoList = client.getCargoList();


		BorderPane bp = new BorderPane();
		bp.getStyleClass().add("gui");
		bp.getStylesheets().add("missionControl/style.css");
		Scene scene = new Scene(bp, 800, 750);
		
		Label sceneTitle = new Label("Mission Control\n");
		sceneTitle.getStyleClass().add("missionControl");
		HBox title = new HBox();
		title.getChildren().add(sceneTitle);
		title.setAlignment(Pos.BOTTOM_CENTER);

		bp.setTop(title);
		bp.setLeft(setTaskModule());
		bp.setBottom(setConsoleView());
		bp.setCenter(getVisualFeed());

		primaryStage.setTitle("Mission Control");
		primaryStage.setResizable(false);
		primaryStage.setScene(scene);
		primaryStage.show();

	}
	
	private GridPane getVisualFeed(){
		GridPane feed = new GridPane();
		feed.setAlignment(Pos.BOTTOM_LEFT);

		while (map == null)
			map = client.getScanMap();				// Loop until there is map 

		/* This loop sets up how the map will look and how many tiles to display*/
		for(int row = 0; row < 7; row++){
			for(int column = 0; column < 7; column++){
				StackPane stack = null;
				Label tile = new Label("");
				labels[row][column] = tile;
				tile.setPrefSize(70, 70);
				if(row == 3 && column == 3){
					Circle circle = new Circle(25);
					circle.setFill(Color.CORNFLOWERBLUE);
					Label text = new Label("03");
					text.getStyleClass().add("rover");
					stack = new StackPane();
					stack.getChildren().addAll(labels[row][column],circle, text);
					feed.add(stack, row, column);
					continue;
				}

				feed.add(labels[row][column], row, column);
			}
		}

		/* This thread will take care of updating the visual feed. */
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true){
					map = client.getScanMap();
					MapTile[][] tiles = map.getScanMap();

					for(int row = 0; row < 7; row++){
						for(int column = 0; column < 7; column++){
							labels[row][column].getStyleClass().clear();
							labels[row][column].getStyleClass().add("grid");
							if(tiles[row][column].getTerrain() == Terrain.ROCK)
								labels[row][column].getStyleClass().add("rock");
							else if(tiles[row][column].getTerrain() == Terrain.NONE)
								labels[row][column].getStyleClass().add("abyss");	
							else if(tiles[row][column].getTerrain() == Terrain.SAND)
								labels[row][column].getStyleClass().add("sand");
							else if(tiles[row][column].getTerrain() == Terrain.GRAVEL)
								labels[row][column].getStyleClass().add("gravel");
							else
								labels[row][column].getStyleClass().add("dirt");
						}	
					}
					try {
						Thread.sleep(1000);					/* Iterate through loop every second */
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
		
		return feed;
	}
	

	private VBox setTaskModule(){
		/* Contains module title and module contents */
		VBox module = new VBox();
		module.setMinHeight(500);
		module.setMinWidth(350);
		module.setAlignment(Pos.CENTER);

		HBox title = new HBox();
		Label taskTitle = new Label("ROVER_03: ONLINE");
		taskTitle.getStyleClass().add("taskTitle");
		/* Adds label to HBox */
		title.getChildren().add(taskTitle);
		title.setMinWidth(350);
		/* Sets background to #000000 */
		title.getStyleClass().add("console");
		
		/* Will contain all the coordinates received from other Rovers if any */
		VBox tasks = new VBox();
		/* Scroll Pane needed in case list of coordinates is long
		 * The tasks VBox is inside the scroll pane */
		ScrollPane scrollPane = new ScrollPane();
		scrollPane.setContent(tasks);
		scrollPane.setPrefSize(350, 475);
		
		/* Place a contents inside main VBox*/
		module.getChildren().addAll(title,scrollPane);

		new Thread(new Runnable() {
			@Override
			public void run(){
				while (true){
					/* While the rover is online we will have to constantly update the task list on mission control
					 * however, since this particular piece of code is running on a separate thread we would not be able
					 * to access the Application thread. That is why we have to use Platform.runLater */
					Platform.runLater(new Runnable(){
						@Override
						public void run(){
							/* Clears contents inside of the scroll pane */
							tasks.getChildren().clear();
							if(queue.isEmpty()){
								/* This message will be shown if there is nothing in the queue */
								Label task = new Label("WAITING FOR NEW TASKS...");
								task.getStyleClass().add("tasks");
								tasks.getChildren().add(task);
							}
							else
								for(Point2D destination: queue.getPositionList()){
									/* Coordinates will be shown for every coordinate in the queue */
									String display = "RECEIVED COORDINATE: [ " + (int)destination.getX() + " , " + (int)destination.getY() + " ]";
									Label task = new Label(display);
									task.getStyleClass().add("tasks");
									tasks.getChildren().add(task);
								}
						}
					});
					
					try {
						/* This thread will execute every second */
						Thread.sleep(1000);
					} catch (Exception e1) {
						continue;
					}
				}
			}
		}).start();

		return module;
	}
	
	
	private HBox setConsoleView(){
		HBox console = new HBox();
		console.setMinWidth(800);
		console.setMinHeight(125);
		console.getStyleClass().add("console");
		console.getChildren().addAll(setCargoModule(),setDestinationModule(),setEquipmentModule());
		
		return console;
	}
	
	private HBox setDestinationModule(){
		/* Main destination module container */
		HBox module = new HBox();
		module.setMinWidth(225);
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true){
					/* While the rover is online we will have to constantly update distance left to destination on mission control
					 * however, since this particular piece of code is running on a separate thread we would not be able
					 * to access the Application thread. That is why we have to use Platform.runLater */
					Platform.runLater(new Runnable(){
						@Override
						public void run(){
							/* Clears contents of main module */
							module.getChildren().clear();
							VBox content = new VBox();
							Label title = new Label("\nROVER_03 TRACKER:\n");
							title.getStyleClass().add("consoleText");
							Label origin = new Label("ORIGIN: [ " + tracker.getStartingPoint().xpos + " , " + tracker.getStartingPoint().ypos + " ]");
							origin.getStyleClass().add("consoleText");
							Label destin = new Label("DESTINATION: [ " + tracker.getDestination().xpos + " , " + tracker.getDestination().ypos + " ]");
							destin.getStyleClass().add("consoleText");
							Label distance = new Label("DISTANCE LEFT: [ " + tracker.getDistanceTracker().xpos + " , " + tracker.getDistanceTracker().ypos + " ]");
							distance.getStyleClass().add("consoleText");
							content.getChildren().addAll(title,origin,destin,distance);
							module.getChildren().add(content);
						}
					});
					
					try {
						Thread.sleep(1000);				/* Lopp will iterate every second */
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
		
		return module;
	}
	
	private VBox setEquipmentModule(){
		/* Main equipment module container */
		VBox module = new VBox();
		
		Label moduleTitle = new Label("\nROVER_03 EQUIPMENT:\n");
		moduleTitle.getStyleClass().add("consoleText");
		
		Label drive = new Label("DRIVE TYPE: " + client.getRover().getDriveType());
		drive.getStyleClass().add("consoleText");
		Label tool1 = new Label("TOOL 1: " + client.getRover().getTools().get(0));
		tool1.getStyleClass().add("consoleText");
		Label tool2 = new Label("TOOL 2: " + client.getRover().getTools().get(1));
		tool2.getStyleClass().add("consoleText");
		
		module.getChildren().addAll(moduleTitle,drive,tool1,tool2);
		module.setMinWidth(225);
		return module;
	}
	
	private VBox setCargoModule(){
		/* Main cargo module container */
		VBox module = new VBox();
		module.setMinWidth(350);
		
		HBox moduleTitle = new HBox();
		Label title = new Label("\nROVER_03 CARGO:\n");
		title.getStyleClass().add("consoleText");
		moduleTitle.getChildren().add(title);
		moduleTitle.setAlignment(Pos.CENTER);
		
		HBox elements = new HBox();
		elements.setAlignment(Pos.CENTER);
		elements.setSpacing(20);
		
		/* Places content inside main cargo module */
		module.getChildren().addAll(moduleTitle, elements);
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true){
					/* While the rover is online we will have to constantly update cargo list on mission control
					 * however, since this particular piece of code is running on a separate thread we would not be able
					 * to access the Application thread. That is why we have to use Platform.runLater */
					Platform.runLater(new Runnable(){
						@Override
						public void run(){
							/* Clears contents inside of the elements container */
							elements.getChildren().clear();
							
							/* We will have two columns each with the value for two of the possible sciences */
							VBox columnOne = new VBox();
							VBox columnTwo = new VBox();
							
							/* List of different types of science and how much was collected from each */
							Label organic = new Label("ORGANIC: " + cargoList.get("ORGANIC"));
							organic.getStyleClass().add("consoleText");
							Label radioactive = new Label("RADIOACTIVE: " + cargoList.get("RADIOACTIVE"));
							radioactive.getStyleClass().add("consoleText");
							Label crystal = new Label("CRYSTAL: " + cargoList.get("CRYSTAL"));
							crystal.getStyleClass().add("consoleText");
							Label mineral = new Label("MINERAL: " + cargoList.get("MINERAL"));
							mineral.getStyleClass().add("consoleText");
							
							/* Update elements container */
							columnOne.getChildren().addAll(organic,radioactive);
							columnTwo.getChildren().addAll(crystal,mineral);
							elements.getChildren().addAll(columnOne,columnTwo);
						}
					});
					
					try {
						Thread.sleep(1000);				/* Loop will execute every second */
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
		
		return module;
	}
	
//	public static void main(String args[]) throws IOException, InterruptedException{
//		Application.launch(args);
//	}
}
