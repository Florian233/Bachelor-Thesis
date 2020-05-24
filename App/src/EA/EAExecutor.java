package EA;

import DatabaseConnection.DBConnection;
import FTElemente.ElementTyp;
import Reader.IExecutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Executor angepasst an die Notwendigkeiten des EA Repository.
 * Implementiert das Interface statt der Abstrakten Klasse, weil zu viel von der Abstrakten Klasse überschrieben werden müsste
 * und die beiden Listen aus der abstrakten Klasse nicht verwendet werden würden.
 */
public class EAExecutor implements IExecutor<EAElement,EAVerbindung> {

    /**
     * Liste aller zu erstellender Verbindungen
     */
    private List<EAVerbindung> connections = new ArrayList<>();

    /**
     * Instanz von DBConnection um Zugriff auf die Datenbank zu haben
     */
    private DBConnection db = DBConnection.getInstance();

    /**
     * Map die alle in die Datenbank einzufügenden Elemente enthält, als Key wird deren ID im EA Repository verwendet
     */
    private Map<Integer,EAElement> elementsMap = new HashMap<>();
    /**
     * Liste von allen failureType Elementen um sie möglicherweise Ports zuzuordnen
     */
    private List<EAElement> failures = new ArrayList<>();

    /**
     * Verarbeiten der eingefügten Daten.
     * Zuerst werden die Daten vervollständigt, dann für jedes Element und jede Verbindung die zugehörige Cypher Query ausgeführt
     */
    public void execute(){

        //matchPortToFailure(); //jetzt über GUI

        completeConnections();

        System.out.println("Es gibt "+elementsMap.size()+ " Elemente");
        System.out.println("Es gibt "+connections.size()+ " Verbindungen");


        //Zuerst die Elemente, weil die Neo4jId benötigt wird um die Verbindungen eindeutig den Elementen/Knoten in der DB zuzuordnen
        elementsMap.values().forEach((element) -> {
            int id = db.insertNode(element.getCreateQuery());
            element.setNeoID(id);
            System.out.println("Element mit der NeoId: "+id+" eingefügt");
        });


        System.out.println("Start des Einfügens der Verbindungen");
        connections.stream().forEach((connection) -> {
                //System.out.println("Verbindung von " + connection.getVon().getNeoID() + " zu " + connection.getNach().getNeoID());
                System.out.println("Query: " + connection.getCreateQuery());
                db.createRelationship(connection.getCreateQuery());
        });
        System.out.println("Daten in die Datenbank einfuegen abgeschlossen");

    }
    /**
     * Hinzufügen eines Elements. Wenn das Element einem bestimmten Typ eintspricht wird es zusätzlich in eine weitere Liste eingefügt,
     * um später weitere notwendige Verbindungen ziehen zu können.
     * @param element hinzuzufügendes Element
     */
    @Override
    public void addElement(EAElement element) {

        elementsMap.put(element.getEAID(),element);
        System.out.println("Executor Element: "+element.getName()+"  "+element.getTyp()+"  "+element.getEAID());

        if(element.getTyp().equals(ElementTyp.FailureType)){
            failures.add(element);
        }

    }

    /**
     * Methode um eine Verbindung hinzuzufügen.
     * @param eaVerbindung hinzuzufügende Verbindung
     */
    @Override
    public void addVerbindung(EAVerbindung eaVerbindung) {
        connections.add(eaVerbindung);

    }

    /**
     * Methode, die den Verbindungen ihre Start und Ziel Elemente setzen, da die Verbindungen bis hier hin und mittels der Id ihres Start und Ziel Elements im
     * EA Repository definiert waren.
     */
    private void completeConnections(){
        connections.forEach((connection) -> {
            if(elementsMap.get(connection.getSourceeaid()) != null && elementsMap.get(connection.getTargeteaid()) != null ) {
                connection.setVon(elementsMap.get(connection.getSourceeaid()));
                connection.setNach(elementsMap.get(connection.getTargeteaid()));
            }
        });
    }
}
