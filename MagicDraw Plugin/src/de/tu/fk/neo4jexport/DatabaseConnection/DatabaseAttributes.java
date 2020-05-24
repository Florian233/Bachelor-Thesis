package de.tu.fk.neo4jexport.DatabaseConnection;

public class DatabaseAttributes {
	
	private final String property;
	
	public DatabaseAttributes(final String propertyName, final String attribute){
		property = propertyName+" : '"+attribute+"'";
	}
	
	public DatabaseAttributes(final String propertyName, final int attribute){
		property = propertyName+" : "+attribute;
	}
	
	public String getProperty(){return property;}

}
