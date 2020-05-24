package FTElemente;

/**
 * Abstrakte Klasse für ein Element
 */
public abstract class Element implements IElement{
    /**
     * Id des Elements in der Datenbank.
     */
    protected int neoid;
    protected ElementTyp typ;
    protected String eigenschaften = ""; // muss von der Struktur eigenschaft : 'xy' , eigenschaft : 1 , ... sein
    protected String name = "";

    public Element(final ElementTyp typ, final String eigenschaften,final String name){
        this.typ = typ;
        this.eigenschaften = eigenschaften;
        this.name = name;
    }

    public Element(final ElementTyp typ,final String name){
        this.typ = typ;
        this.name = name;
    }

    @Override
    public ElementTyp getTyp(){return typ;}

    @Override
    public String getName(){return name;}

    public void setProperty(final String property){
        eigenschaften = eigenschaften + property;
    }

    public String getProperty(){return eigenschaften;}

    @Override
    public void setNeoID(final int id) {
        this.neoid = id;
    }

    @Override
    public int getNeoID() {
        return neoid;
    }


    @Override
    public String getCreateQuery() {
        String query = "";
        if(eigenschaften.equals("")){
            query = "CREATE (a:" +typ +"{ name : '"+ name + "'}) RETURN ID(a)";
        }else{
            query = "CREATE (a:" +typ +"{ name : '"+ name + "'," + eigenschaften +" }) RETURN ID(a)";
        }
        System.out.println(query);
        return query;
    }
}

