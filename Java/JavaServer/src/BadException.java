/**
 * Thrown when a solution to a matrix system is either non-feasible or an approximation
 * @author josh
 *
 */
public class BadException extends Exception {

	public BadException(){
		super();
	}
	
	public BadException(String message){
		super(message);
	}
}
