package com.govini.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Record object is equal to a row of a csv file.
 * 
 * @author Bradley Sheets
 * @date June 2019
 *
 */
public class Record implements Comparable<Record>{

	// The contents of a row.  Each value is a cell in the row and the key is the column header of the row in the csv file
	private Map<String, String> data;
	
	// Records can be compared against each other, but the key to compare on is set to name by default
	// if there is no name column in the csv file, this will need to be changed with the setSortKey method
	// before sorting the records
	private String sortKey = "name";
	
	public Record() {
		data = new HashMap<>();
	}
	
	
	/**
	 * Add data to the record
	 * 
	 * @param key  The key of the cell
	 * @param value The value of the cell
	 */
	public void addData(String key, String value) {
		data.put(key, value);
	}
	
	/**
	 * Get the value at the give key for the record (row)
	 * @param key
	 * @return
	 */
	public String getData(String key) {
		String value = null;
		
		if(data.containsKey(key))
			value = data.get(key);
		
		return value;
	}
	
	/**
	 * Set the value to compare the record against 
	 * 
	 * @param key
	 */
	public void setSortKey(String key) {
		this.sortKey = key;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for(String key : data.keySet()) {
			builder.append(key);
			builder.append(":");
			builder.append(data.get(key));
			builder.append(",  ");
		}
		return builder.toString();
	}


	@Override
	public int compareTo(Record record) {
		return data.get(sortKey).compareTo(record.getData(sortKey));
	}
	
}
