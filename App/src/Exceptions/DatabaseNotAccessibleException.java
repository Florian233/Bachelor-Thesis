package Exceptions;

/**
 * Exception, dass die Datenbank nicht erreichbar ist.
 */
public class DatabaseNotAccessibleException extends RuntimeException {

    public DatabaseNotAccessibleException(final String s){
        super(s);
    }

    public DatabaseNotAccessibleException(){
        super();
    }
}
