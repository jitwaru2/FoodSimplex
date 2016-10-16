import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Works in conjunction with the Protocol class to handle input from Node.js
 * @author josh
 *
 */
public class NodeProtocol {
	
	private String data;
	private ArrayList<Integer> combinations;
	private List<Integer> currentCombo;
	private NCR ncr;
	
	private int status;
	private int macrosCount;
	private int numFoods;
	private ArrayList<ArrayList<Double>> foods;
	private ArrayList<Double> desiredMacros;
	
	public ArrayList<Integer> getCurrentCombo(){
		ArrayList<Integer> copy = new ArrayList<Integer>();
		for(Integer i : currentCombo){
			copy.add(i);
		}
		
		return copy;
	}
	
	public String toString(){
		String str = "NCR DATA:\n";
		str += "status: " + status + "\n";
		str += "macrosCount: " + macrosCount + "\n";
		str += "numFoods: " + numFoods + "\n";
		str += "foods:\n";
		for(ArrayList<Double> f : foods){
			for(Double d : f){
				str += d + " ";
			}
			str += "[endfood]\n";
		}
		str += "desiredMacros: \n";
		for(Double d : desiredMacros){
			str += d + " ";
		}
		str += "\n";
		
		return str;
	}
	
	//good
	public NodeProtocol(){
		currentCombo = null;
		
		status = 0;
		numFoods = 0;
		macrosCount = 0;
		foods = new ArrayList<ArrayList<Double>>();
		desiredMacros = new ArrayList<Double>();
	}
	
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
	public String getOutput(int protocol, Matrix soln, Matrix lsb, boolean leastSquares, boolean simplex, List<Integer> curCombo){
		String s = "";
		
		int[] inds = new int[macrosCount];
		for(Integer i : curCombo){
			inds[i] = 1;
		}
		
		String indsString = "";
		for(int i : inds){
			indsString += i;
		}
		
		
		switch(protocol){
			/*
			 * Protocol.noSolution
			 * -Focused macros
			 * Protocol.noSolutionMsg
			 * Protocol.noFoodComboMsg
			 * */
			case Protocol.noSolution: {
				s+= Protocol.noSolution + "\n";
				s+= indsString + "\n";
				s+= Protocol.nullSolutionMsg + "\n";
				s+= Protocol.noFoodComboMsg + "\n";
			} break;
			/*
			 * Protocol.solution
			 * -Focused macros
			 * Protocol.leastSquares/noLeastSquares
			 * (Protocol.leastSquaresMsg)
			 * (least squares b vector)
			 * solution vector
			 * Protocol.simplex/noSimplex
			 * Protocol.simplexMsg
			 * */
			case Protocol.solution: {
				s+= Protocol.solution + "\n";
				s+= indsString + "\n";
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
			 * -Focused macros
			 * Protocol.leastSquares/noLeastSquares
			 * (least squares b vector)
			 * solution vector
			 * Protocol.deadSolutionMsg
			 * */
			case Protocol.deadSolution: {
				s+= Protocol.deadSolution + "\n";
				s+= indsString + "\n";
				if(leastSquares==true){
					s+= Protocol.leastSquares + "\n";
					s+= Protocol.leastSquaresMsg + "\n";
					s+= getString(lsb) + "\n";
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
	 * Sets the state of the NodeProtocol object to contain the data stored in the input data string
	 * @param data
	 */
	public void setData(String data){
		//clear all preexisting data
		status = 0;
		macrosCount = 0;
		numFoods = 0;
		foods.clear();
		desiredMacros.clear();
		
		/* 
		 * beginSystem
		 * macrosCount
		 * numFoods
		 * foodList
		 * desiredMacrosList
		 * endSystem
		 *  */
		
		//set data
		this.data = data;
		
		//set status, number of supplies macros per food, and the number of total foods
		Scanner scan = new Scanner(data);
		status = Integer.parseInt(scan.nextLine().trim());
		macrosCount = Integer.parseInt(scan.nextLine().trim());	//matrix rows
		numFoods = Integer.parseInt(scan.nextLine().trim());	//matrix columns
		
		combinations = new ArrayList<Integer>();
		for(int i=0; i<macrosCount; i++){
			combinations.add(i);
		}
		
		ncr = new NCR(combinations);
		
		System.out.println("status: " + status+"--macrosCount: " + macrosCount + "--numFoods: " + numFoods);
		
		//convert each string macro list into an ArrayList<Double> and store in foods
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
			
			ArrayList<Double> food = new ArrayList<Double>();
			food.add(fscan.nextDouble());
			
			for(int j=0; j<macrosCount; j++){
				food.add(fscan.nextDouble());
			}
			fscan.close();
			
			foods.add(food);
		}
		flistscan.close();
		
		//set desired macro list
		String dml = scan.nextLine();
		dml = dml.replace("|", " ");
		Scanner dmlscan = new Scanner(dml);
		
		for(int i=0; i<macrosCount; i++){
			desiredMacros.add(dmlscan.nextDouble());
		}
		dmlscan.close();
	}
	
	/**
	 * Calls the hasNext() method of this NodeProtocol object's NCR object
	 * @return
	 */
	public boolean hasNextMatrixCombo(){
		return ncr.hasNext();
	}
	
	/**
	 * Produces a Matrix system Ax=b only using the concerned macros in the next combination series
	 * @return An array of matrices containing A and b in Ax=b, and c, the Matrix containing maximization values
	 */
	public Matrix[] extractNextCombo(){
		currentCombo = ncr.next();
		
		System.out.println("Current combo size: " + currentCombo.size());
		
		/* A in Ax=b */
		Matrix A = new Matrix(currentCombo.size(), numFoods);

		/* b in Ax=b */
		Matrix b = new Matrix(new double[currentCombo.size()]);
		
		/* Foods to maximize, used for building the objective function in the simplex method */
		Matrix c = new Matrix(new double[numFoods]);
		
		for(int i=0; i<foods.size(); i++){
			
			c.matrix[i][0] = foods.get(i).get(0);
			
			for(int j=0; j<currentCombo.size(); j++){
				A.matrix[j][i] = foods.get(i).get(currentCombo.get(j)+1);
			}
		}
		
		for(int i=0; i<currentCombo.size(); i++){
			b.matrix[i][0] = desiredMacros.get(currentCombo.get(i));
		}
		
		return new Matrix[] {A, b, c};
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
		status = Integer.parseInt(scan.nextLine().trim());
		macrosCount = Integer.parseInt(scan.nextLine().trim());	//matrix rows
		numFoods = Integer.parseInt(scan.nextLine().trim());	//matrix columns
		
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