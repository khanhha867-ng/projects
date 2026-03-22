package core;

import edu.princeton.cs.algs4.StdDraw;
import tileengine.TERenderer;
import tileengine.TETile;
import tileengine.Tileset;
import utils.RandomUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Random;

public class Game {
    public static final int WIDTH = 80;
    public static final int HEIGHT = 40;
    public static final double xCenter = (double) WIDTH / 2;
    public static final double yCenter = (double) HEIGHT / 2;
    private static int aX;
    private static int aY;
    private static final String SAVE_FILE = "save.txt";
    public static TETile avatar = Tileset.AVATAR;

    public static void MainMenuAppearance() {
        StdDraw.setCanvasSize(WIDTH * 10, HEIGHT * 10);
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);
        StdDraw.clear(Color.BLACK); // Set the background's color
        StdDraw.setPenColor(Color.WHITE); // Set the text's color
        StdDraw.text(xCenter, yCenter + 16, "CS61B: BYOW");
        StdDraw.text(xCenter, yCenter + 6, "(N) New Game");
        StdDraw.text(xCenter, yCenter, "(L) Load Game");
        StdDraw.text(xCenter, yCenter - 6, "(C) Change avatar");
        StdDraw.text(xCenter, yCenter - 12, "(Q) Quit Game");
        StdDraw.show();

    }

    public static void MainMenuFeatures() throws IOException {
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char option = Character.toUpperCase(StdDraw.nextKeyTyped()); // Gets the pressed key and converts it to upper case
                switch (option) {
                    case 'N': // Creates a new game
                        NewGame();
                        return;
                    case 'L': // Loads a saved game from the save file
                        loadGame();
                        return;
                    case 'C': // Change avatar's appearance
                        TETile[][] grid = new TETile[WIDTH][HEIGHT];
                        changeAvatar(grid);
                        MainMenuAppearance();
                        MainMenuFeatures();
                        return;
                    case 'Q': // Closes the window
                        System.exit(0);
                        break;
                    default:
                        StdDraw.setPenColor(Color.RED);
                        StdDraw.text(xCenter, yCenter - 18, "Invalid Option! Try Again.");
                        StdDraw.show();
                        break;
                }
            }
        }
    }

    public static void NewGame() throws IOException {
        String seed = getSeed();
        startGame(Long.parseLong(seed));
    }

    public static String getSeed() {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(xCenter, yCenter + 10, "Enter seed followed by S");
        StdDraw.show();

        StringBuilder seed = new StringBuilder();
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = Character.toUpperCase(StdDraw.nextKeyTyped());
                if (Character.isDigit(c)) {
                    seed.append(c);
                    redrawGetSeedScreen(seed.toString());
                } else if (c == 'S') {
                    return String.valueOf(seed);
                } else {
                    StdDraw.setPenColor(Color.RED);
                    StdDraw.text(xCenter, yCenter - 10, "Invalid Input!");
                    StdDraw.show();
                }
            }
        }
    }

    public static void redrawGetSeedScreen(String seed) {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(xCenter, yCenter + 10, "Enter seed followed by S");
        StdDraw.setPenColor(Color.YELLOW);
        StdDraw.text(xCenter, yCenter, seed);
        StdDraw.show();
    }

    private static void saveGame(TETile[][] grid) {
        try (BufferedWriter savedGame = new BufferedWriter(new FileWriter(SAVE_FILE))) {
            for (int y = grid[0].length - 1; y >= 0; y--) { // Save from top to bottom
                for (TETile[] teTiles : grid) {
                    savedGame.write(teTiles[y].character()); // Save each tile's character
                }
                savedGame.newLine(); // New line for each row
            }
            savedGame.flush();
        } catch (IOException e) {
            StdDraw.clear(Color.BLACK);
            StdDraw.setPenColor(Color.RED);
            StdDraw.text(xCenter, yCenter, "Error saving game!");
            StdDraw.show();
        }
    }

    public static void loadGame() {
        try (BufferedReader loading = new BufferedReader(new FileReader(SAVE_FILE))) {
            TETile[][] grid = new TETile[WIDTH][HEIGHT];
            String line;
            int y = HEIGHT - 1;

            while ((line = loading.readLine()) != null) {
                for (int x = 0; x < line.length(); x++) {
                    grid[x][y] = charToTile(line.charAt(x)); // Convert character to tile
                    if (grid[x][y] == avatar) { // Restore avatar's position
                        aX = x;
                        aY = y;
                    }
                }
                y--; // Move to the next row
            }

            playGame(grid);
        } catch (IOException e) {
            StdDraw.clear(Color.BLACK);
            StdDraw.setPenColor(Color.RED);
            StdDraw.text(xCenter, yCenter, "No saved game found!");
            StdDraw.show();
        }
    }

    private static TETile charToTile(char c) {
        return switch (c) {
            case '·' -> Tileset.FLOOR;
            case '#' -> Tileset.WALL;
            case '@' -> Tileset.AVATAR;
            case '❀' -> Tileset.FLOWER;
            case 'p' -> avatar = new TETile('p', Color.WHITE, Color.BLACK, "Avatar", "src/gameAvatar/prince.png", 13);
            case 'P' -> avatar = new TETile('P', Color.WHITE, Color.BLACK, "Avatar", "src/gameAvatar/Princess.png", 14);
            case 'm' -> avatar = new TETile('m', Color.WHITE, Color.BLACK, "Avatar", "src/gameAvatar/mummy.png", 15);
            case 'n' -> avatar = new TETile('n', Color.WHITE, Color.BLACK, "Avatar", "src/gameAvatar/naruto.png", 16);
            default -> Tileset.NOTHING;
        };
    }


    private static void playGame(TETile[][] grid) throws IOException {
        TERenderer te = new TERenderer();
        te.initialize(WIDTH, HEIGHT);
        te.renderFrame(grid);

        boolean colonPressed = false;
        while (true) {
            te.renderFrame(grid);

            int mouseX = (int) StdDraw.mouseX();
            int mouseY = (int) StdDraw.mouseY();
            if (mouseX >= 0 && mouseX < WIDTH && mouseY >= 0 && mouseY < HEIGHT) {
                String text = grid[mouseX][mouseY].description();
                StdDraw.setPenColor(Color.WHITE);
                StdDraw.text(xCenter, HEIGHT - 1, text);
                StdDraw.show();
                te.drawTiles(grid);
            }

            if (StdDraw.hasNextKeyTyped()) {
                char move = Character.toUpperCase(StdDraw.nextKeyTyped());
                int newX = aX;
                int newY = aY;

                if (move == ':') {
                    colonPressed = true;
                } else if (colonPressed && move == 'Q') {
                    saveGame(grid);
                    System.exit(0);
                } else if (colonPressed && move == 'C') {
                    changeAvatar(grid); // Update avatar
                    continue;
                } else {
                    colonPressed = false;
                }

                if (move == 'W') {
                    newY++;
                } else if (move == 'S') {
                    newY--;
                } else if (move == 'A') {
                    newX--;
                } else if (move == 'D'){
                    newX++;
                }

                //  trigger ambition feature: encounter
                if (grid[newX][newY] == Tileset.FLOWER) {
                    grid[newX][newY] = Tileset.FLOOR;
                    Encounter.startEncounter();
                    te.renderFrame(grid);
                    continue;
                }

                if (newX > 0 && newX < WIDTH
                        && newY > 0 && newY < HEIGHT
                        && grid[newX][newY] != Tileset.WALL) {
                    grid[aX][aY] = Tileset.FLOOR;
                    aX = newX;
                    aY = newY;
                    grid[aX][aY] = avatar;
                    te.renderFrame(grid);
                }
            }
        }
    }

    private static void startGame(long seed) throws IOException {
        World world = new World(WIDTH, HEIGHT, seed);
        world.createRooms();
        TETile[][] grid = world.getWorld();

        boolean start = true;
        for (int x = 0; x < WIDTH && start; x++) {
            for (int y = 0; y < HEIGHT && start; y++) {
                if (grid[x][y] == Tileset.FLOOR) {
                    aX = x;
                    aY = y;
                    grid[aX][aY] = avatar;
                    start = false;
                }
            }
        }

        // put random numbers of tileset.flower for encounter at random places
        Random random = new Random();
        int num = RandomUtils.uniform(random, 2, 6);
        int n = 0;
        for (int i = 0; i < 100 && n < num; i++){
            int putX = RandomUtils.uniform(random, 1, 60);
            int putY = RandomUtils.uniform(random, 1, 30);
            if (grid[putX][putY] == Tileset.FLOOR) {
                grid[putX][putY] = Tileset.FLOWER;
                n++;
            }
        }

        playGame(grid);
    }

    public static void changeAvatar(TETile[][] grid) throws IOException {
        // Display avatar options
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(xCenter, yCenter + 10, "(1) Prince");
        StdDraw.text(xCenter, yCenter + 5, "(2) Princess");
        StdDraw.text(xCenter, yCenter, "(3) Mummy");
        StdDraw.text(xCenter, yCenter - 5, "(4) Naruto");
        StdDraw.show();

        String fileName;
        BufferedImage avatarImage;

        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char a = StdDraw.nextKeyTyped();
                switch (a) {
                    case '1' -> fileName = "prince.png";
                    case '2' -> fileName = "Princess.png";
                    case '3' -> fileName = "mummy.png";
                    case '4' -> fileName = "naruto.png";
                    default -> fileName = null;
                }
                break;
            }
        }

        if (fileName != null) {
            String filePath = "src/gameAvatar/" + fileName;

            // Update the grid with the new avatar
            avatar = new TETile(fileName.charAt(0), Color.WHITE, Color.BLACK, "Avatar", filePath, 13);
            grid[aX][aY] = avatar;

            StdDraw.clear(Color.BLACK);
            StdDraw.setPenColor(Color.WHITE);
            StdDraw.text(xCenter, yCenter, "Avatar Updated Successfully!");
            StdDraw.show();
            StdDraw.pause(1000);
        }
    }
}
