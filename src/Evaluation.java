

/** Evaluation strategies */
enum EvaluationType { CALL_BY_VALUE, CALL_BY_NAME, CALL_BY_NEED }

/** Evaluation strategies */
class EvaluationPolicy {
    EvaluationType callType;
    EvaluationType consType;
    public EvaluationPolicy(EvaluationType callType, EvaluationType consType) {
        this.callType = callType;
        this.consType = consType;
    }
}


