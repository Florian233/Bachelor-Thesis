package Essarel;

import FTElemente.IElement;
import FTElemente.IVerbindung;
import Reader.Executor;

/**
 * Executor Klasse für Essarel. In dieser Klasse werden die Elemente und Verbindungen gesammelt und später die zugehörigen Queries ausgeführt.
 */
public class EssarelExecutor<T extends IElement,V extends IVerbindung> extends Executor<T,V> {


    @Override
    public void execute() {

        System.out.println("Es gibt "+elements.size()+ " Elemente");
        System.out.println("Es gibt "+connections.size()+ " Verbindungen");

        executeQueries();
    }
}
