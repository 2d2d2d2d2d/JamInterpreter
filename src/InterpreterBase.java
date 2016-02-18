
/** Base class of all interpreters */
abstract class InterpreterBase {
    protected PureList<Binding> env;
    protected EvaluationType type;
    public InterpreterBase(PureList<Binding> env, EvaluationType type) { this.env = env; this.type = type; }
    public PureList<Binding> env() { return this.env; }
    public EvaluationType type() { return this.type; }
}



/** Evaluation Exception */
@SuppressWarnings("serial")
class EvalException extends RuntimeException {
    EvalException(String msg) { super(msg); }
}


