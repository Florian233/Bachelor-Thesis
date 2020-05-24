package Integrator;

import DatabaseConnection.DBConnection;
import DatabaseConnection.IntIntQueryResult;
import DatabaseConnection.NameIDQueryResult;
import FTElemente.ElementTyp;
import FTElemente.VerbindungsTyp;

import java.util.ArrayList;
import java.util.List;

/**
 * Klasse damit die GUI alle benötigten Daten zur Integration aus der Datenbank abfragen kann.
 */
public class DatabaseReading {

    private static DBConnection db = DBConnection.getInstance();

    public List<NameIDQueryResult> getFailureTypes(){
        String query = "MATCH (a:"+ElementTyp.FailureType+") RETURN a.name, ID(a)";
        return db.executeQuery(query);
    }

    public List<NameIDQueryResult> getPortsWithoutFailureType(){
        String query = "MATCH a WHERE (a ="+ElementTyp.Input+" OR a ="+ ElementTyp.Output+" OR a ="+ ElementTyp.Inport+" OR a = "+ElementTyp.Outport+") AND NOT (a)-[:"+VerbindungsTyp.FailureTypeOf+"]->() RETURN a.name,ID(a)";
        return db.executeQuery(query);
    }

    public List<NameIDQueryResult> getAllComponents(){
        String query = "MATCH (a:"+ElementTyp.LogicalComponent+") RETURN a.name, ID(a)";
        return db.executeQuery(query);
    }

    public List<NameIDQueryResult> getAllFmAndComponents(){
        String query = "MATCH (a:"+ElementTyp.LogicalComponent+") RETURN a.name, ID(a) UNION MATCH (a:"+ElementTyp.CFT+") RETURN a.name,ID(a) UNION MATCH (a:"+ElementTyp.FaultTree+") RETURN a.name,ID(a)";
        return db.executeQuery(query);
    }

    public List<NameIDQueryResult> getPortsOfFmOrComponentWithoutFailureType(final int id){
        String query = "MATCH (a)-[:"+VerbindungsTyp.InputOf+"]-(b) WHERE ID(a)="+id+" AND NOT (b)-[:"+VerbindungsTyp.FailureTypeOf+"]-() RETURN b.name,ID(b)" +
                " UNION MATCH (a)-[:"+VerbindungsTyp.OutputOf+"]-(b) WHERE ID(a)="+id+" AND NOT (b)-[:"+VerbindungsTyp.FailureTypeOf+"]-() RETURN b.name,ID(b)";
        return db.executeQuery(query);
    }

    public List<NameIDQueryResult> getAllCFTsWithoutFailureModeTrace(){
        String query = "MATCH (a:"+ElementTyp.CFT+") WHERE NOT ()-[:"+VerbindungsTyp.FailureModelOf+"]->(a) RETURN a.name, ID(a)" +
                " UNION MATCH (a:"+ElementTyp.FaultTree+") WHERE NOT ()-[:"+VerbindungsTyp.FailureModelOf+"]->(a) RETURN a.name, ID(a)";
        return db.executeQuery(query);
    }

    public List<NameIDQueryResult> getInputsOfCFT(final int id){
        String query = "MATCH (a)-[r:"+VerbindungsTyp.InputOf+"]->(b) WHERE ID(a)="+id+" RETURN b.name,ID(b),labels(b)[0]";
        return db.getNodeIDNameType(query);
    }

    public List<NameIDQueryResult> getOutputsOfCFT(final int id){
        String query = "MATCH (a)-[r:"+VerbindungsTyp.OutputOf+"]->(b) WHERE ID(a)="+id+" RETURN b.name,ID(b),labels(b)[0]";
        return db.getNodeIDNameType(query);
    }

    public List<NameIDQueryResult> getInportsOfLogicalComponent(final int id){
        String query = "MATCH (a:"+ElementTyp.LogicalComponent+")-[r:"+VerbindungsTyp.InportOf+"]->(b) WHERE ID(a)="+id+" RETURN b.name,ID(b)";
        return db.executeQuery(query);
    }

    public List<NameIDQueryResult> getOutportsOfLogicalComponent(final int id){
        String query = "MATCH (a:"+ElementTyp.LogicalComponent+")-[r:"+VerbindungsTyp.OutportOf+"]->(b) WHERE ID(a)="+id+" RETURN b.name,ID(b)";
        return db.executeQuery(query);
    }

    public List<NameIDQueryResult> getLogicalComponentsWith2FailureModels(){
        String query = "MATCH (a:"+ElementTyp.LogicalComponent+") WHERE size((a)-[:"+ VerbindungsTyp.FailureModelOf+"]->())>1 RETURN a.name,ID(a)";
        return db.executeQuery(query);
    }

    public List<NameIDQueryResult> getCFTsOfLogicalComponent(final int componentId){
        String query = "MATCH (a)-[r:"+VerbindungsTyp.FailureModelOf+"]->(b) WHERE ID(a)="+componentId+" RETURN b.name,ID(b)";
        return db.executeQuery(query);
    }

    /**
     * Methode um die Namen aller Komponenten ohne Fehlermodell zu erhalten.
     * @return List<String> mit allen Komponenten ohne Fehlermodell
     */
    public List<String> getComponentsWithoutFailureModel(){
        String query = "MATCH (a:"+ ElementTyp.LogicalComponent+") WHERE NOT (a)-[:"+VerbindungsTyp.FailureModelOf+"]->() RETURN a.name,ID(a)";
        List<NameIDQueryResult> queryResults = db.executeQuery(query);

        List<String> resultList = new ArrayList<>();

        for(NameIDQueryResult result:queryResults){
            resultList.add(result.getName());
        }

        return resultList;
    }


    public static NameIDQueryResult getPortmappingOfFailureMode(final int id){
        String query = "MATCH (a)-[:"+VerbindungsTyp.PortMapping+"]-(b) WHERE ID(a)="+id+" RETURN ID(a),ID(b)";
        List<IntIntQueryResult> list = db.getRelationshipReturnIntInt(query);
        IntIntQueryResult relationship = list.remove(0);
        int idPort = relationship.getId2();
        String nodeQuery = "MATCH (n) WHERE ID(n)="+idPort+" RETURN n.name, ID(n)";
        return db.executeQuery(nodeQuery).remove(0);
    }

    public static int getIdOfRealization(final int id){
        String query = "MATCH (n)-[:"+VerbindungsTyp.InstanceOf+"]-(b) WHERE ID(n)="+id+" RETURN b.name,ID(b)";
        List<NameIDQueryResult> result = db.executeQuery(query);
        if(!result.isEmpty()){
            return result.get(0).getId();
        }else{
            return -1;
        }
    }

    public static List<Integer> getIdsOfConnectedInstances(final int id){
        String query = "MATCH (n)-[:"+VerbindungsTyp.InstanceOf+"]-(b) WHERE ID(n)="+id+" RETURN b.name,ID(b)";
        List<NameIDQueryResult> queryResult = db.executeQuery(query);

        List<Integer> result = new ArrayList<>();

        queryResult.forEach(r -> result.add(r.getId()));

        return result;
    }
}
