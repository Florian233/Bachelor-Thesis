package Essarel;

import java.util.ArrayList;

/**
 * Klasse um Daten zu Components zu speichern. Wie Namen der In- und Outputs , den Namen der Komponente und die Systempfade zu der .cft und .interface Datei der Komponente.
 *
 */
public class EssarelComponentData {

    /**
     * Systempfad zu der .cft Datei der Komponente
     */
    private String realizationPath;
    /**
     * Systempfad zu der .interface Datei der Komponente
     */
    private String specificationPath;
    /**
     * Name der Komponente
     */
    private String name;
    /**
     * Name der CFT Realisierung der Komponente
     */
    private String cftName;
    /**
     * Namen der Inputs der Komponente in der Reihenfolge wie sie in der .interface Datei abgelegt sind.
     * Das ist notwendig um sie später gemäß den XML Connections auszulesen.
     */
    private ArrayList<String> inputNamen = new ArrayList<>();//Reihenfolge muss dem aus dem Specification interface entsprechen
    /**
     * Namen der Outputs der Komponente in der Reihenfolge wie sie in der .interface Datei abgelegt sind.
     * Das ist notwendig um sie später gemäß den XML Connections auszulesen.
     */
    private ArrayList<String> outputNamen = new ArrayList<>();//Reihenfolge muss dem aus dem Specification interface entsprechen
    /**
     * EssarelElement mit der Componente, um später die Verbindung zwischen einer ComponentInstance und der Componente zu ziehen
     */
    private EssarelElement component;


    public EssarelComponentData(final String name,final String realizationPath, final String specificationPath) {

        this.name = name;
        this.realizationPath = realizationPath;
        this.specificationPath = specificationPath;
    }

    public void setInputNamen(final ArrayList<String> inputNamen){this.inputNamen.addAll(inputNamen);}

    public void setOutputNamen(final ArrayList<String> outputNamen){this.outputNamen.addAll(outputNamen);}

    public String getRealizationPath(){
        return realizationPath;
    }

    public String getSpecificationPath() { return specificationPath; }

    public String getName(){return name;}

    public String getCftName(){return cftName;}

    public void setCftName(final String cftName){this.cftName=cftName;}

    public String getInputName(final int index){return inputNamen.get(index);}

    public String getOutputName(final int index){return outputNamen.get(index);}

    public void addEssarelComponent(final EssarelElement essarelElement) {
        this.component = essarelElement;
    }

    public EssarelElement getComponent (){return component;}
}
