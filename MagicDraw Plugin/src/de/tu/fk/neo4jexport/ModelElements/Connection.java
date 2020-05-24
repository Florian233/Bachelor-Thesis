package de.tu.fk.neo4jexport.ModelElements;

import java.util.ArrayList;
import java.util.List;

import de.tu.fk.neo4jexport.DatabaseConnection.DatabaseAttributes;
import de.tu.fk.neo4jexport.DatabaseConnection.DatabaseRelationshipLabels;

//Interne Repräsentation einer Verbindung im Modell, die später in die Datenbank eingefügt werden soll
public class Connection {
	
	private Node source;
	private Node target;
	private String mdID;
	private String name;
	private DatabaseRelationshipLabels type;
	private List<DatabaseAttributes> attributes = new ArrayList<DatabaseAttributes>();
	
	public Connection(final Node source, final Node target, final String mdID, final String name, final DatabaseRelationshipLabels type, final List<DatabaseAttributes> attributes){
		this.source = source;
		this.target = target;
		this.mdID = mdID;
		this.name = name;
		this.type = type;
		this.attributes.addAll(attributes);
	}
	
	public Connection(final Node source, final Node target, final String mdID, final String name, final DatabaseRelationshipLabels type){
		this.source = source;
		this.target = target;
		this.mdID = mdID;
		this.name = name;
		this.type = type;
	}
	
	public Node getSource(){return source;}
	
	public Node getTarget(){return target;}
	
	public String getName(){return name;}
	
	public List<DatabaseAttributes> getAttributes(){
		List<DatabaseAttributes> temp = new ArrayList<DatabaseAttributes>();
		temp.addAll(attributes);
		return temp;
	}
	
	public DatabaseRelationshipLabels getType(){return type;}
	
	public String getMdID(){return mdID;}

}
