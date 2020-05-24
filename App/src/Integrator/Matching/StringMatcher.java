package Integrator.Matching;

import DatabaseConnection.NameIDQueryResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Klasse, die eine Methode bereitstellt, die 2 Listen von Elementen, die aus der Datenbank ausgelsen wurden, nimmt und aufeinander mappt. Id ist aus der Datenbank und soll hier keine Beachtung finden, wird nur benötigt um später in der Datenbank richtige Verbindungen zu ziehen.
 */
public class StringMatcher {

    /**
     * Klasse um die Levhenstein Distanz zu berechnen.
     */
    private LevhensteinDistance distCalc = new LevhensteinDistance();
    /**
     * Threshold bis zu der Distanz die Elemente gleich sein können. Wird nicht mehr benutzt
     */
    private int matchingThreshold = 520;

    /**
     * Faktor zu wie viel Prozent zwei Strings ungleich sein dürfen. Mit Faktor 10 also 1000% faktisch nicht mehr in ausschlaggebend.
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
        String[] cuttedInput = cutString(input.getName());
        final int[] minimum = {999999999};
        final NameIDQueryResult[] correspondingObjectToMin = new NameIDQueryResult[1];
        input2.forEach((compareString) -> {

            String[] cuttedCompareString = cutString(compareString.getName());

            int matchingThreshold = calculateThreshold(cuttedCompareString,cuttedInput);

            int dist = compareStrings(cuttedInput,cuttedCompareString);

            if(dist < minimum[0] && dist < matchingThreshold){
                minimum[0] = dist;
                correspondingObjectToMin[0] = compareString;
            }
        });
        if(correspondingObjectToMin[0] == null){return new Match(input.getId(),input);}
        return new Match(input.getId(), correspondingObjectToMin[0].getId(),minimum[0],input,correspondingObjectToMin[0]);
    }

    private int calculateThreshold(String[] cuttedCompareString, String[] cuttedInput) {
        int dist1 = 0;
        int dist2 = 0;

        for(String s:cuttedCompareString)dist1+=s.length();
        for(String s:cuttedInput)dist2+=s.length();

        return (int) (dist1*dist2*matchingThresholdFactor);
    }

    /**
     * Methode die die Distanz der beiden zerlegten Strings berechnet.
     * @param cuttedInput String Array
     * @param cuttedCompareString String Array
     * @return Distanz der beiden Strings
     */
    public int compareStrings(String[] cuttedInput, String[] cuttedCompareString) {
        int[] comparing1 = new int[cuttedInput.length];
        int[] comparing2 = new int[cuttedCompareString.length];

        //for for über beide geschachtelt in beide richtungen mit min berechnung

        for(int i = 0;i<cuttedInput.length;i++){
            String stringToCompare1 = cuttedInput[i];
            int min=99999;
            for(String stringToCompare2 : cuttedCompareString){
                int dist = distCalc.levenshteinDistance(stringToCompare1,stringToCompare2);
                if(dist<min){min = dist;}
            }
            comparing1[i] = min;
        }

        for(int i = 0;i<cuttedCompareString.length;i++){
            String stringToCompare1 = cuttedCompareString[i];
            int min = 99999;
            for(String stringToCompare2 : cuttedInput){
                int dist = distCalc.levenshteinDistance(stringToCompare1,stringToCompare2);
                if(dist<min){min = dist;}
            }
            comparing2[i] = min;
        }

        int resultLeft = 0;
        int resultRight = 0;
        for(int c:comparing1){
            resultLeft+=c;
        }

        for(int c:comparing2){
            resultRight +=c;
        }

        return resultLeft*resultRight;
    }


    /**
     * Methode die einen String an den vorgegebenen Trennzeichen trennt.
     * @param str String der zerlegt werden soll.
     * @return Array des in zertrennten Strings
     */
    public String[] cutString(final String str){
        ArrayList<String> parts = new ArrayList<>();

        int index = 0;

        for(int i = 0;i<str.length();i++){
            if(str.charAt(i) == '_'  || str.charAt(i) == ':' || str.charAt(i) == ' ' || str.charAt(i) == '.' || str.charAt(i) == ';' || str.charAt(i)=='-' || str.charAt(i) == ',' ){
                parts.add(str.substring(index,i));
                index = i+1;
            }else if(i+1 == str.length()){//Ende des Strings
                parts.add(str.substring(index,i+1));
            }

        }
        parts.remove("");
        String[] result = new String[parts.size()];
        for (int i = 0;i<parts.size();i++){
            result[i] = parts.get(i);
        }
        return result;
    }

    public void setMatchingThreshold(final int matchingThreshold){
        this.matchingThreshold = matchingThreshold;
    }

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
