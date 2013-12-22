import java.util.Scanner;

/**
 * Works in conjunction with the Protocol class to handle input from Node.js
 * @author josh
 *
 */
public class NodeProtocol {
	
	/**
	 * Interprets a message header as a Protocol field
	 * @param data Input data from Node.js
	 * @return Protocol number
	 */
	public int getMsgType(String data){
		Scanner scan = new Scanner(data);
		int t = scan.nextInt();
		scan.close();
		return t;
	}
	
	/**
	 * Determines the appropriate output message to send to Node.js based on the inputted parameters.
	 * 
	 * @param protocol
	 * @param soln
	 * @param lsb
	 * @param leastSquares
	 * @param simplex
	 * @return
	 */
	public String getOutput(int protocol, Matrix soln, Matrix lsb, boolean leastSquares, boolean simplex){
		String s = "";
		switch(protocol){
			/*
			 * Protocol.noSolution
			 * Protocol.noSolutionMsg
			 * Protocol.noFoodComboMsg
			 * */
			case Protocol.noSolution: {
				s+= Protocol.noSolution + "\n";
				s+= Protocol.nullSolutionMsg + "\n";
				s+= Protocol.noFoodComboMsg + "\n";
			} break;
			/*
			 * Protocol.solution
			 * Protocol.leastSquares/noLeastSquares
			 * (Protocol.leastSquaresMsg)
			 * (least squares b vector)
			 * solution vector
			 * Protocol.simplex/noSimplex
			 * Protocol.simplexMsg
			 * */
			case Protocol.solution: {
				s+= Protocol.solution + "\n";
				if(leastSquares==true){
					s+= Protocol.leastSquares + "\n";
					s+= Protocol.leastSquaresMsg + "\n";
					s+= getString(lsb) + '\n';
				} else {
					s+= Protocol.noLeastSquares + "\n";
				}
				s+= getString(soln) + "\n";
				if(simplex==true){
					s+= Protocol.simplex + "\n";
					s+= Protocol.simplexMsg + "\n";
				} else {
					s+= Protocol.noSimplex + "\n";
					s+= Protocol.noSimplexMsg + "\n";
				}
			} break;
			/*
			 * Protocol.deadSolution
			 * Protocol.leastSquares/noLeastSquares
			 * (least squares b vector)
			 * solution vector
			 * Protocol.deadSolutionMsg
			 * */
			case Protocol.deadSolution: {
				s+= Protocol.deadSolution + "\n";
				if(leastSquares==true){
					s+= Protocol.leastSquares + "\n";
					s+= Protocol.leastSquaresMsg + "\n";
					s+= getString(lsb);
				} else {
					s+= Protocol.noLeastSquares + "\n";
				}
				s+= getString(soln) + "\n";
				s+= Protocol.deadSolutionMsg + "\n";
			}
		}
		
		return s;
	}
	
	/**
	 * Helper method that converts a vector into a single line of a String
	 * @param m Vector
	 * @return
	 */
	private String getString(Matrix m){
		String s = "";
		for(int i=0; i<m.rows; i++){
			s += m.matrix[i][0] + " ";
		}
		return s;
	}
	
	/**
	 * Extracts, from the inputted data, the matrices A and b (as in Ax=b) as well as a matrix c which foods to maximize
	 * @param data Data from Node.js
	 * @return An array of matrices A, b, and c
	 */
	public Matrix[] extractMatrices(String data){
		/* 
		 * beginSystem
		 * macrosCount
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
		
		/* A in Ax=b */
		Matrix A = new Matrix(macrosCount, numFoods);
		double[] beta = new double[macrosCount];
		/* b in Ax=b */
		Matrix b = new Matrix(beta);
		double[] ceta = new double[numFoods];
		/* Foods to maximize, used for building the objective function in the simplex method */
		Matrix c = new Matrix(ceta);
		
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
			
			c.matrix[i][0] = fscan.nextDouble();
			
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
		
		return new Matrix[] {A, b, c};
	}

}