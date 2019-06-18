package com.govini.datatransform;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.govini.data.Dataset;
import com.govini.data.Record;

/**
 * This is the main class that is the entry point for the application.  The main class
 * will read in the raw data files, join them on the known id's to create two datasets
 * (finance and procurement) and then continue processing to create a mapping of these
 * two datasets.   The mapping will be output to an output.csv file
 * 
 * @author Bradley.Sheets
 * @date June 2019
 *
 */
public class Main {
	
	// Map to keep track of the discovered relationships between procurement and finance data
	private Map<String, String> idMap;
	
	public static void main(String[] args) {
		Main main = new Main();
		main.process();
		
	}
	
	public Main() {
		idMap = new HashMap<>();
	}
	
	/**
	 * Import the data into Dataset objects in memory and then try to create a mapping
	 * between the two with clustering and fuzzy distance calculations
	 */
	public void process() {
		// import
		Dataset financeData = importDataset("factset_entity_id", new File("data\\ds1"));
		Dataset procurmentData = importDataset("geo_id", new File("data\\ds2"));
		
		// look for the same company in both datasets 
		fuzzyCombineData(financeData, procurmentData);
		
		// output the found mapping
		outputData();
	}
	
	/**
	 * Creates a new Dataset object from files imported from a directory and joined
	 * by the key value (if there are multiple files)
	 * 
	 * @param key - Key/ID to join the data on
	 * @param directory - Directory location of the dataset
	 * @return Dataset object
	 */
	private Dataset importDataset(String key, File directory) {
		return new Dataset(key , directory);
	}
	
	/**
	 * Compare a finance dataset with a procurement dataset to try to find a mapping
	 * between the two.  The dataset object is generic and this method is written specifically
	 * with two types of datasets in mind.  It would be better for me to create a Dataset interface
	 * that a FinanceData and ProcurementData classes implement.  For the scope of this assessment though,
	 * I was more focused on the processes of clustering and joining data
	 * 
	 * @param financeData
	 * @param procurmentData
	 * @return a combined dataset that could be used to output a single file containing both data
	 */
	private Dataset fuzzyCombineData(Dataset financeData, Dataset procurmentData) {
		Dataset combined = new Dataset();
		
		List<Record> financeRecords = new ArrayList<Record>(financeData.getRecords().values());
		List<Record> procurmentRecords = new ArrayList<Record>(procurmentData.getRecords().values());
		
		Set<String> isoCodes = new HashSet<>();
		
		// Try to cluster records based on iso code.  
		for(Record record : procurmentRecords) {
			isoCodes.add(record.getData("country"));
		}
		
		// although not super efficient, compare records in one dataset 
		// against records in the other ONLY if they are located in the same country.  
		// the comparison is performed via levenshtien distance on the company name
		for(int i = 0; i < financeRecords.size() - 1; i++) {
			Record financeRecord = financeRecords.get(i);
			if(isoCodes.contains(financeRecord.getData("iso_country"))) {
				int min = Integer.MAX_VALUE;
				Record closest = null;
				for(int j = 0; j < procurmentRecords.size(); j++) {
					Record procurmentRecord = procurmentRecords.get(j);
					int distance = calcLevDistance(financeRecord.getData("entity_name"), procurmentRecord.getData("name"));
					if(distance < min) {
						min = distance;
						closest = procurmentRecord;
					}
				}
				// If a very close match was found, record the mapping
				if(min < 2){
					System.out.println(min +" : " +closest.getData("name") + " ==> " + financeRecord.getData("entity_name"));
					idMap.put(financeRecord.getData("factset_entity_id"), closest.getData("vendor_id"));
				}
			} else {
				combined.addRecord(financeRecord);
			}
		}
				
		return combined;
	}
	
	/**
	 * Performs the levenshtien distance between two strings
	 * 
	 * @param x - Root String
	 * @param y - String to compare against root string
	 * @return - the Levenshtien Distance.  0 is a match and lower distances are closer match
	 */
	private int calcLevDistance(String x, String y) {
	    int[][] dp = new int[x.length() + 1][y.length() + 1];
	 
	    for (int i = 0; i <= x.length(); i++) {
	        for (int j = 0; j <= y.length(); j++) {
	            if (i == 0) {
	                dp[i][j] = j;
	            }
	            else if (j == 0) {
	                dp[i][j] = i;
	            }
	            else {
	                dp[i][j] = min(dp[i - 1][j - 1] + substitutionCost(x.charAt(i - 1), y.charAt(j - 1)), 
	                  dp[i - 1][j] + 1, 
	                  dp[i][j - 1] + 1);
	            }
	        }
	    }
	 
	    return dp[x.length()][y.length()];
	}
	
	/**
	 * Helper method for the levenshtien distance, determines if two
	 * characters are equal
	 * @param a
	 * @param b
	 * @return
	 */
	private int substitutionCost(char a, char b) {
        return a == b ? 0 : 1;
    }
	
	/**
	 * Helper method for the levenshtien distance.  Finds a minimum number
	 * @param numbers
	 * @return
	 */
	private int min(int... numbers) {
        return Arrays.stream(numbers).min().orElse(Integer.MAX_VALUE);
    }
	
	/**
	 * Output the discovered mapping/relationships to an csv
	 * file on disk
	 */
	private void outputData() {
		try {
		FileWriter writer = new FileWriter("output.csv");
		writer.write("factset_entity_id,vendor_id\n");
			for(String fid : idMap.keySet()) {
				System.out.println(fid + ", "+idMap.get(fid));
				writer.write(fid + ", "+idMap.get(fid)+"\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
}
