package DatabaseConnection;

import FTElemente.ElementTyp;

/**
 * Klasse um ein Ergebnis von einer Name, ID Anfrage zu speichern, zusätzlich kann noch ein ElementTyp gespeichert werden, da diese Klasse hauptsächlich Ergebnisse von Abfragen nach Knoten enthält.
 *
 */
public class NameIDQueryResult {

    private String name;
    private int id;
    private ElementTyp type;

    public NameIDQueryResult(final String name, final int id){
        this.name = name;
        this.id = id;
    }

    public NameIDQueryResult(final String name, final int id, final ElementTyp type){
        this.name = name;
        this.id = id;
        this.type = type;
    }

    public String getName(){return name;}

    public int getId(){return id;}

    public ElementTyp getType(){return type;}

    @Override
    public boolean equals(Object object)
    {
        boolean equal  = false;

        if (object != null && object instanceof NameIDQueryResult)
        {
            if(((NameIDQueryResult) object).getName().equals(this.name) && ((NameIDQueryResult) object).getId() == this.id)equal=true;
        }

        return equal;
    }
}
