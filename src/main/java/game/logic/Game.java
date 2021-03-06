package game.logic;

import game.inventory.Inventory;
import game.monster.Monster;
import game.player.Player;
import game.room.InformationCenter;
import game.room.Lobby;
import game.room.Outside;
import game.room.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class Game {
    private Parser parser;
    private Player player;
    private List<Room> roomHistories;
    private List<Room> availableRoom;
    private Room currentRoom;


    /**
     * Create the game and initialise its internal map.
     */
    public Game() {
        availableRoom = new ArrayList<>();
        roomHistories = new ArrayList<>();
        parser = new Parser();
        System.out.println("< ---- CREATE ROOMS ---- >");
        createRooms();
        System.out.println("--DONE! ROOM CREATED--");
        System.out.println("< ---- CREATE A PLAYER ---- >");
        createPlayer();
        System.out.println("--DONE PLAYER CREATED--");


    }

    private void createPlayer() {
        System.out.println("Enter the player name : ");
        Scanner sc = new Scanner(System.in);
        String name = sc.nextLine();
        player = new Player(name);
        System.out.println("----------------------------------------");
        System.out.println("Hi, " + name);
        System.out.println("You have 1000 hp");
        System.out.println("You are at level 1");
        System.out.println("The capacity of your bag is 10");
        System.out.println("Kill enemies to earn the score");
        System.out.println("----------------------------------------");


    }

    /**
     * Create all the rooms and link their exits together.
     */
    private void createRooms() {
        Room outside = new Outside();
        Room lobby = new Lobby("<-MUIC LOBBY->");
        Room infomationCenter = new InformationCenter(" information center");


        availableRoom.add(outside);
        availableRoom.add(lobby);

        outside.setExit("up", lobby);
        lobby.setExit("left", infomationCenter);




        /* The current room is the last the last index in the list*/
        roomHistories.add(outside);
        currentRoom = outside;

    }

    /**
     * game.main.Main play routine.  Loops until end of play.
     */
    public void play() {
        printWelcome();


        // Enter the main command loop.  Here we repeatedly read commands and
        // execute them until the game is over.

        boolean finished = false;
        while (!finished) {
            System.out.print("> ");
            Command command = parser.getCommand();
            if (command.getCommandWord() == null) continue;

            finished = processCommand(command);


        }
        System.out.println("Thank you for playing.  Good bye.");
    }

    /**
     * Print out the opening message for the player.
     */
    private void printWelcome() {
        StringBuilder s = new StringBuilder();
        s.append("======================================================= \n");
        s.append(" Welcome to the World of Zork!\nWorld of Zork is a new, \n" +
                "incredibly boring adventure game. You have to try your \n" +
                "best to reach to end of the game.  \n\n " +
                "Your objectives are kill one monster and find a diamond.\n" +
                "========================================================\n");

        s.append("Type 'help' if you need help.\n");
        System.out.println(s);

        System.out.println(getAvailableRoom());


    }

    /**
     * Given a command, process (that is: execute) the command.
     *
     * @param command The command to be processed.
     * @return true If the command ends the game, false otherwise.
     */
    private boolean processCommand(Command command) {
        boolean wantToQuit = false;
        CommandWord word = command.getCommandWord();
        switch (word) {
            case GO:
                goRoom(command);
                break;
            case HELP:
                printHelp();
                break;
            case QUIT:
                wantToQuit = quit(command);
                break;
            case UNKNOWN:
                System.out.println(" I don't know what you mean....");
                break;
            case LOOK:
                look();
                break;
            case ROOM:
                System.out.println(getAvailableRoom());
                break;
            case FIGHT:
                fight(command);
                break;
            case TAKE:
                take(command);
                break;
            case DROP:
                drop(command);
                break;
            case INFO:
                printInfo();
                break;


        }
        if (missionFulfilled()) {
            wantToQuit = true;
        }
        return wantToQuit;

    }

    private boolean missionFulfilled() {
        if (player.getMonstersKilled() == 1 && player.foundDiamond()){
            return true;
        }
        return false;

    }

    private void drop(Command command) {
        List<Inventory> playerInventory = player.getInventoryList();
        int playerBagSize = playerInventory.size();
        if (playerBagSize == 0){
            System.out.println("Hmm Your bag is empty.");
            return;
        }
        if (!command.hasSecondWord()) {
            System.out.println("Drop what ? ");
            System.out.println(player.getInventoryString());
            return;
        }
        try {
            int g = Integer.parseInt(command.getSecondWord());
            if (g > playerBagSize) {
                System.out.println("You've possessed only " + playerBagSize + "things in your bag");
                System.out.println(player.getInventoryString());
                return;
            }
            Inventory toDrop = player.getInventoryAt(g-1);
            if (player.drop(toDrop)) {
                currentRoom.placeInventory(toDrop);
            }
        } catch (NumberFormatException n) {
            System.out.println("the second argument must be an integer");
        }




    }

    private void take(Command command) {

        if (currentRoom.inventoriesSize() == 0) {
            System.out.println("There is nothing to take");
            return;
        }

        if (player.bagIsFull()) {
            System.out.println("Your bag is full! Consider dropping something with command drop [num] ");
            return;
        }
        if (!command.hasSecondWord()) {
            System.out.println("take what ?");
            return;
        }
        int inventoriesSize = currentRoom.inventoriesSize();

        String number = command.getSecondWord();

        try {
            int g = Integer.parseInt(number);
            if (g > inventoriesSize) {
                System.out.println("There are only " + inventoriesSize + " items in this room");
                return;
            }

            Inventory inventory = currentRoom.getInventoryAt(g - 1);
            if (player.take(inventory)) {
                currentRoom.removeInventory(inventory);
            }
        } catch (NumberFormatException n) {
            System.out.println("the second argument must be an integer");
        }


    }

    private void fight(Command command) {


        if (!command.hasSecondWord()) {
            System.out.println("Fight what ? ");
            return;

        }
        int monstersSize = currentRoom.monstersSize();
        if (monstersSize == 0) {
            System.out.println("No monsters to flight! You're in luck.");
            return;
        }

        String number = command.getSecondWord();

        try {
            int g = Integer.parseInt(number);
            if (g > monstersSize) {
                System.out.println("There are only " + monstersSize + " monsters in this room");
                return;
            }

            // Final touch cause I care about the user;
            if (!player.hasInventoryToFight()) {
                System.out.println("You haven't get any inventory to fight! Please find one first ");
                return;
            }


            Monster monster = currentRoom.getMonsterAt(g - 1);
            player.intoTheFightWith(monster, parser);
            if (!player.isRunAwayLastTime()){
                currentRoom.removeMonster(monster);
            }
        } catch (NumberFormatException n) {
            System.out.println("the second argument must be an integer");
        }


    }


    private void printInfo() {
        double proprtionHP = (player.getHp() / Player.getLEVEL_HP_TABLE().get(player.getLevel())) * 100.0;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(player.getName() + "'s information \n " +
                "You are at level " + player.getLevel() + "\n " +
                "HP : " + player.getHp() + " (" + proprtionHP + " % )  \n " +
                "Your inventories are \n " +
                 player.getInventoryString());
        System.out.println(stringBuilder);

    }
    // implementations of user commands:

    /**
     * Print out some help information.
     * Here we print some stupid, cryptic message and a list of the
     * command words.
     */
    private void printHelp() {
        System.out.println("You are lost. You are alone. You wander around like crazy");
        System.out.println();
        System.out.println("Your command words are:");
        System.out.println("go [direction]   -- go to the specific location (e.g. go up) ps.\"go back\" will go one room backward\n" +
                "quit             -- quit the game\n" +
                "help             -- print this help\n" +
                "info             -- print current player's status\n" +
                "print            -- print the current room and its exits\n" +
                "fight [num]      -- fight the [num] monster in this room\n" +
                "take [num]       -- pick up the [num] inventory from this room\n");

    }

    /**
     * Try to go in one direction. If there is an exit, enter
     * the new room, otherwise print an error message.
     */
    private void goRoom(Command command) {
        if (!command.hasSecondWord()) {
            // if there is no second word, we don't know where to go...
            System.out.println("Go where?");
            return;
        }

        if (command.getSecondWord().equals("back")) {
            back();
            return;
        }

        String direction = command.getSecondWord();


        // Try to leave current room.
        Room nextRoom = currentRoom.getExit(direction);


        if (nextRoom != null) {

            currentRoom = nextRoom;
            roomHistories.add(nextRoom);
            System.out.println(getAvailableRoom());
            System.out.println();
        } else
            System.out.println("There is no door!");

    }

    /**
     * Get rid of duplicate code by implementing a method getAvailableRoom.
     * It takes no parameter, just print all the possible direction the user can go in.
     */
    private StringBuilder getAvailableRoom() {
        StringBuilder roomToGo = new StringBuilder();
        roomToGo.append("You are at " + currentRoom.getLocationName()).append("\n");


        int countRoom = 0;
        if (currentRoom.getExit("up") != null) {
            roomToGo.append("go up >>  ").append(currentRoom.getExit("up").getLocationName());
            countRoom++;
        }
        if (currentRoom.getExit("down") != null) {
            roomToGo.append("go down >>  ").append(currentRoom.getExit("down").getLocationName());
            countRoom++;
        }
        if (currentRoom.getExit("left") != null) {
            roomToGo.append("go left >>  ").append(currentRoom.getExit("left").getLocationName());
            countRoom++;
        }
        if (currentRoom.getExit("right") != null) {
            roomToGo.append("go right >>  ").append(currentRoom.getExit("right").getLocationName());
            countRoom++;
        }


        if (countRoom != 0) {

            return roomToGo.append("\n").append(currentRoom.prettyPrintMonstersAndInventories()).append("\n");
        } else
            return roomToGo.append(currentRoom.prettyPrintMonstersAndInventories()).append("\n No exit to be found, please consider going back\n");
    }

    /**
     * "Quit" was entered. Check the rest of the command to see
     * whether we really quit the game.
     *
     * @return true, if this command quits the game, false otherwise.
     */
    private boolean quit(Command command) {
        if (command.hasSecondWord()) {
            System.out.println("Quit what?");
            return false;
        } else {
            return true;  // signal that we want to quit
        }
    }

    private void look() {
        //StringBuilder printRoom = new StringBuilder();
        System.out.println(getAvailableRoom());
    }

    private void back() {

        if (roomHistories.size() < 2) {
            System.out.println("You have no where to go back");
            return;
        }

        Room previousRoom = roomHistories.get(roomHistories.size() - 2);

        currentRoom = previousRoom;
        System.out.println(getAvailableRoom());
        System.out.println();
        roomHistories.remove(roomHistories.size() - 1);


    }


}





