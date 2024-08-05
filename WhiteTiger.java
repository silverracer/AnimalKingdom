import java.awt.*;

public class WhiteTiger extends Tiger {
    boolean hasInfected;

    public WhiteTiger(){
        hasInfected=false;
    }


    public Color getColor() {
        return Color.WHITE;
    }


    public String toString() {
        if (hasInfected){
            return super.toString();
        } else {
            return "tgr";
        }
    }


    public Action getMove(CritterInfo info) {
        if (info.getFront()==Neighbor.OTHER){
            hasInfected=true;
        }
        return super.getMove(info);

    }
}
