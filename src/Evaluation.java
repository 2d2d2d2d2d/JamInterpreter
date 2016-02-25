

/** Evaluation strategies */
enum EvaluationType { BY_VALUE, BY_NAME, BY_NEED }


/** Evaluation policies */
class EvaluationPolicy {
    
    /** Evaluation strategy for program-defined procedures */
    EvaluationType callType;
    
    /** Evaluation strategy for Cons */
    EvaluationType consType;
    
    /** Constructor */
    public EvaluationPolicy(EvaluationType callType, EvaluationType consType) {
        this.callType = callType;
        this.consType = consType;
    }
    
}


