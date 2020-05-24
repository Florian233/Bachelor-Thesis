package EA;

import FTElemente.Element;
import FTElemente.ElementTyp;

/**
 * Klasse um ein Element zu erstellen, dass an die Gegebenheiten des Parsens der EA Elemente angepasst ist.
 */
public class EAElement extends Element {

    /**
     * Id des zugehörigen Elements im EA Repository
     */
    private int eaid;

    public EAElement(final int id, final ElementTyp typ, final String eigenschaften, final String name){

        super(typ, eigenschaften, name);
        this.eaid = id;
    }


    public EAElement(final int id, final ElementTyp typ,final String name){
        super(typ, "", name);
        this.eaid = id;
    }

    public int getEAID() {
        return eaid;
    }

}
