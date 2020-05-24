package de.tu.fk.neo4jexport.DatabaseConnection;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Value;

public class DBConnection {

	//Singleton Pattern
	private static DBConnection instance;
	
	private Session session;
	
	private Driver driver;
	
	private String url="bolt://localhost/";
	
	private String username="neo4j";
	
	private String passwd="hallo";
	
	public static DBConnection getDBConnectionInstance(){
		if(instance == null){
			instance = new DBConnection();
		}
		return instance;
	}
	
	private DBConnection(){
		connectToDatabase();
	}
	
	public void connectToDatabase() throws NotAbleToConnectToDatabaseException{
		try{
			driver = GraphDatabase.driver( url, AuthTokens.basic( username, passwd ) );
			session = driver.session();
		}catch(Exception e){
			throw new NotAbleToConnectToDatabaseException(e.getMessage());
		}
	}
	
	
	public void closeConnection(){
		session.close();
		driver.close();
	}
//---------------------------------------------------------------
//Getter and Setter section
//---------------------------------------------------------------	
	public String getUrl(){return url;}
	
	public String getUsername() {return username;}
	
	public String getPasswd(){return passwd;}
	
	public void setUrl(final String url){this.url = url;}
	
	public void setUsername(final String username){this.username = username;}
	
	public void setPasswd(final String passwd){this.passwd = passwd;}
	
//----------------------------------------------------------------
//Methods for Graph Manipulation
//----------------------------------------------------------------
	
	public void createRelationship(final int startid, final int endid, DatabaseRelationshipLabels type){
		session.run( "MATCH (a),(b) WHERE ID(a)="+startid+" AND ID(b)="+endid+" CREATE (a)-[:"+type+"]->(b)" );
	}
	
	public int createNode(DatabaseNodeLabels type){
		int id = -1;
		StatementResult result = session.run( "CREATE (n:"+type+") RETURN ID(n)" );
		while ( result.hasNext() )
		{
		    Record record = result.next();
		    Value value = record.get(0);
		    id = value.asInt();
		}
		return id;
	}
	
	public void createRelationship(final int startid, final int endid, DatabaseRelationshipLabels type,List<DatabaseAttributes> properties){
		session.run( "MATCH (a),(b) WHERE ID(a)="+startid+" AND ID(b)="+endid+" CREATE (a)-[:"+type+"{"+buildPropertyString(properties)+"}]->(b)" );
	}
	
	public void createRelationship(final int startid,final int endid,final DatabaseRelationshipLabels type,final String name){
		List<DatabaseAttributes> temp = new ArrayList<DatabaseAttributes>();
		temp.add(new DatabaseAttributes("name",name));
		createRelationship(startid,endid,type,temp);
	}
	
	public void createRelationship(final int startid,final int endid,final DatabaseRelationshipLabels type,final String name,final List<DatabaseAttributes> attributes){
		List<DatabaseAttributes> temp = new ArrayList<DatabaseAttributes>();
		temp.add(new DatabaseAttributes("name",name));
		temp.addAll(attributes);
		createRelationship(startid,endid,type,temp);
	}
	
	public int createNode(DatabaseNodeLabels type,List<DatabaseAttributes> properties){
		int id = -1;
		StatementResult result = session.run( "CREATE (n:"+type+"{"+buildPropertyString(properties)+"}) RETURN ID(n)" );
		while ( result.hasNext() )
		{
		    Record record = result.next();
		    Value value = record.get(0);
		    id = value.asInt();
		}
		return id;
	}
	
	public int createNode(final DatabaseNodeLabels type,final String name,final List<DatabaseAttributes> attributes){
		List<DatabaseAttributes> temp = new ArrayList<DatabaseAttributes>();
		temp.add(new DatabaseAttributes("name",name));
		temp.addAll(attributes);
		return createNode(type,temp);
		
	}
	
	public int createNode(final DatabaseNodeLabels type,final String name){
		List<DatabaseAttributes> temp = new ArrayList<DatabaseAttributes>();
		temp.add(new DatabaseAttributes("name",name));
		return createNode(type,temp);
	}	
	
	//Methode um aus einer Liste von DatabaseAttributes einen String zum Einfügen in die Query zu machen
	private String buildPropertyString(final List<DatabaseAttributes> properties){
		
		StringBuilder sb = new StringBuilder(properties.get(0).getProperty());
		
		for(int i = 1;i<properties.size();i++){
			sb.append(","+properties.get(i).getProperty());
		}
		
		return sb.toString();
	}
	
	public void clearDatabase(){
		session.run("MATCH (n) DETACH DELETE n");
	}
	
	
}
