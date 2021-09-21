package ed.inf.adbs.lightdb;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.jsqlparser.statement.select.Distinct;
import net.sf.jsqlparser.statement.select.SelectItem;

public class DuplicateEliminationOperator extends Operator {
	
	//ArrayList<Tuple> allTuples; 
	ArrayList<Tuple> newList = new ArrayList<Tuple>();
	//List<SelectItem> selectItems;
	String outputFile;
	Operator o;

	public DuplicateEliminationOperator(Operator o, String outputFile){
		
		this.schema = o.schema;
		this.o = o;
		this.outputFile = outputFile;
		removeDuplicates();
	}
	
	void removeDuplicates(){
		
		Tuple t;
		
		while((t = o.getNextTuple()) != null){
			boolean match = false;
			
			System.out.println(t.toString());
			if(newList.size() == 0){
				newList.add(t);
			}
			else{
				for(Tuple tupl: newList){
					if(tupl.isEqual(t)){
						match = true;
						break;
					}
				}
				if(!match){
					newList.add(t);
				}
			}
		}
		
	}
	
	@Override
	Tuple getNextTuple() {
		// TODO Auto-generated method stub
		if(newList.size() > 0) {
			Tuple returned = newList.get(0);
			newList.remove(0);
			return returned;
		}
		
		return null;
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
	
	@Override
	void reset() {
		// TODO Auto-generated method stub
		
	}
	

}
