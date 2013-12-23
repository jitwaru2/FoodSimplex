
/**
 * This protocol class is the communication contract between Java and Node.js
 * @author josh
 *
 */
public class Protocol {
	final static int beginSystem = 111;
	final static int endSystem = 112;
	final static int endMsg = 99;
	final static int solution = 222;
	final static int noSolution = 223;
	final static int deadSolution = 224;
	final static int leastSquares = 230;
	final static int noLeastSquares = 231;
	final static int simplex = 311;
	final static int noSimplex = 312;
	
	final static String nullSolutionMsg = "[No solution vector]";
	final static String deadSolutionMsg = "This isn't ideal, but this is it...(note, negatives mean that you must 'excrete' food...)";
	final static String noFoodComboMsg = "There is no combination of foods that you can eat to achieve the specific macro set!";
	final static String leastSquaresMsg = "Couldn't find a solution to your macro set, but I tried to a solution as close to ideal as possible:";
	final static String simplexMsg = "Solution optimized using simplex method";
	final static String noSimplexMsg = "Solution could not be optimized";
	
	
}
