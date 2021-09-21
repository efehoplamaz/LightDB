package ed.inf.adbs.lightdb;

import java.util.Arrays;

public class Tuple {

	int[] values;
	public Tuple(int[] values) {
		this.values = values;
	}
	
	public String toString() {
		String withoutSpaces = Arrays.toString(values).replaceAll("\\s", "");
		return withoutSpaces;	
	}	
	
	public int get(int index) {
		return values[index];
	}
	
	public boolean isEqual(Tuple a) {
		
		boolean isEq = true;
		
		for(int i = 0; i<values.length; i++){
				if(this.values[i] != a.values[i]){
					isEq = false;
				}
		}
		return isEq;
	}
	
	public Tuple join(Tuple t){
		int lenA = values.length;
		int lenB = t.values.length;
		
		int[] result = new int[lenA + lenB];
		
		System.arraycopy(values, 0, result, 0, lenA);
        System.arraycopy(t.values, 0, result, lenA, lenB);		
        
        return new Tuple(result);
	}
	
//	public static void main(String[] args){
//		Tuple i = new Tuple(new int[] {1,2,3});
//		Tuple j = new Tuple(new int[] {4,5,6});
//		
//		i.join(j);
//		
//		System.out.println(i.toString());
//	}
}
