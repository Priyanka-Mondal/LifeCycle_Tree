package atomicityChecker;

public class Edge 
{
	Value source;
	Value dest;
	
	public Edge()
	{
		
	}
	
	public Edge(Value s, Value d){
    	this.source = s;
		this.dest = d;
    }
	
	public int hashCode(){
		//System.out.println("In hashcode");
		int hashcode = 0;
		hashcode += source.hashCode();
		hashcode += dest.hashCode();
		return hashcode;
	}
		     
	public boolean equalsNode(Object k){
		        
		if (k instanceof Edge) {
			
			Edge kk = (Edge) k;
		    return (kk.source.node == this.source.node && kk.dest.node == this.dest.node);
		} 
		else {
			
		    return false;
		}
	}
	
	public boolean equalsLine(Object k){
        
		if (k instanceof Edge) {
			
			Edge kk = (Edge) k;
		    return (kk.source.line == this.source.line && kk.dest.line == this.dest.line);
		} 
		else {
			
		    return false;
		}
	}
		     
	public Value getSource() {
		return source;
	}
	public void setsource(Value obj) {
		this.source = obj;
    }
	public Value getdest() {
		return dest;
	}
	public void setdest(Value fld) {
		this.dest = fld;
    }
	
	public String toString(){
		        return "(Source: "+source.node+","+source.line+" ->Dest: "+dest.node +"," + dest.line +")"; 
	}
}