package ed.inf.adbs.lightdb;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import com.opencsv.CSVReader;

public class ScanOperator extends Operator{
	
	CSVReader csvReader;
	DataBaseCatalog dbcatalog;
	String dumpString = "";
	String databaseDir;
	
	public ScanOperator(String baseTable, String databaseDir) {
		
		this.databaseDir = databaseDir;
		
		String pathToCSV;
		
		if (baseTable.contains(" ")){
			
			this.hasAliase = true;
			String[] dividedWRTSpace = baseTable.split(" ");
			this.baseTable = dividedWRTSpace[1];
			this.aliaseBaseTable = dividedWRTSpace[0];
			dbcatalog = DataBaseCatalog.getInstance(this.databaseDir);
			pathToCSV = dbcatalog.getPathInstance(dividedWRTSpace[0]);
			String tempSchema = dbcatalog.getSchemaInstance(dividedWRTSpace[0]);
			this.schema = tempSchema.replace(dividedWRTSpace[0], dividedWRTSpace[1]);
			System.out.println(this.schema);
			
		}
		else{
			this.baseTable = baseTable;
			
			dbcatalog = DataBaseCatalog.getInstance(this.databaseDir);
			pathToCSV = dbcatalog.getPathInstance(baseTable);
			this.schema = dbcatalog.getSchemaInstance(baseTable);
		}
		
	    try { 
	        FileReader filereader = new FileReader(pathToCSV); 
 
	        csvReader = new CSVReader(filereader); 
	    } 
	    catch (Exception e) { 
	        e.printStackTrace(); 
	    } 
	}

	@Override
	Tuple getNextTuple() {
		// TODO Auto-generated method stub
        String[] nextRecord; 
        int[] elements;
        // we are going to read data line by line 
        try {
			if ((nextRecord = csvReader.readNext()) != null) {
				 elements = new int[nextRecord.length];
			        for(int i = 0;i < nextRecord.length;i++)
			        {
			        	elements[i] = Integer.parseInt(nextRecord[i]);
			        }
			    //System.out.println("Returned element from ScanOperator: " + new Tuple(elements).toString());    
				return new Tuple(elements);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null; 
		
	}

	@Override
	void reset(){
		// TODO Auto-generated method stub
		try {
			csvReader.close();
			if(hasAliase) {
				csvReader = new CSVReader(new FileReader(dbcatalog.getPathInstance(aliaseBaseTable)));
			}
			else{
				csvReader = new CSVReader(new FileReader(dbcatalog.getPathInstance(baseTable)));
				}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
}
