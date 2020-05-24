package FTElemente;

/**
 * Interface für Verbindung.
 */
public interface IVerbindung {

    /**
     * Methode um die zugehörige Cypher Query zu generieren, die die Verbindung in der Neo4j Datenbank erstellt
     * @return Cypher Query um Verbindung zu erzeugen
     */
    String getCreateQuery();

    IElement getVon();

    void setVon(final IElement e);

    void setNach(final IElement e);

    IElement getNach();
}