package Integrator;

import DatabaseConnection.DBConnection;
import FTElemente.VerbindungsTyp;
import Integrator.CFTMerging.CFT;

/**
 * Klasse um alle relevanten Daten beim Integrieren in die Datenbank einzufügen, nach GUI Bestätigung
 */
public class DatabaseWriting {

    private DBConnection db = DBConnection.getInstance();

    public void writeCftFunctionalComponentMatch(final int idCFT, final int idComponent){
        String query = "MATCH a, b " +
                " WHERE ID(a) =" + idComponent + " AND ID(b) =" + idCFT +
                " CREATE (a)-[r:" + VerbindungsTyp.FailureModelOf + " {name:'" + VerbindungsTyp.FailureModelOf + "'}]->(b)";
        db.createRelationship(query);

    }

    public void writePortMatch(final int idPort1, final int idPort2){
        String query = "MATCH a, b " +
                " WHERE ID(a) =" + idPort1 + " AND ID(b) =" + idPort2 +
                " CREATE (a)-[r:" + VerbindungsTyp.PortMapping + " {name:'" + VerbindungsTyp.PortMapping + "'}]->(b)";
        db.createRelationship(query);

    }

    public void writeFailureTypePortMatch(final int idFailureType, final int idPort){
        String query = "MATCH a, b " +
                " WHERE ID(a) =" + idPort + " AND ID(b) =" + idFailureType +
                " CREATE (a)-[r:" + VerbindungsTyp.FailureTypeOf + " {name:'" + VerbindungsTyp.FailureTypeOf + "'}]->(b)";
        db.createRelationship(query);
    }

    public void deleteCFT(final CFT cft){
        cft.getElements().forEach(element -> {
            db.deleteNodeAndRelationships(element.getNeoID());
        });
    }

}
