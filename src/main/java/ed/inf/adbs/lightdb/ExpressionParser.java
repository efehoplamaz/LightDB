package ed.inf.adbs.lightdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

public class ExpressionParser extends ExpressionDeParser{

	Expression e;
	List <Expression> rules = new ArrayList<Expression>();
	List <Boolean> results = new ArrayList<Boolean>();
	String leftSchema[];
	String rightSchema[];
	Tuple leftTuple;
	Tuple rightTuple;
	
	public ExpressionParser(Expression e){
		this.e = e;
	
	}
	
	@Override
	public void visit(AndExpression expr) {
		
		if(expr.getLeftExpression() instanceof AndExpression){
    		expr.getLeftExpression().accept(this);
    	}
    	else {
    		rules.add(expr.getLeftExpression());
    	}
		rules.add(expr.getRightExpression());
		
	}
	
	@Override
	public void visit(EqualsTo expr) {
		
		String left = (expr.getLeftExpression().toString());
		String right = (expr.getRightExpression().toString());
		
		int[] eqIntegers = returnIntegers(left, right);
		
		results.add(eqIntegers[0] == eqIntegers[1]);
	}
	
	@Override
	public void visit(NotEqualsTo expr) {
		
		String left = (expr.getLeftExpression().toString());
		String right = (expr.getRightExpression().toString());
		
		int[] eqIntegers = returnIntegers(left, right);
		
		results.add(eqIntegers[0] != eqIntegers[1]);
	}
	
	@Override
	public void visit(MinorThan expr) {
		
		String left = (expr.getLeftExpression().toString());
		String right = (expr.getRightExpression().toString());
		
		int[] eqIntegers = returnIntegers(left, right);
		
		results.add(eqIntegers[0] < eqIntegers[1]);
	}
	
	@Override
	public void visit(GreaterThan expr) {
		
		String left = (expr.getLeftExpression().toString());
		String right = (expr.getRightExpression().toString());
		
		int[] eqIntegers = returnIntegers(left, right);
		
		results.add(eqIntegers[0] > eqIntegers[1]);
	}
	@Override
	public void visit(MinorThanEquals expr) {
		
		String left = (expr.getLeftExpression().toString());
		String right = (expr.getRightExpression().toString());
		
		int[] eqIntegers = returnIntegers(left, right);
		
		results.add(eqIntegers[0] <= eqIntegers[1]);
	}
	@Override
	public void visit(GreaterThanEquals expr) {
		
		String left = (expr.getLeftExpression().toString());
		String right = (expr.getRightExpression().toString());
		
		int[] eqIntegers = returnIntegers(left, right);
		
		results.add(eqIntegers[0] >= eqIntegers[1]);
	}
	
	public int[] returnIntegers(String left, String right) {
		
		int lhs = 0, rhs = -1;
		if (left.contains(".")) {
			boolean found = false;
			//String[] divideWRDotL = left.split("\\.");
			for(int i = 0; i < leftSchema.length; i++){
				if (leftSchema[i].equals(left)){
					found = true;
					lhs = leftTuple.get(i);
				}
			}
			
			if(!found){
				System.out.println("Left not found in left schema!");
				for(int i = 0; i < rightSchema.length; i++){
					if (rightSchema[i].equals(left)){
						lhs = rightTuple.get(i);
					}
				}
			}
			
		}
		
		else {
			lhs = Integer.parseInt(left);
		}
				
		if (right.contains(".")) {	
			boolean found2 = false;
			//String[] divideWRDotR = right.split("\\.");
			for(int j = 0; j < rightSchema.length; j++){
				if (rightSchema[j].equals(right)){
					found2 = true;
					rhs = rightTuple.get(j);
				}
			}
			
			if(!found2){
				for(int j = 0; j < leftSchema.length; j++){
					if (leftSchema[j].equals(right)){
						rhs = leftTuple.get(j);
					}
				}
			}
			
		}
		
		else {
			rhs = Integer.parseInt(right);
		}
		
		return new int[] {lhs, rhs};
	}
	
	boolean checkArithmetic(ArrayList<Expression> listOfArithmetic){
		for(Expression ex: listOfArithmetic){
			ex.accept(this);
		}
		
		for (boolean b: results) {
			if(!b){
				rules.clear();
				results.clear();
				return false;
			}
				
		}
		return true;
	}
	
	Tuple checkTuple(String schemaString, Tuple t) {
		
		this.leftSchema = schemaString.split(" ");
		this.leftTuple = t;
		this.rightSchema = schemaString.split(" ");
		this.rightTuple = t;
		
		//System.out.println("Left and right schema: " + Arrays.toString(this.leftSchema)+ " " + Arrays.toString(this.leftSchema));
		
		if(e instanceof AndExpression) {
			e.accept(this);
		}
		
		else{
			rules.add(e);
		}
		
		for (Expression e: rules) {
			e.accept(this);
		}
				
		for (boolean b: results) {
			if(!b){
				rules.clear();
				results.clear();
				return null;
			}
				
		}
		rules.clear();
		results.clear();
		System.out.println("Tuple " + leftTuple.toString() + " is being returned, all correct!");
		return leftTuple;
	}
	
	Tuple checkTupleJoin(String leftSchema, Tuple leftTuple, String rightSchema, Tuple rightTuple) {
			this.leftSchema = leftSchema.split(" ");
			this.leftTuple = leftTuple;
			this.rightSchema = rightSchema.split(" ");
			this.rightTuple = rightTuple;
			System.out.println(this.e);
			
			if(e == null){
				return null;
			}
					
			if(e instanceof AndExpression) {
				e.accept(this);
			}
			
			else{
				rules.add(e);
			}
						
			for (Expression e: rules) {
				e.accept(this);
			}
					
			for (boolean b: results) {
				if(!b){
					rules.clear();
					results.clear();
					return null;
				}
					
			}
			rules.clear();
			results.clear();
			System.out.println("Tuple " + leftTuple.toString() + " is being returned, all correct!");
			return leftTuple.join(rightTuple);
	}
	
	
	
}
