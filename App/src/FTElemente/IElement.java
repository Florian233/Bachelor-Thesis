package FTElemente;

/**
 * Interface für alle möglichen Elemente, die später in die Datenbank eingefügt werden sollen.
 */
public interface IElement {

    /**
     * Methode um nach erstellen des Elements die ID des Elements in der Datenbank einzutragen, damit Verbindungen richtig gesetzt werden können.
     * @param id des Elements in der Datenbank
     */
    void setNeoID(final int id);

    int getNeoID();

    /**
     * Methode um die zugehörige Cypher Query zu generieren, die das Element in der Neo4j Datenbank erstellt
     * @return Cypher Query um Element zu erzeugen
     */
    String getCreateQuery();

    ElementTyp getTyp();

    String getName();
}
