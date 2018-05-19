package mycontroller;

import tiles.LavaTrap;
import tiles.MapTile;
import java.util.ArrayList;
import java.util.PriorityQueue;

public class AStar {

    private MapRecorder.TileStatus[][] mapStatus;
    private MapTile[][] mapTiles;
    private int width;
    private int height;
    private Node start;
    private Node end;

    private PriorityQueue<Node> openList = new PriorityQueue<>(); // priority queue (ascending)
    private ArrayList<Node> closeList = new ArrayList<>();

    public AStar(MapRecorder mapRecorder, int x1, int y1, int x2, int y2) {
        width = mapRecorder.getWidth();
        height = mapRecorder.getHeight();
        mapStatus = mapRecorder.mapStatus;
        mapTiles = mapRecorder.mapTiles;

        end = new Node(x1, y1);
        start = new Node(x2, y2);

    }

    // Manhattan distance as heuristic distance
    private int calcH(Coord end, Coord coord) {
        return Math.abs(end.x - coord.x) + Math.abs(end.y - coord.y);
    }

    // check whether is the end coordinate
    private boolean isEndNode(Coord end, Coord coord) {
        return end.equals(coord);
    }

    private boolean canAddNodeToOpen(int x, int y) {
        // check whether is in map
        if (x < 0 || x >= width || y < 0 || y >= height) return false;
        // check whether is UNREACHABLE
        if (mapStatus[x][y] == MapRecorder.TileStatus.UNREACHABLE) return false;
        // check node whether exists in closeList
        return !isCoordInClose(x, y);
    }

    // check whether coordinate
    private boolean isCoordInClose(Coord coord) {
        return coord!=null && isCoordInClose(coord.x, coord.y);
    }

    private boolean isCoordInClose(int x, int y) {
        if (closeList.isEmpty()) return false;
        for (Node node : closeList) {
            if (node.coord.x == x && node.coord.y == y) return true;
        }
        return false;
    }

    private Node findNodeInOpen(Coord coord) {
        if (coord == null || openList.isEmpty()) return null;
        for (Node node : openList) {
            if (node.coord.equals(coord)) return node;
        }
        return null;
    }

    // add all neighbor nodes into openList
    private void addNeighborNodeInOpen(Node current) {
        int x = current.coord.x;
        int y = current.coord.y;
        addNeighborNodeInOpen(current, x - 1, y);    // left
        addNeighborNodeInOpen(current, x, y - 1);    // up
        addNeighborNodeInOpen(current, x + 1, y);    // right
        addNeighborNodeInOpen(current, x, y + 1);    // down
    }

    // add a neighbor node into openList
    private void addNeighborNodeInOpen(Node current, int x, int y) {
        if (canAddNodeToOpen(x, y)) {
            Coord coord = new Coord(x, y);

            int value = 1;
            if (mapTiles[x][y] instanceof LavaTrap) value += 4;

            int G = current.G + value; // calculate G value for neighbor node
            Node child = findNodeInOpen(coord);
            if (child == null) {
                int H=calcH(end.coord,coord); // calculate H value
                if (isEndNode(end.coord,coord)) {
                    child = end;
                    child.parent = current;
                    child.G = G;
                    child.H = H;
                } else {
                    child = new Node(coord, current, G, H);
                }
                openList.add(child);
            } else if (child.G > G) {
                child.G = G;
                child.parent = current;
                // re-adjust heap
                openList.add(child);
            }
        }
    }

    public ArrayList<Node> start() {
        // clean
        openList.clear();
        closeList.clear();
        // start search
        openList.add(start);
        moveNodes();
        // path from start to end
        ArrayList<Node> path = new ArrayList<>();
        // add to path
        while (end != null) {
            path.add(end);
            end = end.parent;
        }
        return path;
    }

    // move the nodes
    private void moveNodes() {
        while (!openList.isEmpty()) {
            if (isCoordInClose(end.coord)) break;

            Node current = openList.poll();
            closeList.add(current);
            if (current != null) addNeighborNodeInOpen(current);
        }
    }

}

class Coord {

    public int x;
    public int y;

    public Coord(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof Coord) {
            Coord c = (Coord) obj;
            return x == c.x && y == c.y;
        }
        return false;
    }
}

class Node implements Comparable<Node> {

    public Coord coord; // coordinate
    public Node parent; // parent
    public int G; // G: correct value, from start to current node
    public int H; // H: estimated value, from current node to end

    public Node(int x, int y) {
        this.coord = new Coord(x, y);
    }

    public Node(Coord coord, Node parent, int g, int h) {
        this.coord = coord;
        this.parent = parent;
        G = g;
        H = h;
    }

    @Override
    public int compareTo(Node o) {
        if (o == null) return -1;
        if (G + H > o.G + o.H)
            return 1;
        else if (G + H < o.G + o.H) return -1;
        return 0;
    }

}
