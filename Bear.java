import java.awt.*;

public class Bear extends Critter {
    private boolean polar;
    private int moves;

    public Bear(boolean polar) {
        this.polar = polar;
        this.moves = 0; // Initialize moves to 0
    }

    public Color getColor() {
        return this.polar ? Color.WHITE : Color.BLACK;
    }

    public String toString() {
        return (moves % 2 == 0) ? "/" : "\\";
    }

    public Action getMove(CritterInfo info) {
        moves++; // Increment moves counter
        if (info.getFront() == Neighbor.OTHER) {
            return Action.INFECT;
        } else if (info.getFront() == Neighbor.EMPTY) {
            return Action.HOP;
        } else {
            return Action.LEFT;
        }
    }
}
