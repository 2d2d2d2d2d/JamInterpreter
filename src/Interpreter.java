import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

class Interpreter {
    
    public static void main(String [] args) {
        String program = "number?(hello)";
        Interpreter interp = new Interpreter(new StringReader(program));
        JamVal val = interp.callByValue();
        System.out.println(val);
    }
    
    private Parser parser;
    
    private AST ast;
    
    Interpreter(String fileName) throws IOException { this(new Parser(fileName)); }
   
    Interpreter(Reader reader) { this(new Parser(reader)); }
   
    Interpreter(Parser p) { parser = p; ast = p.parse(); }

    public JamVal callByValue() {
        System.out.println(ast.toString());
        ASTInterpreter astinterp = new ASTInterpreter(new Empty<Binding>());
        JamVal val = ast.accept(astinterp);
        return val;
    }
   
    public JamVal callByName()  {
        return callByValue();
    }
   
    public JamVal callByNeed()  {
        return callByValue();
    }
}


class EvalException extends RuntimeException {
    EvalException(String msg) { super(msg); }
}


abstract class InterpreterBase {
    protected PureList<Binding> env;
    public InterpreterBase(PureList<Binding> env) { this.env = env; }
}


class CallByValueBinding extends Binding {

    CallByValueBinding(Variable var, AST ast, ASTInterpreter astInterpreter) {
        super(var, ast.accept(astInterpreter));
    }
    
}

