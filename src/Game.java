import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Class Game - the main class of the "Zork" game.
 *
 * Author: Michael Kolling Version: 1.1 Date: March 2000
 * 
 * This class is the main class of the "Zork" application. Zork is a very
 * simple, text based adventure game. Users can walk around some scenery. That's
 * all. It should really be extended to make it more interesting!
 * 
 * To play this game, create an instance of this class and call the "play"
 * routine.
 * 
 * This main class creates and initialises all the others: it creates all rooms,
 * creates the parser and starts the game. It also evaluates the commands that
 * the parser returns.
 */
class Game {
	private Parser parser;
	private Room currentRoom;
	private Inventory inventory;
	// This is a MASTER object that contains all of the rooms and is easily
	// accessible.
	// The key will be the name of the room -> no spaces (Use all caps and
	// underscore -> Great Room would have a key of GREAT_ROOM
	// In a hashmap keys are case sensitive.
	// masterRoomMap.get("GREAT_ROOM") will return the Room Object that is the Great
	// Room (assuming you have one).
	private HashMap<String, Room> masterRoomMap;
	private HashMap<String, Item> masterItemMap;


	private void initItems(String fileName) throws Exception{
		Scanner itemScanner;
		masterItemMap = new HashMap<String, Item>();

		try {
			
			itemScanner = new Scanner(new File(fileName));
			while (itemScanner.hasNext()) {
				Item item = new Item();
				String itemName = itemScanner.nextLine().split(":")[1].trim();
				item.setName(itemName);
				String itemDesc = itemScanner.nextLine().split(":")[1].trim();
				item.setDescription(itemDesc);	
				Boolean openable = Boolean.valueOf(itemScanner.nextLine().split(":")[1].trim());
				item.setOpenable(openable);
				
				masterItemMap.put(itemName.toUpperCase().replaceAll(" ", "_"), item);
				
				String temp = itemScanner.nextLine();
				String itemType = temp.split(":")[0].trim();
				String name = temp.split(":")[1].trim();
				if (itemType.equals("Room"))
					masterRoomMap.get(name).getInventory().addItem(item);
				else
					masterItemMap.get(name).addItem(item);
			}
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void initRooms(String fileName) throws Exception {
		masterRoomMap = new HashMap<String, Room>();
		Scanner roomScanner;
		try {
			HashMap<String, HashMap<String, String>> exits = new HashMap<String, HashMap<String, String>>();
			roomScanner = new Scanner(new File(fileName));
			while (roomScanner.hasNext()) {
				Room room = new Room();
				// Read the Name
				String roomName = roomScanner.nextLine();
				room.setRoomName(roomName.split(":")[1].trim());
				// Read the Description
				String roomDescription = roomScanner.nextLine();
				room.setDescription(roomDescription.split(":")[1].replaceAll("<br>", "\n").trim());
				// Read the Exits
				String roomExits = roomScanner.nextLine();
				// An array of strings in the format E-RoomName
				String[] rooms = roomExits.split(":")[1].split(",");
				HashMap<String, String> temp = new HashMap<String, String>();
				for (String s : rooms) {
					temp.put(s.split("-")[0].trim(), s.split("-")[1]);
				}

				exits.put(roomName.substring(10).trim().toUpperCase().replaceAll(" ", "_"), temp);

				// This puts the room we created (Without the exits in the masterMap)
				masterRoomMap.put(roomName.toUpperCase().substring(10).trim().replaceAll(" ", "_"), room);

				// Now we better set the exits.
			}

			for (String key : masterRoomMap.keySet()) {
				Room roomTemp = masterRoomMap.get(key);
				HashMap<String, String> tempExits = exits.get(key);
				for (String s : tempExits.keySet()) {
					// s = direction
					// value is the room.

					String roomName2 = tempExits.get(s.trim());
					Room exitRoom = masterRoomMap.get(roomName2.toUpperCase().replaceAll(" ", "_"));
					roomTemp.setExit(s.trim().charAt(0), exitRoom);

				}

			}

			roomScanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the game and initialise its internal map.
	 */
	public Game() {
		try {
			initRooms("data/Rooms.dat");	// creates the map from the rooms.dat file
			// initRooms is responsible for building/ initializing the masterRoomMap (private instance variable)
			currentRoom = masterRoomMap.get("MARCH_2020");	// the key for the masterRoomMap is the name of the room all in Upper Case (spaces replaced with _)
			inventory = new Inventory();
			initItems("data/items.dat");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		parser = new Parser();
	}

	

	/**
	 * Main play routine. Loops until end of play.
	 */
	public void play() {
		printWelcome();
		// Enter the main command loop.  Here we repeatedly read commands and
		// execute them until the game is over.

		boolean finished = false;
		while (!finished) {
			Command command = parser.getCommand();
		  	if(processCommand(command)){
        		finished = true;
      		}
		}
		System.out.println("Thank you for playing.  Good bye.");
	}

	/**
	 * Print out the opening message for the player.
	 */
	private void printWelcome() {
		System.out.println();
		System.out.println("Welcome to Zork!");
		System.out.println("Zork is a new, incredibly boring adventure game.");
		System.out.println("Type 'help' if you need help.");
		System.out.println();
		System.out.println(currentRoom.longDescription());
	}

	/**
	 * Given a command, process (that is: execute) the command. If this command ends
	 * the game, true is returned, otherwise false is returned.
	 */
	private boolean processCommand(Command command) {
		if (command.isUnknown()) {
			System.out.println("I don't know what you mean...");
			return false;
		}
		String commandWord = command.getCommandWord();
		if (commandWord.equals("help"))
			printHelp();
		else if (commandWord.equals("go"))
			return goRoom(command);
		else if (commandWord.equals("quit")) {
			if (command.hasSecondWord())
				System.out.println("Quit what?");
			else
				return true; // signal that we want to quit
		} else if (commandWord.equals("eat")) {
			eat(command.getSecondWord());
		} else if (commandWord.equals("jump")) {
			return jump();
		} else if (commandWord.equals("sit")) {
			sit();
		} else if ("udeswn".indexOf(commandWord) > -1) {
			return goRoom(command);
		} else if (commandWord.equals("take")) {
			if (!command.hasSecondWord())
				System.out.println("Take what?");
			else
				takeItem(command.getSecondWord());
		} else if (commandWord.equals("drop")) {
			if (!command.hasSecondWord())
				System.out.println("Drop what?");
			else
				dropItem(command.getSecondWord());
		} else if (commandWord.equals("i")) {
			System.out.println(inventory);
		} else if (commandWord.equals("open")) {
			if (!command.hasSecondWord())
				System.out.println("Open what?");
			else
				openItem(command.getSecondWord());
		} else if (commandWord.equals("access")) {
			if (!command.hasSecondWord())
				System.out.println("access what?");
			else
				accessItem(command.getSecondWord());
		}
		return false;
	}

	private void accessItem(String itemName) {
    Item item = inventory.contains(itemName);

    if(item != null){
      System.out.println("You have access the " + itemName);
    } else {
      System.out.println("What is it you think you can access but you cannot?");
    }
    }

    private void openItem(String itemName) {
		Item item = inventory.contains(itemName);
		
		if(item != null) {
			System.out.println(item.displayContents());
		}else {
			System.out.println("What is it that you think you have but do not.");
		}
		
	}

	private void takeItem(String itemName) {
		Inventory temp = currentRoom.getInventory();
		
		Item item = temp.removeItem(itemName);
		
		if (item != null) {
			if (inventory.addItem(item)) {
				System.out.println("You have taken the " + itemName);
				// following code allows you to do something with an item in the same room
				// if (currentRoom.getRoomName().equals("Hallway") &&  itemName.equals("ball")) {
				// 	currentRoom = masterRoomMap.get("ATTIC");
				// 	System.out.println("You seem to be lying on the floor all confused. It seems you have been here for a while.\n");
				// 	System.out.println(currentRoom.longDescription());
				
			}else {
				System.out.println("You were unable to take the " + itemName);
			}
		}else {
			System.out.println("There is no " + itemName + " here.");
		}
	}
	
	private void dropItem(String itemName) {
		Item item = inventory.removeItem(itemName);
		
		if (item != null) {
			if (currentRoom.getInventory().addItem(item)) {
				System.out.println("You have dropped the " + itemName);
			}else {
				System.out.println("You were unable to drop the " + itemName);
			}
		}else {
			System.out.println("You are not carrying a " + itemName + ".");
		}
	}

	private void eat(String secondWord) {
		if (secondWord.equals("steak"))
			System.out.println("YUMMY");
		else if (secondWord.equals("bread"))
			System.out.println("I don't eat carbs...");
		else 
			System.out.println("You are the " + secondWord);
		
	}

	private void sit() {
		System.out.println("You are now sitting. You lazy excuse for a person.");
		
	}

	private boolean jump() {
		System.out.println("You jumped. Ouch you fell. You fell hard. Really hard. You are getting sleepy. Very sleepy! Yuo are dead!");
		return true;
	}

// implementations of user commands:
	/**
	 * Print out some help information. Here we print some stupid, cryptic message
	 * and a list of the command words.
	 */
	private void printHelp() {
		System.out.println("You are lost. You are alone. You wander");
		System.out.println("around at Monash Uni, Peninsula Campus.");
		System.out.println();
		System.out.println("Your command words are:");
		parser.showCommands();
	}

	/**
	 * Try to go to one direction. If there is an exit, enter the new room,
	 * otherwise print an error message.
	 */
	private boolean goRoom(Command command) {
		if (!command.hasSecondWord() && ("udeswn".indexOf(command.getCommandWord()) < 0)) {
			// if there is no second word, we don't know where to go...
			System.out.println("Go where?");
			return false;
		}
		
		String direction = command.getSecondWord();
		if ("udeswn".indexOf(command.getCommandWord()) > -1) {
			direction = command.getCommandWord();
			if (direction.equals("u"))
				direction = "up";
			else if (direction.equals("d"))
				direction = "down";
			else if (direction.equals("e"))
				direction = "east";
			else if (direction.equals("w"))
				direction = "west";
			else if (direction.equals("n"))
				direction = "north";
			else if (direction.equals("s"))
				direction = "south";
		}
		
// Try to leave current room.
		Room nextRoom = currentRoom.nextRoom(direction);
		if (nextRoom == null)
			System.out.println("There is no door!");
		else {
			currentRoom = nextRoom;
			System.out.println(currentRoom.longDescription());
			if(currentRoom.getRoomName().equals("Math Class")){
				System.out.println("What is 8!");
				Scanner in = new Scanner(System.in);
				while(!in.nextLine().equalsIgnoreCase("40320")){
					System.out.println("Wrong, what is your favourite colour?");
				}
			} else if(currentRoom.getRoomName().equals("New York New York") && inventory.hasItem("Pfizer vaccine")){
				return true;
			}
		}
		return false;
  }
}
