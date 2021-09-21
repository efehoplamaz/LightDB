package ed.inf.adbs.lightdb;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;

public class ProjectOperator extends Operator {

	Operator o;
	List<SelectItem> selectItems;
	String outputFile;
	
	public ProjectOperator(Operator o, List<SelectItem> selectItems, String outputFile){
		this.o = o;
		this.selectItems = selectItems;
		this.schema = "";
		this.outputFile = outputFile;
		for(SelectItem si: selectItems){
			this.schema += si.toString() + " ";
		}
	}
	@Override
	Tuple getNextTuple() {
		Tuple t = o.getNextTuple();
		if (t == null) {
			System.out.println("Input tuple is null in project operator");
			return null;
		}
		String[] schema = o.schema.split(" ");
		int[] selectedInts = new int[selectItems.size()];
		for (int i = 0; i<selectedInts.length; i++) {
			String selectItem = selectItems.get(i).toString();
			//String columnName = selectItem.split("\\.")[1];
			
			//System.out.println("Select item from SELECT: " + selectItem + " and its column name : " + columnName);
			
			for(int j = 0; j<schema.length; j++) {
				if (schema[j].equals(selectItem)){
					selectedInts[i] = t.get(j);
				}
			}
			
		}
		
		System.out.println("New returned tuple: " + Arrays.toString(selectedInts));
		return new Tuple(selectedInts);
	}
	@Override
	void reset() {
		// TODO Auto-generated method stub
		
	}
	
	void dump(){
		String dumpString = "";
		Tuple t;
		while((t = getNextTuple()) != null){
			String s = t.toString().replace("[","").replace("]", "");
			dumpString += s + "\n";
		}
		
		try (PrintWriter out = new PrintWriter(outputFile)) {
			if (dumpString.equals("")){
			    out.print(dumpString);
			}
			else{
				dumpString = dumpString.substring(0, dumpString.length()-1);
			    out.print(dumpString);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	

}
