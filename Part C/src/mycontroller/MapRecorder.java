package mycontroller;

import tiles.HealthTrap;
import tiles.LavaTrap;
import tiles.MapTile;
import utilities.Coordinate;
import world.World;

import java.util.*;

/**
 * Class that responsible for recording the map for MyAIController
 */
public class MapRecorder {

    // status enum for tile
    public enum TileStatus {
        UNREACHABLE, SEARCHED, UNSEARCHED
    }

    public static final int LAVA_FOUND     = 0b001;
    public static final int NEXT_KEY_FOUND = 0b010;


    public TileStatus[][] mapStatus;
    public MapTile[][] mapTiles;

    // Coordinate for special tile
    public Coordinate[] keysCoord;
    public HashSet<Coordinate> finishCoords = new HashSet<>();
    public HashSet<Coordinate> healthCoords = new HashSet<>();

    public int width = World.MAP_WIDTH, height = World.MAP_HEIGHT;


    /**
     * Constructor
     * @param mapHashMap map get from CarController.getMap()
     * @param keySize the number of key in this map
     */
    public MapRecorder(HashMap<Coordinate, MapTile> mapHashMap, int keySize) {
        
        mapStatus = new TileStatus[width][height];
        mapTiles = new MapTile[width][height];
        keysCoord = new Coordinate[keySize-1];

        Coordinate startCoord = null;

        // add all tiles into mapTiles
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

        // use DFS to find out all reachable tiles in this map
        findReachableDFS(startCoord.x, startCoord.y);

        // mark everything unreachable if not reached by DFS
        for (int i=0; i<width; i++) {
            for (int j=0; j<height; j++) {
                if (mapStatus[i][j]==null) {
                    mapStatus[i][j] = TileStatus.UNREACHABLE;
                }
            }
        }

        // mark finish tiles as searched
        for (Coordinate coord: finishCoords) {
            mapStatus[coord.x][coord.y] = TileStatus.SEARCHED;
        }

    }

    /**
     * print the map to stdout for debug
     */
    private void printMap() {
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


    /**
     * Add a view of the car to the map.
     * @param view from CarController.getView()
     * @param currentKey current number of key
     * @return a status that indicates special tiles found (0 or a sum of flags responding to type of tiles found).
     * Including LAVA_FOUND and NEXT_KEY_FOUND
     */
    public int addCarView(HashMap<Coordinate,MapTile> view, int currentKey) {

        int tilesFoundFlag = 0;

        for (Map.Entry<Coordinate, MapTile> entry: view.entrySet()) {
            int tileX = entry.getKey().x;
            int tileY = entry.getKey().y;

            if (inRange(tileX, tileY)) {
                if (mapStatus[tileX][tileY] == TileStatus.UNSEARCHED) {
                    // mark as searched
                    mapStatus[tileX][tileY] = TileStatus.SEARCHED;
                    mapTiles[tileX][tileY] = entry.getValue();


                    if (mapTiles[tileX][tileY] instanceof LavaTrap)  {
                        tilesFoundFlag |= LAVA_FOUND;
                        LavaTrap trap = (LavaTrap) mapTiles[tileX][tileY];
                        if (trap.getKey() > 0) {
                            if (trap.getKey() == currentKey - 1)
                                tilesFoundFlag |= NEXT_KEY_FOUND;
                            if (Arrays.asList(keysCoord).get(trap.getKey() - 1) == null) {
                                keysCoord[trap.getKey() - 1] = new Coordinate(tileX, tileY);
                            }
                        }
                    } else if (mapTiles[tileX][tileY] instanceof HealthTrap) {
                        healthCoords.add(new Coordinate(tileX, tileY));
                    }
                }
            }
        }

        return tilesFoundFlag;
    }

    /**
     * Use DFS to find out all reachable position
     * @param x starting position
     * @param y starting position
     */
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

    /**
     * @param x a position
     * @param y a position
     * @return true if the given position is in the map range
     */
    public boolean inRange(int x, int y) {
        return !(x<0||x>=width||y<0||y>=height);
    }

    /**
     * @return a list of coordinates that can uncover all cells when visited
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
            Coordinate nonFireCoordinate = null, fireCoordinate = null, exploreCoordinate;
            for (int j = 4; j >= 0; j--) {
                if (i.x + j < width && i.y + j < height &&
                        mapStatus[i.x + j][i.y + j] != TileStatus.UNREACHABLE) {
                    Coordinate c = new Coordinate(i.x + j, i.y + j);
                    if (mapTiles[i.x + j][i.y + j] instanceof LavaTrap && fireCoordinate == null)
                        fireCoordinate = c;
                    else {
                        nonFireCoordinate = c;
                        break;
                    }
                }
            }
            exploreCoordinate = nonFireCoordinate != null ? nonFireCoordinate : fireCoordinate;
            if (exploreCoordinate == null) continue;
            queue.add(exploreCoordinate);
            for (int x = exploreCoordinate.x - 4; x < exploreCoordinate.x + 5; x++)
                for (int y = exploreCoordinate.y - 4; y < exploreCoordinate.y + 5; y++) {
                    coordinatesPending.remove(new Coordinate(x, y));
                }
        }

        return queue;
    }

}
