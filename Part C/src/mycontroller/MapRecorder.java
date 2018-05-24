package mycontroller;

import tiles.HealthTrap;
import tiles.LavaTrap;
import tiles.MapTile;
import utilities.Coordinate;
import world.World;

import java.util.*;

public class MapRecorder {
    public enum TileStatus {
        UNREACHABLE, SEARCHED, UNSEARCHED
    }

    TileStatus[][] mapStatus;
    MapTile[][] mapTiles;
    Coordinate[] keysCoord;
    ArrayList<Coordinate> finishCoords = new ArrayList<>();
    ArrayList<Coordinate> healthCoords = new ArrayList<>();
    int width = World.MAP_WIDTH, height = World.MAP_HEIGHT;


    public MapRecorder(HashMap<Coordinate, MapTile> mapHashMap, int keySize) {
        
        mapStatus = new TileStatus[width][height];
        mapTiles = new MapTile[width][height];
        keysCoord = new Coordinate[keySize-1];

        Coordinate startCoord = null;

        for (Map.Entry<Coordinate, MapTile> entry: mapHashMap.entrySet()) {
            int x = entry.getKey().x;
            int y = entry.getKey().y;
            mapTiles[x][y] = entry.getValue();
            if (mapTiles[x][y].getType()==MapTile.Type.START) {
                startCoord = entry.getKey();
            } else if (mapTiles[x][y].getType() == MapTile.Type.FINISH) {
                finishCoords.add(entry.getKey());
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
                } else if (Arrays.asList(keysCoord).contains(new Coordinate(i, j))) {
                        System.out.print(Arrays.asList(keysCoord).indexOf(new Coordinate(i, j)) + 1);
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


    public boolean addCarView(int x, int y, HashMap<Coordinate,MapTile> view) {

        boolean lavaFound = false;

        for (Map.Entry<Coordinate, MapTile> entry: view.entrySet()) {
            int tileX = entry.getKey().x;
            int tileY = entry.getKey().y;

            if (inRange(tileX, tileY)) {
                if (mapStatus[tileX][tileY] == TileStatus.UNSEARCHED) {
                    mapStatus[tileX][tileY] = TileStatus.SEARCHED;
                    mapTiles[tileX][tileY] = entry.getValue();
//                    System.out.println("---" + mapTiles[tileX][tileY]);
                    if (mapTiles[tileX][tileY] instanceof LavaTrap)  {
                        lavaFound = true;
                        LavaTrap trap = (LavaTrap) mapTiles[tileX][tileY];
                        if (trap.getKey() > 0) {
//                            System.out.println("Size:" + keysCoord.length + "Key:" + trap.getKey());
                            if (Arrays.asList(keysCoord).get(trap.getKey()-1) == null) {
                                keysCoord[trap.getKey()-1] = new Coordinate(tileX, tileY);
                            }
                        }
                    } else if (mapTiles[tileX][tileY] instanceof HealthTrap) {
                        healthCoords.add(new Coordinate(tileX, tileY));
                    }
                }
            }
        }
        print();

        return lavaFound;
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

    /**
     * Return a list of coordinates that can uncover all cells when visited
     * Coordinates are returned in no order.
     */
    public ArrayList<Coordinate> coordinatesToExplore() {
        HashSet<Coordinate> coordinatesPending = new HashSet<>();
        ArrayList<Coordinate> queue = new ArrayList<>();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (mapStatus[i][j] == TileStatus.UNSEARCHED) {
                    coordinatesPending.add(new Coordinate(i, j));
                }
            }
        }

        while (!coordinatesPending.isEmpty()) {
            Coordinate i = coordinatesPending.iterator().next();
            for (int j = 4; j >= 0; j--) {
                if (i.x + j < width && i.y + j < height &&
                        mapStatus[i.x + j][i.y + j] != TileStatus.UNREACHABLE) {
                    queue.add(new Coordinate(i.x + j, i.y + j));
                    for (int x = i.x + j - 4; x < i.x + j + 5; x++)
                        for (int y = i.y + j - 4; y < i.y + j + 5; y++) {
                            coordinatesPending.remove(new Coordinate(x, y));
                        }
                    break;
                }
            }
        }

        return queue;
    }

    /**
     * Get the point for map exploration nearest to the provided location
     */
    public Coordinate getNearestExplorationPoint(float x, float y) {
        ArrayList<Coordinate> list = coordinatesToExplore();
        Coordinate nearest = null;
        double distance = Double.POSITIVE_INFINITY;
        for (Coordinate i: list) {
            double thisDistance = Math.sqrt((i.x - x) * (i.x - x) + (i.y - y) * (i.y - y));
            if (thisDistance < distance) {
                distance = thisDistance;
                nearest = i;
            }
        }
        return nearest;
    }


}
