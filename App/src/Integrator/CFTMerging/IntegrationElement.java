package Integrator.CFTMerging;

import FTElemente.Element;
import FTElemente.ElementTyp;

/**
 * Element, das alle notwendigen Informationen speichert für die Zusammenführung von CFTs.
 */
public class IntegrationElement extends Element {

    private boolean processed = false;

    public IntegrationElement(final ElementTyp typ,final String name,final int neoid) {
        super(typ, name);
        this.neoid = neoid;
    }

    public boolean getProcessed(){return processed;}

    public void setProcessed(){processed = true;}
}
