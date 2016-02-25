
/** interprets Jam primitive functions to a JamVal */
public class PrimFunInterpreter extends InterpreterBase implements PrimFunVisitor<JamVal> {
    
    private AST [] args;

    /** Constructor */
    public PrimFunInterpreter(PureList<Binding> env, AST [] args, EvaluationPolicy ep) {
        super(env, ep);
        this.args = args;
    }

    /** Interprets 'function?' */
    @Override
    public JamVal forFunctionPPrim() {
        if (this.args.length == 1) {
            return BoolConstant.toBoolConstant(this.args[0].accept(new ASTInterpreter(this.env, this.ep)) instanceof JamFun);
        }
        throw new EvalException("'function?' takes exactly one argument");
    }

    /** Interprets 'number?' */
    @Override
    public JamVal forNumberPPrim() {
        if (this.args.length == 1) {
            return BoolConstant.toBoolConstant(this.args[0].accept(new ASTInterpreter(this.env, this.ep)) instanceof IntConstant);
        }
        throw new EvalException("'number?' takes exactly one argument");
    }

    /** Interprets 'list?' */
    @Override
    public JamVal forListPPrim() {
        if (this.args.length == 1) {
            return BoolConstant.toBoolConstant(this.args[0].accept(new ASTInterpreter(this.env, this.ep)) instanceof JamList);
        }
        throw new EvalException("'list?' takes exactly one argument");
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

    /** Interprets 'arity' */
    @Override
    public JamVal forArityPrim() {
        if (this.args.length == 1) {
            JamVal val = this.args[0].accept(new ASTInterpreter(this.env, this.ep));
            if (val instanceof JamClosure) {
                return new IntConstant(((JamClosure)val).body().vars().length);
            }
            else if (val instanceof ConsPrim) {
                return new IntConstant(2);
            }
            else if (val instanceof PrimFun) {
                return new IntConstant(1);
            }
            throw new EvalException("The argument of 'arity?' should be a function");
        }
        throw new EvalException("'arity?' takes exactly one argument");
    }

    /** Interprets 'cons' */
    @Override
    public JamVal forConsPrim() {
        if (this.args.length == 2) {
//            JamVal first = this.args[0].accept(new ASTInterpreter(this.env, this.ep));
//            JamVal second = this.args[1].accept(new ASTInterpreter(this.env, this.ep));
//            if (second instanceof JamList) {
//                return new JamCons(first, (JamList)second);
//            }
//            throw new EvalException("Invalid argument for 'cons'");
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
