package at.medevit.aspectj;

public aspect TimeAspectJ {
	 before(Object t) : target(t) && execution(* ch.elexis.core.ui..*.run(..)) {
	       System.out.println("ASPECT----------------run----------------");
	       System.out.println("This is the target object: "+t);
	   }
	 
	 before(): execution(* start(..)){
		 System.out.println("ASPECT----------------start(..)----------------");
	 }
	 
	 before(): execution(* askForUsageConditionAcceptance(..)){
		 System.out.println("ASPECT----------------usageConditions----------------");
	 }
}
