package de.tu.fk.neo4jexport.DatabaseConnection;

public class NotAbleToConnectToDatabaseException extends RuntimeException {
	
	public NotAbleToConnectToDatabaseException(final String a){
		super(a);
	}

}
