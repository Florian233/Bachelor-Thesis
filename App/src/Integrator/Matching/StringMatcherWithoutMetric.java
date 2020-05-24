package Integrator.Matching;

import DatabaseConnection.NameIDQueryResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Klasst, die statt der Metrik nur die pure Lev. Dist. verwendet.
 */
public class StringMatcherWithoutMetric {



    /**
     * Klasse um die Levhenstein Distanz zu berechnen.
     */
    private LevhensteinDistance distCalc = new LevhensteinDistance();
    /**
     * Threshold bis zu der Distanz die Elemente gleich sein können. Wird nicht benutzt.
     */
    private int matchingThreshold = 520;
    /**
     * Faktor zu wie viel Prozent die beiden Strings ungleich sein dürfen. Spielt mit dem Wert 10 dann auch keine Rolle mehr.
     * 1000% ungleich kann sowieso nie überschritten werden :D.
     */
    private double matchingThresholdFactor = 10;

    /**
     * Methode um Namen der Elemente aus zwei Listen aufeinander abzubilden.
     * @param input1 Liste von NameIDQueryResult wovon jedes Element eines aus der Liste 2 abgebildet wird
     * @param input2 Liste von NameIDQueryResult auf dessen Elemente abgebildet wird. Auf ein Element kann mehrmals abgebildet werden.
     * @return Liste von Match
     */
    //=> Größere Liste erster Parameter
    public List<Match> matchStrings(final List<NameIDQueryResult> input1/*einmal*/, final List<NameIDQueryResult> input2/*mehrmals*/){

        List<Match> result = new ArrayList<>();

        input1.forEach((input) -> result.add(matchSingleString(input,input2)));

        return result;
    }


    /**
     * Methode um das Element aus der Liste zu finden was am besten auf das input passt.
     * @param input NameIDQueryResult
     * @param input2 Liste von NameIDQueryResult
     * @return Match von input auf ein Element der Liste input2
     */
    public Match matchSingleString(final NameIDQueryResult input,final List<NameIDQueryResult> input2) {
        String inputName = input.getName();
        final int[] minimum = {};
        final NameIDQueryResult[] correspondingObjectToMin = new NameIDQueryResult[1];
        input2.forEach((comparePort) -> {
            String compareString = comparePort.getName();

            int threshold = (int) (inputName.length()*matchingThresholdFactor);

            int dist = compareStrings(inputName,compareString);

            if(dist< minimum[0] && dist<threshold){
                minimum[0] = dist;
                correspondingObjectToMin[0] = comparePort;
            }
        });
        if(correspondingObjectToMin[0] == null){return new Match(input.getId(),input);}
        return new Match(input.getId(), correspondingObjectToMin[0].getId(),minimum[0],input,correspondingObjectToMin[0]);
    }

    /**
     * Methode die die Distanz der beiden Strings berechnet.
     * @param input String
     * @param input2 String
     * @return Distanz der beiden Strings
     */
    public int compareStrings(String input, String input2) {
       return distCalc.levenshteinDistance(input,input2);
    }


    public void setMatchingThreshold(final int matchingThreshold){
        this.matchingThreshold = matchingThreshold;
    }
    public void setMatchingThresholdFactor(final double matchingThresholdFactor){this.matchingThresholdFactor =matchingThresholdFactor;}
    /**
     * Methode, die jedem Element aus Liste1 eins aus Liste2 zuordnet, aber keines mehrmals getroffen wird. Umgekehrt genauso.
     * @param list1 Liste von NameIDQueryResult
     * @param list2 Liste von NameIDQueryResult
     * @return Liste von Übereinstimmungen
     */
    public List<Match> matchEverythingOnce(final List<NameIDQueryResult> list1,final List<NameIDQueryResult> list2){
        Map<Integer,Match> matchMap = new HashMap<>();
        Map<NameIDQueryResult,List<NameIDQueryResult>> possibleMatches = new HashMap<>();
        List<Match> result = new ArrayList<>();
        Map<Integer,NameIDQueryResult> idQueryResultMap = new HashMap<>();
        Map<Integer,NameIDQueryResult> idToNameIDQueryResultObject = new HashMap<>();



        list1.forEach((element) -> {
            idToNameIDQueryResultObject.put(element.getId(),element);
            List<NameIDQueryResult> a = new ArrayList<>();
            a.addAll(list2);//Kopie der Liste
            possibleMatches.put(element,a);
            idQueryResultMap.put(element.getId(),element);
        });

        List<NameIDQueryResult> runningList = new ArrayList<>();
        runningList.addAll(list1);//sichere kopie der liste

        while(!runningList.isEmpty()){
            NameIDQueryResult compareObject = runningList.remove(0);
            Match a = matchSingleString(compareObject,possibleMatches.get(compareObject));
            if(a.getB() == -1){result.add(a);}
            else if(matchMap.containsKey(a.getB())){
                if(a.getB()<matchMap.get(a.getB()).getDist()){
                    Match oldValue = matchMap.get(a.getB());
                    List<NameIDQueryResult> oldList = possibleMatches.get(idToNameIDQueryResultObject.get(oldValue.getA()));
                    List<NameIDQueryResult> newList = removeElementWithID(oldValue.getB(),oldList);
                    if(newList.isEmpty()){
                        result.add(new Match(oldValue.getA(),oldValue.getAA()));
                    }else {
                        possibleMatches.replace(idQueryResultMap.get(oldValue.getA()), newList);
                        runningList.add(idQueryResultMap.get(oldValue.getA()));
                    }
                    matchMap.replace(a.getB(), a);
                }else{
                    List<NameIDQueryResult> newList = removeElementWithID(a.getB(),possibleMatches.get(compareObject));
                    if(newList.isEmpty()){result.add(new Match(compareObject.getId(),compareObject));
                    }else {
                        possibleMatches.replace(compareObject, newList);
                        runningList.add(compareObject);
                    }
                }
            }else{matchMap.put(a.getB(),a);}
        }

        matchMap.values().forEach(result::add);

        return result;
    }

    /**
     * Methode um ein Element mit einer ID aus der Liste zu löschen.
     * @param id des Elements, das aus der Liste gelöscht werden soll.
     * @param matches Liste aus der das Element gelöscht werden soll.
     * @return Liste ohne das zu löschende Element
     */
    private List<NameIDQueryResult> removeElementWithID(final int id,List<NameIDQueryResult> matches){
        List<NameIDQueryResult> result = new ArrayList<>();
        matches.stream().forEach((match) -> {
            if(match.getId() != id) result.add(match);
        });
        return result;
    }




}
