public class CritterMain {
    public static void main(String[] args) {
        CritterFrame frame = new CritterFrame(60, 40);

         frame.add(30, Giant.class);

        frame.add(30, FlyTrap.class);
        frame.add(30, Food.class);

        frame.start();
    }
}
