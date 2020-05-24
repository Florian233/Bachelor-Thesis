package Integrator.Matching;

import DatabaseConnection.DBConnection;
import DatabaseConnection.IntIntQueryResult;
import DatabaseConnection.NameIDQueryResult;
import FTElemente.ElementTyp;
import FTElemente.VerbindungsTyp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Klasse um Ports aufeinander abzubilden unter berücksichtung deren Ports oder möglichen Ports laut TypeMatcher
 */
public class PortMatchingWithTypes {

    private StringMatcher stringMatcher = new StringMatcher();
    private DBConnection db = DBConnection.getInstance();
    private NameIDQueryResult noType = new NameIDQueryResult("NoTypeExists",-1);
    private List<NameIDQueryResult> allTypesInDatabase = new ArrayList<>();
    private Map<Integer,Integer> generalizationMap = new HashMap<>();


    public PortMatchingWithTypes(){
        String query = "MATCH (a:"+ ElementTyp.FailureType+") RETURN a.name, ID(a)";
        allTypesInDatabase.addAll(db.executeQuery(query));
        String queryForGeneralization = "MATCH (a)-[:"+VerbindungsTyp.SuperFailureType+"]->(b) RETURN ID(a),ID(b)";
        List<IntIntQueryResult> generalizationOfTypes = new ArrayList<>();
        generalizationOfTypes.addAll(db.getRelationshipReturnIntInt(queryForGeneralization));
        generalizationOfTypes.stream().forEach(generalization -> generalizationMap.put(generalization.getId1(),generalization.getId2()));
    }


    public Match matchPort(final NameIDQueryResult port,final List<NameIDQueryResult> ports2){
        List<NameIDQueryResult> ports = new ArrayList<>();
        ports.add(port);
        List<Match> matches = matchPorts(ports,ports2);
        return matches.get(0);
    }

    public List<Match> matchPorts(final List<NameIDQueryResult> ports1,final List<NameIDQueryResult> ports2){
        List<Match> resultMatches = new ArrayList<>();
        Map<Integer,List<NameIDQueryResult>> type2Ports2Map;
        //Ports aus Liste2 in Map
        type2Ports2Map = initializeTypeMap(ports2);


        //Liste1 durchlaufen
        ports1.stream().forEach(port1 -> {
            //Alle in Frage kommenden Ports anhand der Fehlertypen bestimmen
            List<NameIDQueryResult> selectedPorts2 = new ArrayList<>();
            selectedPorts2.addAll(type2Ports2Map.get(noType.getId()));
            List<NameIDQueryResult> typeAndSupertypesOfPort1 = getTypeAndSupertypesOfPort(port1);
            if (!typeAndSupertypesOfPort1.isEmpty()) {
                typeAndSupertypesOfPort1.stream().forEach(type ->{
                    if(type2Ports2Map.containsKey(type.getId())) {
                        selectedPorts2.addAll(type2Ports2Map.get(type.getId()));
                    }
                });
            }
            //Besten Match mittels der Metrik finden
            resultMatches.add(stringMatcher.matchSingleString(port1,selectedPorts2));
        });
        return resultMatches;
    }

    private Map<Integer, NameIDQueryResult> calculateTypesOfPorts(final List<NameIDQueryResult> portsWithoutType) {
        Map<Integer,NameIDQueryResult> resultMap = new HashMap<>();
        TypeMatcher typeMatcher = new TypeMatcher();
        List<Match> matches = typeMatcher.matchPortToType(portsWithoutType,allTypesInDatabase);
        matches.stream().forEach(match -> resultMap.put(match.getA(),match.getBB()));
        return resultMap;
    }

    /**
     * Methode erzeugt eine Map mit der ein jeder Typ auf eine Liste der Ports abgebildet wird, die diesen Typ haben
     * @param puts2 Ports aus denen die Map erstellt werden soll
     * @return Map Typ auf Liste von Ports
     */
    private Map<Integer,List<NameIDQueryResult>> initializeTypeMap(final List<NameIDQueryResult> puts2) {
        Map<Integer,List<NameIDQueryResult>> typeOfPorts = new HashMap<>();
        typeOfPorts.put(noType.getId(),new ArrayList<>());
        puts2.forEach(put -> {
            NameIDQueryResult type = getTypeOfPort(put.getId());
            if(type == null ){
                List<NameIDQueryResult> tempList = typeOfPorts.get(noType.getId());
                tempList.add(put);
                typeOfPorts.put(noType.getId(),tempList);
            }else{
                if (typeOfPorts.containsKey(type.getId())) {
                    List<NameIDQueryResult> tempList = typeOfPorts.get(type.getId());
                    tempList.add(put);
                    typeOfPorts.put(type.getId(), tempList);
                } else {
                    List<NameIDQueryResult> tempList = new ArrayList<>();
                    tempList.add(put);
                    typeOfPorts.put(type.getId(), tempList);
                }
            }
        });
        List<NameIDQueryResult> notypesList = typeOfPorts.get(noType.getId());
        Map<Integer, NameIDQueryResult> mapOfmatchesTypesToPortsWithoutType = calculateTypesOfPorts(notypesList);
        List<NameIDQueryResult> newNoTypeList = new ArrayList<>();
        notypesList.stream().forEach( p -> {
            if(mapOfmatchesTypesToPortsWithoutType.containsKey(p.getId())){
                NameIDQueryResult type = mapOfmatchesTypesToPortsWithoutType.get(p.getId());
                if (typeOfPorts.containsKey(type.getId())) {
                    List<NameIDQueryResult> tempList = typeOfPorts.get(type.getId());
                    tempList.add(p);
                    typeOfPorts.put(type.getId(), tempList);
                } else {
                    List<NameIDQueryResult> tempList = new ArrayList<>();
                    tempList.add(p);
                    typeOfPorts.put(type.getId(), tempList);
                }
            }else{
                newNoTypeList.add(p);
            }
        });
        typeOfPorts.put(noType.getId(),newNoTypeList);
        return typeOfPorts;
    }

    /**
     * Methode, die den FailureTyp Knoten zu der der Port eine Verbindung hat, aus der DB holt.
     * @param portId ID des Ports zu dem der Typ ermittelt werden soll
     * @return NameIDQueryResult Objekt des Typs wenn vorhanden, wenn nicht dann null
     */
    private NameIDQueryResult getTypeOfPort(final int portId){
        List<NameIDQueryResult> queryResult = db.executeQuery("MATCH (b),(a) WHERE ID(a)="+portId+" MATCH (a)-[:"+ VerbindungsTyp.FailureTypeOf+"]-(b) RETURN b.name,ID(b)");
        if(queryResult.isEmpty()) return null;
        return queryResult.get(0);
    }

    private List<NameIDQueryResult> getTypeAndSupertypesOfPort(final NameIDQueryResult port){
        List<NameIDQueryResult> queryResult = db.executeQuery("MATCH (b),(a) WHERE ID(a)="+port.getId()+" MATCH (a)-[:"+ VerbindungsTyp.FailureTypeOf+"]-(b) RETURN b.name,ID(b)");
        NameIDQueryResult typeOfPort = null;
        if(queryResult.isEmpty()){
            Match m = stringMatcher.matchSingleString(port,allTypesInDatabase);
            typeOfPort = m.getBB();
        }else {
            typeOfPort = queryResult.get(0);
        }
        List<Integer> supertypeids = new ArrayList<>();
        List<NameIDQueryResult> result = new ArrayList<>();
        result.add(typeOfPort);

        int typeId = typeOfPort.getId();
        while(generalizationMap.containsKey(typeId)){
            typeId = generalizationMap.get(typeId);
            supertypeids.add(typeId);
            }

        allTypesInDatabase.stream().forEach(t -> {
            if(supertypeids.contains(t.getId())){
                result.add(t);
            }
        });

        return result;
    }


    public List<Match> mapPortsOnce(final List<NameIDQueryResult> ports1 , final List<NameIDQueryResult> ports2){

        Map<Integer,List<NameIDQueryResult>> type2Ports2Map;
        //Ports aus Liste2 in Map
        type2Ports2Map = initializeTypeMap(ports2);

        List<NameIDQueryResult> queue = new ArrayList<>();
        queue.addAll(ports1);
        Map<NameIDQueryResult,Match> resultMap = new HashMap<>();
        Map<NameIDQueryResult,List<NameIDQueryResult>> possibleMatchesMap = new HashMap<>();
        ports1.parallelStream().forEach(port -> {
            List<NameIDQueryResult> selectedPorts2 = new ArrayList<>();
            selectedPorts2.addAll(type2Ports2Map.get(noType.getId()));
            List<NameIDQueryResult> typeAndSupertypesOfPort1 = getTypeAndSupertypesOfPort(port);
            if (typeAndSupertypesOfPort1 != null && !typeAndSupertypesOfPort1.isEmpty()) {
                typeAndSupertypesOfPort1.stream().forEach(type -> selectedPorts2.addAll(type2Ports2Map.get(type.getId())));
            }
        });


        while(!queue.isEmpty()){
            NameIDQueryResult portToMatch = queue.remove(0);
            Match m = matchPort(portToMatch,possibleMatchesMap.get(portToMatch));
            NameIDQueryResult portMappedAt = m.getBB();
            if(resultMap.containsKey(portMappedAt)){
                if(resultMap.get(portMappedAt).getDist() > m.getDist()){
                    //Element ermitteln, dass vorher auf diese abgebildet wurde
                    NameIDQueryResult n = resultMap.get(portMappedAt).getAA();
                    //Altes Element mit schlechterer Dist wieder hinten anhängen
                    queue.add(n);
                    //Element mit zu hoher dist aus Liste der möglichen Matches entfernen
                    List<NameIDQueryResult> l = possibleMatchesMap.get(n);
                    l.remove(portMappedAt);
                    if(l.isEmpty()){resultMap.put(n,new Match(n.getId(),n));}
                    possibleMatchesMap.put(n,l);
                    //Neues Element mit Match in die Map eintragen
                    resultMap.put(portMappedAt,m);
                }else{//Wenn Dist nicht besser, das Element auf das gemappt wurde aus der Liste der mögliche Matches entfernen
                    List<NameIDQueryResult> l = possibleMatchesMap.get(portToMatch);
                    l.remove(portMappedAt);
                    if(l.isEmpty()){resultMap.put(portToMatch,new Match(portToMatch.getId(),portToMatch));}
                    queue.add(portToMatch);
                }
            }

        }

        List<Match> result = new ArrayList<>();
        resultMap.values().forEach(result::add);
        return result;
    }

    //Jeder Typ sollte nur einen Supertypen haben
    private boolean isSupertype(final NameIDQueryResult type, final NameIDQueryResult possibleSuperType){
        int typeId = type.getId();
        int superTypeId = possibleSuperType.getId();

        if(typeId == superTypeId) return true;

        while(generalizationMap.containsKey(typeId)){
            if(generalizationMap.get(typeId) == superTypeId){
                return true;
            }
            typeId = generalizationMap.get(typeId);
        }

        return false;
    }

}
