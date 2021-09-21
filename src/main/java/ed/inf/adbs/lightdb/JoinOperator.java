package ed.inf.adbs.lightdb;

import net.sf.jsqlparser.expression.Expression;

public class JoinOperator extends Operator{

	Operator left;
	Operator right;
	//Expression join;
	ExpressionParser expParse;
	Tuple lastleftTuple;
	
	public JoinOperator(Operator left, Operator right, Expression join) {
		this.left = left;
		this.right = right;
		this.join = join;
		this.baseTable = left.baseTable + " " + right.baseTable;
		this.schema = left.schema + " " + right.schema;
		//System.out.println("Left tuple: " + left.getClass().toString());
		//System.out.println("The join expression: " + this.join.toString());
		expParse = new ExpressionParser(join);
	}
	
	@Override
	Tuple getNextTuple() {
		// TODO Auto-generated method stub
		
		Tuple rightTuple;
		
		if(lastleftTuple == null) {
			lastleftTuple = left.getNextTuple();
		}
		
		if(join == null){
			
			while(lastleftTuple != null) {
				
				while((rightTuple = right.getNextTuple()) != null) {
					
					Tuple joinResult = lastleftTuple.join(rightTuple);
					System.out.println("Returning the tuple: " + joinResult.toString());
					return joinResult;
				}
							
				lastleftTuple = left.getNextTuple();
				right.reset();
				
				//System.out.println("Left tuple is now: " + lastleftTuple.toString());
				
			}
		}
		
		else {
		while(lastleftTuple != null) {
			//System.out.println("Printed left tuple: " + lastleftTuple.toString());
			while((rightTuple = right.getNextTuple()) != null) {
				Tuple joinResult = expParse.checkTupleJoin(left.schema, lastleftTuple, right.schema, rightTuple);
				System.out.println(this.left.schema + " " + this.right.schema);
				System.out.println(lastleftTuple.toString() + " " + rightTuple.toString());
				System.out.println(joinResult);
				if(this.join.toString() == "Sailors.A = Boats.E") {System.out.println("Left tuple is:" + lastleftTuple.toString());}
				if (joinResult != null) {
					System.out.println("Returning the tuple: " + joinResult.toString());
					System.out.println("New schema: " + this.schema);
					return joinResult;
				}
				
			}
						
			lastleftTuple = left.getNextTuple();
			right.reset();
			
			//System.out.println("Left tuple is now: " + lastleftTuple.toString());
			
		}
	}
		return null;
	}

	@Override
	void reset() {
		// TODO Auto-generated method stub
	}
	
}
