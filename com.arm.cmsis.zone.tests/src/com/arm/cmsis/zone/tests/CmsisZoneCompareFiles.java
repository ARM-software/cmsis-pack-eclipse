package com.arm.cmsis.zone.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class CmsisZoneCompareFiles {

	
	/**
	 * Compares files
	 * @param firstFile: golden result file (workspace\CmsisZoneTest\GoldenResults\TC#\TC#_Device.fzone)
	 * @param secondFile: test result file (workspace\CmsisZoneTest\TestResults\TC#\TC#_Device.fzone) 
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<Set<String>> compareFiles(String firstFile, String secondFile) throws Exception {

		//Create scanner per file and get a set of its content
	    Scanner file1 = new Scanner(new File(firstFile)); //golden result file (workspace\CmsisZoneTest\GoldenResults\TC#\TC#_Device.fzone)
	    Set<String> set1 = getSet(file1);
	    
	    Scanner file2Plus = new Scanner(new File(secondFile)); //test result file (workspace\CmsisZoneTest\TestResults\TC#\TC#_Device.fzone)
	    Set<String> set2Plus = getSet(file2Plus);
	   
	    Scanner file2Minus = new Scanner(new File(secondFile));
	    Set<String> set2Minus = getSet(file2Minus);

	    //Close scanners
	    file1.close();
	    file2Plus.close();
	    file2Minus.close();

	 
	    Set<String> plusFiles = new TreeSet<>();
	    //Removes from set2(test result file) all of its elements that are contained in set1(golden result file)
	    plusFiles = compareSets(set1, set2Plus); 
	    									 

	    Set<String> minusFiles = new TreeSet<>(); 
	    //Removes from set1(golden result file) all of its elements that are contained in set2(test result file)
	    minusFiles = compareSets(set2Minus, set1);

		ArrayList<Set<String>> comparisonResult = new ArrayList<Set<String>>();		
		comparisonResult.add(plusFiles);
		comparisonResult.add(minusFiles);
	    
		return comparisonResult;
	    
	}

	
	/**
	 * Gets a list of scanner's content
	 * @param sc
	 * @return a list of strings
	 * @throws Exception
	 */
	public static Set<String> getSet(Scanner sc) throws Exception {
	    Set<String> scannerSet = new TreeSet<>();
	    while (sc.hasNext())
	        scannerSet.add(sc.nextLine());
	    return scannerSet;
	}

	
	/**	 
	 * Removes from set2 all of its elements that are contained in set1
	 * @param set1
	 * @param set2
	 * @return updated set2
	 * @throws Exception
	 */
	public static Set<String> compareSets(Set<String> set1, Set<String> set2) throws Exception {		
		set2.removeAll(set1);
	    return set2;
	}
	
}
