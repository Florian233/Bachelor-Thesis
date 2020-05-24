package FTElemente;


/**
 * Enumeration aller möglichen Labels für Relationships(Verbindungen), die in die Datenbank eingetragen werden können.
 */
public enum VerbindungsTyp {
    /**
     * Failure Propagation in CFTs oder FTs
     */
    FailurePropagation,
    /**
     * Port mapping zwischen Inport einer logischen Komponente und Input des zugehörigen Fehlermodells
     */
    PortMapping,
    /**
     * Informationsfluss zwischen logischen Komponenten
     */
    InformationFlow,
    /**
     * Verbindung zwischen einer logischen Komponente ( oder Instanz) und seinem Inport
     */
    InportOf,
    /**
     * Verbindung zwischen einer logischen Komponente ( oder Instanz) und seinem Outport
     */
    OutportOf,
    /**
     * Verbindung zwischen einer logischen Komponente und seiner Instanz oder einem CFT(einer Komponente) und seiner Instanz
     */
    InstanceOf,
    /**
     * Verbindung zwischen einer logischen Komponente und seinem Fehlermodell (CFT oder FT)
     */
    FailureModelOf,
    /**
     * Verbindung zwischen einer  Komponente/CFT ( oder Instanz) und seinem Input
     */
    InputOf,
    /**
     * Verbindung zwischen einer  Komponente/CFT ( oder Instanz) oder FT und seinem Output
     */
    OutputOf,
    /**
     * Verbindung zwischen zwei Elementen im Failure Type System (Generalisierung)
     */
    SuperFailureType,
    /**
     * Verbindung zwischen einem Port und seinem FailureType
     */
    FailureTypeOf
}
