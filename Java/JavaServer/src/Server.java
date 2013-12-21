import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;


public class Server {
	public static void main(String[] args){
		ServerSocket server;
		Socket client;
		InputStream input;
		
		try {
			server = new ServerSocket(8007);
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
		    	int type = Protocol.beginSystem;
		    	switch(type){
		    		/* 1. Convert frontend input into matrices
		    		 * 2. Solve system of equations in matrices
		    		 * 3. Run simplex on solution vector and nullspace vectors to determine nullspace coefficients
		    		 * 4. Solve the new system of equations (by arithmetic) to get a single vector
		    		 * 5. Return the resulting vector to Node.js */
		    		case(Protocol.beginSystem): {
		    			Matrix[] matrices = np.extractMatrices(msg);
		    			
		    			System.out.println("Here are the matrices from the client:");
		    			
		    			for(Matrix m : matrices){
		    				System.out.println(m.toString());
		    			}
		    			
		    			Matrix[][] solns = MatOps.solveSystem(matrices[0], matrices[1]);
		    			System.out.println("Solution vector:");
		    			System.out.println(solns[0][0].toString());
		    			System.out.println("Nullspace vectors:");
		    			for(Matrix m : solns[1]){
		    				System.out.println(m.toString());
		    			}
		    			
		    			Tableau tab = MatOps.TwoPhaseSimplex(solns);
		    			double[] coeffs = new double[solns[1].length+1];
		    			coeffs[coeffs.length-1] = 1;
		    			for(int i=0; i<tab.BVC.rows; i++){
		    				if(tab.BVC.matrix[i][0]<solns[1].length){
		    					coeffs[(int)tab.BVC.matrix[i][0]] = tab.RHS.matrix[i][0];
		    				}
		    			}
		    			
		    			System.out.println("Modified Solution vector:");
		    			System.out.println(solns[0][0].toString());
		    			System.out.println("Modified Nullspace vectors:");
		    			for(Matrix m : solns[1]){
		    				System.out.println(m.toString());
		    			}
		    			
		    			
		    			Matrix coefficients = new Matrix(coeffs);
		    			
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
		    			String s = "";
		    			for(int i=0; i<x.rows; i++){
		    				s += x.matrix[i][0]+" ";
		    			}
		    			s+="\n";
		    			System.out.println(x.toString());
		    			
		    			out.write(s);
		    		}
		    	}
			    
			    /* Convert JSON data to Java data */
			    
			    /* Perform computations */
			    
			    /* Convert to JSON format */
			    
			    /* Send JSON data to Node.js */

			    
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