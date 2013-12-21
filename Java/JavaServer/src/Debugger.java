import java.util.Scanner;


public class Debugger {
	public static void main(String[] args){
		String data = "111\n"
				+ "5\n"
				+ "2\n"
				+ "1,1,1,1,1|2,2,2,2,2\n"
				+ "3|3|3|3|3\n"
				+ "112\n"
				+ "99\n";
		/* 
		 * beginSystem
		 * macroCount
		 * numFoods
		 * foodList
		 * desiredMacrosList
		 * endSystem
		 *  */
		
		Scanner scan = new Scanner(data);
		int status = Integer.parseInt(scan.nextLine().trim());
		int macrosCount = Integer.parseInt(scan.nextLine().trim());	//matrix rows
		int numFoods = Integer.parseInt(scan.nextLine().trim());	//matrix columns
		
		System.out.println("status: " + status+"--macrosCount: " + macrosCount + "--numFoods: " + numFoods);
		
		Matrix A = new Matrix(macrosCount, numFoods);
		double[] beta = new double[macrosCount];
		Matrix b = new Matrix(beta);
		
		//Put foods in a matrix where each column is a food item
		String flist = scan.nextLine().trim();
		System.out.println("flist: " + flist);
		flist = flist.replace(",", " ");
		flist = flist.replace("|", "\n");
		System.out.println("flist: " + flist);
		
		Scanner flistscan = new Scanner(flist);
		for(int i=0; i<numFoods; i++){
			String foodstr = flistscan.nextLine();
			System.out.println("foodstr: " + foodstr);
			Scanner fscan = new Scanner(foodstr);
			
			for(int j=0; j<macrosCount; j++){
				A.matrix[j][i] = fscan.nextDouble();
			}
			fscan.close();
		}
		flistscan.close();
		
		System.out.println(A.toString());
		
		//Set desired macros as a vector
		String dml = scan.nextLine();
		dml = dml.replace("|", " ");
		Scanner dmlscan = new Scanner(dml);
		for(int i=0; i<macrosCount; i++){
			b.matrix[i][0] = dmlscan.nextDouble();
		}
		dmlscan.close();
		
		System.out.println(b.toString());
	}
}
