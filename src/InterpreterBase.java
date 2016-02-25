
/** Base class of all interpreters */
abstract class InterpreterBase {
    protected PureList<Binding> env;
    protected EvaluationPolicy ep;
    public InterpreterBase(PureList<Binding> env, EvaluationPolicy ep) {
        this.env = env;
        this.ep = ep;
    }
    public PureList<Binding> env() { return this.env; }
    public void setEnv(PureList<Binding> env) { this.env = env; }
    public EvaluationPolicy ep() { return this.ep; }
}


/** Evaluation Exception */
@SuppressWarnings("serial")
class EvalException extends RuntimeException {
    EvalException(String msg) { super(msg); }
}


/** Syntax Exception */
@SuppressWarnings("serial")
class SyntaxException extends RuntimeException {
    SyntaxException(String msg) { super(msg); }
}


/** Range Exception */
@SuppressWarnings("serial")
class RangeException extends RuntimeException {
    RangeException() { super("Maximun call stack size exceeded"); }
}


