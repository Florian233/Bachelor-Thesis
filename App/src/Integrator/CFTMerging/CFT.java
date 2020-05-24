package Integrator.CFTMerging;

import DatabaseConnection.NameIDQueryResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Klasse, die die Verbindungen und Elemente eines CFT speichern kann.
 */
public class CFT {

    private List<IntegrationElement> elements = new ArrayList<>();
    private List<IntegratorVerbindung> connections = new ArrayList<>();
    /**
     * Speicherung der ID des Wurzelknotens in der Datenbank
     */
    private int cftRootNodeId;

    //Speichern nur für die Zuordnung per GUI
    private List<NameIDQueryResult> basicEventsList = new ArrayList<>();
    private List<NameIDQueryResult> outputList = new ArrayList<>();
    private List<NameIDQueryResult> inputList = new ArrayList<>();
    private List<NameIDQueryResult> instancesList = new ArrayList<>();

    public List<NameIDQueryResult> getInputList() {
        ArrayList<NameIDQueryResult> temp = new ArrayList<>();
        temp.addAll(inputList);
        return temp;
    }

    public void setInputList(List<NameIDQueryResult> inputList) {
        this.inputList = inputList;
    }

    public List<NameIDQueryResult> getInstancesList() {
        ArrayList<NameIDQueryResult> temp = new ArrayList<>();
        temp.addAll(instancesList);
        return temp;
    }

    public void setInstancesList(List<NameIDQueryResult> instancesList) {
        this.instancesList = instancesList;
    }

    public List<NameIDQueryResult> getOutputList() {
        ArrayList<NameIDQueryResult> temp = new ArrayList<>();
        temp.addAll(outputList);
        return temp;
    }

    public void setOutputList(List<NameIDQueryResult> outputList) {
        this.outputList = outputList;
    }

    public List<NameIDQueryResult> getBasicEventsList() {
        ArrayList<NameIDQueryResult> temp = new ArrayList<>();
        temp.addAll(basicEventsList);
        return temp;
    }

    public void setBasicEventsList(List<NameIDQueryResult> basicEventsList) {
        this.basicEventsList = basicEventsList;
    }

    public void setCftRootNodeId(final int id){this.cftRootNodeId = id;}

    public int getCftRootNodeId(){return cftRootNodeId;}

    public void addElement(final IntegrationElement element){
        elements.add(element);
    }

    public void addVerbindung(final IntegratorVerbindung verbindung){
        connections.add(verbindung);
    }

    public List<IntegrationElement> getElements(){
        List<IntegrationElement> returnList = new ArrayList<>();
        returnList.addAll(elements);
        return returnList;
    }

    public List<IntegratorVerbindung> getConnections(){
        ArrayList<IntegratorVerbindung> temp = new ArrayList<>();
        temp.addAll(connections);
        return temp;
    }

    /**
     * Methode um alle Elemente und Verbindungen des CFTs auf die Konsole auszugeben
     */
    public void printCFT(){
        elements.forEach(element -> {
            System.out.println(element.getName()+" Typ  "+element.getTyp()+" ID:  "+element.getNeoID());
        });
        connections.forEach(connection -> {
            System.out.println(connection.getType()+" von "+connection.getStartid()+" nach "+connection.getEndid());
        });
    }

    /**
     * Methode, die eine Cypher Query erstellt, um den kompletten CFT anzuzeigen
     * @return Cypher Query um diesen CFT anzuzeigen
     */
    public String getCypherQuery(){
        String query ="MATCH (n) WHERE ID(n)="+cftRootNodeId;

        for(IntegrationElement e:elements){
            query = query+" OR ID(n)="+e.getNeoID();
        }
        query +=" RETURN (n)";
        return query;
    }


}
