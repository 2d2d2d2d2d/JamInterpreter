
/** Interprets '|' */
public class UnOpInterpreter extends InterpreterBase implements UnOpVisitor<JamVal> {
    private AST arg;
    private JamVal arg_val;

    /** Constructor */
    public UnOpInterpreter(PureList<Binding> env, AST arg, EvaluationPolicy ep) {
        super(env, ep);
        this.arg = arg;
        this.arg_val = arg.accept(new ASTInterpreter(env, ep));
    }

    /** Interprets unary operator '+' */
    @Override
    public JamVal forUnOpPlus(UnOpPlus op) {
        if (this.arg_val instanceof IntConstant) {
            return this.arg_val;
        }
        throw new EvalException(getExceptionStr(op, "int"));
    }

    /** Interprets unary operator '-' */
    @Override
    public JamVal forUnOpMinus(UnOpMinus op) {
        if (this.arg_val instanceof IntConstant) {
            return new IntConstant(((IntConstant)this.arg_val).value() * (-1));
        }
        throw new EvalException(getExceptionStr(op, "int"));
    }

    /** Interprets unary operator '~' */
    @Override
    public JamVal forOpTilde(OpTilde op) {
        if (this.arg_val instanceof BoolConstant) {
            return ((BoolConstant)this.arg_val).not();
        }
        throw new EvalException(getExceptionStr(op, "bool"));
    }

    /** Generate exception message */
    private String getExceptionStr(UnOp op, String expect) {
        return "Failed to apply '" + op.toString() + "' to '" + this.arg.toString() + "' "
             + "'" + expect + "' type of operand expected";
    }
}
