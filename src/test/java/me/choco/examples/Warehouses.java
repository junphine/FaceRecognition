package me.choco.examples;

import java.util.Arrays;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.*;
import org.chocosolver.solver.variables.*;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * 
Warehouse Location Problem
In the Warehouse Location problem (WLP), a company considers opening warehouses at some candidate locations in order to supply its existing stores.

Each possible warehouse has the same maintenance cost, and a capacity designating the maximum number of stores that it can supply.

Each store must be supplied by exactly one open warehouse. The supply cost to a store depends on the warehouse.

The objective is to determine which warehouses to open, and which of these warehouses should supply the various stores, such that the sum of the maintenance and supply costs is minimized.

See this page for more details.
 * @author Hunteron-cp
 *
 */
public class Warehouses {
	
	// number of warehouses
	int W = 5;
	// number of stores
	int S = 10;
	// maintenance cost
	int C = 30;
	// capacity of each warehouse
	int[] K = new int[]{1, 4, 2, 1, 3};
	// matrix of supply costs, store x warehouse
	int[][] P = new int[][]{
	    {20, 24, 11, 25, 30},
	    {28, 27, 82, 83, 74},
	    {74, 97, 71, 96, 70},
	    {2, 55, 73, 69, 61},
	    {46, 96, 59, 83, 4},
	    {42, 22, 29, 67, 59},
	    {1, 5, 73, 59, 56},
	    {10, 73, 13, 43, 96},
	    {93, 35, 63, 85, 46},
	    {47, 65, 55, 71, 95}};
	
/**	
	Variables
	A boolean variable openiopeni per warehouse i is needed, set to true if the corresponding warehouse is open, false otherwise.

	∀i∈[1,5],openi={0,1}∀i∈[1,5],openi={0,1}
	An integer variable supplierjsupplierj per store j is needed, it indicates which warehouse supplies it.

	∀j∈[1,10],supplierj=[[1,5]]∀j∈[1,10],supplierj=[[1,5]]
	An integer variable costjcostj per store j is needed too, it stores the cost of being supplied by a warehouse (the range is deduced from the matrix P).

	∀j∈[1,10],costj=[[1,96]]∀j∈[1,10],costj=[[1,96]]
	An integer variable totcosttotcost totals all costs:

	tot_cost=[[1,+∞)
*/	
	
	
	// A new model instance
	Model model = new Model("WarehouseLocation");

	// VARIABLES
	// a warehouse is either open or closed
	BoolVar[] open = model.boolVarArray("o", W);
	// which warehouse supplies a store
	IntVar[] supplier = model.intVarArray("supplier", S, 1, W, false);
	// supplying cost per store
	IntVar[] cost = model.intVarArray("cost", S, 1, 96, true);
	// Total of all costs
	IntVar tot_cost = model.intVar("tot_cost", 0, 99999, true);
			
	public Model buildModel(){
		// CONSTRAINTS
		for (int j = 0; j < S; j++) {
		    // a warehouse is 'open', if it supplies to a store
		    model.element(model.intVar(1), open, supplier[j], 1).post();
		    // Compute 'cost' for each store
		    model.element(cost[j], P[j], supplier[j], 1).post();
		}
		for (int i = 0; i < W; i++) {
		    // additional variable 'occ' is created on the fly
		    // its domain includes the constraint on capacity
		    IntVar occ = model.intVar("occur_" + i, 0, K[i], true);
		    // for-loop starts at 0, warehouse index starts at 1
		    // => we count occurrences of (i+1) in 'supplier'
		    model.count(i+1, supplier, occ).post();
		    // redundant link between 'occ' and 'open' for better propagation
		    occ.ge(open[i]).post();
		}
		// Prepare the constraint that maintains 'tot_cost'
		int[] coeffs = new int[W + S];
		Arrays.fill(coeffs, 0, W, C);
		Arrays.fill(coeffs, W, W + S, 1);
		// then post it
		model.scalar(ArrayUtils.append(open, cost), coeffs, "=", tot_cost).post();

		model.setObjective(false, tot_cost);
		
		return model;
	}
		
	void doSolver(Model model){
		Solver solver = model.getSolver();
		solver.setSearch(Search.intVarSearch(
		    new VariableSelectorWithTies<>(
		        new FirstFail(model),
		        new Smallest()),
		    new IntDomainMiddle(false),
		    ArrayUtils.append(supplier, cost, open))
		);
		solver.showShortStatistics();
		while(solver.solve()){
		    prettyPrint(model, open, W, supplier, S, tot_cost);
		}
	}
	
	private void prettyPrint(Model model, IntVar[] open, int W, IntVar[] supplier, int S, IntVar tot_cost) {
	    StringBuilder st = new StringBuilder();
	    st.append("Solution #").append(model.getSolver().getSolutionCount()).append("\n");
	    for (int i = 0; i < W; i++) {
	        if (open[i].getValue() > 0) {
	            st.append(String.format("\tWarehouse %d supplies customers : ", (i + 1)));
	            for (int j = 0; j < S; j++) {
	                if (supplier[j].getValue() == (i + 1)) {
	                    st.append(String.format("%d ", (j + 1)));
	                }
	            }
	            st.append("\n");
	        }
	    }
	    st.append("\tTotal C: ").append(tot_cost.getValue());
	    System.out.println(st.toString());
	}
	
	public static void main(String[] args){
		Warehouses warehouses= new Warehouses();
		warehouses.buildModel();
		//寻找每个仓库服务的商店，使维护费用最低
		warehouses.doSolver(warehouses.model);
	}
}
