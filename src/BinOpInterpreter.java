
/** Interprets binary operator App to a JamVal */
public class BinOpInterpreter extends InterpreterBase implements BinOpVisitor<JamVal> {

    /** AST of the left operand */
    private AST arg1;

    /** AST of the right operand */
    private AST arg2;

    /** Jam value of the left operand */
    private JamVal arg1_val;

    /** Jam value of the right operand */
    private JamVal arg2_val;

    /** Constructor */
    public BinOpInterpreter(PureList<Binding> env, AST arg1, AST arg2, EvaluationPolicy ep) {
        super(env, ep);
        this.arg1 = arg1;
        this.arg2 = arg2;
    }

    /** Interprets '+' */
    @Override
    public JamVal forBinOpPlus(BinOpPlus op) {
        this.arg1_val = this.arg1.accept(new ASTInterpreter(env, ep));
        this.arg2_val = this.arg2.accept(new ASTInterpreter(env, ep));
        if (this.arg1_val instanceof IntConstant && this.arg2_val instanceof IntConstant) {
            return new IntConstant(((IntConstant)this.arg1_val).value() + ((IntConstant)this.arg2_val).value());
        }
        throw new EvalException(getExceptionStr(op, "int"));
    }

    /** Interprets '-' */
    @Override
    public JamVal forBinOpMinus(BinOpMinus op) {
        this.arg1_val = this.arg1.accept(new ASTInterpreter(env, ep));
        this.arg2_val = this.arg2.accept(new ASTInterpreter(env, ep));
        if (this.arg1_val instanceof IntConstant && this.arg2_val instanceof IntConstant) {
            return new IntConstant(((IntConstant)this.arg1_val).value() - ((IntConstant)this.arg2_val).value());
        }
        throw new EvalException(getExceptionStr(op, "int"));
    }

    /** Interprets '*' */
    @Override
    public JamVal forOpTimes(OpTimes op) {
        this.arg1_val = this.arg1.accept(new ASTInterpreter(env, ep));
        this.arg2_val = this.arg2.accept(new ASTInterpreter(env, ep));
        if (this.arg1_val instanceof IntConstant && this.arg2_val instanceof IntConstant) {
            return new IntConstant(((IntConstant)this.arg1_val).value() * ((IntConstant)this.arg2_val).value());
        }
        throw new EvalException(getExceptionStr(op, "int"));
    }

    /** Interprets '/' */
    @Override
    public JamVal forOpDivide(OpDivide op) {
        this.arg1_val = this.arg1.accept(new ASTInterpreter(env, ep));
        this.arg2_val = this.arg2.accept(new ASTInterpreter(env, ep));
        if (this.arg1_val instanceof IntConstant && this.arg2_val instanceof IntConstant) {
            if (((IntConstant)this.arg2_val).value() != 0)
                return new IntConstant(((IntConstant)this.arg1_val).value() / ((IntConstant)this.arg2_val).value());
            else
                throw new EvalException("Cannot divide by zero");
        }
        throw new EvalException(getExceptionStr(op, "int"));
    }

    /** Interprets '=' */
    @Override
    public JamVal forOpEquals(OpEquals op) {
        this.arg1_val = this.arg1.accept(new ASTInterpreter(env, ep));
        this.arg2_val = this.arg2.accept(new ASTInterpreter(env, ep));
        return BoolConstant.toBoolConstant(this.arg1_val.equals(this.arg2_val));
    }

    /** Interprets '!=' */
    @Override
    public JamVal forOpNotEquals(OpNotEquals op) {
        this.arg1_val = this.arg1.accept(new ASTInterpreter(env, ep));
        this.arg2_val = this.arg2.accept(new ASTInterpreter(env, ep));
        return BoolConstant.toBoolConstant(! this.arg1_val.equals(this.arg2_val));
    }

    /** Interprets '<' */
    @Override
    public JamVal forOpLessThan(OpLessThan op) {
        this.arg1_val = this.arg1.accept(new ASTInterpreter(env, ep));
        this.arg2_val = this.arg2.accept(new ASTInterpreter(env, ep));
        if (this.arg1_val instanceof IntConstant && this.arg2_val instanceof IntConstant) {
            return BoolConstant.toBoolConstant(((IntConstant)this.arg1_val).value() < ((IntConstant)this.arg2_val).value());
        }
        throw new EvalException(getExceptionStr(op, "int"));
    }

    /** Interprets '>' */
    @Override
    public JamVal forOpGreaterThan(OpGreaterThan op) {
        this.arg1_val = this.arg1.accept(new ASTInterpreter(env, ep));
        this.arg2_val = this.arg2.accept(new ASTInterpreter(env, ep));
        if (this.arg1_val instanceof IntConstant && this.arg2_val instanceof IntConstant) {
            return BoolConstant.toBoolConstant(((IntConstant)this.arg1_val).value() > ((IntConstant)this.arg2_val).value());
        }
        throw new EvalException(getExceptionStr(op, "int"));
    }

    /** Interprets '<=' */
    @Override
    public JamVal forOpLessThanEquals(OpLessThanEquals op) {
        this.arg1_val = this.arg1.accept(new ASTInterpreter(env, ep));
        this.arg2_val = this.arg2.accept(new ASTInterpreter(env, ep));
        if (this.arg1_val instanceof IntConstant && this.arg2_val instanceof IntConstant) {
            return BoolConstant.toBoolConstant(((IntConstant)this.arg1_val).value() <= ((IntConstant)this.arg2_val).value());
        }
        throw new EvalException(getExceptionStr(op, "int"));
    }

    /** Interprets '>=' */
    @Override
    public JamVal forOpGreaterThanEquals(OpGreaterThanEquals op) {
        this.arg1_val = this.arg1.accept(new ASTInterpreter(env, ep));
        this.arg2_val = this.arg2.accept(new ASTInterpreter(env, ep));
        if (this.arg1_val instanceof IntConstant && this.arg2_val instanceof IntConstant) {
            return BoolConstant.toBoolConstant(((IntConstant)this.arg1_val).value() >= ((IntConstant)this.arg2_val).value());
        }
        throw new EvalException(getExceptionStr(op, "int"));
    }

    /** Interprets '&' */
    @Override
    public JamVal forOpAnd(OpAnd op) {
        this.arg1_val = this.arg1.accept(new ASTInterpreter(env, ep));
        if (this.arg1_val == BoolConstant.FALSE) {
            return BoolConstant.FALSE;
        }
        else if(this.arg1_val == BoolConstant.TRUE) {
            this.arg2_val = this.arg2.accept(new ASTInterpreter(env, ep));
            if (this.arg2_val == BoolConstant.TRUE) {
                return BoolConstant.TRUE;
            }
            else if (this.arg2_val == BoolConstant.FALSE) {
                return BoolConstant.FALSE;
            }
        }
        throw new EvalException(getExceptionStr(op, "bool"));
    }

    /** Interprets '|' */
    @Override
    public JamVal forOpOr(OpOr op) {
        this.arg1_val = this.arg1.accept(new ASTInterpreter(env, ep));
        if (this.arg1_val == BoolConstant.TRUE) {
            return BoolConstant.TRUE;
        }
        else if(this.arg1_val == BoolConstant.FALSE) {
            this.arg2_val = this.arg2.accept(new ASTInterpreter(env, ep));
            if (this.arg2_val == BoolConstant.TRUE) {
                return BoolConstant.TRUE;
            }
            else if (this.arg2_val == BoolConstant.FALSE) {
                return BoolConstant.FALSE;
            }
        }
        throw new EvalException(getExceptionStr(op, "bool"));
    }

    /** Generate exception message */
    private String getExceptionStr(BinOp op, String expect) {
        return "Failed to apply '" + op.toString() + "', "// to '" + this.arg1.toString() + "' and '" + this.arg2.toString() + "', "
             + "'" + expect + "' type of operand expected";
    }

}


