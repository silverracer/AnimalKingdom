import java.awt.*;

public class Critter {

    // Enumeration to represent the type of neighbor in a given direction
    public static enum Neighbor {
        WALL, EMPTY, SAME, OTHER
    }

    // Enumeration to represent the possible actions a critter can take
    public static enum Action {
        HOP, LEFT, RIGHT, INFECT
    }

    // Enumeration to represent the possible directions a critter can face
    public static enum Direction {
        NORTH, SOUTH, EAST, WEST
    }

    // Default method to determine the action a critter will take
    public Action getMove(CritterInfo info) {
        return Action.LEFT;  // Default action is to turn left
    }

    // Default method to get the color of the critter
    public Color getColor() {
        return Color.BLACK;  // Default color is black
    }

    // Default method to get the string representation of the critter
    public String toString() {
        return "?";  // Default representation is "?"
    }

    // Override the equals method to ensure critters are considered equal if they are the same instance
    public final boolean equals(Object other) {
        return this == other;
    }
}