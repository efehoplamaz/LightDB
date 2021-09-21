package ed.inf.adbs.lightdb;

import java.util.Map;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class DataBaseCatalog {
	
   static Map paths;
   static Map schemas;
   
   private DataBaseCatalog(String databaseDir){
	   
	   String[] pathnames;
	   paths = new HashMap();
	   
	   String DATA_DIR = databaseDir + "/data/";
	   
	   File f = new File(DATA_DIR);
	   
	   pathnames = f.list();
	   
       for (String pathname : pathnames) {
           // Print the names of files and directories
           paths.put(pathname.split(".csv")[0], DATA_DIR + pathname);
       }
	   
	   
	   schemas = new HashMap();
	   
		try {
			Scanner scanner = new Scanner(new File(databaseDir + "/schema.txt"));
			while (scanner.hasNextLine()) {
				String schema = scanner.nextLine();
				String dataFile = schema.substring(0, schema.indexOf(' '));
				String columnNames = schema.substring(schema.indexOf(' ') + 1);
				String[] sepColNames = columnNames.split(" ");
				String comb = "";
				for(int i = 0; i<sepColNames.length; i++) {
					sepColNames[i] = dataFile + "." + sepColNames[i];
				}
				for(int j = 0; j<sepColNames.length; j++) {
					if (j == sepColNames.length -1){
						comb += sepColNames[j];
					}
					else {
						comb += sepColNames[j] + " ";
					}
				}
				//System.out.println(comb);
				schemas.put(dataFile, comb);
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
   }
	
   //create an object of SingleObject
   private static DataBaseCatalog pathInstance;

   public static DataBaseCatalog getInstance(String databaseDir){
	   	pathInstance = new DataBaseCatalog(databaseDir);
	    return pathInstance;
	   }
   
   //Get the only object available
   public static String getPathInstance(String dataFile){
      return (String) paths.get(dataFile);
   }

   public static String getSchemaInstance(String dataFile){
	      return (String) schemas.get(dataFile);
	   }
}
