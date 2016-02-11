
public class PrimFunInterpreter extends InterpreterBase implements PrimFunVisitor<JamVal> {
    
    private AST [] args;
    
    public PrimFunInterpreter(PureList<Binding> env, AST [] args) {
        super(env);
        this.args = args;
    }
    
    @Override
    public JamVal forFunctionPPrim() {
        if (this.args.length == 1) {
            return BoolConstant.toBoolConstant(this.args[0].accept(new ASTInterpreter(this.env)) instanceof JamFun);
        }
        throw new EvalException("'function?' takes exactly one argument");
    }

    @Override
    public JamVal forNumberPPrim() {
        if (this.args.length == 1) {
            return BoolConstant.toBoolConstant(this.args[0].accept(new ASTInterpreter(this.env)) instanceof IntConstant);
        }
        throw new EvalException("'number?' takes exactly one argument");
    }

    @Override
    public JamVal forListPPrim() {
        if (this.args.length == 1) {
            return BoolConstant.toBoolConstant(this.args[0].accept(new ASTInterpreter(this.env)) instanceof JamList);
        }
        throw new EvalException("'list?' takes exactly one argument");
    }

    @Override
    public JamVal forConsPPrim() {
        if (this.args.length == 1) {
            return BoolConstant.toBoolConstant(this.args[0].accept(new ASTInterpreter(this.env)) instanceof JamCons);
        }
        throw new EvalException("'cons?' takes exactly one argument");
    }

    @Override
    public JamVal forNullPPrim() {
        if (this.args.length == 1) {
            return BoolConstant.toBoolConstant(this.args[0].accept(new ASTInterpreter(this.env)) instanceof JamEmpty);
        }
        throw new EvalException("'null?' takes exactly one argument");
    }

    @Override
    public JamVal forArityPrim() {
        if (this.args.length == 1) {
            JamVal val = this.args[0].accept(new ASTInterpreter(this.env));
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

    @Override
    public JamVal forConsPrim() {
        if (this.args.length == 2) {
            JamVal first = this.args[0].accept(new ASTInterpreter(this.env));
            JamVal second = this.args[1].accept(new ASTInterpreter(this.env));
            if (second instanceof JamList) {
                return new JamCons(first, (JamList)second);
            }
            throw new EvalException("Invalid argument for 'cons");
        }
        throw new EvalException("'cons' takes exactly two argument");
    }

    @Override
    public JamVal forFirstPrim() {
        if (this.args.length == 1) {
            JamVal val = this.args[0].accept(new ASTInterpreter(this.env));
            if (val instanceof JamCons)
                return ((JamCons)val).first();
            throw new EvalException("The argument of 'first' should be a non-empty list");
        }
        throw new EvalException("'first' takes exactly one argument");
    }

    @Override
    public JamVal forRestPrim() {
        if (this.args.length == 1) {
            JamVal val = this.args[0].accept(new ASTInterpreter(this.env));
            if (val instanceof JamCons)
                return ((JamCons)val).rest();
            throw new EvalException("The argument of 'rest' should be a non-empty list");
        }
        throw new EvalException("'rest' takes exactly one argument");
    }

}
