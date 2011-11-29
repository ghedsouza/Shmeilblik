import static java.lang.System.err;

import java.io.IOException;
import java.util.*;

/**
 * Starter bot implementation.
 */
public class MyBot extends Bot {

  Map<Tile, Tile> orders = new HashMap<Tile, Tile>();
  Map<Tile, List<Tile>> pathings = new HashMap<Tile, List<Tile>>();
  Set<Tile> unseenTiles;
  Set<Tile> enemyHills = new HashSet<Tile>();
  Random rand = new Random(0);
  int turn = 0;

  int explorerIndex = 0, attackerIndex = 0;

  // Collision tracking
  private boolean doMoveDirection(Tile antLoc, Aim direction) {
    Ants ants = getAnts();
    // Track all moves, prevent collisions
    Tile newLoc = ants.getTile(antLoc, direction);
    if (ants.getIlk(newLoc).isUnoccupied() && !orders.containsKey(newLoc))
    {
      ants.issueOrder(antLoc, direction); // actually perform move
      orders.put(newLoc, antLoc);
      return true;
    }
    else
    {
      return false;
    }
  }

  // Gathering food
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
   * @param args
   *          command line arguments
   * 
   * @throws IOException
   *           if an I/O error occurs
   */
  public static void main(String[] args) throws IOException {
    (new MyBot()).readSystemInput();
  }

  /**
   * For every ant check every direction in fixed order (N, E, S, W) and move it
   * if the tile is passable.
   */
  @Override
  public void doTurn() {
    err.println("Begin turn " + (turn++));
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
    //err.println("unseen");
    // remove any tiles that can be seen, run each turn
    for (Iterator<Tile> locIter = unseenTiles.iterator(); locIter.hasNext();) {
      Tile next = locIter.next();
      if (ants.isVisible(next)) {
        locIter.remove();
      }
    }

    // reexplore random tiles again
    for (int row = 0; row < ants.getRows(); row++) {
      for (int col = 0; col < ants.getCols(); col++) {
        Tile tile = new Tile(row, col);
        if (!ants.isVisible(tile) && ants.getIlk(tile).isPassable() && rand.nextInt(50) == 0)
          unseenTiles.add(tile);
      }
    }

    // prevent stepping on hill
    for (Tile myHill : ants.getMyHills()) {
      orders.put(myHill, null);
    }

    //err.println("food");
    // find close food
    List<Route> foodRoutes = new ArrayList<Route>();
    TreeSet<Tile> sortedFood = new TreeSet<Tile>(ants.getFoodTiles());
    List<Tile> sortedAnts = new ArrayList<Tile>(ants.getMyAnts());
    for (Tile foodLoc : sortedFood) {
      for (Tile antLoc : sortedAnts) {
        int distance = ants.getDistance(antLoc, foodLoc);
        Route route = new Route(antLoc, foodLoc, distance);
        foodRoutes.add(route);
      }
    }
    Collections.sort(foodRoutes);
    int gatherers = 0;
    int maxGatherers = 15;
    boolean moving = false;
    for (Route route : foodRoutes) {
      if (!foodTargets.containsKey(route.getEnd()) &&
          !foodTargets.containsValue(route.getStart())) {
        List<Tile> path = null;
        if (gatherers++ < maxGatherers)
          path = (new Pathing()).path(route.getStart(), route.getEnd(), ants);
        if (path != null && path.size() > 1) {
          moving = doMoveLocation(route.getStart(), path.get(1));
        } else {
          //System.err.println("turn: " + turn + ", Path error: from: " + route.getStart() + ", to: " + route.getEnd() + ", ");
          //moving = doMoveLocation(route.getStart(), route.getEnd());
        }
        if (moving)
        {
          foodTargets.put(route.getEnd(), route.getStart());
          //err.println("\tant " + route.getStart() + " -> " + route.getEnd());
        }
      }
    }

    // add new hills to set
    for (Tile enemyHill : ants.getEnemyHills()) {
      if (!enemyHills.contains(enemyHill)) {
        enemyHills.add(enemyHill);
      }
    }

    // remove hills that do not exist anymore
    for (Iterator<Tile> locIter = enemyHills.iterator(); locIter.hasNext();) {
      Tile enemyHill = locIter.next();
      if (ants.isVisible(enemyHill)
          && !ants.getEnemyHills().contains(enemyHill)) {
        locIter.remove();
      }
    }

    // attack hills
    //err.println("attack hills");
    int maxAttackers = 5, attackerCounter = 0;
    for (int i=0; i < sortedAnts.size(); i++)
    {
      Tile antLoc = sortedAnts.get( (attackerIndex + i) % sortedAnts.size() );
      if (!orders.containsValue(antLoc))
      {
        List<Tile> path = null;
        if (attackerCounter++ < maxAttackers)
        {
          List<Route> hillRoutes = new ArrayList<Route>();
          for (Tile hillLoc : enemyHills)
          {
            int distance = ants.getDistance(antLoc, hillLoc);
            Route route = new Route(antLoc, hillLoc, distance);
            hillRoutes.add(route);
          }
          Collections.sort(hillRoutes);

          int maxRoutes = 1, routeCounter = 0;
          for (Route route : hillRoutes)
          {
            if (routeCounter++ >= maxRoutes)
              break;
            //err.println("\t\attack " + routeCounter + ", route: " + route);
            path = new Pathing().path(route.getStart(), route.getEnd(), ants);
            if (path != null && path.size() > 1)
              break;
          }
        }
        else if (pathings.containsKey(antLoc))
        {
          path = pathings.get(antLoc);
        }

        if (path != null && path.size() > 1) {
          Tile nextPos = path.get(1);
          if (doMoveLocation(antLoc, nextPos))
          {
            pathings.put(nextPos, path);
            path.remove(0);
          }
        } else {
          //doMoveLocation(route.getStart(), route.getEnd());
        }
      }
    }
    if (sortedAnts.size() > 0)
      attackerIndex = (attackerIndex + maxAttackers) % sortedAnts.size();

    //err.println("explore");
    int maxExplorers = 5, explorerCounter = 0;
    for (int i=0; i < sortedAnts.size(); i++) 
    {
      Tile antLoc = sortedAnts.get( (explorerIndex + i) % sortedAnts.size() );
      //err.println("\tant: " + antLoc);
      if (!orders.containsValue(antLoc))
      {
        List<Tile> path = null;
        if (explorerCounter++ < maxExplorers)
        {
          List<Route> unseenRoutes = new ArrayList<Route>();
          for (Tile unseenLoc : unseenTiles)
          {
            int distance = ants.getDistance(antLoc, unseenLoc);
            Route route = new Route(antLoc, unseenLoc, distance);
            unseenRoutes.add(route);
          }
          Collections.sort(unseenRoutes);

          //err.println("\t\troutes");
          int maxRoutes = 1, routeCounter = 0;
          for (Route route : unseenRoutes)
          {
            if (routeCounter++ >= maxRoutes)
              break;
            //err.println("\t\tpath " + routeCounter + ", route: " + route);
            path = new Pathing().path(route.getStart(), route.getEnd(), ants);
            if (path != null && path.size() > 1)
              break;
            else
            {
              //err.println("\t\t\tfailed: " + path);
            }
          }
        } else if (pathings.containsKey(antLoc)) {
          path = pathings.get(antLoc);
        }

        if (path != null && path.size() > 1)
        {
          Tile nextPos = path.get(1);
          if (doMoveLocation(antLoc, nextPos))
          {
            pathings.put(nextPos, path);
            path.remove(0);
          }
        } else {
          //err.println("\t\t\tfailed: " + path);
        }
        //doMoveLocation(unseenRoutes.get(0).getStart(), unseenRoutes.get(0).getEnd());
        //err.println("routes: " + i);
      } else {
        //err.println("\t\talready");
      }
    }
    if (sortedAnts.size() > 0)
      explorerIndex = (explorerIndex + maxExplorers) % sortedAnts.size();

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
