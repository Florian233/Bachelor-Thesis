package EA;

import FTElemente.Verbindung;
import FTElemente.VerbindungsTyp;

/**
 * Subklasse von Verbindung um die Quell und Ziel Id der Elemente im EA Repository aufzunehmen. So können Verbindungen unabhängig von den Elementen eingefügt werden.
 */
public class EAVerbindung extends Verbindung {

    /**
     * Id des Elements im EA Repository von dem die Verbindung ausgeht.
     */
    private int sourceeaid;
    /**
     * Id des Elements in EA Repository, das Ziel der Verbindung ist.
     */
    private int targeteaid;


    public EAVerbindung(final int von, final int nach, final String name, final String eigenschaften, final VerbindungsTyp typ){
        super(null,null,name,eigenschaften,typ);
        this.sourceeaid = von;
        this.targeteaid = nach;

    }

    public int getSourceeaid() {
        return sourceeaid;
    }

    public int getTargeteaid() {
        return targeteaid;
    }


}
