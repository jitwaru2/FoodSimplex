import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Backend Java server, designed specifically to communicate with Node.js via NodeProtocol and Protocol
 * @author josh
 *
 */
public class Server {
	public static void main(String[] args){
		ServerSocket server;
		Socket client;
		InputStream input;
		
		try {
			server = new ServerSocket(8015);
			client = server.accept();
			System.out.println("Client accepted");
			
			PrintWriter out = new PrintWriter(client.getOutputStream(), true);
		    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			
			int counter = 0;
			
			NodeProtocol np = new NodeProtocol();
			
			/* while(true) means that this program will flip out if the client drops */
			while(true){
				input = client.getInputStream();
				System.out.println("passed input = client.getInputStream()");
				String inputLine = "";
				
				/* Get data from Node.js */
				int status = 0;
				String msg = "";
			    while (status!=Protocol.endMsg) { //blocking here until it gets a newline
			    	counter++;
			    	inputLine = in.readLine();
			    	msg+= inputLine+'\n';
			    	
			    	try {
			    		status = Integer.parseInt(inputLine);
			    		System.out.println("#Status set to: " + status+ " while endMsg is: " + Protocol.endMsg);
			    	} catch (Exception e){
			    		System.out.println("#Failed to convert to int:" + e.getMessage());
			    	}
			    	
			    	System.out.println("Read " + counter + " lines:");
				    System.out.println(inputLine);
			    }
			    
			    System.out.println("Here is the total data:");
			    System.out.println(msg);
			    
			    /* Figure out what to do with data */
		    	int type = np.getMsgType(msg);
		    	switch(type){
		    		/* 1. 	Convert frontend input into matrices
		    		 * 2. 	Solve system of equations in matrices
		    		 * 2.1 	If there is no solution, project b onto the column space of A, and perform least squares approximation
		    		 * 3. 	Run simplex on solution vector and nullspace vectors to determine nullspace coefficients
		    		 * 4. 	Solve the new system of equations (by arithmetic) to get a single vector
		    		 * 5. 	Return the resulting vector to Node.js */
		    		case(Protocol.beginSystem): {
		    			System.out.println("Protocol matched as: beginSystem");
		    			
		    			np.setData(msg);
		    			String solutionMsg = "";
		    			while(np.hasNextMatrixCombo()){
			    			try {
			    				/* Extract matrices A, b, and objMax from user input */
//				    			Matrix[] matrices = np.extractMatrices(msg);
			    				Matrix[] matrices = np.extractNextCombo();
				    			
				    			System.out.println("Here are the matrices from the client:");
				    			
				    			for(Matrix m : matrices){
				    				System.out.println(m.toString());
				    			}
				    			
				    			/* Solve system of equations. */
				    			Matrix[][] solns = MatOps.solveSystem(matrices[0], matrices[1]);
				    			
				    			
				    			/* If there is no solution, try to solve for the projection */
				    			boolean leastSquares = false;
				    			boolean simplex = false;
				    			if(solns==null){
				    				System.out.println("Solutions are null -- attempting a least squares approximation.");
				    				solns = MatOps.leastSquaresApproximation(matrices[0], matrices[1]);
				    				
				    				leastSquares = true;
				    				
				    				/* If least squares approximation doesn't produce an answer, then there is no solution */
				    				if(solns==null){
				    					System.out.println("Least squares solutions are null");
				    					String s = np.getOutput(Protocol.noSolution, null, null, leastSquares, simplex, np.getCurrentCombo());
						    			solutionMsg += s;
					    				throw new BadException();
				    				}
				    			}
				    			
				    			System.out.println("@@@@@ CHECKPOINT 1:");
				    			int w=0;
				    			for(Matrix[] mm : solns){
				    				for(Matrix m : mm){
				    					System.out.println("solns " + w++);
				    					System.out.println(m.toString());
				    				}
				    			}
				    			
				    			System.out.println("Solution vector:");
				    			System.out.println(solns[0][0].toString());
				    			System.out.println("Nullspace vectors:");
				    			for(Matrix m : solns[1]){
				    				System.out.println(m.toString());
				    			}
				    			
				    			/* If the nullspace is 0 vector, then simplex won't work --> if solution vector x is positive, then return x */
				    			if(solns[1].length==1 && Matrix.isZeroVector(solns[1][0])){
				    				if(Matrix.isNonnegative(solns[0][0])){
				    					System.out.println("Zero nullspace and non-negative solution vector x:");
				    					Matrix lsb = null;
				    					if(leastSquares==true){
				    						lsb = solns[3][0];
				    					}
						    			String s = np.getOutput(Protocol.solution, solns[0][0], lsb, leastSquares, simplex, np.getCurrentCombo());
				    					solutionMsg += s;
				    					throw new BadException();
				    				} else {
				    					System.out.println("Least squares failed and there is no feasible solution");
				    					Matrix lsb = null;
				    					if(leastSquares==true){
				    						lsb = solns[3][0];
				    					}
						    			String s = np.getOutput(Protocol.deadSolution, solns[0][0], lsb, leastSquares, simplex, np.getCurrentCombo());
				    					solutionMsg += s;
				    					throw new BadException();
				    				}
				    			}
				    			
				    			
				    			int q=0;
				    			System.out.println("BEFORE SIMPLEX MATRICES!");
				    			for(Matrix[] mm : solns){
				    				for(Matrix m : mm){
				    					System.out.println("solns " + q++);
				    					System.out.println(m.toString());
				    				}
				    			}
				    			
				    			/* Run simplex on matrices to get solution tab */
				    			Tableau tab = MatOps.TwoPhaseSimplex(solns, matrices[2]);
				    			
				    			/* If there is no solution tab, then there is no solution */
				    			if(tab==null){
				    				System.out.println("No solution tab; dead solution");
				    				Matrix lsb = null;
				    				if(leastSquares==true){
				    					lsb = solns[3][0];
				    				}
					    			String s = np.getOutput(Protocol.deadSolution, solns[0][0], lsb, leastSquares, simplex, np.getCurrentCombo());
			    					solutionMsg += s;
			    					throw new BadException();
				    			} else {
				    				simplex = true;
				    			}
				    			
				    			/* Extract BVC to matrix */
				    			double[] coeffs = new double[solns[1].length+1];
				    			coeffs[coeffs.length-1] = 1;
				    			for(int i=0; i<tab.BVC.rows; i++){
				    				if(tab.BVC.matrix[i][0]<solns[1].length){
				    					coeffs[(int)tab.BVC.matrix[i][0]] = tab.RHS.matrix[i][0];
				    				}
				    			}
				    			Matrix coefficients = new Matrix(coeffs);
				    			
				    			/* Plug in coefficients for nullspace vectors and add all solution vectors to get new vector x */
				    			Matrix[] addends = new Matrix[solns[1].length+1];
				    			addends[addends.length-1] = solns[0][0];
				    			for(int i=0; i<solns[1].length; i++){
				    				addends[i] = solns[1][i];
				    			}
				    			
				    			System.out.println("Matrices in addends:");
				    			for(Matrix m : addends){
				    				System.out.println(m.toString());
				    			}
				    			System.out.println("Coeffients:");
				    			for(int i=0; i<coefficients.rows; i++){
				    				System.out.println(coefficients.matrix[i][0]);
				    			}
				    			
				    			
				    			Matrix x = MatOps.addVectors(addends, coefficients);
				    			
				    			/* Send back resulting vector */
				    			Matrix lsb = null;
				    			if(leastSquares==true){
				    				lsb = solns[3][0];
				    			}
				    			
				    			int i=0;
				    			for(Matrix[] mm : solns){
				    				for(Matrix m : mm){
				    					System.out.println("solns " + i++);
				    					System.out.println(m.toString());
				    				}
				    			}
				
				    			
				    			System.out.println("Good solution");
				    			String s = np.getOutput(Protocol.solution, x, lsb, leastSquares, simplex, np.getCurrentCombo());
				    			System.out.println(x.toString());
				    			
				    			solutionMsg += s;
				    			System.out.println("hasNextMatrixCombo is " + np.hasNextMatrixCombo());
			    			} catch (BadException e){
			    				continue;
			    			}
		    			}
		    			
		    			solutionMsg += Protocol.endTransmission + "\n";
		    			System.out.println("Final solution message:");
		    			System.out.println(solutionMsg);
		    			
		    			out.write(solutionMsg);
		    		}
		    	}
			    
			    
			    if(out.checkError()){
			    	System.out.println("Error detected; closing.");
			    	break;
			    }

			}
		    
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}