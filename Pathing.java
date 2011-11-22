import java.util.*;

public class Pathing {
	Ants ants;
	HashMap<Tile, Integer> g_score = new HashMap<Tile, Integer>();
	HashMap<Tile, Integer> h_score = new HashMap<Tile, Integer>();
	HashMap<Tile, Integer> f_score = new HashMap<Tile, Integer>();
	
	public List<Tile> path(Tile from, Tile to, Ants ants) {
		this.ants = ants;
		return A(from, to);
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
			//System.err.println("x: " + x + ", goal: " + goal);
			if (x.equals(goal))
				return reconstruct_path(came_from, came_from.get(goal));
			
			openset.remove(x);
			closedset.add(x);
			for (Tile y : neighbours(x)) {
				//System.err.println("\ty: " + y);
				if (closedset.contains(y))
					continue;
				int tentative_g_score = g_score.get(x) + ants.getDistance(x, y);
				////System.err.println("\t\tscore: " + tentative_g_score);
				
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
		//System.err.println("failure");
		// failure; return empty
		return new ArrayList<Tile>();
	}
	
	int h(Tile a, Tile b) {
		return ants.getDistance(a, b);
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
        	Tile neighbour = ants.getTile(t, direction);
        	if (!ants.getIlk(neighbour).isFriendly())
        		neighbours.add(neighbour);
        }
		return neighbours;
	}
	
	List<Tile> reconstruct_path(HashMap<Tile, Tile> came_from, Tile current_node) {
		List<Tile> p;
		//System.err.println("recon: " + came_from.values());
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
