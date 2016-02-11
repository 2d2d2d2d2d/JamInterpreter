
/** Interprets binary operator App to a JamVal */
public class BinOpInterpreter extends InterpreterBase implements BinOpVisitor<JamVal> {
    
    private AST arg1;
    private AST arg2;
    private JamVal arg1_val;
    private JamVal arg2_val;

    /** Constructor */
    public BinOpInterpreter(PureList<Binding> env, AST arg1, AST arg2, EvaluationType type) {
        super(env, type);
        this.arg1 = arg1;
        this.arg2 = arg2;
    }

    /** Interprets '+' */
    @Override
    public JamVal forBinOpPlus(BinOpPlus op) {
        this.arg1_val = this.arg1.accept(new ASTInterpreter(env, type));
        this.arg2_val = this.arg2.accept(new ASTInterpreter(env, type));
        if (this.arg1_val instanceof IntConstant && this.arg2_val instanceof IntConstant) {
            return new IntConstant(((IntConstant)this.arg1_val).value() + ((IntConstant)this.arg2_val).value());
        }
        throw new EvalException(getExceptionStr(op));
    }

    /** Interprets '-' */
    @Override
    public JamVal forBinOpMinus(BinOpMinus op) {
        this.arg1_val = this.arg1.accept(new ASTInterpreter(env, type));
        this.arg2_val = this.arg2.accept(new ASTInterpreter(env, type));
        if (this.arg1_val instanceof IntConstant && this.arg2_val instanceof IntConstant) {
            return new IntConstant(((IntConstant)this.arg1_val).value() - ((IntConstant)this.arg2_val).value());
        }
        throw new EvalException(getExceptionStr(op));
    }

    /** Interprets '*' */
    @Override
    public JamVal forOpTimes(OpTimes op) {
        this.arg1_val = this.arg1.accept(new ASTInterpreter(env, type));
        this.arg2_val = this.arg2.accept(new ASTInterpreter(env, type));
        if (this.arg1_val instanceof IntConstant && this.arg2_val instanceof IntConstant) {
            return new IntConstant(((IntConstant)this.arg1_val).value() * ((IntConstant)this.arg2_val).value());
        }
        throw new EvalException(getExceptionStr(op));
    }

    /** Interprets '/' */
    @Override
    public JamVal forOpDivide(OpDivide op) {
        this.arg1_val = this.arg1.accept(new ASTInterpreter(env, type));
        this.arg2_val = this.arg2.accept(new ASTInterpreter(env, type));
        if (this.arg1_val instanceof IntConstant && this.arg2_val instanceof IntConstant) {
            return new IntConstant(((IntConstant)this.arg1_val).value() / ((IntConstant)this.arg2_val).value());
        }
        throw new EvalException(getExceptionStr(op));
    }

    /** Interprets '=' */
    @Override
    public JamVal forOpEquals(OpEquals op) {
        this.arg1_val = this.arg1.accept(new ASTInterpreter(env, type));
        this.arg2_val = this.arg2.accept(new ASTInterpreter(env, type));
        return BoolConstant.toBoolConstant(this.arg1_val.equals(this.arg2_val));
    }

    /** Interprets '!=' */
    @Override
    public JamVal forOpNotEquals(OpNotEquals op) {
        this.arg1_val = this.arg1.accept(new ASTInterpreter(env, type));
        this.arg2_val = this.arg2.accept(new ASTInterpreter(env, type));
        return BoolConstant.toBoolConstant(! this.arg1_val.equals(this.arg2_val));
    }

    /** Interprets '<' */
    @Override
    public JamVal forOpLessThan(OpLessThan op) {
        this.arg1_val = this.arg1.accept(new ASTInterpreter(env, type));
        this.arg2_val = this.arg2.accept(new ASTInterpreter(env, type));
        if (this.arg1_val instanceof IntConstant && this.arg2_val instanceof IntConstant) {
            return BoolConstant.toBoolConstant(((IntConstant)this.arg1_val).value() < ((IntConstant)this.arg2_val).value());
        }
        throw new EvalException(getExceptionStr(op));
    }

    /** Interprets '>' */
    @Override
    public JamVal forOpGreaterThan(OpGreaterThan op) {
        this.arg1_val = this.arg1.accept(new ASTInterpreter(env, type));
        this.arg2_val = this.arg2.accept(new ASTInterpreter(env, type));
        if (this.arg1_val instanceof IntConstant && this.arg2_val instanceof IntConstant) {
            return BoolConstant.toBoolConstant(((IntConstant)this.arg1_val).value() > ((IntConstant)this.arg2_val).value());
        }
        throw new EvalException(getExceptionStr(op));
    }

    /** Interprets '<=' */
    @Override
    public JamVal forOpLessThanEquals(OpLessThanEquals op) {
        this.arg1_val = this.arg1.accept(new ASTInterpreter(env, type));
        this.arg2_val = this.arg2.accept(new ASTInterpreter(env, type));
        if (this.arg1_val instanceof IntConstant && this.arg2_val instanceof IntConstant) {
            return BoolConstant.toBoolConstant(((IntConstant)this.arg1_val).value() <= ((IntConstant)this.arg2_val).value());
        }
        throw new EvalException(getExceptionStr(op));
    }

    /** Interprets '>=' */
    @Override
    public JamVal forOpGreaterThanEquals(OpGreaterThanEquals op) {
        this.arg1_val = this.arg1.accept(new ASTInterpreter(env, type));
        this.arg2_val = this.arg2.accept(new ASTInterpreter(env, type));
        if (this.arg1_val instanceof IntConstant && this.arg2_val instanceof IntConstant) {
            return BoolConstant.toBoolConstant(((IntConstant)this.arg1_val).value() >= ((IntConstant)this.arg2_val).value());
        }
        throw new EvalException(getExceptionStr(op));
    }

    /** Interprets '&' */
    @Override
    public JamVal forOpAnd(OpAnd op) {
        this.arg1_val = this.arg1.accept(new ASTInterpreter(env, type));
        if (this.arg1_val == BoolConstant.FALSE) {
            return BoolConstant.FALSE;
        }
        else if(this.arg1_val == BoolConstant.TRUE) {
            this.arg2_val = this.arg2.accept(new ASTInterpreter(env, type));
            if (this.arg2_val == BoolConstant.TRUE) {
                return BoolConstant.TRUE;
            }
            else if (this.arg2_val == BoolConstant.FALSE) {
                return BoolConstant.FALSE;
            }
        }
        throw new EvalException(getExceptionStr(op));
    }

    /** Interprets '|' */
    @Override
    public JamVal forOpOr(OpOr op) {
        this.arg1_val = this.arg1.accept(new ASTInterpreter(env, type));
        if (this.arg1_val == BoolConstant.TRUE) {
            return BoolConstant.TRUE;
        }
        else if(this.arg1_val == BoolConstant.FALSE) {
            this.arg2_val = this.arg2.accept(new ASTInterpreter(env, type));
            if (this.arg2_val == BoolConstant.TRUE) {
                return BoolConstant.TRUE;
            }
            else if (this.arg2_val == BoolConstant.FALSE) {
                return BoolConstant.FALSE;
            }
        }
        throw new EvalException(getExceptionStr(op));
    }

    /** Generate exception message */
    private String getExceptionStr(BinOp op) {
        return "Failed to apply '" + op.toString() + "' to '" + this.arg1.toString() + "' and '" + this.arg2.toString() + "'";
    }

}


