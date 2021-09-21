package ed.inf.adbs.lightdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.ComparisonOperator;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

public class WhereClauseParser extends ExpressionDeParser{

	Expression e;
	List <Expression> conjElements = new ArrayList<Expression>();
	HashMap<String, ArrayList<Expression>> expDict = new HashMap<String, ArrayList<Expression>>();
	
	public WhereClauseParser(Expression e) {
		this.e = e;
	}
	
	public void visit(AndExpression expr) {
		
		if(expr.getLeftExpression() instanceof AndExpression){
    		expr.getLeftExpression().accept(this);
    	}
    	else {
    		conjElements.add(expr.getLeftExpression());
    	}
		conjElements.add(expr.getRightExpression());
		
	}
	
	@Override
	public void visit(EqualsTo expr) {
		decide(expr);
	}
	
	@Override
	public void visit(NotEqualsTo expr) {
		decide(expr);
	}
	
	@Override
	public void visit(MinorThan expr) {
		decide(expr);
	}
	
	@Override
	public void visit(GreaterThan expr) {
		decide(expr);
	}
	@Override
	public void visit(MinorThanEquals expr) {
		decide(expr);
	}
	@Override
	public void visit(GreaterThanEquals expr) {
		decide(expr);
	}
	

	public void decide(ComparisonOperator expr){
		if ((expr.getLeftExpression() instanceof Column) && (expr.getRightExpression() instanceof Column))
		{
			if(((Column) expr.getLeftExpression()).getTable().toString().equals(((Column) expr.getRightExpression()).getTable().toString()))
				{
				if(expDict.containsKey(((Column) expr.getLeftExpression()).getTable().toString())){
					expDict.get(((Column) expr.getLeftExpression()).getTable().toString()).add(expr);
				}
				else{
					expDict.put(((Column) expr.getLeftExpression()).getTable().toString(), new ArrayList<Expression>(Arrays.asList(expr)));
				}
			}
			else {
				if(expDict.containsKey("joins")){
					expDict.get("joins").add(expr);
				}
				else {
					expDict.put("joins", new ArrayList<Expression>(Arrays.asList(expr)));
				}
			}
		}
		else if ((expr.getLeftExpression() instanceof Column) && (expr.getRightExpression() instanceof LongValue)) {
			if(expDict.containsKey(((Column) expr.getLeftExpression()).getTable().toString())){
				expDict.get(((Column) expr.getLeftExpression()).getTable().toString()).add(expr);
			}
			else{
				expDict.put(((Column) expr.getLeftExpression()).getTable().toString(), new ArrayList<Expression>(Arrays.asList(expr)));
			}
		}
		
		else if ((expr.getLeftExpression() instanceof LongValue) && (expr.getRightExpression() instanceof Column)) {
			if(expDict.containsKey(((Column) expr.getRightExpression()).getTable().toString())){
				expDict.get(((Column) expr.getRightExpression()).getTable().toString()).add(expr);
			}
			else{
				expDict.put(((Column) expr.getRightExpression()).getTable().toString(), new ArrayList<Expression>(Arrays.asList(expr)));
			}
		}
		
		else{
			if(expDict.containsKey("arithmetic")){
				expDict.get("arithmetic").add(expr);
			}
			else {
				expDict.put("arithmetic", new ArrayList<Expression>(Arrays.asList(expr)));
			}
		}
	}
	
	public HashMap<String, ArrayList<Expression>> divideExpressions(){
		
		if(e == null) {
			return null;
		}
		
		if(e instanceof AndExpression) {
			e.accept(this);
		}
		
		else{
			conjElements.add(e);
		}
		
		for (Expression e: conjElements) {
			e.accept(this);
		}
		
//		System.out.println();
//		for (String name: expDict.keySet()){
//            String key = name.toString();
//            String value = expDict.get(name).toString();  
//            System.out.println(key + " " + value);  }
//		System.out.println();
		
		return expDict;
		//System.out.println("AND Divided elements: " + conjElements.toString());
	}
	
}
