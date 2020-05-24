package Integrator.CFTMerging;

import DatabaseConnection.DBConnection;
import DatabaseConnection.IntIntQueryResult;
import DatabaseConnection.NameIDQueryResult;
import FTElemente.ElementTyp;
import FTElemente.VerbindungsTyp;

import java.util.ArrayList;
import java.util.List;

/**
 * Klasse, die die ID des CFT Knotens in der Datenbank nimmt und damit den gesamten CFT ausliest und mittels IntegrationElement und IbtegrationVerbindung rekonstruiert.
 * Der CFT wird von den Output Fehlermodis ausgehend sukzessive ausgelesen.
 */
public class CFTRekonstruktion {

    /**
     * CFT Objekt in dem alle ausgelesenen Elemente und Verbindungen gespeichert werden
     */
    private CFT cft = new CFT();
    private DBConnection db = DBConnection.getInstance();
    /**
     * Queue nach der die Elemente ausgelesen werden
     */
    private List<Integer> workQueue = new ArrayList<>();
    /**
     * ID des Wurzelknotens des CFTs
     */
    private final int id;
    /**
     * Liste aller gefunden Instanzen
     */
    private List<Integer> processedInstances = new ArrayList<>();

    //Speichern nur für die Zuordnung per GUI
    private List<NameIDQueryResult> basicEventsList = new ArrayList<>();
    private List<NameIDQueryResult> outputList = new ArrayList<>();
    private List<NameIDQueryResult> inputList = new ArrayList<>();
    private List<NameIDQueryResult> instancesList = new ArrayList<>();


    /**
     * Im Konstruktor wird schon festgelegt welches Modell ausgelesen werden soll.
     * @param id ID des Wurzelknotens des auszulesenden Fehlermodells
     */
    public CFTRekonstruktion(final int id){this.id = id;cft.setCftRootNodeId(id);}

    /**
     * Methode, die ein CFT ausliest
     * @return CFT Objekt mit allen Verbindungen und Elementen des auszulesenden CFTs
     */
    public CFT readCFT(){

        //Alle Input Fehlermodi ermitteln
        inputList.addAll(db.executeQuery("MATCH (a)-[r:"+VerbindungsTyp.InputOf+"]->(b) WHERE ID(a)="+id+" RETURN b.name,ID(b)"));

        //Output Fehlermodi ermitteln
        findOutPuts();

        //Queue abarbeiten und sukzessive von den Outputs den CFT auslesen
        while(!workQueue.isEmpty()){
            int id = workQueue.remove(0);
            findGate(id);
            findAllIngoingEdges(id);
        }


        cft.setBasicEventsList(basicEventsList);
        cft.setInputList(inputList);
        cft.setOutputList(outputList);
        cft.setInstancesList(instancesList);

        return cft;

    }

    /**
     * Ermitteln der Outputs Fehlermodi.
     * Diese werden als erste Elemente in die Queue eingefügt.
     */
    private void findOutPuts(){
        List<NameIDQueryResult> outputs = db.executeQuery("MATCH (a)-[r:"+VerbindungsTyp.OutputOf+"]->(b) WHERE ID(a) ="+ id+" RETURN b.name,ID(b) ");
        outputList.addAll(outputs);

        outputs.forEach((output) -> {
            //cft.addElement(new IntegrationElement(ElementTyp.Output,output.getName(),output.getId()));

            workQueue.add(output.getId());
        });
    }

    /**
     * Methode um zu einem Knoten mit der gegebenen ID alle eingehnden Kanten auszulesen
     * @param id ID des Knotens für den alle eingehenden Kanten ausgelesen werden sollen
     */
    private void findAllIngoingEdges(final int id){
        String query = "MATCH (n)-[r:"+ VerbindungsTyp.FailurePropagation+"]->(b) WHERE ID(b) ="+id+" RETURN ID(n),ID(r)";
        List<IntIntQueryResult> resultsOfQuery = db.getRelationshipReturnIntInt(query);

        resultsOfQuery.forEach((result) -> {
            cft.addVerbindung(new IntegratorVerbindung(result.getId1(),id,VerbindungsTyp.FailurePropagation,result.getId2()));
            workQueue.add(result.getId1());
        });
    }

    /**
     * Methode um ein Element mit der gegebenen ID auszulesen
     * @param id ID des Elements
     */
    private void findGate(final int id){
        String query = "MATCH n WHERE ID(n)="+id+" RETURN n.name,ID(n),labels(n)[0] ";
        List<NameIDQueryResult> gates = db.getNodeIDNameType(query);//kommt eigentlich nur ein eintrag zurück, aber so kann man die Methode der DBConnection klasse öfter  verwenden
        gates.forEach((gate) -> {
            System.out.println(gate.getType());
            if(gate.getType().equals(ElementTyp.OutputInstance)){
                readCFTInstance(gate.getId());
            }
            cft.addElement(new IntegrationElement(gate.getType(),gate.getName(),gate.getId()));
            if (gate.getType().equals(ElementTyp.BasicEvent) && !basicEventsList.contains(gate)) basicEventsList.add(gate);
        });
    }

    /** Auslesen einer Instanz. Wird aufgerufen, wenn eine OutputInstance gefunden wird.
     * Zuvor wird überprüft, ob die Instanz nicht schon ausgelesen wurde.
     * @param outputid ID des Output Elements von dem die komplette Instanz ausgelesen werden soll
     */
    private synchronized void readCFTInstance(final int outputid){
        List<NameIDQueryResult> cftList = db.executeQuery("MATCH (n)-[r:"+VerbindungsTyp.OutputOf+"]-(b) WHERE ID(n)="+outputid+" RETURN b.name,ID(b)");
        int id = cftList.get(0).getId();
        NameIDQueryResult cftInstance = cftList.get(0);
        if(!processedInstances.contains(id)){
            processedInstances.add(id);
            cft.addElement(new IntegrationElement(ElementTyp.CFTInstance,cftInstance.getName(),cftInstance.getId()));
            List<NameIDQueryResult> inputsList = db.executeQuery("MATCH (n)-[r:"+VerbindungsTyp.InputOf+"]-(b) WHERE ID(n)="+id+" RETURN b.name,ID(b)");
            inputsList.forEach((input) -> {
                workQueue.add(input.getId());
            });
            List<IntIntQueryResult> connections1 = db.getRelationshipReturnIntInt(
                    "MATCH (n)-[r:"+VerbindungsTyp.InputOf+"]->(b) WHERE ID(n)="+id+" RETURN ID(b),ID(r)"
            );
            List<IntIntQueryResult> connections2 = db.getRelationshipReturnIntInt(
                    "MATCH (n)-[r:"+VerbindungsTyp.OutputOf+"]->(b) WHERE ID(n)="+id+" RETURN ID(b),ID(r)"
            );
            connections1.forEach((connection) -> {
                cft.addVerbindung(new IntegratorVerbindung(id,connection.getId1(),VerbindungsTyp.InputOf,connection.getId2()));
            });
            connections2.forEach((connection) -> {
                cft.addVerbindung(new IntegratorVerbindung(id,connection.getId1(),VerbindungsTyp.OutputOf,connection.getId2()));
            });
            instancesList.addAll(cftList);
        }
    }

    public List<NameIDQueryResult> getBasicEventsList(){return basicEventsList;}

    public List<NameIDQueryResult> getOutputList(){return outputList;}

    public List<NameIDQueryResult> getInputList(){return inputList;}

    public List<NameIDQueryResult> getInstancesList(){return instancesList;}
}
