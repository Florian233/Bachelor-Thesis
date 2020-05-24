package Integrator.Matching;

import DatabaseConnection.NameIDQueryResult;

/**
 * Match von 2 Strings, dazu wird dann hier die passenden IDs ( des Elements mit dem Namen in der DB) gespeichert
 */
public class Match {

    private int a;

    private int b = -1; //Wenn -1 kein match

    private int dist;

    private NameIDQueryResult aa;

    private NameIDQueryResult bb;

    public Match( final int a, final int b,final int dist,final NameIDQueryResult aa,final NameIDQueryResult bb){
        this.a = a;
        this.b = b;
        this.dist = dist;
        this.aa = aa;
        this.bb = bb;
    }

    public Match(final int a,final NameIDQueryResult aa){
        this.aa = aa;
        this.a = a;

    }

    public Match(int id, NameIDQueryResult obj, int id1, NameIDQueryResult obj1) {
        this.a = id;
        this.aa = obj;
        this.b = id1;
        this.bb = obj1;
    }

    public int getA(){return a;}

    public int getB(){return b;}

    public int getDist(){return dist;}

    public NameIDQueryResult getAA(){return aa;}

    public NameIDQueryResult getBB(){return bb;}
}
