package core;

import tileengine.TETile;
import tileengine.Tileset;

public class Hallway {
    private final TETile floor;

    public Hallway() {
        this.floor = Tileset.FLOOR;
    }

    public void createHallway(int startX, int startY, int endX, int endY, TETile[][] world) {
        if (startX == endX) {
            createVerticalHallway(startX, startY, endY, world);
        } else if (startY == endY) {
            createHorizontalHallway(startX, startY, endX, world);
        } else {
            int turnX = (startX + endX) / 2; // Turn at the middle point
            createHorizontalHallway(startX, startY, turnX, world);
            createVerticalHallway(turnX, startY, endY, world);
            createHorizontalHallway(turnX, endY, endX, world);
        }
    }

    public void createHorizontalHallway(int startX, int startY, int endX, TETile[][] world) {
        assert startX != endX;
        if (startX < endX) { // Create hallway from left to right
            for (int x = startX; x <= endX; x++) {
                if (world[x][startY] == Tileset.NOTHING || world[x][startY] == Tileset.WALL) {
                    world[x][startY] = this.floor;
                    addWallsAroundHallway(x, startY, world);
                }
            }
        } else { // Handle reversed order
            for (int x = startX; x >= endX; x--) {
                if (world[x][startY] == Tileset.NOTHING || world[x][startY] == Tileset.WALL) {
                    world[x][startY] = this.floor;
                    addWallsAroundHallway(x, startY, world);
                }
            }
        }

    }

    public void createVerticalHallway(int startX, int startY, int endY, TETile[][] world) {
        assert startY != endY;
        if (startY < endY) { // Going upward
            for (int y = startY; y <= endY; y++) {
                if (world[startX][y] != Tileset.NOTHING || world[startX][y] == Tileset.WALL) {
                    world[startX][y] = this.floor;
                    addWallsAroundHallway(startX, y, world);
                }
            }
        } else { // Handle reversed order
            for (int y = startY; y >= endY; y--) {
                if (world[startX][y] != Tileset.NOTHING || world[startX][y] == Tileset.WALL) {
                    world[startX][y] = this.floor;
                    addWallsAroundHallway(startX, y, world);
                }
            }
        }
    }

    public  void addWallsAroundHallway(int x, int y, TETile[][] world) {
        if (world[x][y + 1] == Tileset.NOTHING) {
            world[x][y + 1] = Tileset.WALL;
        }
        if (world[x][y - 1] == Tileset.NOTHING) {
            world[x][y - 1] = Tileset.WALL;
        }
        if (world[x - 1][y] == Tileset.NOTHING) {
            world[x - 1][y] = Tileset.WALL;
        }
        if (world[x + 1][y] == Tileset.NOTHING) {
            world[x + 1][y] = Tileset.WALL;
        };
    }
}
