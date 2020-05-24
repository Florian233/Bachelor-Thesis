package Essarel;

import java.util.HashMap;
import java.util.Map;

/**
 * Klasse um eine Liste aller EssarelComponentData Objekte zu verwalten.
 */
public class EssarelComponentDataContainer {
    /**
     * HashMap um den Pfad zu einer Component.xml einer EssarelComponentData Instanz zuzuordnen
     */
    private Map<String,EssarelComponentData> pathComponentMap = new HashMap<>();

    /**
     * Instance um Singleton Pattern zu realisierung damit diese Klasse nur genau einmal instanziiert wird und alle drauf zu greifen können.
     */
    private static EssarelComponentDataContainer instance;

    /**
     * Methode um Singleton Pattern zu realisieren
     * @return instance dieser Klasse
     */
    public static synchronized EssarelComponentDataContainer getInstance ()  {
        if (EssarelComponentDataContainer.instance == null) {
            EssarelComponentDataContainer.instance = new EssarelComponentDataContainer();
        }
        return EssarelComponentDataContainer.instance;
    }

    /**
     * Methode um zu einem Pfad zu einer component.xml die zugehörige EssarelComponentData Instanz zu finden
     * @param componentPath Pfad zu component.xml der Component zu der das EssarelComponentData Objekt benötigt wird
     * @return das zugehörige EssarelComponentData Objekt zu dem Pfad
     */
    public synchronized EssarelComponentData getComponentData(final String componentPath){
        System.out.println("Anfrage nach Data zu: "+componentPath);
        return pathComponentMap.get(componentPath);
    }

    /**
     * Path wird das key und ComponentData als Value in die hashmap pathComponentMap eingetragen.
     * Path ist der Systempfad zu der Component zu der das componentData Objekt gehört.
     * @param path Systempfad zu der component.xml Datei
     * @param componentData Daten über die Component
     */
    public synchronized void addComponentData(final String path,final EssarelComponentData componentData){
        System.out.println("Einfügen von Data zu: "+path);
        pathComponentMap.put(path,componentData);
    }
}
