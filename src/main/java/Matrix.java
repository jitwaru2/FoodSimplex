/**
 * Wrapper for a 2 dimensional double array with "rows" and "columns" attributes for convenience
 * @author josh
 *
 */
public class Matrix {

	double[][] matrix;
	int rows;
	int columns;
	
	/**
	 * Creates an empty matrix with n "rows" rows and "columns" columns
	 * @param rows Number of rows
	 * @param columns Number of columns
	 */
	public Matrix(int rows, int columns){
		matrix = new double[rows][columns];
		this.rows = rows;
		this.columns = columns;
	}
	
	/**
	 *	Creates a square "dimensions" by "dimensions" matrix  
	 * @param dimensions Number of rows and columns
	 */
	public Matrix(int dimensions){
		matrix = new double[dimensions][dimensions];
		this.rows = dimensions;
		this.columns = dimensions;
	}
	
	/**
	 * Creates a matrix using a pre-constructed 2 dimensional array of doubles
	 * @param mat Preconstructed double[][]
	 */
	public Matrix(double[][] mat){
		matrix = mat;
		rows = mat.length;
		columns = mat[0].length;
	}
	
	/**
	 * Creates a matrix that acts as a vector
	 * @param mat Preconstructed double[]
	 */
	public Matrix(double[] mat){
		matrix = new double[mat.length][1];
		rows = mat.length;
		columns = 1;
		
		for(int i=0; i<mat.length; i++){
			matrix[i][0] = mat[i];
		}
	}
	
	/**
	 * Sets a specified column of this matrix to take the values of a given column vector
	 * @param vector Column vector to copy
	 * @param column Column number to replace
	 */
	public void setColumnVector(Matrix vector, int column){
		if(vector.rows==this.rows){
			for(int i=0; i<this.rows; i++){
				this.matrix[i][column] = vector.matrix[i][0];
			}
		}
	}
	
	/**
	 * Checks if a vector is a zero vector
	 * @param m Vector to check
	 * @return 
	 */
	public static boolean isZeroVector(Matrix m){
		if(m.columns>1){
			return false;
		}
		
		for(int i=0; i<m.rows; i++){
			if(m.matrix[i][0]!=0){
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if a matrix is non-negative
	 * @param m Matrix to check
	 * @return
	 */
	public static boolean isNonnegative(Matrix m){
		for(int i=0; i<m.rows; i++){
			for(int j=0; j<m.columns; j++){
				if(m.matrix[i][j]<0){
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Gets a table representation of the matrix
	 * @return Matrix string
	 */
	public String toString(){
		String m = "";
		for(int i=0; i<this.rows; i++){
			for(int j=0; j<this.columns; j++){
				m += "["+this.matrix[i][j]+"]";
			}
			m+="\n";
		}
		return m;
	}
}
