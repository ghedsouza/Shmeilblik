import java.util.*;

public class Pathing {
	Ants ants;
	HashMap<Tile, Integer> g_score = new HashMap<Tile, Integer>();
	HashMap<Tile, Integer> h_score = new HashMap<Tile, Integer>();
	HashMap<Tile, Integer> f_score = new HashMap<Tile, Integer>();
	
	Tile path(Tile from, Tile to, Ants ants) {
		this.ants = ants;
		return new Tile(from.getRow(), from.getCol());
	}
	
	List<Tile> A(Tile start, Tile goal) {
		HashSet<Tile> closedset = new HashSet<Tile>(),
				openset = new HashSet<Tile>();
		
		HashMap<Tile, Tile> came_from = new HashMap<Tile, Tile>();
		openset.add(start);
		
		g_score.put(start, 0);
		h_score.put(start, h(start, goal));
		f_score.put(start, f(start));
		
		while(!openset.isEmpty()) {
			Tile x = min_score(openset);
			if (x == goal)
				return reconstruct_path(came_from, came_from.get(goal));
			
			openset.remove(x);
			closedset.add(x);
			for (Tile y : neighbours(x)) {
				if (closedset.contains(y))
					continue;
				int tentative_g_score = g_score.get(x) + Tile.distance(x, y);
				
				boolean tentative_is_better;
				if (!openset.contains(y)) {
					openset.add(y);
					tentative_is_better = true;
				} else if (tentative_g_score < g_score.get(y)) {
					tentative_is_better = true;
				} else {
					tentative_is_better = false;
				}
				
				if (tentative_is_better) {
					came_from.put(y, x);
					g_score.put(y, tentative_g_score);
					h_score.put(y, h(y, goal));
					f_score.put(y, g_score.get(y) + h_score.get(y));
				}
			}
		}
		
		// failure; return empty
		return new ArrayList<Tile>();
	}
	
	int h(Tile a, Tile b) {
		return Integer.MAX_VALUE;
	}
	int f(Tile tile) {
		return g_score.get(tile) + h_score.get(tile);
	}
	
	Tile min_score(Collection<Tile> c) {
		Tile min = null;
		for(Tile t : c) {
			if (min == null || f(t) < f(min)) min = t;
		}
		return min;
	}
	
	List<Tile> neighbours(Tile t) {
		ArrayList<Tile> neighbours = new ArrayList<Tile>();		
        for (Aim direction : Aim.values()) {
        	neighbours.add(ants.getTile(t, direction));
        }
		return neighbours;
	}
	
	List<Tile> reconstruct_path(HashMap<Tile, Tile> came_from, Tile current_node) {
		List<Tile> p;
		if (came_from.containsKey(current_node)) {
			p = reconstruct_path(came_from, came_from.get(current_node));
			p.add(current_node);
		} else {
			p = new ArrayList<Tile>();
			p.add(current_node);	
		}
		return p;
	}
}
