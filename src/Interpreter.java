import java.io.IOException;
import java.io.Reader;

class Interpreter {
    
    private Parser parser;
    
    Interpreter(String fileName) throws IOException { this(new Parser(fileName)); }
   
    Interpreter(Reader reader) { this(new Parser(reader)); }
   
    Interpreter(Parser p) { parser = p; }

    public JamVal callByValue() {
        return null;
    }
   
    public JamVal callByName()  {
        return null;
    }
   
    public JamVal callByNeed()  {
        return null;
    }
}



class EvalException extends RuntimeException {
    EvalException(String msg) { super(msg); }
}



class InterpreterVisitor implements ASTVisitor<JamVal> {

    @Override
    public JamVal forBoolConstant(BoolConstant b) {
        return b;
    }

    @Override
    public JamVal forIntConstant(IntConstant i) {
        return i;
    }

    @Override
    public JamVal forNullConstant(NullConstant n) {
        return JamEmpty.ONLY;
    }

    @Override
    public JamVal forVariable(Variable v) {
        // TODO Auto-generated method stub
        return null;//new Cons<AST>();
    }

    @Override
    public JamVal forPrimFun(PrimFun f) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JamVal forUnOpApp(UnOpApp u) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JamVal forBinOpApp(BinOpApp b) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JamVal forApp(App a) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JamVal forMap(Map m) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JamVal forIf(If i) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JamVal forLet(Let l) {
        // TODO Auto-generated method stub
        return null;
    }
    
}


