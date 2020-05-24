package Integrator.CFTMerging;

import DatabaseConnection.DBConnection;
import DatabaseConnection.IntIntQueryResult;
import DatabaseConnection.NameIDQueryResult;
import Exceptions.NoEqualityCanBeFoundYetException;
import Exceptions.NoEqualityExistsException;
import FTElemente.ElementTyp;
import FTElemente.VerbindungsTyp;
import Integrator.DatabaseReading;
import Integrator.Matching.Match;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Klasse, die aus zwei CFT einen zusammengefassten CFT erstellt
 */
public class CFTMerge {

    private CFT cft1;
    private CFT cft2;
    /**
     * Liste der Übereinstimmungen
     */
    private List<Match> matches = new ArrayList<>();
    /**
     * CFT Objekt in dem alle Elemente und Verbindungen des neuen CFTs gespeichert werden
     */
    private CFT newCFT = new CFT();
    /**
     * Map, die die IDs von als gleich ermittelten Elementen aufeinander abbildet
     */
    private Map<Integer,Integer> matchMap = new HashMap<>();
    /**
     * IDs der Elemente, die im neuen Fehlermodell nicht mehr vorhanden sein werden, weil ein äquivalentes Element existiert
     */
    private List<Integer> idsToRemove = new ArrayList<>();
    private DBConnection db = DBConnection.getInstance();
    /**
     * Map die einer ID eine Liste mit allen ausgehenden Verbindungen aus dem Element mit der ID zuordnet
     */
    private Map<Integer,List<IntegratorVerbindung>> successorMap = new HashMap<>(); //getEndID - start ist immer  mit der key id
    /**
     * Map die einer ID einer Liste mit allen eingehenden Verbindungen in das Element mit der ID zuordnet
     */
    private Map<Integer,List<IntegratorVerbindung>> predecessorMap = new HashMap<>(); //getStartID - ende ist immer mit der key id
    /**
     * Liste mit allen neuen Verbindungen, die in die Datenbank eingefügt werden sollen
     */
    private List<IntegratorVerbindung> connectionsToAdd = new ArrayList<>();


    public CFTMerge(final CFT cft1,final CFT cft2,final List<Match> matches){
        this.cft1 = cft1;
        this.cft2 = cft2;
        this.matches.addAll(matches);
    }

    /**
     * Methode um das Zusammenführen zu starten
     * @return boolean ob mergen erfolgreich, true -> ja, false -> nein
     */
    public boolean startMerging(){
        System.out.println("Main:Mergen gestartet!");
        // jeden von der Benutzer in der GUI angegebenen Match in die Map eintragen
        matches.forEach((match) -> {
            int a = match.getA();
            int b = match.getB();
            matchMap.put(a,b);
            if(b!=-1){
                matchMap.put(b,a); //-1 bedeutet a hat keinen partner im anderen cft
            }
        });

        //Successor und predecessor map erstellen
        cft1.getConnections().stream().forEach((connection) -> {
            if(successorMap.containsKey(connection.getStartid())){
                successorMap.get(connection.getStartid()).add(connection);
            }else{
                List<IntegratorVerbindung> newList = new ArrayList<>();
                newList.add(connection);
                successorMap.put(connection.getStartid(),newList);
            }

            if(predecessorMap.containsKey(connection.getEndid())){
                predecessorMap.get(connection.getEndid()).add(connection);
            }else{
                List<IntegratorVerbindung> newList = new ArrayList<>();
                newList.add(connection);
                predecessorMap.put(connection.getEndid(),newList);
            }

        });

        cft2.getConnections().stream().forEach((connection) -> {
            if(successorMap.containsKey(connection.getStartid())){
                successorMap.get(connection.getStartid()).add(connection);
            }else{
                List<IntegratorVerbindung> newList = new ArrayList<>();
                newList.add(connection);
                successorMap.put(connection.getStartid(),newList);
            }

            if(predecessorMap.containsKey(connection.getEndid())){
                predecessorMap.get(connection.getEndid()).add(connection);
            }else{
                List<IntegratorVerbindung> newList = new ArrayList<>();
                newList.add(connection);
                predecessorMap.put(connection.getEndid(),newList);
            }
        });

        System.out.println("Main:Listen erstellt");

        findEqualElements();

        //Testausgabe
        matchMap.entrySet().forEach(entry -> System.out.println(entry.getKey() +"  passt zu:  "+entry.getValue()));
        //Testausgabe Ende

        System.out.println("Main:Gleiche Elemente gefunden!");

        combineCFTRootNodes();

        System.out.println("Main:Wurzelelemente zusammengefügt");

        buildNewCft();

        System.out.println("Main:Neue CFT erstellt");

        addConnections();

        removeElements();

        System.out.println("Main:Kontrolle auf behebbare Inkonsistenzen");

        checkForInconsistencies();

        System.out.println("Mergen abgeschlossen!");

        return true;
    }

    /**
     * Methode um alle Verbindungen in der Liste connectionToAdd in die Datenbank einzufügen
     */
    private void addConnections() {
        connectionsToAdd.stream().forEach((connection) -> db.createRelationship(connection.getStartid(),connection.getEndid(),connection.getType()));
    }

    /**
     * Methode um zu prüfen, ob Outputs oder InputInstances nur einen Eingang haben, wenn nein dann OR-Gate einfügen
     */
    private void checkForInconsistencies() {
        newCFT.getElements().stream().forEach((element) -> {
            if((element.getTyp().equals(ElementTyp.InputInstance) || element.getTyp().equals(ElementTyp.Output)) ){
                if(predecessorMap.containsKey(element.getNeoID())){
                    List<IntegratorVerbindung> ingoingEdges = getIngoingEdges(element.getNeoID());
                    if(ingoingEdges.size()>1){
                        //Neues ORGate hinzufügen
                        int orGateID = db.insertNode("CREATE (a:" +ElementTyp.ORGate +"{ name : 'ORGate'}) RETURN ID(a)");
                        //Verbindungen löschen
                        ingoingEdges.forEach(predecessor -> {
                            db.deleteRelationship(predecessor.getNeoId());
                            //Verbindungen von den Elementen zum ORGate
                            db.createRelationship(predecessor.getStartid(),orGateID,VerbindungsTyp.FailurePropagation);
                        });
                        //Verbindung vom ORGate zum Output/IntputInstance
                        db.createRelationship(orGateID,element.getNeoID(),VerbindungsTyp.FailurePropagation);
                    }
                }
            }
        });
    }

    /**
     * Methode um alle eingehenden Kanten eines Knotens mit einer angegebenen ID zu ermitteln
     * @param neoID ID aus Neo4j des Elements
     * @return Liste aller eingehenden Verbindungen in das Element mit der angegebenen ID
     */
    private List<IntegratorVerbindung> getIngoingEdges(final int neoID) {
        String query = "MATCH (n)-[r:"+VerbindungsTyp.FailurePropagation+"]->(b) WHERE ID(b)="+neoID+" RETURN ID(n),ID(r)";
        List<IntIntQueryResult> queryResult = db.getRelationshipReturnIntInt(query);
        List<IntegratorVerbindung> resultList = new ArrayList<>();
        queryResult.stream().forEach(connection -> resultList.add(new IntegratorVerbindung(connection.getId1(),neoID,VerbindungsTyp.FailurePropagation,connection.getId2())));
        return resultList;
    }

    /**
     * Alle Elemente in der Liste idsToRemove aus der Datenbank löschen
     */
    private void removeElements() {
        idsToRemove.stream().forEach(id -> db.deleteNodeAndRelationships(id));
    }

    /**
     * Neuen CFT erstellen: Verbleibende Elemente und zu löschende Elemente wie auch Verbindungen bestimmen.
     * Neue Verbindungen zwischen den verbleibenden Elementen der beiden CFTs einfügen.
     */
    private void buildNewCft() {
        //alle doppelten Elemente ( Elemente, die auf ein anderes gematched werden können) in Liste der zu Entfernenden hinzufügen
        //alle elemente aus cft1 hinzufügen
        cft1.getElements().stream().forEach(element1 -> {
            newCFT.addElement(element1);
            if(matchMap.containsKey(element1.getNeoID())){
                if(matchMap.get(element1.getNeoID()) != -1 && !idsToRemove.contains(matchMap.get(element1.getNeoID()))){
                    idsToRemove.add(matchMap.get(element1.getNeoID()));
                }
            }
        });


        //Testausgabe Anfang
        idsToRemove.forEach(System.out::println);
        //Testausgabe Ende

        //notwendige elemente aus cft2hinzufügen, die keine matching partner haben
        cft2.getElements().stream().forEach((element) -> {
            if(!idsToRemove.contains(element.getNeoID())){
                newCFT.addElement(element);
                //checkSuccessors(element.getNeoID());
            }//else{
                //addUnequalOutgoingConnections(matchMap.get(element.getNeoID()),element.getNeoID());
            //}
        });

        //Verbindungen aus cft2 übernehmen, bzw anpassen, wenn die verbindung zwischen einem element verläuft, dass gelöscht werden soll
        cft2.getConnections().stream().forEach(connection -> {

                int startid = connection.getStartid();
                int endid = connection.getEndid();

                if (matchMap.containsKey(startid) && matchMap.containsKey(endid)) {
                    if(!checkIfConnectionIsPartOfFM1(connection))connectionsToAdd.add(new IntegratorVerbindung(matchMap.get(startid),matchMap.get(endid),VerbindungsTyp.FailurePropagation,0));
                } else if (matchMap.containsKey(startid) && !matchMap.containsKey(endid)) {
                    connectionsToAdd.add(new IntegratorVerbindung(matchMap.get(startid),endid,VerbindungsTyp.FailurePropagation,0));
                } else if (!matchMap.containsKey(startid) && matchMap.containsKey(endid)) {
                    connectionsToAdd.add(new IntegratorVerbindung(startid,matchMap.get(endid),VerbindungsTyp.FailurePropagation,0));
                }


        });
    }


    /**
     * @param connection Verbindung, die überprüft werden soll
     * @return boolean, ob Verbindung in Fehlermodell1 enthalten ist , true -> ja, false -> nein
     */
    private boolean checkIfConnectionIsPartOfFM1(final IntegratorVerbindung connection){

        final boolean[] result = {false};

        int startid = matchMap.get(connection.getStartid());
        int endid = matchMap.get(connection.getEndid());

        successorMap.get(startid).forEach(successor -> {
            if(successor.getEndid() == endid) result[0] =true;
        });

        return result[0];
    }

    /**
     * Prüft ob ein nachfolger entfernt werden soll, wenn dem so ist wird die Verbindung auf das equivalente Element umgeleitet.
     * @param neoID Id des elements, dessen Nachfolger überprüft werden sollen
     */
    private void checkSuccessors(final int neoID) {
        if(successorMap.containsKey(neoID)){
            List<IntegratorVerbindung> successors = successorMap.get(neoID);
            successors.stream().forEach(successor -> {
                if(idsToRemove.contains(successor.getEndid())){
                    IntegratorVerbindung newConnection = new IntegratorVerbindung(neoID,matchMap.get(successor.getEndid()),VerbindungsTyp.FailurePropagation,0);
                    if(!connectionsToAdd.contains(newConnection))connectionsToAdd.add(newConnection);
                }
            });
        }
    }

    /**
     * Methode um gleiche Gatter festzustellen.
     * Ein Gatter ist gleich, wenn die Eingänge gleich sind und der Typ gleich ist.
     */
    private void findEqualElements(){
        List<IntegrationElement> workingQueue = cft1.getElements();
        while(!workingQueue.isEmpty()){
            IntegrationElement element = workingQueue.remove(0);
            if(!matchMap.containsKey(element.getNeoID())) {
                List<IntegratorVerbindung> predecessors = predecessorMap.get(element.getNeoID());
                try {
                    findCorrespondingElementInFM2(element.getNeoID(),predecessors);
                }catch(NoEqualityCanBeFoundYetException e){
                    workingQueue.add(element);
                }catch(NoEqualityExistsException ex){
                    matchMap.put(element.getNeoID(),-1);
                }
            }
        }
    }

    /**
     * Die beiden Wurzelknoten werden zu einem zusammengefügt. Übernehmen der InstanceOf Verbindungen sowie der Verbindungen zu allen Fehlermodi
     */
    private void combineCFTRootNodes(){
        List<NameIDQueryResult> inputsOfCFT2 = cft2.getInputList();
        List<NameIDQueryResult> outputsOfCFT2 = cft2.getOutputList();

        int startid = cft1.getCftRootNodeId();

        //Alle Inputs und Outputs an den verbleibenden Wurzelknoten anhängen - duplikate verschwinden später sowieso
        inputsOfCFT2.stream().forEach((input) -> db.createRelationship(startid,input.getId(), VerbindungsTyp.InputOf));

        outputsOfCFT2.stream().forEach((output) -> db.createRelationship(startid,output.getId(),VerbindungsTyp.OutputOf));

        //Instanzen des zu löschenden Wurzelknotens an verbleibenden Wurzelknoten anhängen
        List<Integer> instanceIdsList = DatabaseReading.getIdsOfConnectedInstances(cft2.getCftRootNodeId());
        instanceIdsList.forEach(instance -> db.createRelationship(startid,instance,VerbindungsTyp.InstanceOf));

        db.deleteNodeAndRelationships(cft2.getCftRootNodeId());
    }


    /**
     * Überprüfen, ob eine ausgehende Kante von einem Knoten der gelöscht wird in das neue Fehlermodell übernommen werden muss.
     * Dazu wird die ID des äquivalenten verbleibenden Knotens zusätzlich angegeben, um die Verbindungen der beiden abzugleichen und eventuell eine neue Verbindung anzulegen.
     * @param idStay ID des Elements, das nicht gelöscht wird
     * @param idDelete ID des Elements, das gelöscht wird
     */
    private void addUnequalOutgoingConnections(final int idStay, final int idDelete){
        List<Integer> idStayOutgoing = new ArrayList<>();
        if(successorMap.containsKey(idStay)) {
            successorMap.get(idStay).forEach(id -> idStayOutgoing.add(id.getEndid()));
        }
        List<IntegratorVerbindung> idDeleteoutgoing = new ArrayList<>();
        if(successorMap.containsKey(idDelete)) {
            idDeleteoutgoing.addAll(successorMap.get(idDelete));
        }

        idDeleteoutgoing.stream().forEach((deleteElement) -> {
            if(!idStayOutgoing.contains(matchMap.get(deleteElement.getEndid()))){
                IntegratorVerbindung connection = new IntegratorVerbindung(idStay,deleteElement.getEndid(),VerbindungsTyp.FailurePropagation,0);

                if(!connectionsToAdd.contains(connection))connectionsToAdd.add(connection);
                newCFT.addVerbindung(connection);
            }
        });
    }

    /**
     * Methode, die für ein Element ermittelt, ob zu diesem Zeitpunkt ein Element aus dem anderen Fehlermodell  äquivalent ist.
     * @param idNotToFind ID des Elements für das eine Äquivalenz ermittelt werden soll
     * @param predecessors Vorgänger des Elements
     * @throws NoEqualityCanBeFoundYetException Exception wird geworfen, wenn zu dem Zeitpunkt noch keine Äquvalenz ermittelt werden kann, weil noch nicht alle Vorgänger behandelt wurden
     * @throws NoEqualityExistsException Exception wird geworfen wenn keine Äquivalenz existiert
     */
    private void findCorrespondingElementInFM2(final int idNotToFind,final List<IntegratorVerbindung> predecessors) throws NoEqualityCanBeFoundYetException,NoEqualityExistsException {
        List<Integer> correspondingIDsOfThePredecessors = new ArrayList<>();
        predecessors.stream().forEach(predecessor -> {
            if(matchMap.containsKey(predecessor.getStartid())){
                if(matchMap.get(predecessor.getStartid())!=-1){
                    correspondingIDsOfThePredecessors.add(matchMap.get(predecessor.getStartid()));
                }else{
                    throw new NoEqualityExistsException();
                }
            }else{
                throw new NoEqualityCanBeFoundYetException();
            }
        });

        predecessorMap.values().forEach(predecessorValue -> {
            List<Integer> predecessorIDs = predecessorValue.stream().map(IntegratorVerbindung::getStartid).collect(Collectors.toList());

            if(predecessorIDs.containsAll(correspondingIDsOfThePredecessors) && correspondingIDsOfThePredecessors.containsAll(predecessorIDs)){//leere liste bei vorgängern sollte nich vorkommen,weil alle solchen elemente mit der match liste übergeben wurden
                int elementid = predecessorValue.get(0).getEndid();
                if(elementid != idNotToFind) {
                    matchMap.put(idNotToFind, elementid);
                    matchMap.put(elementid, idNotToFind);
                }
            }
        });

        //Wenn alle vorgänge gemachted werden konnten, aber dennoch kein match gefunden werden konnte, dann kann es kein equivalent geben
        // denn eine Änderung kann im Laufe des Programms nicht eintreten, weil sich ein matching nicht  veränder kann/sollte
        if(!matchMap.containsKey(idNotToFind)){
            matchMap.put(idNotToFind,-1);
        }

    }


}
