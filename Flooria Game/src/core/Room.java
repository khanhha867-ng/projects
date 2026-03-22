package core;

import tileengine.TETile;
import tileengine.Tileset;

public class Room {
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    public Room (int x, int y, int width, int height){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void walls(TETile[][] world) {
        for (int i = x - 1; i <= x + width; i++) {
            for (int j = y - 1; j <= y + height; j++) {
                if (i < world.length && j < world[0].length) {
                    if (world[i][j] == Tileset.NOTHING) {
                        world[i][j] = Tileset.WALL;
                    }
                }
            }
        }
    }

    public void render(TETile[][] world) {
        for (int i = x; i < x + width; i++) {
            for (int j = y; j < y + height; j++) {
                world[i][j] = Tileset.FLOOR;
            }
        }
        walls(world);
    }

    public int centerX () {
        return x + width/2;
    }

    public int centerY () {
        return y + height/2;
    }
}
