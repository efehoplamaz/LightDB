package ed.inf.adbs.lightdb;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.sf.jsqlparser.statement.select.OrderByElement;

class OrderByIndex implements Comparator<Tuple>
{
    // Used for sorting in ascending order of
    // roll number
	int[] index;
	OrderByIndex(int[] index){
		this.index = index;
	}
	
    public int compare(Tuple a, Tuple b)
    {
    	int result = a.get(this.index[0]) - b.get(this.index[0]);
    	
    	for(int i = 1; i<this.index.length; i++){
    		if(result == 0){
        		result = a.get(this.index[i]) - b.get(this.index[i]);
    		}
    	}
        return result;
    }
}

public class SortOperator extends Operator{
	
	Operator o;
	List<OrderByElement> obe;
	ArrayList<Tuple> allTuples = new ArrayList<Tuple>();
	ArrayList<Tuple> test = new ArrayList<Tuple>();
	String outputFile;
	
	public SortOperator(Operator o, List<OrderByElement> obe, String outputFile){
		
		this.o = o;
		this.obe = obe;
		this.schema = o.schema;
		this.outputFile = outputFile;
		System.out.println("Sort operator schema: " + this.schema);
		
	}
	
	public void sort(){
		Tuple t;
		while((t = o.getNextTuple()) != null){
			allTuples.add(t);
		}
		
		String[] schemaList = schema.split(" ");
		
		int[] indiciesToCompare = new int[obe.size()];
		
		System.out.println(Arrays.toString(schemaList) + " " + obe.toString());
		
		for(int i = 0; i<schemaList.length; i++){
			for(int j= 0; j<obe.size(); j++){
				if(schemaList[i].equals(obe.get(j).toString())){
					indiciesToCompare[j] = i;
				}
			}
		}
		System.out.println(Arrays.toString(indiciesToCompare));
		
		Collections.sort(allTuples, new OrderByIndex(indiciesToCompare));
	}
	
	@Override
	Tuple getNextTuple() {
		
		if(allTuples.size() > 0) {
			Tuple returned = allTuples.get(0);
			allTuples.remove(0);
			return returned;
		}
		
		// TODO Auto-generated method stub
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
