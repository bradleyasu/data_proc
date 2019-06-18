package com.govini.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dataset keeps track of records loaded into memory from an external csv.
 * multiple csvs can be loaded into one dataset object if there is a known key
 * to join the data on
 * 
 * @author Bradley Sheets
 * @date June 2019
 *
 */
public class Dataset {

	// Unique ID/KEY ==> record
	private Map<String, Record> records;
	private String keyName;
	private int genericId = 0;
	
	public Dataset(String keyName, File file) {
		records = new HashMap<>();
		this.keyName = keyName;
		importFiles(file);
	}
	
	public Dataset() {
		records = new HashMap<>();
	}
	
	/**
	 * Import the file or multiple files into memory.  The expected
	 * format is either a directory containing a csv file, or one
	 * csv file
	 * 
	 * @param file
	 */
	private void importFiles(File file) {
		
		if(file.isDirectory()) {
			for(File f : file.listFiles()) {
				loadFileContent(f);
			}
		} else {
			loadFileContent(file);
		}
		
	}
	
	/**
	 * Helper method to read the file into memory
	 * @param file
	 */
	private void loadFileContent(File file) {
		if(file != null) {
			FileInputStream is = null ;
			BufferedReader reader = null;
			try {
				is = new FileInputStream(file);
				reader = new BufferedReader(new InputStreamReader(is));
				
				String line = null;
				int count = 0;
				
				List<String> keyMap = new ArrayList<String>();
				int keyIndex = -1;
				
				while((line = reader.readLine()) != null) {
					if(count == 0) { // then we are on the header line
						for(String s : line.split(",")) {
							keyMap.add(s);
							if(keyName.equals(s)) {
								keyIndex = keyMap.size() - 1;
							}
						}
					} else {
						// Some elements have a comma in the cell, this attempts to split the string only
						// on the commas separating fields
						String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
						if(keyIndex < values.length){
							String key = values[keyIndex];
							if(!this.records.containsKey(key)){
								this.records.put(key, new Record());
							}
							Record record = records.get(key);
							for(int i = 0; i < values.length; i++) {
								String value = values[i];
								record.addData(keyMap.get(i), value);
							}
						} else {
							System.out.println(line);
						}
						
					}
					
					count++;
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally { 
				if(reader != null) {
					try{
						reader.close();
					} catch (Exception e) {}
				}
			}
			
		}
	}
	
	/**
	 * Add a record to the dataset
	 * @param id
	 * @param record
	 */
	public void addRecord(String id, Record record) {
		records.put(id, record);
	}
	
	/**
	 * Add a record to the dataset and generate a generic id
	 * @param record
	 */
	public void addRecord(Record record) {
		records.put(String.valueOf(genericId), record);
		genericId++;
	}
	
	/**
	 * Get the records from the dataset
	 * 
	 * @return
	 */
	public Map<String, Record> getRecords() {
		return records;
	}
}
