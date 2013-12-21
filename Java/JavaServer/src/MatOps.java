import java.util.ArrayList;

public class MatOps {
	
	/**
	 * 
	 * @param solns
	 */
	public static Tableau TwoPhaseSimplex(Matrix[][] solns){
		Tableau tab = new Tableau(solns);
		System.out.println("Initial tab:");
		System.out.println(tab.toString());
		
		/* Phase 1 removes artificial variables from the tab to provide starting basic variables that yield a basic feasible solution */
		if(tab.hasArtificials==true){
			System.out.println("Artificials detected: beginning phase1:");
			tab = phase1(tab);
		}
		
		System.out.println("Beginning phase2:");
		System.out.println("\n\n\n\n**********\n*********\n***********\n**********\n********");
		/* Phase 2 is just the usual simplex maximization */
		tab = simplexMax(tab, false);
		
		return tab;
	}
	
	public static Tableau phase1(Tableau seedTab){
		/* Initial iteration:
		 * The first iteration of simplex initializes the first tab as a seed for every successive iteration of the simplex
		 *  */
		//Replace objective function with all 0's, and -1 for artificial vars
		Matrix originalOFC = new Matrix((copyDoubleArray(seedTab.OFC.matrix)));
		for(int i=0; i<seedTab.artIndex; i++){
			seedTab.OFC.matrix[i][0] = 0;
		}
		for(int i=seedTab.artIndex; i<seedTab.OFC.rows; i++){
			seedTab.OFC.matrix[i][0] = -1;
		}
		
		System.out.println("Phase 1: tab with new objCoefficients:");
		System.out.println(seedTab.toString());
		
		Tableau newSeed = new Tableau(simplexMax(seedTab, true), originalOFC, false, true);
		System.out.println("Phase 1: tab after end of phase 1:");
		System.out.println(newSeed.toString());
		
		return newSeed;
		
	}
	
	public static Tableau simplexMax(Tableau seedTab, boolean setBVC){
		//Find scattered identity matrix in constraints to set BVC
		/* Idea is: instead of brute force scanning through the constraints, notice: the slack/surplus variables will form an
		 * identity matrix, mixed with negatives, and each negative will correspond to a positive artificial countercolumn within
		 * a calculatable distance. Scan down the "fake" identity matrix of slacks/surplus and reference artificials in O(n) time */
		if(setBVC==true){
			int curRow = 0;
			int artCounter = 0;
			for(int i=seedTab.realVars; i<seedTab.realVars+seedTab.numCons; i++){
				if(seedTab.constraints.matrix[curRow][i]==1){
					seedTab.BVC.matrix[curRow][0] = i; //column index of OFC
				} else if(seedTab.constraints.matrix[curRow][i]==-1){
					//add corresponding artificial variable index
					seedTab.BVC.matrix[curRow][0] = seedTab.artIndex + artCounter;
					artCounter++;
				}
				curRow++;
			}
		}
		
		//TEST: pass!
		System.out.println("After adding BVC:");
		System.out.println(seedTab.toString());
		
		//Calculate cj-zj and keep track of largest cjzj value
		int pivotCol=0;
		for(int i=0; i<seedTab.constraints.columns; i++){
			double zj=0;
			for(int j=0; j<seedTab.constraints.rows; j++){
				System.out.println(seedTab.BVC.matrix[j][0]);
				double z1 = seedTab.OFC.matrix[((int)seedTab.BVC.matrix[j][0])][0];
				double z2 = seedTab.constraints.matrix[j][i];
				zj += z1*z2;
			}
			
			seedTab.cjzj.matrix[i][0] = seedTab.OFC.matrix[i][0] - zj;
			
			if(seedTab.cjzj.matrix[i][0]>seedTab.cjzj.matrix[pivotCol][0]){
				System.out.println("lawls");
				pivotCol= i;
			} 
			
//			else if(seedTab.cjzj.matrix[i][0]==seedTab.cjzj.matrix[pivotCol][0] && pivotCol!=i){
//				System.out.println("ERROR: MULTIPLE SIMILAR CJ-ZJ VALUES; ALTERNATE OPTIMUM; EXITING");
//				System.exit(1);
//			}
		}
		//Calculate RHS for cj-zj, which is the objective function value
		for(int i=0; i<seedTab.BVC.rows; i++){
			System.out.println(i);
			double z1 = seedTab.OFC.matrix[((int)seedTab.BVC.matrix[i][0])][0];
			double z2 = seedTab.RHS.matrix[i][0];
			seedTab.objVal += z1*z2;
		}
	
		System.out.println("After calculating cj-zj:");
		System.out.println(seedTab.toString());

		
		System.out.println("pivot column:" + pivotCol);
		
		//Compute thetas with RHS and pivot column and keep track of smallest theta
		int pivotRow = 0;
		boolean duplicate = false;
		for(int i=0; i<seedTab.thetas.rows; i++){
			//calculate theta
			seedTab.thetas.matrix[i][0] = seedTab.RHS.matrix[i][0]/seedTab.constraints.matrix[i][pivotCol];
			
			//keep track of smallest
			if(seedTab.thetas.matrix[i][0]<seedTab.thetas.matrix[pivotRow][0] && seedTab.thetas.matrix[i][0]>0){
				pivotRow = i;
				duplicate = false;
			} else if(seedTab.thetas.matrix[i][0]==seedTab.thetas.matrix[pivotRow][0] && pivotRow!=i){
				System.out.println("Detected duplicate: " + seedTab.thetas.matrix[i][0] + " | " + seedTab.thetas.matrix[pivotRow][0]);
				duplicate = true;
			}
		}
		if(seedTab.thetas.matrix[pivotRow][0]<=0){
			System.out.println("No viable thetas; objective function value cannot increase further. Exiting");
			System.exit(1);
		}
		if(duplicate==true){
			System.out.println("Multiple lowest theta values; alternate optimum detected. Exiting with current solution.");
			System.exit(1);
		}
		
		
		System.out.println("After computing thetas:");
		System.out.println(seedTab.toString());
		System.out.println("pivot element: " + seedTab.constraints.matrix[pivotRow][pivotCol]);
		double pivotElem = seedTab.constraints.matrix[pivotRow][pivotCol];

		
		//if the largest cjzj value is <=0, then simplex is finished
		if(apprxZeroOne(seedTab.cjzj.matrix[pivotCol][0])<=0.0){
			System.out.println("SIMPLEX IS FINISHED! Objective function maximized with:");
			for(int i=0; i<seedTab.BVC.rows; i++){
				System.out.println("Var " + seedTab.BVC.matrix[i][0] + ": " + seedTab.RHS.matrix[i][0]);
			}
			System.out.println("objective function value: " + seedTab.objVal);
			
			//check if final solution contains artificial variables
			for(int i=0; i<seedTab.BVC.rows; i++){
				if(seedTab.BVC.matrix[i][0]>=seedTab.artIndex){
					System.out.println("Artificial variables detected in final solution; no feasible solution exists");
					System.exit(1);
				}
			}
			
			return seedTab;
		}
		
//checkpoint: PASS
		
		/*
		 * Initial tab creation/calculation is complete. The N iterations of simplex can now proceed with the initial tab as a seed
		 * */
		ArrayList<Tableau> tabs = new ArrayList<Tableau>();
		tabs.add(seedTab);
		int loopInd = 0;
		boolean isMaximized = false;
		while(!isMaximized){
			Tableau prevTab = tabs.get(loopInd);
			Tableau curTab = new Tableau(prevTab, prevTab.OFC, true);
			tabs.add(curTab);
			System.out.println(curTab.toString());
			
			//set BVC
			for(int i=0; i<curTab.BVC.rows; i++){
				curTab.BVC.matrix[i][0] = prevTab.BVC.matrix[i][0];
			}
			curTab.BVC.matrix[pivotRow][0] = pivotCol;
			
			//in new tab, replace pivot row with pivot row/pivot element
			for(int i=0; i<prevTab.constraints.columns; i++){
				curTab.constraints.matrix[pivotRow][i] = prevTab.constraints.matrix[pivotRow][i]/pivotElem;
			}
			curTab.RHS.matrix[pivotRow][0] = prevTab.RHS.matrix[pivotRow][0]/pivotElem;
			
			System.out.println("Added divided pivot row to curTab:");
			System.out.println(curTab.toString());

//checkpoint: PASS

			//in new tab, set identity 1's
			for(int i=0; i<curTab.BVC.rows; i++){
				curTab.constraints.matrix[i][(int)curTab.BVC.matrix[i][0]] = 1;
			}
			
			/* Do row subtractions */
			for(int i=0; i<curTab.BVC.rows; i++){
				if(i!=pivotRow){
					int subRow1 = i;
					int subRow2 = 0;
					double multiplier = 0;
					//row i in prevTab is the first subtractant row
					//find second subtractant row
					for(int j=0; j<prevTab.constraints.rows; j++){
						//the second subtrant row cannot be the first subtractant row
						if(j!=i){
							System.out.println("j!=i: " + j + " " + i);
							
							//if first subtractant minus current row checks out, use this current row as the second subtractant row
							/* Cases to check for:
							 * 1. row 1 has 1, row 2 has 0, and 0 is desired --> bad, else if 1 is desired --> good
							 * 2. row 1 has 1, row 2 has x>0, and 1 is desired --> bad, else if 0 is desired --> good
							 * 3. row 1 has 0, row 2 has 0, and 1 is desired --> bad, else if 0 is desired --> good
							 * 4. row 1 has 0, row 2 has x>0, --> always bad
							 * */
							boolean bad = false;
							
							for(int w=0; w<curTab.BVC.rows; w++){
								int curBVCvar= (int)curTab.BVC.matrix[w][0];
								double row1var = apprxZeroOne(prevTab.constraints.matrix[i][curBVCvar]);
								double row2var = apprxZeroOne(prevTab.constraints.matrix[j][curBVCvar]);
								double desiredVar = apprxZeroOne(curTab.constraints.matrix[i][curBVCvar]);
								//case 1 and case 2
								System.out.println("curBVCvar: " + curBVCvar);
								System.out.println("row1var: " + row1var);
								System.out.println("row2var: " + row2var);
								System.out.println("desiredVar: " + desiredVar);
								
								if(row1var==1.0){
									System.out.println("case 1 or 2");
									if((row2var>0.0 && desiredVar!=0.0) || (row2var==0.0 && desiredVar!=1.0)){
										System.out.println("bad");
										bad = true;
										//if row is bad, stop testing the row and move on to the next row
										break;
									} 
									/* Since there will be exactly 1 of these situations, this situation will identify the divider */
									else if(row2var>0.0 && desiredVar==0.0){
										System.out.println("set divider");
										multiplier = row1var/row2var;
									} else {
										System.out.println("neither!!!");
									}
								}
								//case 3 and case 4
								else if(row1var==0.0){
									System.out.println("case 3 or 4");
									if((row2var>0.0) || (row2var==0.0 && desiredVar!=0.0)){
										bad = true;
										//if row is bad, stop testing the row and move on to the next row
										break;
									}
								} else if(row1var>0.0){
									System.out.println(row1var + " is greater than 0.0");
									if(row2var>0.0){
										multiplier = row1var/row2var;
									} else if(row2var==0.0){
										bad = true;
										break;
									}
								}
							}
							
							if(bad==false){
								subRow2 = j;
								//row is found, stop checking rows and perform subtraction
								break;
							}
						}
					}
					
					/* By this point, subrow2 MUST have a value; otherwise the simplex has no solution */
					for(int j=0; j<curTab.constraints.columns; j++){
						curTab.constraints.matrix[i][j] = prevTab.constraints.matrix[subRow1][j] - prevTab.constraints.matrix[subRow2][j]*multiplier;
						System.out.println(prevTab.constraints.matrix[subRow2][j]);
					}
					curTab.RHS.matrix[i][0] = prevTab.RHS.matrix[subRow1][0] - prevTab.RHS.matrix[subRow2][0]*multiplier;
					System.out.println(subRow2);
					System.out.println(multiplier);
				}
			}
			System.out.println("God, I hope this works... tab after row subtractions...");
			System.out.println(curTab.toString());
			
			
			/*************************************/
			
			
			//calculate cj-zj and keep track of largest one
			pivotCol=0;
			for(int i=0; i<curTab.constraints.columns; i++){
				double zj=0;
				for(int j=0; j<curTab.constraints.rows; j++){
					double z1 = curTab.OFC.matrix[((int)curTab.BVC.matrix[j][0])][0];
					double z2 = curTab.constraints.matrix[j][i];
					zj += z1*z2;
				}
				
				curTab.cjzj.matrix[i][0] = curTab.OFC.matrix[i][0] - zj;
				
				if(curTab.cjzj.matrix[i][0]>curTab.cjzj.matrix[pivotCol][0]){
					System.out.println("lawls");
					pivotCol= i;
				}
				//the repeats of the largest must be checked after the largest is found
//				else if(curTab.cjzj.matrix[i][0]==curTab.cjzj.matrix[pivotCol][0] && pivotCol!=i){
//					System.out.println("ERROR: MULTIPLE SIMILAR CJ-ZJ VALUES; ALTERNATE OPTIMUM; EXITING");
//					System.exit(1);
//				}
			}
			
			System.out.println("after calculating cj-zj for curTab:");
			System.out.println(curTab.toString());
			
			//calculate RHS for cj-zj
			for(int i=0; i<curTab.BVC.rows; i++){
				System.out.println(i);
				double z1 = curTab.OFC.matrix[((int)curTab.BVC.matrix[i][0])][0];
				double z2 = curTab.RHS.matrix[i][0];
				curTab.objVal += z1*z2;
			}
			System.out.println("Objective function value:");
			System.out.println(curTab.objVal);
			
			//if the largest cjzj value is <=0, then simplex is finished
			if(apprxZeroOne(curTab.cjzj.matrix[pivotCol][0])<=0.0){
				System.out.println("SIMPLEX IS FINISHED! Objective function maximized with:");
				for(int i=0; i<curTab.BVC.rows; i++){
					System.out.println("Var " + curTab.BVC.matrix[i][0] + ": " + curTab.RHS.matrix[i][0]);
				}
				System.out.println("objective function value: " + curTab.objVal);
				
				//check if final solution contains artificial variables
				for(int i=0; i<curTab.BVC.rows; i++){
					if(curTab.BVC.matrix[i][0]>=curTab.artIndex){
						System.out.println("Artificial variables detected in final solution; no feasible solution exists");
						System.exit(1);
					}
				}
				isMaximized = true;
				break;
			}

			
			System.out.println("pivot column:" + pivotCol);
			//compute thetas for with RHS and pivot column and keep track of largest theta
			pivotRow = 0;
			duplicate = false;
			for(int i=0; i<curTab.thetas.rows; i++){
				//calculate theta
				curTab.thetas.matrix[i][0] = curTab.RHS.matrix[i][0]/curTab.constraints.matrix[i][pivotCol];
				
				//keep track of smallest
				if(curTab.thetas.matrix[i][0]<curTab.thetas.matrix[pivotRow][0]){
					pivotRow = i;
					duplicate = false;
				} else if(curTab.thetas.matrix[i][0]==curTab.thetas.matrix[pivotRow][0] && pivotRow!=i){
					System.out.println("Detected duplicate: " + curTab.thetas.matrix[i][0] + " | " + curTab.thetas.matrix[pivotRow][0]);
					duplicate = true;
				}
			}
			if(curTab.thetas.matrix[pivotRow][0]<=0){
				System.out.println("No viable thetas; objective function value cannot increase further. Exiting");
				System.exit(1);
			}
			if(duplicate==true){
				System.out.println("Multiple lowest theta values; alternate optimum detected. Exiting with current solution.");
				System.exit(1);
			}
			
			
			System.out.println("after computing cjzj and theta:");
			System.out.println(curTab.toString());
			
			loopInd++;
		}
		
		return tabs.get(loopInd+1);
		
	}
	
	private static double apprxZeroOne(double i){
		double epsilon = 0.0000000000001;
		if(i>=0.0 && i<=epsilon){
			return 0.0;
		} else if(i>=1.0 && i<=1.0+epsilon){
			return 1.0;
		} else return i;
	}
	
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
	 * Solves Ax = b by performing:
	 * 1. forward elimination on A
	 * 2. conversion of A to reduced row echelon form
	 * 3. back substitution to yield a particular x with the nullspace of A
	 * 
	 * @param A The left hand side Matrix A in Ax=b
	 * @param b The right hand side vector b in Ax=b
	 * @return A Matrix[][] whose first element is an array only containing the single Matrix (or vector, a 1 column Matrix) x that solves Ax=b, and whose second element is an array of special solutions that constitute the nullspace of A
	 * */
	public static Matrix[][] solveSystem(Matrix A, Matrix b){
		//forward elimination
		Matrix[] r = forwardEliminate(A, b);

		//convert to row reduced echelon form
		r = RREF(r[0], r[1]);

		//solve for x
		/* solutions[0] contains a single array which has the particular x solution, and solutions[1] contains an array of special 
		 * solutions which constitute the nullspace of A  */
		Matrix[][] solutions = findX(r[0], r[1]);
		
		return solutions;
	}
	
	
	
	/**
	 * Computes the summation of all the elements in an array. Runs in linear time.
	 * @param t The array of values
	 * @return The summation of all the values in t
	 * */
	public static double summation(double[] t){
		double sum = 0;
		for(int i=0; i<t.length; i++){
			sum += t[i];
		}
		return sum;
	}
	
	/**
	 * Multiplies each entry t1[i]*t2[i] and stores the result in a new array result[i]. Runs in linear time.
	 * @param t1 The left side array in t1[i]*t2[i]
	 * @param t2 The right side array in t1[i]*t[2]
	 * @return A new array Result with each Result[i] = t1[i]*t2[i]
	 * */
	public static double[] partialDotProduct(double[] t1, double[] t2){
		double[] result = new double[t1.length];
		for(int i=0; i<t1.length; i++){
			result[i] = t1[i]*t2[i];
		}
		
		return result;
	}
	
	/**
	 * Solves for x, including the special solutions.
	 * Finds the special solutions, if they exist, by using the shortcut of reading them off of the free columns, which runs in
	 * O(mn) time, because the upper bound for the number of entries to scan is the size of the matrix.
	 * This method also finds the particular solution by using the shortcut of reading it off of the b vector in RREF, which runs in O(m)
	 * time because the length of the vector b is m. The overall runtime for this method is in O(mn) because the most nesting is one for loop
	 * within another.
	 * 
	 * 
	 * @param A Matrix A in Ax=b
	 * @param b Matrix b in Ax=b
	 * @return An array with the first element being an array containing a particular x solution (this is null if no solution exists), and 
	 * the second element being an array of special solutions that constitute the nullspace of A (null if the only special solution is the 0 vector).
	 * */
	private static Matrix[][] findX(Matrix A, Matrix b){
		Matrix reduced = new Matrix(A.matrix);
		Matrix beta = new Matrix(b.matrix);
		Matrix soln = new Matrix(A.columns, 1);
		
		/* Find pivot columns, free columns, and rank */
		int[] fp = new int[reduced.columns];
		int shift=0;
		for(int i=0; i<reduced.rows; i++){
			for(int j=i+shift; j<reduced.columns; j++){
				if(reduced.matrix[i][j]==0){
					//free column
					fp[j] = 0;
					//move to the right
					shift++;
					continue;
				} else {
					//pivot column
					fp[j] = 1;
					//move down and to the right
					break;
				}
			}
		}
		
		//count the number of pivots and free columns
		int free = 0;
		int pivot = 0;
		for(int i=0; i<fp.length; i++){
			if(fp[i]==0){
				free++;
			} else {
				pivot++;
			}
		}
		
		//create 2 arrays containing the column indices of the pivot and free columns
		int[] freeInd = new int[free];
		int[] pivInd = new int[pivot];
		
		int nextFr = 0;
		int nextPiv = 0;
		for(int i=0; i<fp.length; i++){
			if(fp[i]==0){
				freeInd[nextFr] = i;
				nextFr++;
			} else {
				pivInd[nextPiv] = i;
				nextPiv++;
			}
		}
		
		//get the column rank
		int rank = pivInd.length;
		
		//preinitialize an array for the special solutions
		//if there are no free variables, then the nullspace contains only the zero vector
		Matrix[] special;
		if(free==0){
			special = new Matrix[1];
			double[] zeroVector = new double[A.columns];
			for(int z=0; z<zeroVector.length; z++){
				zeroVector[z] = 0;
			}
			special[0] = new Matrix(zeroVector);
		} else {
			special = new Matrix[free];
		}
		
//		for(int i=0; i<fp.length; i++){
//			System.out.println("Pivot or free in row " + i + ": "+ fp[i]);
//		}
		/* End finding pivot columns, free columns, and rank */
		
		//in general, for any shape matrix, if there is a 0 row that corresponds to a nonzero b, then no solution exists
		boolean noSolutions = false;
		for(int i=reduced.rows-1; i>=0; i--){
			if(isZeroRow(reduced, i)){
				if(beta.matrix[i][0]!=0){
					noSolutions = true;
					return null;
				}
			}
		}
		
		//if full column rank, find unique solution. Else, find particular solution and special solutions
		if(rank==reduced.columns){
			//find unique
			//here, i will be the rows!
			for(int i=reduced.columns-1; i>=0; i--){
				soln.matrix[i][0] = beta.matrix[i][0];
			}
			//nullspace only contains the 0 vector
			
		} else {
			/* Find special solutions */
			for(int i=0; i<special.length; i++){
				double[] x = new double[reduced.columns];
				for(int j=0; j<pivInd.length; j++){
					x[pivInd[j]] = reduced.matrix[j][freeInd[i]] * (-1);	
				}
				for(int j=0; j<freeInd.length; j++){
					x[freeInd[j]] = 0;
				}
				x[freeInd[i]] = 1;
				
				special[i] = new Matrix(x);
			}
			
			/* Find particular solutions */
			//in RREF, vector b has entries for particular solution
			for(int i=0; i<soln.rows; i++){
				soln.matrix[i][0] = 0;
			}
			for(int i=0; i<pivInd.length; i++){
				soln.matrix[pivInd[i]][0] = beta.matrix[i][0];
			}
		}
		
		Matrix[] s = {soln};
		Matrix[][] ret = {s, special};
		return ret;
	}
	
	
	/**
	 * Converts a matrix to Upper Triangular form (while treating A and b as a single augmented matrix). The most expensive operation is the row subtractions. The number of operations 
	 * for the subtractions are (n)^2 + (n-1)^2 + (n-2)^2 +...+ 2 = O(n^3). So the running time of elimination is O(n^3). 
	 * Returns an array of matrices containing the upper triangular for of A with the corresponding b vector.
	 * @param A Matrix A in Ax=b
	 * @param b Matrix b in Ax=b
	 * @return An array containing the upper triangular form of A and the corresponding b vector
	 * */
	public static Matrix[] forwardEliminate(Matrix A, Matrix b){
		//convert to upper triangular U
		
		//start at the upper left corner of the matrix
		Matrix reduced = new Matrix(A.matrix);
		Matrix beta = new Matrix (b.matrix);
		
		/* If there's a non-zero, eliminate downwards and move down and to the right.
		 * If there's a 0, scan down the column to find a row to swap with. If you find the row, the swap, eliminate downwards, and 
		 * move down and to the right. Else, if you don't find the row, it must have been all 0's! So move directly to the right, to the
		 * next column (NOT downwards) and apply the loop above. 
		 * */
		int shift = 0;
		for(int i=0; i<reduced.rows && i<reduced.columns; i++){
			
			for(int j=i+shift; j<reduced.columns; j++){
				boolean foundswap = false;
				if(reduced.matrix[i][j] == 0){
					//if there is a zero in the pivot area...
					//scan down the rows for a nonzero term
					for(int z=i; z<reduced.rows; z++){
						if(reduced.matrix[z][j] != 0){
							//swap
							swap(reduced, beta, z, i, j);
							foundswap = true;
							break;
						}
					}
					
					//if, after scanning, we hit the end of column and no non-zeros, row is already eliminated.
					//shift++
					//move right (NOT DOWN) continue;
					if(!foundswap){
						//we don't eliminate downwards, because clearly at this point, all entries below the current entry are already 0
						shift++;
						continue;
					}
				}
				
				
				//eliminate downwards. break
				//produce zeros below pivot
				for(int z=i+1; z<reduced.rows; z++){
					double l = reduced.matrix[z][j]/reduced.matrix[i][j];
					
					if(reduced.matrix[i][j]!=0) {
						for(int k=j; k<reduced.columns; k++){
							//subtract bottom top*l from bottom row
							reduced.matrix[z][k] = reduced.matrix[z][k] - l*reduced.matrix[i][k];
						}
						//subtract vector entries as well
						beta.matrix[z][0] = beta.matrix[z][0] - l*beta.matrix[i][0];
					}
				}
				
				break;
			}
		}
		
		Matrix[] r = new Matrix[] {reduced, beta};
		return r;
		
	}
	

	/**
	 * This method converts an Upper Triangular matrix to reduced row echelon form (while treating A and b as a single augmented matrix). 
	 * This operation progresses from the last row to the first row, and for each row, performs row subtractions for every row above the current row. So the number of operations is
	 * n + 2(n-1) + 3(n-2) + n(1) = O(n^3).
	 * @param A Matrix A in Ax=b
	 * @param b Matrix b in Ax=b
	 * @return An array containing the RREF form of A and the corresponding b vector
	 * */
	public static Matrix[] RREF(Matrix A, Matrix b){
		Matrix reduced = new Matrix(A.matrix);
		Matrix beta = new Matrix(b.matrix);
		//convert to reduced RREF
		//divide all rows by first non-zero term
		for(int i=0; i<reduced.rows; i++){
			for(int j=i; j<reduced.columns; j++){
				if(reduced.matrix[i][j] != 0){
					double divider = reduced.matrix[i][j];
					divideRows(reduced, beta, i, j, divider);
					break;
				}
			}
		}
		
		//eliminate upwards
		//start at bottom left and scan right until first non-zero
		//subtract upwards
		for(int i=reduced.rows-1; i>0; i--){
			for(int j=0; j<reduced.columns; j++){
				if(reduced.matrix[i][j] != 0){
					for(int z=i-1; z>=0; z--){
						double divider = reduced.matrix[z][j];
						//do row(z) = row(z) - divider*row(i)
						subtractRows(reduced, beta, i, z, divider);
					}
					break;
				}
			}
		}
		
		Matrix[] r = new Matrix[] {reduced, beta};
		return r;
	}
	
	/**
	 * Multiples to compatible matrices together in O(n^3) time. Both Matrices must have dimensions m by n, and n by q, respectfully - that is, the number of rows in A2 must equal the number of columns in A1.
	 * @param A1 The left side Matrix in A1*A2
	 * @param A2 The right side Matrix in A1*A2
	 * @return A Matrix A3 with dimensions m by q (given that A1 is m by n and A2 is n by q)
	 * */
	public static Matrix multiply(Matrix A1, Matrix A2){
		Matrix result = new Matrix(A1.rows, A2.columns);
		for(int i=0; i<result.rows; i++){
			for(int j=0; j<result.columns; j++){
				result.matrix[i][j] = dotProduct(A2, j, A1, i);
			}
		}
		
		return result;
	}
	
	/**
	 * Multiplies the row of A1 times the column of A2 in A1*A2 to get a scalar value
	 * 
	 * @param A2 The right side Matrix in A1*A2
	 * @param column The index of the column of A2 to dot with a row of A1
	 * @param A1 The left side Matrix in A1*A2
	 * @param row The index of the row of A1 to dot with a column of A2
	 * @return The resulting dot product of the operation
	 * */
	public static double dotProduct(Matrix A2, int column, Matrix A1, int row){
		double dp = 0;
		for(int i=0; i<A2.rows; i++){
			dp += A2.matrix[i][column] * A1.matrix[row][i];
		}
		return dp;
	}
	
	
	
	/**
	 * Determines if a row is all 0's by scanning through the row once. Runs in O(n).
	 * @param A Matrix A in Ax=b
	 * @param row The row in A to check
	 * @return True if the row is all 0's
	 * */
	public static boolean isZeroRow(Matrix A, int row){
		for(int i=0; i<A.columns; i++){
			if(A.matrix[row][i] != 0){
				return false;
			}
		}
		
		return true;
	}
	
	
	/**
	 * Divides every value in a row by a divider, treating A and b as a single augmented matrix.
	 * Runs in O(n) time
	 * @param A Matrix A in Ax=b
	 * @param b Matrix b in Ax=b
	 * @param start The column from which to start dividing
	 * @param divider The number to divide each row entry by
	 * Note: this is a mutator method.
	 * */
	public static void divideRows(Matrix A, Matrix b, int row, int start, double divider){
		for(int i=start; i<A.columns; i++){
			A.matrix[row][i] = A.matrix[row][i]/divider;
		}
		//for vector b as well
		b.matrix[row][0] = b.matrix[row][0]/divider;
	}
	
	public static Matrix addVectors(Matrix[] ms, Matrix coeffs){
		double[] d = new double[ms[0].rows];
		
		for(int i=0; i<ms.length; i++){
			for(int j=0; j<ms[i].rows; j++){
				d[j] += ms[i].matrix[j][0]*coeffs.matrix[i][0];
			}
		}
		
		Matrix vec = new Matrix(d);
		return vec;
	}
	
	/**
	 * Subtracts a given row from another given row, treating A and b as a single augmented matrix.
	 * The operation is topRow = topRow - divider*pivRow
	 * Runs in O(n) time.
	 * Note: this is a mutator method.
	 * @param A Matrix A in Ax=b
	 * @param b Matrix b in Ax=b
	 * @param pivRow pivRow in topRow = topRow - pivRow
	 * @param topRow topRow in topRow = topRow - pivRow
	 * @param divider multiplies pivRow in topRow - divider*pivRow
	 * Note: this is a mutator method.
	 * */
	public static void subtractRows(Matrix A, Matrix b, int pivRow, int topRow, double divider){
		//need to do topRow = topRow - pivRow
		for(int i=0; i<A.columns; i++){
			A.matrix[topRow][i] = A.matrix[topRow][i] - divider*A.matrix[pivRow][i];
		}
		//for vector b as well
		b.matrix[topRow][0] = b.matrix[topRow][0] - divider*b.matrix[pivRow][0];
	}
	
	/**
	 * Swaps 2 rows in O(n) time. This method treats A and b as a single augmented matrix and scans that augmented matrix from left to right,
	 * swapping row elements. The "start" parameter indicates where to start swapping from (i.e. for start=2, this method will swap the elements
	 * of row1 and row2 starting from column 2)
	 * Some algebra allows it to use 2 cells to swap 2 values instead of 3 cells, which is pretty cool.
	 * Note: this is a mutator method.
	 * @param A Matrix A in Ax=b
	 * @param b Matrix b in Ax=b
	 * @param row1 Row to swap
	 * @param row2 Row to swap
	 * @param start Column to start swapping from
	 * */
	public static void swap(Matrix A, Matrix b, int row1, int row2, int start){
		int length = A.columns;
		//swap rows in Matrix A
		for(int i=start; i<length; i++){
			A.matrix[row1][i] += A.matrix[row2][i];
			A.matrix[row2][i] = A.matrix[row1][i] - A.matrix[row2][i];
			A.matrix[row1][i] -= A.matrix[row2][i];
		}

		//swap rows in vector b
		b.matrix[row1][0] += b.matrix[row2][0];
		b.matrix[row2][0] = b.matrix[row1][0] - b.matrix[row2][0];
		b.matrix[row1][0] -= b.matrix[row2][0];

	}
	
	/**
	 * This method never got anywhere so I guess it's deprecated.
	 * 
	 * */
	private static Matrix augment(Matrix[] matrices){
		//get rows of Ab
		int rows = 0;
		int columns = 0;
		for(int i=0; i<matrices.length; i++){
			rows += matrices[i].rows;
			columns += matrices[i].columns;
		}
		Matrix Ab = new Matrix(rows, columns);
		
		for(int i=0; i<Ab.rows; i++){
			
		}
		
		return Ab;
	}
}
