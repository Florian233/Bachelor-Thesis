package Reader;

import DatabaseConnection.DBConnection;
import FTElemente.IElement;
import FTElemente.IVerbindung;

import java.util.ArrayList;

/**
 * Abstrakte Klasse für den Executor
 */
public abstract class Executor<T extends IElement,V extends IVerbindung> implements IExecutor<T,V> {

    /**
     * Liste für alle Elemente, die in die Datenbank eingefügt werden sollen
     */
    protected ArrayList<T> elements = new ArrayList<>();
    /**
     * Liste für alle Verbindungen zwischen den Elementen, die in die Datenbank eingefügt werden sollen
     */
    protected ArrayList<V> connections = new ArrayList<>();
    protected DBConnection db = DBConnection.getInstance();

    /**
     * Methode, die Listen der Elemente und Verbindungen ausliest, deren Query sich holt und der dbconnection übergibt zum ausführen.
     */
    protected void executeQueries(){
        System.out.println("Start des Einfügens der Daten in die Datenbank");
        elements.parallelStream().forEach((element) -> {
            int id = db.insertNode(element.getCreateQuery());
            element.setNeoID(id);
        });

        connections.parallelStream().forEach((verbindung) -> {
            db.createRelationship(verbindung.getCreateQuery());
        });
        System.out.println("Daten in die Datenbank einfuegen abgeschlossen");
    }


    /**
     * Methode mittels derer dem Executor ein Element übergeben werden kann.
     * @param element Element, das ins Modell eingefügt werden soll.
     */
    @Override
    public void addElement(final T element){
        elements.add(element);
    }

    /**
     * Methode mittels der dem Executor eine Verbindung übergeben werden kann.
     * @param v Verbindung, die ins Modell eingefügt werden soll.
     */
    @Override
    public void addVerbindung(final V v){
        connections.add(v);
    }
}
