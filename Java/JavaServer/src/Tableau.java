
/**
 * A Tableau is a special data structure used to hold and manipulate data during simplex operations.
 * @author josh
 *
 */
public class Tableau {
	Matrix OFC; 			//objective function coefficients
	Matrix BVC; 			//index of basic variables column within OFC
	Matrix constraints; 	//constraint equations (with slack/surplus/artificial variables)
	Matrix RHS;				//right hand side of constraints
	Matrix thetas;			//calculated thetas
	Matrix cjzj;			//calculated cj-zj
	double objVal;			//calculated current max objective function value
	int realVars;			//number of real variables
	int auxVars;			//number of auxiliary variables (slack/surplus/artificial)
	int totalVars;			//total number of variables (real+auxiliary)
	int slackIndex;			//starting index for slack/surplus variables in constraints
	int artIndex;			//starting index for artificial variables in constraints
	int numCons;			//number of constraints
	boolean hasArtificials;	//true if the tab has artificial variables
	
	/**
	 * This is just if you want to copy the dimensions/structure of a Tableau, but include only OFC as the preinitialized values
	 * @param seed
	 * @param OFC
	 * @param empty
	 */
	public Tableau(Tableau seed, Matrix OFC, boolean empty){
		this.OFC = new Matrix(copyDoubleArray(OFC.matrix));
		tabHelper(seed, empty, false);
	}
	
	/**
	 * 
	 * @param seed The inputted Tableau to copy, if desired
	 * @param OFC The objective function coefficients, in Matrix format
	 * @param empty True if only the format of the seed Tableau is desired; false for a full copy
	 * @param removeArtificials True if wants to forcibly remove artificial variables from the inputted seed tab to be copied
	 */
	public Tableau(Tableau seed, Matrix OFC, boolean empty, boolean removeArtificials){
		this.OFC = new Matrix(copyDoubleArray(OFC.matrix));
		tabHelper(seed, empty, removeArtificials);
	}
	
	/**
	 * This is basically to prevent code duplication
	 * @param seed
	 * @param empty
	 * @param removeArtificials
	 */
	private void tabHelper(Tableau seed, boolean empty, boolean removeArtificials){
		if(removeArtificials==true){
			auxVars = seed.numCons;
			totalVars = seed.realVars+auxVars;
			artIndex = -1; //there are no artificial variables
			hasArtificials = false;
		} else {
			auxVars = seed.auxVars;
			totalVars = seed.totalVars;
			artIndex = seed.artIndex;
			if(seed.auxVars>seed.numCons){
				hasArtificials = true;
			} else {
				hasArtificials = false;
			}
		}
		
		realVars = seed.realVars;
		slackIndex = seed.slackIndex;
		numCons = seed.numCons;
		
		if(empty==true){
			BVC = new Matrix(new double[numCons]);
			constraints = new Matrix(new double[numCons][totalVars]);
			RHS = new Matrix(new double[numCons]);
		} else {
			BVC = new Matrix(copyDoubleArray(seed.BVC.matrix));
			if(removeArtificials==true){
				constraints = new Matrix(copyDoubleArray(seed.constraints.matrix, 0, totalVars));
			} else {
				constraints = new Matrix(copyDoubleArray(seed.constraints.matrix));
			}
			RHS = new Matrix(copyDoubleArray(seed.RHS.matrix));
		}
		
		//don't copy thetas and cjzj; leave calculations to MatOps
		thetas = new Matrix(new double[numCons]);
		cjzj = new Matrix(new double[totalVars]);
	}
	
	/**
	 * Creates a Tableau preinitialized with the OFC, constraints, and RHS
	 * @param solns
	 */
	public Tableau(Matrix[][] solns, Matrix objMax) throws Exception {
		//we need these original vectors intact and unchanged at the end, so make copies to operate on
		Matrix vectorX = solns[0][0];
		Matrix[] nullspace = solns[1];
		
		//copies of x vector and nullspace vectors
		numCons= vectorX.rows - nullspace.length; //number of constraint equations involving ALL nullspace coefficients
		if(numCons==0){
			throw new Exception();
		}
		
		double[] rhsd = new double[numCons];
		for(int i=0; i<rhsd.length; i++){
			rhsd[i] = vectorX.matrix[i][0];
		}
		RHS = new Matrix(rhsd);
		
		Matrix[] cons = new Matrix[nullspace.length];
		for(int i=0; i<nullspace.length; i++){
			cons[i] = new Matrix(copyDoubleArray(nullspace[i].matrix));
		}
		
		//get tab dimensions/indexing information
		realVars = nullspace.length; //each nullspace vector contributes 1 variable
		auxVars = numCons; //slack/surplus/artificial vars
		slackIndex = realVars;
		artIndex = realVars+auxVars;
		
		/* Get data for transforming inequalities to equations 
		 * Set RHS */
		int flags[] = new int[numCons]; //keeps track of which equations require slack (0) or surplus+artificial variables (1)
		for(int i=0; i<numCons; i++){
			RHS.matrix[i][0] *= -1; //simulate subtracting/adding from left to right
			if(RHS.matrix[i][0]>0){
				flags[i] = 1; //additional artificial variable needed
				auxVars++; //increase column count to account for artificial vars
				hasArtificials = true;
			} else {
				//multiply constrain through by -1
				RHS.matrix[i][0] *=-1;
				for(int j=0; j<nullspace.length; j++){
					cons[j].matrix[i][0] *= -1;
				}
				flags[i] = 0; //slack variable needed
			}
		}
		
		totalVars = realVars + auxVars;
		
		System.out.println("moved RHS:");
		System.out.println(RHS.toString());
		
		//TEST flags
		System.out.println("Flag check:");
		for(int i : flags){
			System.out.print("["+i+"]");
		}
		System.out.println("\n");
		
		
		/* Build default objective function coefficients as the sum of all constraint coefficients, with 0's for auxiliary vars */
		System.out.println("building objective function...");
		OFC = new Matrix(new double[totalVars]);
		for(int i=0; i<nullspace.length; i++){
			for(int j=0; j<nullspace[i].rows; j++){
				if(objMax.matrix[j][0]==1){
					OFC.matrix[i][0] += nullspace[i].matrix[j][0];
					System.out.println(nullspace[i].matrix[j][0]);
				}
			}
		}
		
		
		System.out.println("passed building objective function\n");
		
		
		/* Write full equations into constraints table, including auxiliary vars
		 * format:
		 * x1 x2 ... xn c1 c2 ... cn a1 a2 ...am
		 * <basic vars> <slack/surplus vars> <artificial vars>
		 * where |x|=|c|=/=|a| */
		System.out.println("numCons:" + numCons);
		System.out.println("totalVars: " + totalVars);
		constraints = new Matrix(new double[numCons][totalVars]);
		
		//fill constraints table a row at a time
		int sli = slackIndex;
		int ari = artIndex;
		for(int i=0; i<numCons; i++){
			//set basic vars
			for(int j=0; j<realVars; j++){
				constraints.matrix[i][j] = cons[j].matrix[i][0];
			}
			//set slack/surplus/artificial vars
			if(flags[i]==0){
				constraints.matrix[i][sli] = 1;
				sli++;
			} else if(flags[i]==1){
				constraints.matrix[i][sli] = -1;
				sli++;
				constraints.matrix[i][ari] = 1;
				ari++;
			}
		}
		
		/* The following do not have default values; they need to be calculated on the fly, so we initialize their data structures */
		thetas = new Matrix(new double[numCons]);
		cjzj = new Matrix(new double[totalVars]);
		BVC = new Matrix(new double[numCons]);
	}
	
	/**
	 * Helper method to copy a 2 dimensional double array
	 * @param mat Inputted 2 dimensional double array
	 * @return Copy of the 2 dimensional double array
	 */
	public static double[][] copyDoubleArray(double[][] mat){
		double[][] matCopy = new double[mat.length][mat[0].length];
		for(int i=0; i<mat.length; i++){
			for(int j=0; j<mat[i].length; j++){
				matCopy[i][j] = mat[i][j];
			}
		}
		return matCopy;
	}
	
	/**
	 * Helper method to copy a vertical slice of a 2 dimensional double array
	 * @param mat Inputted 2 dimensional double array
	 * @param colStart Column to start copying from
	 * @param colEnd Column to stop copying at
	 * @return Copied slice of the inputted 2 dimensional double array
	 */
	public static double[][] copyDoubleArray(double[][] mat, int colStart, int colEnd){
		double[][] matCopy = new double[mat.length][colEnd-colStart];
		for(int i=0; i<mat.length; i++){
			for(int j=colStart; j<colEnd; j++){
				matCopy[i][j] = mat[i][j];
			}
		}
		return matCopy;
	}
	
	/**
	 * Checks to see if this Tableau currently has artificial variables in its BVC
	 * @return True if this Tableau currently has artificial variables in its BVC, false otherwise
	 */
	public boolean solnHasArtificials(){
		if(hasArtificials==true){
			for(int i=0; i<BVC.rows; i++){
				if(BVC.matrix[i][0]>=artIndex){
					System.out.println("artificial variable is element " + i + ": "+ BVC.matrix[i][0]);
					System.out.println("artindex is: " + artIndex);
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Returns a String format of the Tableau
	 */
	public String toString(){
		String t = "__|";
		for(int i=0; i<OFC.rows; i++){
			t+= "[" + OFC.matrix[i][0] + "]";
		}
		t+=" | RHS | " + " THETA |\n";
		
		for(int i=0; i<constraints.rows; i++){
			t+= BVC.matrix[i][0];
			for(int j=0; j<constraints.columns; j++){
				t+= "[" + constraints.matrix[i][j]+ "]";
			}
			t+= " | " + RHS.matrix[i][0] + " | " + thetas.matrix[i][0] + "\n";
		}
		
		t+= "cj-zj|";
		for(int i=0; i<cjzj.rows; i++){
			t+= "[" + cjzj.matrix[i][0] + "]";
		}
		t+= " | " + objVal;
		
		return t;
	}
	
}
