import java.io.File;
import java.util.Scanner;

/**
 * Used to debug some problems
 * @author josh
 *
 */
public class Debugger {
	public static void main(String[] args){
		Matrix[] ms = getMatricesFromFile();
		System.out.println("Input matrices:");
		for(Matrix m : ms){
			System.out.println(m.toString());
		}
		
		Matrix[][] results = solveSystem(ms);
		
		double[] obj = new double[results[0][0].rows];
		for(int i=0; i<obj.length; i++){
			obj[i] = 1;
		}
		Matrix objMax = new Matrix(obj);
		System.out.println("objMax:");
		System.out.println(objMax.toString());
		
		MatOps.TwoPhaseSimplex(results, objMax);
		
	}
	
	public static Matrix[] getMatricesFromFile(){
		Matrix A = null;
		Matrix b = null;
		
		/* Read matrices from file */
		try {
			File inFile = new File("matrices.txt");
			Scanner scan = new Scanner(inFile);
			int rows = scan.nextInt();
			int columns = scan.nextInt();
			double mat[][] = new double[rows][columns];
			for(int i=0; i<rows; i++){
				for(int j=0; j<columns; j++){
					mat[i][j] = scan.nextDouble();
				}
			}
			A = new Matrix(mat);
			
			double[] beta = new double[rows];
			for(int i=0; i<rows; i++){
				beta[i] = scan.nextDouble();
			}
			b = new Matrix(beta);
			
		} catch (Exception e){
			System.out.println(e.getMessage());
		}
		
		return new Matrix[] {A, b};
	}
	
	public static Matrix[][] solveSystem(Matrix[] ms){
		Matrix[][] results = MatOps.solveSystem(ms[0], ms[1]);
		
		System.out.println("Vector x:");
		System.out.println(results[0][0].toString());
		
		System.out.println("Nullspace vectors:");
		for(int i=0; i<results[1].length; i++){
			System.out.println(results[1][i].toString());
		}
		
		return results;
	}
	
	public static void test1(){
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
