package core;

import java.io.*;

public class Main {
    public static final int WIDTH = 80;
    public static final int HEIGHT = 40;

    public static void main(String[] args) throws IOException {
//        long seed1 = 6578897764558030256L;
//        long seed2 = 1013758890894698811L;
//        long seed3 = 7162790311120124118L;
//        long seed4 = 7003957710856902839L;
//        long seed5 = 6206686636164176845L;
//
//        World world1 = new World(WIDTH, HEIGHT, seed1);
//        World world2 = new World(WIDTH, HEIGHT, seed2);
//        World world3 = new World(WIDTH, HEIGHT, seed3);
//        World world4 = new World(WIDTH, HEIGHT, seed4);
//        World world5 = new World(WIDTH, HEIGHT, seed5);

        // Generate rooms and connect them with hallways
//        world4.createRooms();
//        TETile[][] tiles = world4.getWorld();

//        TERenderer te = new TERenderer();ddd
//        te.initialize(WIDTH, HEIGHT);

//        te.renderFrame(tiles);
//        te.drawTiles(tiles);
        Game.MainMenuAppearance();
        Game.MainMenuFeatures();
    }
}
