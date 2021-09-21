package ed.inf.adbs.lightdb;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

public class SelectOperator extends Operator{
	
	Operator o;
	Expression e;
	ExpressionParser exp;
	
	public SelectOperator(Operator o, Expression e)
	{
		this.o = o;
		this.e = e;
		this.baseTable = o.baseTable;
		exp = new ExpressionParser(e);
		// dbc = DataBaseCatalog.getInstance();
		schema = o.schema;
	
	}

	@Override
	Tuple getNextTuple() {
		// TODO Auto-generated method stub
		Tuple t;
		while((t = o.getNextTuple()) != null){
			//System.out.println(schema + " " + t.toString());
			if(exp.checkTuple(schema, t) != null) {
				System.out.println("From select operator this tuple is returned: " + t.toString());
				return t;
			}
		}
		return null; 
	}

	@Override
	void reset() {
		// TODO Auto-generated method stub
		o.reset();
	}

}
