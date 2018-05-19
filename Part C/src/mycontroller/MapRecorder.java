package mycontroller;

import org.lwjgl.Sys;
import tiles.MapTile;
import utilities.Coordinate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MapRecorder {
    public enum TileStatus {
        UNREACHABLE, SEARCHED, UNSEARCHED
    }

    TileStatus[][] mapStatus;
    MapTile[][] mapTiles;
    int width = 0, height = 0;


    public MapRecorder(HashMap<Coordinate,MapTile> mapHashMap) {

        // find out height and width
        for (Coordinate coord: mapHashMap.keySet()) {
            width = Math.max(coord.x, width);
            height = Math.max(coord.y, height);
        }
        width += 1;
        height += 1;

        mapStatus = new TileStatus[width][height];
        mapTiles = new MapTile[width][height];

        Coordinate startCoord = null;

        for (Map.Entry<Coordinate, MapTile> entry: mapHashMap.entrySet()) {
            int x = entry.getKey().x;
            int y = entry.getKey().y;
            mapTiles[x][y] = entry.getValue();
            if (mapTiles[x][y].getType()==MapTile.Type.START) {
                startCoord = entry.getKey();
            }
        }

        findReachableDFS(startCoord.x, startCoord.y);

        for (int i=0; i<width; i++) {
            for (int j=0; j<height; j++) {
                if (mapStatus[i][j]==null) {
                    mapStatus[i][j] = TileStatus.UNREACHABLE;
                }
            }
        }

        print();
    }

    private void print() {
        System.out.println("------");

        for (int j=height-1; j>=0; j--) {
            for (int i=0; i<width; i++) {
                if (mapStatus[i][j]==TileStatus.UNREACHABLE) {
                    System.out.print('X');
                } else if (mapStatus[i][j]==TileStatus.UNSEARCHED) {
                    System.out.print('?');
                } else {
                    System.out.print(' ');
                }
            }
            System.out.println();
        }


        System.out.println("------");
    }

    public TileStatus[][] getTileStatus() {
        return mapStatus;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }


    public void addCarView(int x, int y, HashMap<Coordinate,MapTile> view) {

        for (Map.Entry<Coordinate, MapTile> entry: view.entrySet()) {
            int tileX = entry.getKey().x;
            int tileY = entry.getKey().y;

            if (inRange(tileX, tileY)) {
                if (mapStatus[tileX][tileY] == TileStatus.UNSEARCHED) {
                    mapStatus[tileX][tileY] = TileStatus.SEARCHED;
                    mapTiles[tileX][tileY] = entry.getValue();
                }
            }
        }
        print();
    }

    private void findReachableDFS(int x, int y) {
        if (!inRange(x, y)) {
            return;
        }

        if (mapStatus[x][y]!=null) {
            return;
        }

        if (mapTiles[x][y].getType()==MapTile.Type.WALL) {
            mapStatus[x][y] = TileStatus.UNREACHABLE;
            return;
        }

        mapStatus[x][y] = TileStatus.UNSEARCHED;

        findReachableDFS(x+1,y);
        findReachableDFS(x-1,y);
        findReachableDFS(x,y+1);
        findReachableDFS(x,y-1);
    }

    public boolean inRange(int x, int y) {
        return !(x<0||x>=width||y<0||y>=height);
    }




}
