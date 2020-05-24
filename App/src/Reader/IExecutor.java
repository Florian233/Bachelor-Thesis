package Reader;

import FTElemente.IElement;
import FTElemente.IVerbindung;

/**
 * Interface für Executor. Klasse soll zum Verarbeiten der eingelesenen und geparsten Daten dienen., z.B. Cypher Queries ausführen.
 */
public interface IExecutor<T extends IElement,V extends IVerbindung>{

    /**
     * Methode soll das Verarbeiten der Daten starten/durchführen.
     */
    void execute();

    /**
     * Methode mittels derer dem Executor ein Element übergeben werden kann.
     * @param element Element, das ins Modell eingefügt werden soll.
     */
    void addElement(final T element);

    /**
     * Methode mittels der dem Executor eine Verbindung übergeben werden kann.
     * @param v Verbindung, die ins Modell eingefügt werden soll.
     */
    void addVerbindung(final V v);

}
