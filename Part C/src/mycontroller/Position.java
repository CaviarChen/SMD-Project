package mycontroller;

import java.util.Objects;

public class Position {
    public float x;
    public float y;


    public Position(float x, float y){
        this.x = x;
        this.y = y;
    }

    public String toString(){
        return x+","+y;
    }

    /**
     * Defined in order to use it as keys in a hashmap
     */
    public boolean equals(Object c){
        if(c == this){
            return true;
        }
        if(!(c instanceof Position)){
            return false;
        }
        Position pos = (Position) c;
        return (pos.x == this.x) && (pos.y == this.y);
    }

    public int hashCode(){
        return Objects.hash(x,y);
    }

}