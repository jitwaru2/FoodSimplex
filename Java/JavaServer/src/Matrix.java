public class Matrix {

	double[][] matrix;
	int rows;
	int columns;
	
	/** 
	 * Creates an empty matrix with n "rows" rows and "columns" columns
	 * */
	public Matrix(int rows, int columns){
		matrix = new double[rows][columns];
		this.rows = rows;
		this.columns = columns;
	}
	
	/** Creates a square "dimensions" by "dimensions" matrix */
	public Matrix(int dimensions){
		matrix = new double[dimensions][dimensions];
		this.rows = dimensions;
		this.columns = dimensions;
	}
	
	/** Creates a matrix using a pre-constructed 2 dimensional array of doubles */
	public Matrix(double[][] mat){
		matrix = mat;
		
		
		rows = mat.length;
		columns = mat[0].length;
	}
	
	/** Creates a matrix that acts as a vector */
	public Matrix(double[] mat){
		matrix = new double[mat.length][1];
		rows = mat.length;
		columns = 1;
		
		for(int i=0; i<mat.length; i++){
			matrix[i][0] = mat[i];
		}
	}
	
	public void setColumnVector(Matrix vector, int column){
		if(vector.rows==this.rows){
			for(int i=0; i<this.rows; i++){
				this.matrix[i][column] = vector.matrix[i][0];
			}
		}
	}

	/** Prints the matrix to the console in a table format */
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
