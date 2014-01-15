import java.util.ArrayList;
import java.util.List;


public class NCRTester {
	public static void main(String[] args){
		List<Integer> ints = new ArrayList<Integer>();
		for(int i=0; i<3; i++){
			ints.add(i);
		}
		
		printList(ints);
		
		NCR ncr = new NCR(ints);
		
		while(ncr.hasNext()){
			printList(ncr.next());
		}
		
	}
	
	public static void printList(List<?> l){
		System.out.print("[TESTER] list: ");
		for(Object d : l){
			System.out.print(d + " ");
		}
		System.out.println();
	}
	
}
