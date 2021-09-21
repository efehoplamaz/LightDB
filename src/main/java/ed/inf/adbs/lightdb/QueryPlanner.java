package ed.inf.adbs.lightdb;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.plaf.synth.SynthOptionPaneUI;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.ComparisonOperator;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.Distinct;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;

public class QueryPlanner {
	
	//String inputFileName;
	static List<Operator> Operators;
	static List<SelectItem> selectItems;
	static Expression whereItem;
	static List<OrderByElement> orderby;
	static Distinct distinctElem;
	static String databaseDir;
	static String inputFile;
	static String outputFile;
	
	public QueryPlanner(String databaseDir, String inputFile, String outputFile){
		this.databaseDir = databaseDir;
		this.inputFile = inputFile;
		this.outputFile = outputFile;
	}
			
	public static void parseStatement() {
		try {
			
			Statement statement = CCJSqlParserUtil.parse(new FileReader(inputFile));
		
			if (statement != null) {
				
				System.out.println("Read statement: " + statement);
				
				Select select = (Select) statement;
				PlainSelect ps = (PlainSelect) select.getSelectBody();
				
				// TABLE NAMES from FROM
				Operators = new ArrayList<Operator>();
				String fromItem = ps.getFromItem().toString();
				System.out.println("Tables in the query are: " + fromItem + " " + ps.getJoins());
				System.out.println(fromItem);
				Operators.add(new ScanOperator(fromItem, databaseDir));
								
				// If there are multiple tables used, use getJoins
				if (ps.getJoins() != null){
					for (Join s: ps.getJoins()) {
						Operators.add(new ScanOperator(s.toString(), databaseDir));
					}
				}
			
				
				//Column names from SELECT
				selectItems = ps.getSelectItems();
				System.out.println("Select items are: "  + selectItems);
				
				//Selection expressions from WHERE
				whereItem = ps.getWhere();
				System.out.println("Where items are:"  + whereItem);
				orderby = ps.getOrderByElements();
				System.out.println("Order By element: " + orderby);
				
				distinctElem = ps.getDistinct();
				System.out.println("Distinct item included: " + distinctElem);

			}
		} catch (Exception e) {
			System.err.println("Exception occurred during parsing");
			e.printStackTrace();
		}
	}
	
	public void executeQueryPlan(){
		
		WhereClauseParser wcp = new WhereClauseParser(whereItem);
		
		HashMap<String, ArrayList<Expression>> divExp = wcp.divideExpressions();
		
		System.out.println("Where item: " + whereItem);
		
		// IF THERE IS A WHERE CLAUSE
		if(whereItem != null) {
			
			//CHECK IF THERE ARE ANY ARITHMETIC EXPRESSIONS, IF YES THEN EVALUATE.
			//IF ONE OF THE ARITHMETIC EXPRESSION IS WRONG THEN RETURN NOTHING AS THE RESULT.
			if (divExp.containsKey("arithmetic")){
				ArrayList<Expression> arithmetic = divExp.get("arithmetic");
				ExpressionParser expParse = new ExpressionParser(null);
				if(!expParse.checkArithmetic(arithmetic)){
					//RETURN NOTHING
					try (PrintWriter out = new PrintWriter(outputFile)) {
						out.print("");
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return;
				}
			}
			
			System.out.println("Type of operators in Operator ArrayList before where clause: ");
			for(Operator o: Operators) {
				System.out.print(o.getClass() + " ");
			}
			System.out.println();
			
			for (Operator so: Operators) {		
				for (String name: divExp.keySet()){
		            if(name.equals(so.baseTable)){
		            	ArrayList<Expression> tableSelects = divExp.get(name);
		            	System.out.println("Table name: " + name + " and related select expressions: " + tableSelects.toString());
		            	
		            	if(tableSelects.size()>1) {
		            		
		            		AndExpression andExp = new AndExpression(tableSelects.get(0), tableSelects.get(1));
		            		tableSelects.remove(0);
		            		tableSelects.remove(0);
		            		
		            		while(tableSelects.size() != 0) {
		            			andExp = new AndExpression(andExp, tableSelects.get(0));
		            			tableSelects.remove(0);
		            		}
		            		
		            		System.out.println("And Expression after conjuncting all expressions: " + andExp.toString());
		            		int index = Operators.indexOf(so);
		            		Operators.set(index, new SelectOperator(so, andExp));
		            		
		            	}
		            	
		            	else
		            	{
		            		int index = Operators.indexOf(so);
		            		System.out.println("And Expression after conjuncting all expressions: " + tableSelects.get(0));
		            		Operators.set(index, new SelectOperator(so, tableSelects.get(0)));
		            	}
		            	
		            }
				} 
				
			}
			System.out.println();
			System.out.println("Type of operators in Operator ArrayList after where clause: ");
			for(Operator o: Operators) {
				System.out.print(o.getClass() + " ");
			}
			System.out.println();
			
			if(divExp.containsKey("joins")){
				System.out.println("Has join expression");
				ArrayList<Expression> joinExps = divExp.get("joins");
								
				while(Operators.size() > 1) {
					System.out.println("Started a new loop");
					ArrayList<Expression> relatedJoinExps = new ArrayList<Expression>();
					Operator firstOperator = Operators.get(0);
					Operator secondOperator = Operators.get(1);
					
					Iterator<Expression> itr = joinExps.iterator();
					
					Expression andJoinExp = null;
					
					while(itr.hasNext()){//for(Expression jExp: joinExps){
						
						Expression jExp = itr.next();
						
						ComparisonOperator ce = (ComparisonOperator) jExp;
						
						String leftTable = ((Column) ce.getLeftExpression()).getTable().toString();
						String rightTable = ((Column) ce.getRightExpression()).getTable().toString();
						
						//System.out.println("First operator: " + firstOperator.getClass() + " Second operator: " + secondOperator.getClass() + " First and second operator base table: " + firstOperator.baseTable + " " +secondOperator.baseTable );
						System.out.println(leftTable + " " + rightTable);
						if((firstOperator.baseTable.contains(leftTable) && secondOperator.baseTable.contains(rightTable)) || (firstOperator.baseTable.contains(rightTable) && secondOperator.baseTable.contains(leftTable)))
						{
							relatedJoinExps.add(jExp);
							itr.remove();
						}

						
					}
										
					System.out.println("Remanining join Exps:" + joinExps.toString());
				
					if(relatedJoinExps.size() == 0){
						andJoinExp = null;
					}
					
					else if(relatedJoinExps.size() == 1) {
						andJoinExp = relatedJoinExps.get(0);
						joinExps.remove(relatedJoinExps.get(0));
						relatedJoinExps.remove(0);
						
					}
					
					else {
	        		andJoinExp = new AndExpression(relatedJoinExps.get(0), relatedJoinExps.get(1));
	        		joinExps.remove(relatedJoinExps.get(0));
	        		joinExps.remove(relatedJoinExps.get(1));
	        		relatedJoinExps.remove(0);
	        		relatedJoinExps.remove(0);
	        		
	        		while(relatedJoinExps.size() != 0) {
	        			andJoinExp = new AndExpression(andJoinExp, relatedJoinExps.get(0));
	        			joinExps.remove(relatedJoinExps.get(0));
	        			relatedJoinExps.remove(0);
	        		}
				}	
										
					int rIndex = Operators.indexOf(firstOperator);
					Operators.set(rIndex, new JoinOperator(firstOperator, secondOperator, andJoinExp));
					Operators.remove(1);
					//System.out.println("New created join operator has schema:" + Operators.get(0).join);
					
					System.out.println("Type of operators in Operator ArrayList after the first Join: ");
					for(Operator o: Operators) {
						System.out.print(o.getClass() + " ");
					}
					
			}
				if(selectItems.size() == 1 && selectItems.get(0) instanceof AllColumns){
					if(orderby != null) {
						SortOperator srtOp = new SortOperator(Operators.get(0), orderby, outputFile);
						srtOp.sort();
						if(distinctElem != null){
							DuplicateEliminationOperator deo = new DuplicateEliminationOperator(srtOp, outputFile);
							deo.dump();
						}
						else {
							srtOp.dump();
						}
					}
					else {
					System.out.println("Only a * element!");
					System.out.println("* element operators: " + Operators.toString());
					//Return everything until it reaches to null
					String dumpString = "";
					Tuple t;
					while((t = Operators.get(0).getNextTuple()) != null){
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
				
				// IF SELECT CLAUSE IS NOT * THEN USE A PROJECTION OPERATOR
				else {
					ProjectOperator pj = new ProjectOperator(Operators.get(0), selectItems, outputFile);
					if (orderby != null){
						SortOperator srtOp = new SortOperator(pj, orderby, outputFile);
						srtOp.sort();
						if(distinctElem != null){
							DuplicateEliminationOperator deo = new DuplicateEliminationOperator(srtOp, outputFile);
							deo.dump();
						}
						else {
							srtOp.dump();
						}
					}
					else {
						if(distinctElem != null){
							DuplicateEliminationOperator deo = new DuplicateEliminationOperator(pj, outputFile);
							deo.dump();
						}
						else {
							pj.dump();
						}
					}
				}
				
			}
			
			else {
				if(Operators.size() > 1) {
					// IF THERE ARE MORE THAN ONE TABLE AND NO JOIN CONDITIONS, DO CROSS PRODUCT
					while(Operators.size() != 1) {
					Operator firstOperator = Operators.get(0);
					Operator secondOperator = Operators.get(1);
					int rIndex = Operators.indexOf(firstOperator);
					Operators.set(rIndex, new JoinOperator(firstOperator, secondOperator, null));
					Operators.remove(1);
					}
					
					if(selectItems.size() == 1 && selectItems.get(0) instanceof AllColumns){
						if(orderby != null) {
							SortOperator srtOp = new SortOperator(Operators.get(0), orderby, outputFile);
							srtOp.sort();
							if(distinctElem != null){
								DuplicateEliminationOperator deo = new DuplicateEliminationOperator(srtOp, outputFile);
								deo.dump();
							}
							else {
								srtOp.dump();
							}
						}
						else {
							if(distinctElem != null){
								DuplicateEliminationOperator deo = new DuplicateEliminationOperator(Operators.get(0), outputFile);
								deo.dump();
							}
							else{
								System.out.println("Only a * element!");
								System.out.println("* element operators: " + Operators.toString());
								//Return everything until it reaches to null
								String dumpString = "";
								Tuple t;
								while((t = Operators.get(0).getNextTuple()) != null){
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
					}
					else {
					ProjectOperator pj = new ProjectOperator(Operators.get(0), selectItems, outputFile);
					if (orderby != null){
						SortOperator srtOp = new SortOperator(pj, orderby, outputFile);
						srtOp.sort();
						if(distinctElem != null){
							DuplicateEliminationOperator deo = new DuplicateEliminationOperator(srtOp, outputFile);
							deo.dump();
						}
						else {
							srtOp.dump();
						}
					}
					else {
						if(distinctElem != null){
							DuplicateEliminationOperator deo = new DuplicateEliminationOperator(pj, outputFile);
							deo.dump();
						}
						else {
							pj.dump();
						}
					}}
					
				}
				else{
					if(selectItems.size() == 1 && selectItems.get(0) instanceof AllColumns){
						if(orderby != null) {
							SortOperator srtOp = new SortOperator(Operators.get(0), orderby, outputFile);
							srtOp.sort();
							if(distinctElem != null){
								DuplicateEliminationOperator deo = new DuplicateEliminationOperator(srtOp, outputFile);
								deo.dump();
							}
							else {
								srtOp.dump();
							}
						}
						else {
							if(distinctElem != null){
								DuplicateEliminationOperator deo = new DuplicateEliminationOperator(Operators.get(0), outputFile);
								deo.dump();
							}
							else{
								System.out.println("Only a * element!");
								System.out.println("* element operators: " + Operators.toString());
								//Return everything until it reaches to null
								String dumpString = "";
								Tuple t;
								while((t = Operators.get(0).getNextTuple()) != null){
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
					}
					
					// IF SELECT CLAUSE IS NOT * THEN USE A PROJECTION OPERATOR
					else {
						ProjectOperator pj = new ProjectOperator(Operators.get(0), selectItems, outputFile);
						if (orderby != null){
							SortOperator srtOp = new SortOperator(pj, orderby, outputFile);
							srtOp.sort();
							if(distinctElem != null){
								DuplicateEliminationOperator deo = new DuplicateEliminationOperator(srtOp, outputFile);
								deo.dump();
							}
							else {
								srtOp.dump();
							}
						}
						else {
							if(distinctElem != null){
								DuplicateEliminationOperator deo = new DuplicateEliminationOperator(pj, outputFile);
								deo.dump();
							}
							else {
								pj.dump();
							}
						}
					}
				}
			}
									
		}
		
		else{
			// IF WHERE CLAUSE DOES NOT CONTAIN ANYTHING, I.E. NO WHERE CLAUSE
			// COMBINE ALL THE SCAN OPERATORS
			while(Operators.size() != 1) {
				Operator firstOperator = Operators.get(0);
				Operator secondOperator = Operators.get(1);
				
				int rIndex = Operators.indexOf(firstOperator);
				Operators.set(rIndex, new JoinOperator(firstOperator, secondOperator, null));
				Operators.remove(1);
				
			}
			
			// IF SELECT CLAUSE HAS A *, THEN DO NOT USE PROJECTION AND RETURN ALL ELEMENTS USING 
			// JOIN OPERATOR getNextTuple METHOD.
			if(selectItems.size() == 1 && selectItems.get(0) instanceof AllColumns){
				if(orderby != null) {
					SortOperator srtOp = new SortOperator(Operators.get(0), orderby, outputFile);
					srtOp.sort();
					if(distinctElem != null){
						DuplicateEliminationOperator deo = new DuplicateEliminationOperator(srtOp, outputFile);
						deo.dump();
					}
					else {
						srtOp.dump();
					}
				}
				else {
					if(distinctElem != null){
						DuplicateEliminationOperator deo = new DuplicateEliminationOperator(Operators.get(0), outputFile);
						deo.dump();
					}
					else{
						System.out.println("Only a * element!");
						System.out.println("* element operators: " + Operators.toString());
						//Return everything until it reaches to null
						String dumpString = "";
						Tuple t;
						while((t = Operators.get(0).getNextTuple()) != null){
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
			}
			
			// IF SELECT CLAUSE IS NOT * THEN USE A PROJECTION OPERATOR
			else {
				ProjectOperator pj = new ProjectOperator(Operators.get(0), selectItems, outputFile);
				if (orderby != null){
					SortOperator srtOp = new SortOperator(pj, orderby, outputFile);
					srtOp.sort();
					if(distinctElem != null){
						DuplicateEliminationOperator deo = new DuplicateEliminationOperator(srtOp, outputFile);
						deo.dump();
					}
					else {
						srtOp.dump();
					}
				}
				else {
					if(distinctElem != null){
						DuplicateEliminationOperator deo = new DuplicateEliminationOperator(pj, outputFile);
						deo.dump();
					}
					else {
						pj.dump();
					}
				}
			}
			
		}
		
	}

}
