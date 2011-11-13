import java.io.IOException;
import java.util.*;


/**
 * Starter bot implementation. 
 */
public class MyBot extends Bot {
    
	private Map<Tile, Tile> orders = new HashMap<Tile, Tile>();
	private Set<Tile> unseenTiles;
	private Set<Tile> enemyHills = new HashSet<Tile>();

	//Collision tracking
    private boolean doMoveDirection(Tile antLoc, Aim direction) {
        Ants ants = getAnts();
        // Track all moves, prevent collisions
        Tile newLoc = ants.getTile(antLoc, direction);
        if (ants.getIlk(newLoc).isUnoccupied() && !orders.containsKey(newLoc)) {
            ants.issueOrder(antLoc, direction);
            orders.put(newLoc, antLoc);
            return true;
        } else {
            return false;
        }
    }

    //Gathering food
    private boolean doMoveLocation(Tile antLoc, Tile destLoc) {
        Ants ants = getAnts();
        // Track targets to prevent 2 ants to the same location
        List<Aim> directions = ants.getDirections(antLoc, destLoc);
        for (Aim direction : directions) {
            if (doMoveDirection(antLoc, direction)) {
                return true;
            }
        }
        return false;
    }
	
	/**
     * Main method executed by the game engine for starting the bot.
     * 
     * @param args command line arguments
     * 
     * @throws IOException if an I/O error occurs
     */
    public static void main(String[] args) throws IOException {
        new MyBot().readSystemInput();
    }
    
    /**
     * For every ant check every direction in fixed order (N, E, S, W) and move it if the tile is
     * passable.
     */
    @Override
    public void doTurn() {
        Ants ants = getAnts();
        orders.clear();
        Map<Tile, Tile> foodTargets = new HashMap<Tile, Tile>();
        // add all locations to unseen tiles set, run once
        if (unseenTiles == null) {
            unseenTiles = new HashSet<Tile>();
            for (int row = 0; row < ants.getRows(); row++) {
                for (int col = 0; col < ants.getCols(); col++) {
                    unseenTiles.add(new Tile(row, col));
                }
            }
        }
        // remove any tiles that can be seen, run each turn
        for (Iterator<Tile> locIter = unseenTiles.iterator(); locIter.hasNext(); ) {
            Tile next = locIter.next();
            if (ants.isVisible(next)) {
                locIter.remove();
            }
        }
        
        //prevent stepping on hill
        for (Tile myHill : ants.getMyHills()) {
        	orders.put(myHill, null);
        }
        // find close food
        List<Route> foodRoutes = new ArrayList<Route>();
        TreeSet<Tile> sortedFood = new TreeSet<Tile>(ants.getFoodTiles());
        TreeSet<Tile> sortedAnts = new TreeSet<Tile>(ants.getMyAnts());
        for (Tile foodLoc : sortedFood) {
            for (Tile antLoc : sortedAnts) {
                int distance = ants.getDistance(antLoc, foodLoc);
                Route route = new Route(antLoc, foodLoc, distance);
                foodRoutes.add(route);
            }
        }
        Collections.sort(foodRoutes);
        int gatherers = 0;
        boolean moving = false;
        for (Route route : foodRoutes) {
            if (!foodTargets.containsKey(route.getEnd())
                    && !foodTargets.containsValue(route.getStart())) {
            	List<Tile> path = null;
            	if (gatherers++ < 15)
            		path = new Pathing().path(route.getStart(), route.getEnd(), ants);
    			if (path != null && path.size() > 1) {
    				moving = doMoveLocation(route.getStart(), path.get(1));
    			} else {
    				moving = doMoveLocation(route.getStart(), route.getEnd());
    			}
    			if (moving)
                foodTargets.put(route.getEnd(), route.getStart());
            }
        }
        
        // add new hills to set
        for (Tile enemyHill : ants.getEnemyHills()) {
            if (!enemyHills.contains(enemyHill)) {
                enemyHills.add(enemyHill);
            }
        }
        
        
        //remove hills that do not exist anymore
        for (Iterator<Tile> locIter = enemyHills.iterator(); locIter.hasNext(); ) {
        	Tile enemyHill = locIter.next();
        	if (ants.isVisible(enemyHill) && !ants.getEnemyHills().contains(enemyHill)) {
        		locIter.remove();
        	}
        }
        
        // attack hills
        List<Route> hillRoutes = new ArrayList<Route>();
        for (Tile hillLoc : enemyHills) {
            for (Tile antLoc : sortedAnts) {
                if (!orders.containsValue(antLoc)) {
                    int distance = ants.getDistance(antLoc, hillLoc);
                    Route route = new Route(antLoc, hillLoc, distance);
                    hillRoutes.add(route);
                }
            }
        }
        Collections.sort(hillRoutes);
        //hillRoutes = hillRoutes.subList(0, 1);
        
        int counter = 0;
        for (Route route : hillRoutes) {
        	List<Tile> path = null;
        	if (counter++ < 25)
        		path = new Pathing().path(route.getStart(), route.getEnd(), ants);
			if (path != null && path.size() > 1) {
				doMoveLocation(route.getStart(), path.get(1));
			} else {
				doMoveLocation(route.getStart(), route.getEnd());
			}
        }
        
        // explore unseen areas
        for (Tile antLoc : sortedAnts) {
            if (!orders.containsValue(antLoc)) {
                List<Route> unseenRoutes = new ArrayList<Route>();
                for (Tile unseenLoc : unseenTiles) {
                    int distance = ants.getDistance(antLoc, unseenLoc);
                    Route route = new Route(antLoc, unseenLoc, distance);
                    unseenRoutes.add(route);
                }
                Collections.sort(unseenRoutes);
                for (Route route : unseenRoutes) {
                    if (doMoveLocation(route.getStart(), route.getEnd())) {
                        break;
                    }
                }
            }
        }
        
        // unblock hills
        for (Tile myHill : ants.getMyHills()) {
            if (ants.getMyAnts().contains(myHill) && !orders.containsValue(myHill)) {
                for (Aim direction : Aim.values()) {
                    if (doMoveDirection(myHill, direction)) {
                        break;
                    }
                }
            }
        }
    }
}
