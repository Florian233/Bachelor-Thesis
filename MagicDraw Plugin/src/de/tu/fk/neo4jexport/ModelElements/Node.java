package de.tu.fk.neo4jexport.ModelElements;

import java.util.ArrayList;
import java.util.List;

import de.tu.fk.neo4jexport.DatabaseConnection.DatabaseNodeLabels;
import de.tu.fk.neo4jexport.DatabaseConnection.DatabaseAttributes;
//Interne Repräsentation eines Knotens im Modell, der später in die Datenbank eingefügt werden soll
public class Node {
	
	private String mdID;
	
	private String name;
	
	private DatabaseNodeLabels type;
	
	private List<DatabaseAttributes> attributes = new ArrayList<DatabaseAttributes>();
	
	private int neoID = 0;
	
	public Node(final String mdID, final String name, final DatabaseNodeLabels type, List<DatabaseAttributes> attributes){
		this.mdID = mdID;
		this.name = name;
		this.type = type;
		this.attributes.addAll(attributes);
	}
	
	public Node(final String mdID, final String name, final DatabaseNodeLabels type){
		this.mdID = mdID;
		this.name = name;
		this.type = type;
	}
	
	public void setNeoId(final int id){this.neoID = id;}
	
	public int getNeoId(){return neoID;}
	
	public String getName(){return name;}
	
	public String getMdID(){return mdID;}
	
	public List<DatabaseAttributes> getAttributes(){
		List<DatabaseAttributes> temp = new ArrayList<DatabaseAttributes>();
		temp.addAll(attributes);
		return temp;
	}
	
	public DatabaseNodeLabels getType(){return type;}

}
