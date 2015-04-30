package atomicityChecker;
import java.util.*;


public class State {	
	//private static final int countLine = 0;
	HashMap<Integer,Integer> C = new HashMap<Integer,Integer>(); //tid is int
	HashMap<Integer,Integer> L = new HashMap<Integer,Integer>();
	// lock map to be coded 
	HashMap<String,Integer> U = new HashMap<String,Integer>();
	//HashSet<Node> H = new HashSet<Node>();
	HashMap<Integer,LinkedHashSet> H = new HashMap<Integer,LinkedHashSet>();
	HashMap<ReadKey,Integer> R = new HashMap<ReadKey,Integer>();
	HashMap<Key,Integer> W = new HashMap<Key,Integer>();
	HashMap<Integer,Integer> F = new HashMap<Integer,Integer>();
	HashMap<Integer,Integer> P = new HashMap<Integer,Integer>();
}
