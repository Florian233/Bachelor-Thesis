package Essarel;

import FTElemente.Verbindung;
import FTElemente.VerbindungsTyp;



/**
 * Unterklasse der Abstrakten Klasse Verbindung. Allerdings bietet diese Klasse keine Erweiterungen zu der Abstrakten Klasse.
 */
public class EssarelVerbindung extends Verbindung {
    /*
    private org.w3c.dom.Element source;
    private org.w3c.dom.Element target;


    public EssarelVerbindung(org.w3c.dom.Element source, org.w3c.dom.Element target, String name, VerbindungsTyp typ) {
        super(null, null, name, typ);

        this.source = source;
        this.target = target;
    }*/

    public EssarelVerbindung(EssarelElement von, EssarelElement nach, String name, VerbindungsTyp typ){
        super(von,nach,name,typ);
    }

    //public org.w3c.dom.Element getSource (){return source;}

    //public org.w3c.dom.Element getTarget(){return target;}

}
