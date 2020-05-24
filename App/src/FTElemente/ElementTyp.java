package FTElemente;

/**
 * DIE ELEMENTTYPEN, DIE SPÄTER ALS LABEL IN DIE DATENBANK GESCHRIEBEN WERDEN SOLLEN
 */
public enum ElementTyp {
    ORGate,
    ANDGate,
    XORGate,
    MoonGate,
    NotGate,
    BasicEvent,
    /**
     * Inport einer logischen Komponente
     */
    Inport,
    /**
     * Outport einer logischen Komponente
     */
    Outport,
    /**
     * Input einer CFT
     */
    Input,
    /**
     * Output einer CFT/FT
     */
    Output,
    LogicalComponent,
    LogicalComponentInstance,
    FaultTree,
    FailureType,
    NORGate,
    CFT,
    CFTInstance,
    InportInstance,
    OutportInstance,
    InputInstance,
    OutputInstance
}
