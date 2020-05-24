package DatabaseConnection;

import Exceptions.DatabaseNotAccessibleException;
import FTElemente.ElementTyp;
import FTElemente.VerbindungsTyp;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Klasse, die die Verbindung zur Datenbank herstellt und über die Queries an die Datenbank gesendet werden können.
 * Klasse implementiert das Singleton Pattern und kann damit nur einmal instanziiert werden.
 */
public class DBConnection {

    /**
     * Connection Object für die Verbindung zur Datenbank
     */
    private Connection conn = null;
    /**
     * URL der Datenbank
     */
    private String url = "http://localhost:7474/";
    /**
     * Username für einen User der Datenbank
     */
    private String username = "neo4j";
    /**
     * Zugehöriges Passwort zu dem user
     */
    private String password = "hallo";
    /**
     * Siehe Singleton Pattern
     */
    private static DBConnection instance;

    /**
     * Konstruktor der Klasse
     * Aufbauen der Verbindung zur Datenbank
     */
    private DBConnection()  {
        try {
            Class.forName("org.neo4j.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new DatabaseNotAccessibleException("During DBConnection Creation Process");
        }
        //connectToDatabase();
    }

    public void connectToDatabase(){
        try {
            conn = DriverManager.getConnection(url.replace("http://","jdbc:neo4j://"), username, password);
        } catch (SQLException e) {
            throw new DatabaseNotAccessibleException("During DBConnection Creation Process");
        }
    }

    /**
     * Methode um sich eine gültige Instanz der Klasse zu holen.
     * @return  gültige Instanz der Klasse
     */
    public static synchronized DBConnection getInstance ()  {
        if (DBConnection.instance == null) {
            DBConnection.instance = new DBConnection();
        }
        return DBConnection.instance;
    }

    /**
     * Methode um die Datenbank zu leeren.
     */
    public void clearDB() throws DatabaseNotAccessibleException{
        try(Statement stmt = conn.createStatement()){
            stmt.executeQuery("MATCH (n) DETACH DELETE n");
            System.out.println("Datenbank geleert");

        } catch (Exception e) {
            throw new DatabaseNotAccessibleException();
        }
    }

    /**
     * Methode um einen Knoten in die Datenbank einzufügen, gibt die ID des Knoten in der Datenbank zurück
     * @param query String der Query
     * @return Id des Knoten in der Datenbank
     */
    public synchronized int insertNode(final String query){
        int nodeID = -1;
        try(Statement stmt = conn.createStatement()){
            ResultSet rs = stmt.executeQuery(query);
            //int i = 1;
            String s = "";
            while(rs.next()) {
                s = s + rs.getString(1) ;
                //i++;
            }
            nodeID = Integer.parseInt(s);

        } catch (SQLException e) {
            System.out.println("Knoten einfuegen fehlgeschlagen");
            e.printStackTrace();
        }
        return nodeID;
    }

    /**
     * Methode um eine Verbindung zwischen zwei Knoten in der Datenbank zu erzeugen
     * @param query String der Query
     */
    public synchronized void createRelationship(final String query){
        try(Statement stmt = conn.createStatement()){
            stmt.executeQuery(query);
        } catch (SQLException e) {
            System.out.println("Erstellen einer Verbindung fehlgeschlagen");
            e.printStackTrace();
        }
    }

    /**
     * Methode speziell für den Integrator um die Namen und IDs von Knoten aus der Datenbank auszulesen
     * @param query Cypher Query die einen String und einen Integer zurückgibt
     * @return List<NameIDQueryResult> für die Namen,ID Ergebnisse
     */
    public synchronized List<NameIDQueryResult> executeQuery(final String query) {
        List<NameIDQueryResult> resultList = new ArrayList<>();
        try (Statement stmt = conn.createStatement()) {
            ResultSet result = stmt.executeQuery(query);
            while(result.next()){
                String name = result.getString(1);
                Integer id = result.getInt(2);
                resultList.add(new NameIDQueryResult(name,id));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultList;
    }


    public void setURL(final String url){
        this.url = url;
    }

    public void setUsername(final String name){
        this.username = name;
    }

    public void setPassword(final String password){
        this.password = password;
    }

    public String getUrl(){return url;}

    public String getUsername(){return username;}

    public String getPassword(){return password;}

    /**
     * Methode zum Testen der Erreichbarkeit der DB
     * @return boolean - true wenn DB erreichbar, false wenn nicht
     */
    public boolean testDB(){
        if(conn == null){ throw new DatabaseNotAccessibleException();}
        try(Statement stmt = conn.createStatement()){
            stmt.executeQuery("MATCH (n:test) RETURN 1");
            return true;
        } catch (Exception e) {
            throw new DatabaseNotAccessibleException();
        }
    }

    /**
     * Methode um einen Knoten identifiziert per ID aus der DB zu löschen.
     * @param id des Knoten, der gelöscht werden soll
     */
    public void deleteNode(final int id){
        try(Statement stmt = conn.createStatement()){
            stmt.executeQuery("MATCH n WHERE ID(n)="+id+" DELETE n");
        } catch (SQLException e) {
            System.out.println("Löschen eines Knotens fehlgeschlagen");
        }
    }

    /**
     * Verbindung in der Datenbank löschen mit der angegebenen ID
     * @param id der Verbindung, die gelöscht werden soll
     */
    public void deleteRelationship(final int id){
        try(Statement stmt = conn.createStatement()){
            stmt.executeQuery("MATCH ()-[n]->() WHERE ID(n)="+id+" DELETE n");
        } catch (SQLException e) {
            System.out.println("Löschen einer Verbindung fehlgeschlagen");
        }
    }

    /**
     * Methode um einen Knoten und alle an diesem Knoten hängenden Verbindungen zu löschen.
     * @param id des zu löschenden Knotens
     */
    public void deleteNodeAndRelationships(final int id){
        try(Statement stmt = conn.createStatement()){
            stmt.executeQuery("MATCH n WHERE ID(n)="+id+" OPTIONAL MATCH (n)-[r]-() DELETE n,r");
        } catch (SQLException e) {
            System.out.println("Löschen eines Knotens und seiner Verbindungen fehlgeschlagen");
        }

        //MATCH n WHERE ID(n) = 825 DETACH DELETE n   auch eine mögliche Query
    }

    /**
     * Methode um Name ID und Type von Knoten abzufragen. Es wird auch für den Typ nur ein String Rückgabetyp erwartet!(labels(n)[0] gibt erstes Label zurück- mehr typen sollte ein Knoten in diesem Fall nicht haben)
     * @param query, die ausgeführt werden soll, muss Rückgabe von String, int , String haben
     * @return Liste von NameIDQueryResult
     */
    public List<NameIDQueryResult> getNodeIDNameType(final String query){
        List<NameIDQueryResult> resultList = new ArrayList<>();
        try (Statement stmt = conn.createStatement()) {
            ResultSet result = stmt.executeQuery(query);
            while(result.next()){
                String name = result.getString(1);
                Integer id = result.getInt(2);
                ElementTyp type = null;
                switch(result.getString(3)){
                    case "ORGate":type= ElementTyp.ORGate;
                        break;
                    case "ANDGate":type= ElementTyp.ANDGate;
                        break;
                    case "XORGate":type=ElementTyp.XORGate;
                        break;
                    case "MoonGate":type= ElementTyp.MoonGate;
                        break;
                    case "NotGate":type=ElementTyp.NotGate;
                        break;
                    case "BasicEvent":type=ElementTyp.BasicEvent;
                        break;
                    case "Input":type=ElementTyp.Input;
                        break;
                    case "NORGate":type=ElementTyp.NORGate;
                        break;
                    case "Output":type = ElementTyp.Output;
                        break;
                    case "OutputInstance":type = ElementTyp.OutputInstance;
                        break;
                    case "InputInstance":type = ElementTyp.InputInstance;
                        break;
                    case "Inport":type = ElementTyp.Inport;
                        break;
                    case "InportInstance":type = ElementTyp.InportInstance;
                        break;
                    case "Outport":type = ElementTyp.Outport;
                        break;
                    case "OuportInstance":type = ElementTyp.OutportInstance;
                        break;
                    case "CFT": type = ElementTyp.CFT;
                        break;
                    case "CFTInstance":type = ElementTyp.CFTInstance;
                        break;
                    case "LogicalComponent":type = ElementTyp.LogicalComponent;
                        break;
                    case "LogicalComponentInstance":type = ElementTyp.LogicalComponentInstance;
                        break;
                    case "FaulTree":type = ElementTyp.FaultTree;
                        break;
                    case "FailureType":type = ElementTyp.FailureType;
                        break;
                    default:
                }
                resultList.add(new NameIDQueryResult(name,id,type));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultList;
    }

    /**
     * Methode um Start/Endid bzw VerbindungsID abzufragen.
     * @param query mit dem Returnwert von Integer,Integer
     * @return Liste von IntIntQueryResult
     */
    public synchronized List<IntIntQueryResult> getRelationshipReturnIntInt(final String query) {
        List<IntIntQueryResult> resultList = new ArrayList<>();
        try (Statement stmt = conn.createStatement()) {
            ResultSet result = stmt.executeQuery(query);
            while(result.next()){
                Integer idstart = result.getInt(1);
                Integer idrelationship = result.getInt(2);
                resultList.add(new IntIntQueryResult(idstart,idrelationship));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultList;
    }

    /**
     * Methode um eine Verbindung zwischen zwei schon existierenden Knoten mit einem anzugebenden Typ zu erstellen.
     * @param idstart Id des Knotens an dem die Verbindung starten soll.
     * @param idend ID des Knoten an dem die Verbindung enden soll.
     * @param type Typ der Verbindung
     */
    public void createRelationship(final int idstart, final int idend, final VerbindungsTyp type){
        String query = "MATCH a,b WHERE ID(a)="+idstart+" AND ID(b)="+idend+" CREATE (a)-[:"+type+"]->(b)";
        createRelationship(query);
    }

    /*
    public void addTypeToInstances(final int portid, final int typeid){
        List<NameIDQueryResult> instances = executeQuery("MATCH (a)-[]-(b) MATCH (b)-[:InstanceOf]-(c) MATCH (c)-[:InputOf]-(d) WHERE ID(a)="+portid+" RETURN d.name,ID(d) UNION " +
                "MATCH (a)-[]-(b) MATCH (b)-[:InstanceOf]-(c) MATCH (c)-[:InputOf]-(d) WHERE ID(a)="+portid+" RETURN d.name,ID(d)");
        instances.forEach(instance -> createRelationship(instance.getId(),typeid,VerbindungsTyp.FailureTypeOf));
    }*/
    /*
    public int countIngogingConnections(final int id){
        String query = "MATCH (a)-[r]->(b) WHERE ID(b)="+id+" RETURN count(r)";
        int resultInt = 0;
        try (Statement stmt = conn.createStatement()) {
            ResultSet result = stmt.executeQuery(query);
            while(result.next()){
                resultInt = result.getInt(1);

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultInt;
    }*/
}
