package de.tu.fk.neo4jexport.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import de.tu.fk.neo4jexport.DatabaseConnection.DBConnection;
import de.tu.fk.neo4jexport.DatabaseConnection.DatabaseNodeLabels;
import de.tu.fk.neo4jexport.DatabaseConnection.DatabaseRelationshipLabels;
import de.tu.fk.neo4jexport.ModelElements.Connection;
import de.tu.fk.neo4jexport.ModelElements.Node;

public class ModelCreator {
	//Singleton Pattern
	private static ModelCreator instance;
	private List<Node> nodes = new ArrayList<Node>();
	private List<Connection> connections = new ArrayList<Connection>();
	private Map<String,Node> mdIdToNodeMap = new HashMap<String,Node>();
	private DBConnection db = DBConnection.getDBConnectionInstance();
	private int internID = 0;
	//Map die eine Instanz einer Komponente der Fehlersicht auf die ID der Realisierung mappt für die spätere InstanceOf Verbindung
	private Map<Node,String> failureViewInstancesMap = new HashMap<Node,String>();
	//Map ID aus MD->Umgewandelten Knoten der funktionalen Komponenten
	private Map<String,Node> convertedFunctViewComponentsMap = new HashMap<String,Node>();
	//Map für umgewandelte Fehlermodelle
	private Map<String,Node> convertedFailureModels = new HashMap<String,Node>();
	
	public synchronized static ModelCreator getInstance(){
		if(instance == null){
			instance = new ModelCreator();
		}
		return instance;
	}
	
	private ModelCreator(){}
	
	public void startCreation(){
		//Verbindungen zwischen den Instanzen und Realisierungen der Fehlersicht
		completeInstanceToRealizationConnections();
		
		//Knoten in die Datenbank einfügen
		for(Node n:nodes){
			if(n.getAttributes().isEmpty()){
				int id = db.createNode(n.getType(), n.getName());
				n.setNeoId(id);
			}else{
				int id = db.createNode(n.getType(), n.getName(), n.getAttributes());
				n.setNeoId(id);
			}
		}
		
		//Verbindungen in die Datenbank einfügen
		for(Connection c:connections){
			if(c.getAttributes().isEmpty()){
				if(c.getSource() == null || c.getTarget() == null){
					//JOptionPane.showMessageDialog(null, c.getType()+" "+c.getName());
				}else{
					int a = c.getSource().getNeoId();
					int b = c.getTarget().getNeoId();
					DatabaseRelationshipLabels t = c.getType();
					String n = c.getName();
					db.createRelationship(a, b, t, n);
				}
			}else{
				db.createRelationship(c.getSource().getNeoId(), c.getTarget().getNeoId(), c.getType(), c.getName(),c.getAttributes());
			}
		}
		
		//aufräumen
		connections.clear();
		nodes.clear();
		mdIdToNodeMap.clear();
		internID = 0;
	}
	
	private void completeInstanceToRealizationConnections() {
		for(Entry<Node,String> e:failureViewInstancesMap.entrySet()){
			Node n = mdIdToNodeMap.get(e.getValue());
			connections.add(new Connection(n,e.getKey(),getInternID(),"Instance Of",DatabaseRelationshipLabels.InstanceOf));
		}
		
	}

	public synchronized void addNode(final Node n){
		nodes.add(n);
		mdIdToNodeMap.put(n.getMdID(), n);
		if(n.getType().equals(DatabaseNodeLabels.LogicalComponent)){
			convertedFunctViewComponentsMap.put(n.getMdID(), n);
		}else if(n.getType().equals(DatabaseNodeLabels.CFT)){
			convertedFailureModels.put(n.getMdID(),n);
		}
	}
	
	public void addConnection(final Connection c){
		connections.add(c);
	}
	
	//Die internID ist eigentlich mittlerweile unnötig 
	public String getInternID(){
		String temp = "internid"+internID;
		internID++;
		return temp;
	}
	
	public Node getNodeByID(final String id){
		return mdIdToNodeMap.get(id);
	}
	
	public void addFailureViewInstanceToMap(final Node n,final String id){
		failureViewInstancesMap.put(n, id);
	}
	
	public synchronized Node isConverted(final String id){
		if(convertedFunctViewComponentsMap.containsKey(id)){
			return convertedFunctViewComponentsMap.get(id);
		}else if(convertedFailureModels.containsKey(id)){
			return convertedFailureModels.get(id);
		}
		return null;
	}
	
	

}
