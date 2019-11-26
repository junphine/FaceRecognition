package me.choco.examples;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.*;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;

public class AircraftLandingProblem {
	// number of planes
	int N = 10;
	// Times per plane: {earliest landing time, target landing time, latest landing time}
	int[][] LT = {
	    {129, 155, 559},
	    {195, 258, 744},
	    {89, 98, 510},
	    {96, 106, 521},
	    {110, 123, 555},
	    {120, 135, 576},
	    {124, 138, 577},
	    {126, 140, 573},
	    {135, 150, 591},
	    {160, 180, 657}};
	// 惩罚成本 penalty cost penalty cost per unit of time per plane: {for landing before target, after target}
	int[][] PC = {
	    {10, 10},
	    {10, 10},
	    {30, 30},
	    {30, 30},
	    {30, 30},
	    {30, 30},
	    {30, 30},
	    {30, 30},
	    {30, 30},
	    {30, 30}};

	// Separation time required after i lands before j can land
	int[][] ST = {
	    {99999, 3, 15, 15, 15, 15, 15, 15, 15, 15},
	    {3, 99999, 15, 15, 15, 15, 15, 15, 15, 15},
	    {15, 15, 99999, 8, 8, 8, 8, 8, 8, 8},
	    {15, 15, 8, 99999, 8, 8, 8, 8, 8, 8},
	    {15, 15, 8, 8, 99999, 8, 8, 8, 8, 8},
	    {15, 15, 8, 8, 8, 99999, 8, 8, 8, 8},
	    {15, 15, 8, 8, 8, 8, 99999, 8, 8, 8},
	    {15, 15, 8, 8, 8, 8, 8, 99999, 8, 8},
	    {15, 15, 8, 8, 8, 8, 8, 8,  99999, 8},
		{15, 15, 8, 8, 8, 8, 8, 8,  8, 99999}};
	
	/**	
	Variables
	An integer variable planeiplanei per plane i indicates its landing time.
	
	∀i∈[1,10],planei=[[LTi,0,LTi,2]]∀i∈[1,10],planei=[[LTi,0,LTi,2]]
	An integer variable earlinessjearlinessj per plane i indicates how early a plane lands.
	
	∀i∈[1,10],earlinessi=[[0,LTi,1−LTi,0]∀i∈[1,10],earlinessi=[[0,LTi,1−LTi,0]
	An integer variable tardinessjtardinessj per plane i indicates how late a plane lands.
	
	∀i∈[1,10],tardinessi=[[0,LTi,2−LTi,1]∀i∈[1,10],tardinessi=[[0,LTi,2−LTi,1]
	An integer variable totdevtotdev totals all costs:
	
	tot_dev=[[0,+∞)
	
	the deviation cost has then to be maintained:
	
	tot_dev=∑10i=1PCi,0⋅earlinessi+PCi,1⋅tardinessi
	
	*/
	Model model;	
	public Model buildModel(){
		// CONSTRAINTS
		// load parameters
		// ...
		// A new model instance
		model = new Model("Aircraft landing");
		// Variables declaration
		IntVar[] planes = IntStream
		        .range(0, N)
		        .mapToObj(i -> model.intVar("plane #" + i, LT[i][0], LT[i][2], false))
		        .toArray(IntVar[]::new);
		IntVar[] earliness = IntStream
		        .range(0, N)
		        .mapToObj(i -> model.intVar("earliness #" + i, 0, LT[i][1] - LT[i][0], false))
		        .toArray(IntVar[]::new);
		IntVar[] tardiness = IntStream
		        .range(0, N)
		        .mapToObj(i -> model.intVar("tardiness #" + i, 0, LT[i][2] - LT[i][1], false))
		        .toArray(IntVar[]::new);
	
		IntVar tot_dev = model.intVar("tot_dev", 0, IntVar.MAX_INT_BOUND);
	
		// Constraint posting
		// one plane per runway at a time:
		model.allDifferent(planes).post();
		// for each plane 'i'
		for(int i = 0; i < N; i++){
		    // maintain earliness
		    earliness[i].eq((planes[i].neg().add(LT[i][1])).max(0)).post();
		    // and tardiness
		    tardiness[i].eq((planes[i].sub(LT[i][1])).max(0)).post();
		    // disjunctions: 'i' lands before 'j' or 'j' lands before 'i'
		    for(int j = i+1; j < N; j++){
		        Constraint iBeforej = model.arithm(planes[i], "<=", planes[j], "-", ST[i][j]);
		        Constraint jBeforei = model.arithm(planes[j], "<=", planes[i], "-", ST[j][i]);
		        model.addClausesBoolNot(iBeforej.reify(), jBeforei.reify());
		    }
		}
		// prepare coefficients of the scalar product
		int[] cs = new int[N*2];
		for(int i = 0 ; i < N; i++){
		    cs[i] = PC[i][0];
		    cs[i + N] = PC[i][1];
		}
		model.scalar(ArrayUtils.append(earliness, tardiness), cs, "=", tot_dev).post();
		
		
		// Resolution process
			Solver solver = model.getSolver();
			solver.plugMonitor((IMonitorSolution) () -> {
			    for (int i = 0; i < N; i++) {
			        System.out.printf("%s lands at %d (%d)\n",
			                planes[i].getName(),
			                planes[i].getValue(),
			                planes[i].getValue() - LT[i][1]);
			    }
			    System.out.printf("Deviation cost: %d\n", tot_dev.getValue());
			});
			Map<IntVar, Integer> map = IntStream
			        .range(0, N)
			        .boxed()
			        .collect(Collectors.toMap(i -> planes[i], i -> LT[i][1]));
			solver.setSearch(Search.intVarSearch(
			    variables -> Arrays.stream(variables)
			          .filter(v -> !v.isInstantiated())
			          .min((v1, v2) -> closest(v2, map) - closest(v1, map))
			          .orElse(null),
			    var -> closest(var, map),
			    DecisionOperatorFactory.makeIntEq(),
			    planes
			));
			
			// Find a solution that minimizes 'tot_dev'
			solver.showShortStatistics();
			Solution best =solver.findOptimalSolution(tot_dev, false);
			
		return model;
	}
	

	private static int closest(IntVar var, Map<IntVar, Integer> map) {
	    int target = map.get(var);
	    if (var.contains(target)) {
	        return target;
	    } else {
	        int p = var.previousValue(target);
	        int n = var.nextValue(target);
	        return Math.abs(target - p) < Math.abs(n - target) ? p : n;
	    }
	}
	void doSolver(Model model){	
		
		
	}
	
	public static void main(String[] args){
		AircraftLandingProblem problem= new AircraftLandingProblem();
		problem.buildModel();
		//飞机降落问题。	给定一组飞机和跑道，目标是最小化每个飞机的总着陆时间（加权）偏差。	
		problem.doSolver(problem.model);
	}
}
