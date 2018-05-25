package mycontroller;

import tiles.LavaTrap;
import utilities.Coordinate;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class AStar implements Pipeline.Step<ArrayList<Position>, MapRecorder> {

    class Node implements Comparable<Node> {
        public Coordinate coord; // coordinate
        public Node parent; // parent
        public int G; // G: correct value, from start to current node
        public int H; // H: estimated value, from current node to end

        public Node(int x, int y) {
            this.coord = new Coordinate(x, y);
        }

        public Node(Coordinate coord, Node parent, int g, int h) {
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

    @Override
    public ArrayList<Position> execute(ArrayList<Position> input, MapRecorder mapRecorder) {

        Position destination = input.get(input.size() - 1);
        input.remove(input.size() - 1);

        Node end = start(mapRecorder, destination, input);

        // path
        ArrayList<Position> path = new ArrayList<>();

        while (end != null) {
            path.add(new Position(end.coord));
            end = end.parent;
        }
        return path;

    }

    public Node start(MapRecorder mapRecorder, Position source, ArrayList<Position> destination) {

        PriorityQueue<Node> openList = new PriorityQueue<>();
        ArrayList<Node> closeList = new ArrayList<>();

        Node end = new Node(Math.round(source.x), Math.round(source.y));


        for (Position i: destination) {
            openList.add(new Node(Math.round(i.x), Math.round(i.y)));
        }

        // start search
        while (!openList.isEmpty()) {
//            if (isCoordInClose(closeList, end.coord)) break;
            if (end.coord != null && isCoordInClose(closeList, end.coord.x, end.coord.y)) break;

            Node current = openList.poll();
            closeList.add(current);
            if (current != null) addNeighborNodeInOpen(mapRecorder, openList, closeList, end, current);
        }

        return end;
    }

    // Manhattan distance as heuristic distance
    private int calcH(Coordinate end, Coordinate coord) {
        return Math.abs(end.x - coord.x) + Math.abs(end.y - coord.y);
    }

    // check whether is the end coordinate
    private boolean isEndNode(Coordinate end, Coordinate coord) {
        return end.equals(coord);
    }

    private boolean canAddNodeToOpen(MapRecorder mapRecorder, ArrayList<Node> closeList, int x, int y) {
        // check whether is in map
        if (x < 0 || x >= mapRecorder.width || y < 0 || y >= mapRecorder.height) return false;
        // check whether is UNREACHABLE
        if (mapRecorder.mapStatus[x][y] == MapRecorder.TileStatus.UNREACHABLE) return false;
        // check node whether exists in closeList
        return !isCoordInClose(closeList, x, y);
    }

    // check whether coordinate
//    private boolean isCoordInClose(ArrayList<Node> closeList, Coordinate coord) {
//        return coord != null && isCoordInClose(closeList, coord.x, coord.y);
//    }

    private boolean isCoordInClose(ArrayList<Node> closeList, int x, int y) {
        if (closeList.isEmpty()) return false;
        for (Node node : closeList) {
            if (node.coord.x == x && node.coord.y == y) return true;
        }
        return false;
    }

    private Node findNodeInOpen(PriorityQueue<Node> openList, Coordinate coord) {
        if (coord == null || openList.isEmpty()) return null;
        for (Node node : openList) {
            if (node.coord.equals(coord)) return node;
        }
        return null;
    }

    // add all neighbor nodes into openList
    private void addNeighborNodeInOpen(MapRecorder mapRecorder, PriorityQueue<Node> openList, ArrayList<Node> closeList, Node end, Node current) {
        int x = current.coord.x;
        int y = current.coord.y;
        addNeighborNodeInOpen(mapRecorder, openList, closeList, end, current, x - 1, y);    // left
        addNeighborNodeInOpen(mapRecorder, openList, closeList, end, current, x, y - 1);    // up
        addNeighborNodeInOpen(mapRecorder, openList, closeList, end, current, x + 1, y);    // right
        addNeighborNodeInOpen(mapRecorder, openList, closeList, end, current, x, y + 1);    // down
    }

    // add a neighbor node into openList
    private void addNeighborNodeInOpen(MapRecorder mapRecorder, PriorityQueue<Node> openList, ArrayList<Node> closeList,
                                       Node end, Node current, int x, int y) {
        if (canAddNodeToOpen(mapRecorder, closeList, x, y)) {
            Coordinate coord = new Coordinate(x, y);

            int value = 1;
            // add weight for lava trap
            if (mapRecorder.mapTiles[x][y] instanceof LavaTrap) value += 25;
            // add weight for turning
            if (current.parent != null && current.parent.parent != null) {
                int parentXDiff = current.parent.coord.x - current.parent.parent.coord.x;
                int parentYDiff = current.parent.coord.y - current.parent.parent.coord.y;
                int xDiff = current.coord.x - current.parent.coord.x;
                int yDiff = current.coord.y - current.parent.coord.y;

                if (parentXDiff != xDiff || parentYDiff != yDiff) value += 10;
            }

            int G = current.G + value; // calculate G value for neighbor node
            Node child = findNodeInOpen(openList, coord);
            if (child == null) {
                int H = calcH(end.coord, coord); // calculate H value
                if (isEndNode(end.coord, coord)) {
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

}


