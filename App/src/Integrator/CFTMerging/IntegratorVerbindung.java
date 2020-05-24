package Integrator.CFTMerging;

import FTElemente.VerbindungsTyp;

/**
 * Klasse um alle Daten aufzunehmen, die eine Verbindung betreffen und für das Zusammenführen mit einem weiteren CFT notwendig sind.
 */
public class IntegratorVerbindung {

    private int startid;
    private int endid;
    private VerbindungsTyp type;
    private int neoId;

    public IntegratorVerbindung(final int startid, final int endid, final VerbindungsTyp type, final int neoId) {
        this.startid = startid;
        this.endid = endid;
        this.type = type;
        this.neoId = neoId;
    }

    public int getStartid(){return startid;}

    public int getEndid(){return endid;}

    public VerbindungsTyp getType(){return type;}

    public int getNeoId(){return neoId;}

    @Override
    public boolean equals(Object obj) {
        boolean equal  = false;

        if (obj != null && obj instanceof IntegratorVerbindung)
        {
            if(((IntegratorVerbindung) obj).getStartid() == this.startid && ((IntegratorVerbindung) obj).getEndid() == this.endid && ((IntegratorVerbindung) obj).getType().equals(this.type)){
                equal=true;
            }
        }

        return equal;
    }
}
