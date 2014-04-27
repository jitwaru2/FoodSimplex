import java.util.*;

/**
 * **This class was the original plan, until I realized that I needed to use the simplex method on top of the system of equations. 
 * **I don't use the knapsack or rodCutting methods in this project, but they do work, and they're pretty cool.
 * 
 * Calculations:
 * - Given a budget, maximize specified macro: (rod cutting problem, dynamic programming: optimal substructure)
 * - Maximize macros for a knapsack: continuous (knapsack problem, dynamic programming: greedy choice)
 * - Maximize macros for a knapsack: discrete (knapsack problem, dynamic programming: optimal substructure)
 * - Find proportions of food to achieve exact macros (linear algebra, system of equations+least squares approximation)
 * */
public class Optimizer {
	
	/**
	 * Recursive rod cutting method
	 * @param price The price to optimize
	 * @param optTable 2 dimensional array that contains the optimal values for prices 0 through n
	 * @param priceArr 1 dimensional array where index number refers to price and array[index] is the weight for the price
	 * @return
	 */
	public static int rodCutting(int price, int[][] optTable, int[] priceArr){
		/* If an optimal value was already calculated, return that value from the optTable instead of wastefully recalculating */
		if(optTable[price][0] != -1){
			System.out.println("Returning value: " + price + " $" + optTable[price][0]);
			return optTable[price][0];
		}

		/* The optimal cut for a rod of size 1 is always 1 */
		if(price==1){
			System.out.println("Boiled down to price==1. Setting optTable[1][0] to: " + priceArr[1]);
			optTable[1][0] = priceArr[1];
			optTable[1][1] = 1;
			return priceArr[1];
		}

		/* The 'weight' array will hold the values of the sum of each term in the series** */
		int[] weight = new int[(price/2)+1];
		
		//Implicitly: price + 0 = price
		weight[0] = priceArr[price];

		int priceCopy = price;
		/* Generates the terms: 9+1, 8+2, 7+3, 6+4, 5+5 */
		for(int i=1; i<weight.length; i++){
			System.out.println("Solving for " + (priceCopy-1) + " + " + i);
			weight[i] = rodCutting(--priceCopy, optTable, priceArr) + rodCutting(i, optTable, priceArr);
			System.out.println("Finished solving " + (priceCopy) + " + " + i + " = " + weight[i]);
		}

		/* Get the tuple {index, cost} */
		int[] tuple = max(weight);

		//update optimal table
		int arg1 = price-tuple[0];
		int arg2 = tuple[0];
		optTable[price][0] = tuple[1];
		optTable[price][arg1]++;
		if(arg2 != 0){
			optTable[price][arg2]++;
		}
		System.out.println("arg1 is " + arg1 + " arg2 is " + arg2);

		return tuple[1];

	}

	/**
	 * Helper method for rodCutting; returns max value of the input array in the form {index, cost}
	 * @param input
	 * @return
	 */
	public static int[] max(int[] input){
		int index = 0;
		int biggest = input[0];

		for(int i=1; i<input.length; i++){
			if(input[i]>biggest){
				index = i;
				biggest = input[i];
			}
		}

		return new int[] {index, biggest};
	}
	
	/**
	 * Given a set of foods in your fridge and a weight capacity, this method packs the most of the specified macro into your bag under the weight limit.
	 * Given a set of foods at the grocery store and a money budget, this method packs the most of the specified macro into your cart under the money limit.
	 * @param capacity
	 * @param weights
	 * @param values
	 * @param optTable
	 * @param keep
	 */
	public static void packKnapsack(int capacity, Matrix weights, Matrix values, Matrix optTable, Matrix keep){
		/* Recursive approach is to recursively call knapsack on capacity-1, but we will use an iterative approach to 
		 * go easy on the call stack. */
		
		for(int i=1; i<optTable.rows; i++){
			for(int j=1; j<optTable.columns; j++){
				/* Does it fit in the sack? If yes, then compare the inclusion with the exclusion */
				if(0 <= j-(int)weights.matrix[i][0]){
					double include = values.matrix[i][0] + optTable.matrix[i-1][j-(int)weights.matrix[i][0]];
					double exclude = optTable.matrix[i-1][j];
					
					/* If the inclusion value is better than the exclusion value, then include the item in the sack */
					if(include > exclude){
						optTable.matrix[i][j] = include;
						keep.matrix[i][j] = 1;
					} 
					/* If excluding the item from the sack yields a better value, then exclude the item*/
					else {
						optTable.matrix[i][j] = exclude;
						//keep is 0 by default
					}
				} else {
					optTable.matrix[i][j] = optTable.matrix[i-1][j];
				}
			}
		}
	}
	
	public static int[] getKnapsack(int capacity, Matrix weights, Matrix values, Matrix optTable, Matrix keep){
		//subtract 1 because weights has a default 0 weight for a default 0 value item which we don't explicitly use
		int i= values.rows-1;
		int c = capacity;
		int[] items = new int[values.rows];
		
		while(i>0){
			if(keep.matrix[i][c]==1){
				//add item i to keep list
				System.out.println("keep item " + i);
				items[i] = 1;
				c -= weights.matrix[i][0];
			}
			i--;
			System.out.println(i);
			System.out.println(c);
		}
		
		return items;
	}
	
	public static void algebraicSimplex(){
		/* Convert inequalties to equalities by adding slack variables */
		//...
		
		/* Initialize matrix:
		 * columns: # of variables + RHS + max push row
		 * rows: well, a lot. I'll make it big for now */
		Matrix A = new Matrix(50); //change 50 later
		
		
		
	}
	
}
