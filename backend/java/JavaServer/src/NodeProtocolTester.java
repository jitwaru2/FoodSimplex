
public class NodeProtocolTester {
	public static void main(String[] args){
		String data = 
			"111\n" +
			"4\n" + 
			"2\n" + 
			"1 2 3 4 5 |1 6 7 8 9 |\n" + 
			"10 11 12 13\n" + 
			"112\n" + 
			"99\n";
		
		NodeProtocol np = new NodeProtocol();
		np.setData(data);
		
		System.out.println(np.toString());
		
		int count = 0;
		while(np.hasNextMatrixCombo()){
			Matrix[] res = np.extractNextCombo();
			System.out.println(count++ +":");
			for(Matrix m : res){
				System.out.println(m.toString());
			}
		}
		
	}
}
