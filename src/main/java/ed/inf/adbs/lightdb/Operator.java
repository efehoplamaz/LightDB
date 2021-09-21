package ed.inf.adbs.lightdb;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import net.sf.jsqlparser.expression.Expression;

public abstract class Operator {
		
	String schema;
	boolean hasAliase;
	String aliaseBaseTable;
	String baseTable;
	abstract Tuple getNextTuple();	
	abstract void reset();
	Expression join;
	
	void dump(){
		reset();
		String dumpString = "";
		Tuple item;
		while ((item = getNextTuple())!= null) {
			dumpString += item.toString() + "\n";
		}
		
		try (PrintWriter out = new PrintWriter("dump.txt")) {
		    out.println(dumpString);
		    out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
