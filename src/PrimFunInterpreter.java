
/** interprets Jam primitive functions to a JamVal */
public class PrimFunInterpreter extends InterpreterBase implements PrimFunVisitor<JamVal> {
    
    /** Arguments of the primitive function */
    private AST [] args;

    /** Constructor */
    public PrimFunInterpreter(PureList<Binding> env, AST [] args, EvaluationPolicy ep) {
        super(env, ep);
        this.args = args;
    }

    /** Interprets 'cons?' */
    @Override
    public JamVal forConsPPrim() {
        if (this.args.length == 1) {
            return BoolConstant.toBoolConstant(this.args[0].accept(new ASTInterpreter(this.env, this.ep)) instanceof JamCons);
        }
        throw new EvalException("'cons?' takes exactly one argument");
    }

    /** Interprets 'null?' */
    @Override
    public JamVal forNullPPrim() {
        if (this.args.length == 1) {
            return BoolConstant.toBoolConstant(this.args[0].accept(new ASTInterpreter(this.env, this.ep)) instanceof JamEmpty);
        }
        throw new EvalException("'null?' takes exactly one argument");
    }

    /** Interprets 'cons' (supports different evaluation strategies) */
    @Override
    public JamVal forConsPrim() {
        if (this.args.length == 2) {
            return LazyCons.generate(this.args[0], this.args[1], this.env, this.ep);
        }
        throw new EvalException("'cons' takes exactly two argument");
    }

    /** Interprets 'first' */
    @Override
    public JamVal forFirstPrim() {
        if (this.args.length == 1) {
            JamVal val = this.args[0].accept(new ASTInterpreter(this.env, this.ep));
            if (val instanceof JamCons)
                return ((JamCons)val).first();
            throw new EvalException("The argument of 'first' should be a non-empty list");
        }
        throw new EvalException("'first' takes exactly one argument");
    }

    /** Interprets 'rest' */
    @Override
    public JamVal forRestPrim() {
        if (this.args.length == 1) {
            JamVal val = this.args[0].accept(new ASTInterpreter(this.env, this.ep));
            if (val instanceof JamCons)
                return ((JamCons)val).rest();
            throw new EvalException("The argument of 'rest' should be a non-empty list");
        }
        throw new EvalException("'rest' takes exactly one argument");
    }

}
