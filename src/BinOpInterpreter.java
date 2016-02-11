
public class BinOpInterpreter extends InterpreterBase implements BinOpVisitor<JamVal> {
    
    private AST arg1;
    private AST arg2;
    private JamVal arg1_val;
    private JamVal arg2_val;
    
    public BinOpInterpreter(PureList<Binding> env, AST arg1, AST arg2) {
        super(env);
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.arg1_val = arg1.accept(new ASTInterpreter(env));
        this.arg2_val = arg2.accept(new ASTInterpreter(env));
    }
    
    @Override
    public JamVal forBinOpPlus(BinOpPlus op) {
        if (this.arg1_val instanceof IntConstant && this.arg2_val instanceof IntConstant) {
            return new IntConstant(((IntConstant)this.arg1_val).value() + ((IntConstant)this.arg2_val).value());
        }
        throw new EvalException(getExceptionStr(op));
    }

    @Override
    public JamVal forBinOpMinus(BinOpMinus op) {
        if (this.arg1_val instanceof IntConstant && this.arg2_val instanceof IntConstant) {
            return new IntConstant(((IntConstant)this.arg1_val).value() - ((IntConstant)this.arg2_val).value());
        }
        throw new EvalException(getExceptionStr(op));
    }

    @Override
    public JamVal forOpTimes(OpTimes op) {
        if (this.arg1_val instanceof IntConstant && this.arg2_val instanceof IntConstant) {
            return new IntConstant(((IntConstant)this.arg1_val).value() * ((IntConstant)this.arg2_val).value());
        }
        throw new EvalException(getExceptionStr(op));
    }

    @Override
    public JamVal forOpDivide(OpDivide op) {
        if (this.arg1_val instanceof IntConstant && this.arg2_val instanceof IntConstant) {
            return new IntConstant(((IntConstant)this.arg1_val).value() / ((IntConstant)this.arg2_val).value());
        }
        throw new EvalException(getExceptionStr(op));
    }

    @Override
    public JamVal forOpEquals(OpEquals op) {
        return BoolConstant.toBoolConstant(this.arg1_val.equals(this.arg2_val));
    }

    @Override
    public JamVal forOpNotEquals(OpNotEquals op) {
        return BoolConstant.toBoolConstant(! this.arg1_val.equals(this.arg2_val));
    }

    @Override
    public JamVal forOpLessThan(OpLessThan op) {
        if (this.arg1_val instanceof IntConstant && this.arg2_val instanceof IntConstant) {
            return BoolConstant.toBoolConstant(((IntConstant)this.arg1_val).value() < ((IntConstant)this.arg2_val).value());
        }
        throw new EvalException(getExceptionStr(op));
    }

    @Override
    public JamVal forOpGreaterThan(OpGreaterThan op) {
        if (this.arg1_val instanceof IntConstant && this.arg2_val instanceof IntConstant) {
            return BoolConstant.toBoolConstant(((IntConstant)this.arg1_val).value() > ((IntConstant)this.arg2_val).value());
        }
        throw new EvalException(getExceptionStr(op));
    }

    @Override
    public JamVal forOpLessThanEquals(OpLessThanEquals op) {
        if (this.arg1_val instanceof IntConstant && this.arg2_val instanceof IntConstant) {
            return BoolConstant.toBoolConstant(((IntConstant)this.arg1_val).value() <= ((IntConstant)this.arg2_val).value());
        }
        throw new EvalException(getExceptionStr(op));
    }

    @Override
    public JamVal forOpGreaterThanEquals(OpGreaterThanEquals op) {
        if (this.arg1_val instanceof IntConstant && this.arg2_val instanceof IntConstant) {
            return BoolConstant.toBoolConstant(((IntConstant)this.arg1_val).value() >= ((IntConstant)this.arg2_val).value());
        }
        throw new EvalException(getExceptionStr(op));
    }

    @Override
    public JamVal forOpAnd(OpAnd op) {
        if (this.arg1_val instanceof BoolConstant && this.arg2_val instanceof BoolConstant) {
            return BoolConstant.toBoolConstant(((BoolConstant)this.arg1_val).value() && ((BoolConstant)this.arg2_val).value());
        }
        throw new EvalException(getExceptionStr(op));
    }

    @Override
    public JamVal forOpOr(OpOr op) {
        if (this.arg1_val instanceof BoolConstant && this.arg2_val instanceof BoolConstant) {
            return BoolConstant.toBoolConstant(((BoolConstant)this.arg1_val).value() || ((BoolConstant)this.arg2_val).value());
        }
        throw new EvalException(getExceptionStr(op));
    }
    
    private String getExceptionStr(BinOp op) {
        return "Failed to apply '" + op.toString() + "' to '" + this.arg1.toString() + "' and '" + this.arg2.toString() + "'";
    }

}


