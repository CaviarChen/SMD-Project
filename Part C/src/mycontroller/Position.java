/**
 * Group 44
 */

package mycontroller;

import utilities.Coordinate;

import java.util.Objects;


/**
 * Float number version of the Coordinate
 */
public class Position {
    public float x;
    public float y;

    /**
     * Constructor
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    public Position(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Constructor
     *
     * @param coord create from this Coordinate object
     */
    public Position(Coordinate coord) {
        this.x = coord.x;
        this.y = coord.y;
    }

    /**
     * return int version of this position
     *
     * @return the required Coordinate object
     */
    public Coordinate toCoordinate() {
        return new Coordinate(Math.round(x), Math.round(y));
    }


    public String toString() {
        return x + "," + y;
    }

    /**
     * Defined in order to use it as keys in a hashmap
     */
    public boolean equals(Object c) {
        if (c == this) {
            return true;
        }
        if (!(c instanceof Position)) {
            return false;
        }
        Position pos = (Position) c;
        return (pos.x == this.x) && (pos.y == this.y);
    }

    public int hashCode() {
        return Objects.hash(x, y);
    }

}