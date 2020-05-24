package DatabaseConnection;

/**
 * Klasse um den Returnwert einer Query aufzunehmen, die zwei int Werte zurückgibt
 */
public class IntIntQueryResult {

    private int id1;
    private int id2;

    public IntIntQueryResult(final int id1, final int id2){
        this.id1 = id1;
        this.id2 = id2;
    }

    public int getId1(){return id1;}

    public int getId2(){return id2;}
}
