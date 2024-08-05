import java.awt.*;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.Map.Entry;

public class CritterModel {
    private static final double HOP_ADVANTAGE = 0.2;
    private static boolean instanceExists;

    private final int width;
    private final int height;
    private final Critter[][] grid;
    private final Map<Critter, CritterData> critterInfo;
    private final SortedMap<String, Integer> critterCount;
    private boolean debugMode;
    private int simulationStepCount;

    public CritterModel(int width, int height) {
        if (instanceExists) {
            throw new RuntimeException("Only one instance of CritterModel allowed.");
        }
        instanceExists = true;

        this.width = width;
        this.height = height;
        this.grid = new Critter[width][height];
        this.critterInfo = new HashMap<>();
        this.critterCount = new TreeMap<>();
        this.debugMode = false;
        this.simulationStepCount = 0;
    }

    public Iterator<Critter> iterator() {
        return critterInfo.keySet().iterator();
    }

    public Point getPosition(Critter critter) {
        return critterInfo.get(critter).position;
    }

    public Color getColor(Critter critter) {
        return critterInfo.get(critter).color;
    }

    public String getAppearance(Critter critter) {
        return debugMode ? critterInfo.get(critter).directionSymbol : critter.toString();
    }

    public void addCritters(int number, Class<? extends Critter> critterClass) {
        if (critterInfo.size() + number > width * height) {
            throw new RuntimeException("Too many critters to add.");
        }

        Random random = new Random();
        Critter.Direction[] directions = Critter.Direction.values();
        
        for (int i = 0; i < number; i++) {
            Critter critter = createCritterInstance(critterClass);
            int x, y;
            do {
                x = random.nextInt(width);
                y = random.nextInt(height);
            } while (grid[x][y] != null);
            
            grid[x][y] = critter;
            Critter.Direction direction = directions[random.nextInt(directions.length)];
            critterInfo.put(critter, new CritterData(new Point(x, y), direction));
            
            String critterName = critterClass.getSimpleName();
            critterCount.merge(critterName, 1, Integer::sum);
        }
    }

    private Critter createCritterInstance(Class<? extends Critter> critterClass) {
        try {
            Constructor<? extends Critter> constructor = critterClass.getDeclaredConstructor();
            if (critterClass.equals(Bear.class)) {
                boolean isPolar = Math.random() < 0.5;
                return constructor.newInstance(isPolar);
            } else {
                return constructor.newInstance();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error creating critter instance: " + e.getMessage(), e);
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void toggleDebugMode() {
        this.debugMode = !this.debugMode;
    }

    public void update() {
        simulationStepCount++;
        List<Critter> critters = new ArrayList<>(critterInfo.keySet());
        Collections.shuffle(critters);
        Set<Critter> lockedCritters = new HashSet<>();
        
        for (Critter critter : critters) {
            CritterData data = critterInfo.get(critter);
            if (data == null) continue;

            boolean hasHopped = data.hasHopped;
            data.hasHopped = false;
            Point position = data.position;
            Point newPosition = getNextPosition(position, data.direction);

            Critter.Action action = critter.getMove(getCritterInfo(data, critter.getClass()));
            handleAction(critter, data, position, newPosition, action, hasHopped, lockedCritters);
        }
        updateCritterAppearance();
    }

    private void handleAction(Critter critter, CritterData data, Point position, Point newPosition,
                              Critter.Action action, boolean hasHopped, Set<Critter> lockedCritters) {
        switch (action) {
            case LEFT:
                data.direction = rotateDirection(data.direction, 3);
                break;
            case RIGHT:
                data.direction = rotateDirection(data.direction, 1);
                break;
            case HOP:
                if (isWithinBounds(newPosition) && grid[newPosition.x][newPosition.y] == null) {
                    grid[newPosition.x][newPosition.y] = grid[position.x][position.y];
                    grid[position.x][position.y] = null;
                    data.position = newPosition;
                    data.hasHopped = true;
                    lockedCritters.add(critter);
                }
                break;
            case INFECT:
                handleInfection(critter, data, newPosition, hasHopped, lockedCritters);
                break;
        }
    }

    private void handleInfection(Critter critter, CritterData data, Point newPosition,
                                 boolean hasHopped, Set<Critter> lockedCritters) {
        if (isWithinBounds(newPosition) && grid[newPosition.x][newPosition.y] != null
            && grid[newPosition.x][newPosition.y].getClass() != critter.getClass()
            && !lockedCritters.contains(grid[newPosition.x][newPosition.y])
            && (hasHopped || Math.random() >= HOP_ADVANTAGE)) {

            Critter otherCritter = grid[newPosition.x][newPosition.y];
            String otherName = otherCritter.getClass().getSimpleName();
            critterCount.merge(otherName, -1, Integer::sum);

            String newName = critter.getClass().getSimpleName();
            critterCount.merge(newName, 1, Integer::sum);

            critterInfo.remove(otherCritter);
            try {
                Critter newCritter = createCritterInstance(critter.getClass());
                grid[newPosition.x][newPosition.y] = newCritter;
                lockedCritters.add(newCritter);
                critterInfo.put(newCritter, new CritterData(newPosition, data.direction));
            } catch (Exception e) {
                throw new RuntimeException("Error creating critter instance: " + e.getMessage(), e);
            }
        }
    }

    private Critter.Direction rotateDirection(Critter.Direction direction, int steps) {
        return Critter.Direction.values()[(direction.ordinal() + steps) % 4];
    }

    private Point getNextPosition(Point position, Critter.Direction direction) {
        switch (direction) {
            case NORTH:
                return new Point(position.x, position.y - 1);
            case SOUTH:
                return new Point(position.x, position.y + 1);
            case EAST:
                return new Point(position.x + 1, position.y);
            case WEST:
                return new Point(position.x - 1, position.y);
            default:
                throw new IllegalArgumentException("Unknown direction: " + direction);
        }
    }

    private boolean isWithinBounds(Point point) {
        return point.x >= 0 && point.x < width && point.y >= 0 && point.y < height;
    }

    private CritterInfo getCritterInfo(CritterData data, Class<? extends Critter> critterClass) {
        Critter.Neighbor[] neighbors = new Critter.Neighbor[4];
        Critter.Direction direction = data.direction;
        boolean[] threats = new boolean[4];

        for (int i = 0; i < 4; i++) {
            Point neighborPoint = getNextPosition(data.position, direction);
            neighbors[i] = getNeighborStatus(neighborPoint, critterClass);
            if (neighbors[i] == Critter.Neighbor.OTHER) {
                Critter otherCritter = grid[neighborPoint.x][neighborPoint.y];
                threats[i] = direction == rotateDirection(rotateDirection(otherCritter.getDirection(), 2), 2);
            }
            direction = rotateDirection(direction, 1);
        }

        return new CritterInfoImpl(neighbors, data.direction, threats);
    }

    private Critter.Neighbor getNeighborStatus(Point point, Class<? extends Critter> critterClass) {
        if (!isWithinBounds(point)) return Critter.Neighbor.WALL;
        if (grid[point.x][point.y] == null) return Critter.Neighbor.EMPTY;
        return grid[point.x][point.y].getClass().equals(critterClass) ? Critter.Neighbor.SAME : Critter.Neighbor.OTHER;
    }

    public void updateCritterAppearance() {
        for (Critter critter : critterInfo.keySet()) {
            CritterData data = critterInfo.get(critter);
            data.color = critter.getColor();
            data.directionSymbol = critter.toString();
        }
    }

    public Set<Entry<String, Integer>> getCritterCounts() {
        return Collections.unmodifiableSet(critterCount.entrySet());
    }

    public int getSimulationStepCount() {
        return simulationStepCount;
    }

    private static class CritterData {
        private Point position;
        private Critter.Direction direction;
        private Color color;
        private String directionSymbol;
        private boolean hasHopped;

        public CritterData(Point position, Critter.Direction direction) {
            this.position = position;
            this.direction = direction;
            this.color = Color.BLACK;
            this.directionSymbol = "?";
            this.hasHopped = false;
        }
    }

    private static class CritterInfoImpl implements CritterInfo {
        private final Critter.Neighbor[] neighbors;
        private final Critter.Direction direction;
        private final boolean[] threats;

        public CritterInfoImpl(Critter.Neighbor[] neighbors, Critter.Direction direction, boolean[] threats) {
            this.neighbors = neighbors;
            this.direction = direction;
            this.threats = threats;
        }

        @Override
        public Critter.Neighbor getFront() {
            return neighbors[0];
        }

        @Override
        public Critter.Neighbor getBack() {
            return neighbors[2];
        }

        @Override
        public Critter.Neighbor getLeft() {
            return neighbors[3];
        }

        @Override
        public Critter.Neighbor getRight() {
            return neighbors[1];
        }

        @Override
        public Critter.Direction getDirection() {
            return direction;
        }

        @Override
        public boolean frontThreat() {
            return threats[0];
        }

        @Override
        public boolean backThreat() {
            return threats[2];
        }

        @Override
        public boolean leftThreat() {
            return threats[3];
        }

        @Override
        public boolean rightThreat() {
            return threats[1];
        }
    }
}
