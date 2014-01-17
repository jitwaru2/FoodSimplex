import java.util.ArrayList;
import java.util.List;

/**
 * Iterates through all the combinations of the items in a list while. 
 * An NCR object keeps track of which combination it has currently calculated, and the next combination in the series can be retrieved using the next() method.
 * Once the NCR has computed all possible combinations via calls to next(), any subsequent calls to next() will return null, so use hasNext() before using next().
 * @author josh
 *
 * @param <T>
 */
public class NCR<T> {

	private int pointers;
	private int base;
	private int current;
	private List<T> list;
	private int[] indices;
	private boolean hasNext;
	private boolean first;
	
	public NCR(List<T> list){
		this.list = list;
		indices = new int[list.size()];
		
		indices[0] = 1;
		base = 0;
		current = 0;
		pointers = 1;
		hasNext = true;
		first = true;
	}
	
	public void reset(){
		indices = new int[list.size()];
		
		indices[0] = 1;
		base = 0;
		current = 0;
		pointers = 1;
		hasNext = true;
		first = true;
		
	}
	
	public List<T> next(){
		if(hasNext==true){
			System.out.println("***BEFORE CALL [NCR] next");
			printIndices();
			
			if(first==true){
				first = false;
				return getCompList();
			}
			
			//current is always rightmost 1
			for(int i=indices.length-1; i>=0; i--){
				if(indices[i]==1){
					current = i;
					break;
				}
			}
			
			//push current pointer right
			if(current<indices.length-1){
				System.out.println("[NCR] pushing");
				//push
				indices[current++] = 0;
				indices[current] = 1;
			} 
			//if current pointer can't be pushed right any further, pull closest base 1 spot right and reset to closest base
			else {
				System.out.println("[NCR] Pulling");
				//scan left to find closest base
				for(int i=indices.length-1; i>=0; i--){
					//skip all consecutive 1's before searching for qualified base
					if(indices[i]==0){
						boolean addBase = true;
						for(int j=i; j>=0; j--){
							if(indices[j]==1){
								indices[j++] = 0;
								indices[j] = 1;
								base = j;
								addBase = false;
								break;
							}
						}
						//if all current 1's are consecutively located at the end, add another base and reset to base
						if(addBase==true){
							pointers++;
							if(pointers==indices.length){
								hasNext = false;
							}
							indices[0] = 1;
							base = 0;
							System.out.println("Add base true; added a base");
							printIndices();
						}
						
						//reset to base
						resetToBase();
						System.out.println("After base reset");
						printIndices();
						
						break;
					}
				}
			}
			
			return getCompList();
		} else {
			return null;
		}
	}
	
	private void resetToBase(){
		//find number of markers after base
		System.out.println("here is the base: " + base);
		int numMarkers = 0;

		for(int i=base+1; i<indices.length; i++){
			if(indices[i]==1){
				indices[i] = 0;
				numMarkers++;
			}
		}
		
		for(int i=base; i<base+numMarkers; i++){
			indices[i+1] = 1;
		}
	}
	
	private List<T> getCompList(){
		List<T> l = new ArrayList<T>();
		for(int i=0; i<list.size(); i++){
			if(indices[i]==1){
				l.add(list.get(i));
			}
		}
		return l;
	}
	
	public boolean hasNext(){
		return hasNext;
	}
	
	private void printIndices(){
		System.out.print("[NCR] printIndices: ");
		for(int i : indices){
			System.out.print(i + " ");
		}
		System.out.println();
	}
}
