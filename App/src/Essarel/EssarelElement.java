package Essarel;

import FTElemente.Element;
import FTElemente.ElementTyp;

/**
 * Nicht Abstrakte Elementklasse für Elemente während des Auslesens von Daten aus Essarelspeicherdateien.
 * Allerdings bietet diese Klasse keine sinnvollen Erweiterungen zu der Abstrakten Oberklasse.
 * Das Attribut Source wird nicht benutzt.
 */
public class EssarelElement extends Element {

    /**
     * Zugehöriges Element aus einer Essarelspeicherdatei im XML Format erstellt von einem XML-Parser.
     * Ungenutzt!
     */
    private org.w3c.dom.Element source;

    public EssarelElement(final org.w3c.dom.Element source,final ElementTyp typ,final String name) {

        super(typ, name);
        this.source = source;

    }

    public EssarelElement(org.w3c.dom.Element item, ElementTyp typ, String eigenschaft, String name) {
        super(typ, eigenschaft, name);
        source = item;
    }


    public org.w3c.dom.Element getSource(){
        return source;
    }

    public ElementTyp getTyp(){return  typ;}

}
