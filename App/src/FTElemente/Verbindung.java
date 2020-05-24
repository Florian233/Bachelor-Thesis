package FTElemente;

/**
 * Abstrakte Klasse einer Verbindung hauptsächlich um die CreateQuery Methode allen subklassen zu vererben.
 */
public abstract class Verbindung implements IVerbindung{

    protected IElement von;
    protected IElement nach;
    protected String name;
    protected String eigenschaften = ""; // muss von der Struktur eigenschaft : 'xy' , eigenschaft : 1 , ... sein
    protected VerbindungsTyp typ;

    public Verbindung(final Element von, final Element nach, final String name, final String eigenschaften){
        this.von = von;
        this.nach = nach;
        this.name = name;
        this.eigenschaften = eigenschaften;
        typ = VerbindungsTyp.FailurePropagation;
    }

    public Verbindung(final Element von, final Element nach, final String name, final String eigenschaften, final VerbindungsTyp typ){
        this.von = von;
        this.nach = nach;
        this.name = name;
        this.eigenschaften = eigenschaften;
        this.typ = typ;
    }

    public Verbindung(final Element von, final Element nach, final String name, final VerbindungsTyp typ){
        this.von = von;
        this.nach = nach;
        this.name = name;
        this.typ = typ;
    }


    public IElement getVon(){
        return von;
    }

    public void setVon(final IElement e){this.von = e;}

    public void setNach(final IElement e){this.nach = e;}

    public IElement getNach(){
        return nach;
    }

    /**
     * Methode um die zugehörige Cypher Query zu generieren, die die Verbindung in der Neo4j Datenbank erstellt
     * @return Cypher Query um Verbindung zu erzeugen
     */
    public String getCreateQuery(){
        String query = "";
            if (name.equals("") && eigenschaften.equals("")) {
                query = "MATCH a, b " +
                        "WHERE ID(a) =" + von.getNeoID() + " AND ID(b) =" + nach.getNeoID() +
                        " CREATE (a)-[r:" + typ + " {}]->(b)";

            } else if (eigenschaften.equals("")) {
                query = "MATCH a, b \n" +
                        "WHERE ID(a) =" + von.getNeoID() + " AND ID(b) =" + nach.getNeoID() + "\n" +
                        "CREATE (a)-[r:" + typ + " {name:'" + name + "'}]->(b)";
            } else {
                query = "MATCH a, b \n" +
                        "WHERE ID(a) =" + von.getNeoID() + " AND ID(b) =" + nach.getNeoID() + "\n" +
                        "CREATE (a)-[r:" + typ + " {name:'" + name + "'," + eigenschaften + "}]->(b)";
            }

        System.out.println(query);
        return query;
    }
}
