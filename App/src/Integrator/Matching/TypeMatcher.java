package Integrator.Matching;

import DatabaseConnection.NameIDQueryResult;

import java.util.List;

/**
 * Klasse um einen möglichen Typ pro Port zu ermitteln
 */
public class TypeMatcher {

    private StringMatcher stringMatcher = new StringMatcher();


    public List<Match> matchPortToType(final List<NameIDQueryResult> ports, final List<NameIDQueryResult> types){


        return stringMatcher.matchStrings(ports,types);
    }

}
