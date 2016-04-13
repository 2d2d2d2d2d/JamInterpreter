import java.io.*;
import java.util.*;

/** Each parser object in this class contains an embedded lexer which contains an embedded input stream.  The
  * class include a parse() method that will translate the program text in the input stream to the corresponding
  * AST assuming that the program text forms a syntactically valid Jam program.
  */
class Parser {
    
    /** Embedded lexer */
    private Lexer in;
    
    /** Constructor */
    Parser(Lexer i) { in = i; initParser(); }

    /** Constructor */
    Parser(Reader inputStream) { this(new Lexer(inputStream)); }

    /** Constructor */
    Parser(String fileName) throws IOException { this(new FileReader(fileName)); }
    
    /** Returns the embedded lexer */
    Lexer lexer() { return in; }
    
    /** Initialize the parser (not needed at this time) */
    private void initParser() {}
    
    /** Public method used for parsing Jam tokens
     *  Raises ParseException if there are syntax errors
     */
    public AST parse() throws ParseException {
        AST exp = parseExp(in.readToken());
        Token token = in.peek();
        if (token != null)
            error(token, "redundant token after the expression");
        return exp;
    }
    
    /** Private method for parsing Exp
     *  Exp ::= Term { Binop Term }
     *        | if Exp then Exp else Exp
     *        | let Def+ in Exp
     *        | letrec MapDef+ in Exp
     *        | map IdList to Exp
     *        | Block
     */
    private AST parseExp(Token token) throws ParseException {
        /** if Exp then Exp else Exp */
        if (token == Lexer.IF) {
            return parseIf(token);
        }
        
        /** let Def+ in Exp */
        if (token == Lexer.LET) {
            return parseLet(token);
        }
        
        /** letrec MapDef+ in Exp */
        if (token == Lexer.LETREC) {
            return parseLetRec(token);
        }
        
        /** map IdList to Exp */
        if (token == Lexer.MAP) {
            return parseMap(token);
        }
        
        /** Block */
        if (token == LeftBrace.ONLY) {
            return parseBlock(token);
        }
        
        /** Term { Binop Term } */
        return parseBinOpApp(token);
    }
    
    /** Private method for parsing
     *  if Exp then Exp else Exp
     */
    private AST parseIf(Token token) throws ParseException {
        AST exp1 = parseExp(in.readToken());
        Token word_then = in.readToken();
        if (word_then == Lexer.THEN) {
            AST exp2 = parseExp(in.readToken());
            Token word_else = in.readToken();
            if (word_else == Lexer.ELSE) {
                AST exp3 = parseExp(in.readToken());
                return new If(exp1, exp2, exp3);
            }
            else {
                error(word_else, "invalid if-then-else expression, missing keyword 'else'");
            }
        }
        else {
            error(word_then, "invalid if-then-else expression, missing keyword 'then'");
        }
        return null;
    }
    
    /** Private method for parsing
     *  let Def+ in Exp
     */
    private AST parseLet(Token token) throws ParseException {
        List<Def> defs_list = new ArrayList<Def>();
        defs_list.add(parseDef(in.readToken()));
        while (in.peek() != Lexer.IN)
        {
            defs_list.add(parseDef(in.readToken()));
        }
        Token word_in = in.readToken();
        if (word_in == Lexer.IN) {
            AST exp = parseExp(in.readToken());
            return new Let(defs_list.toArray(new Def[0]), exp);
        }
        else {
            error(word_in, "invalid let-in expression, missing keyword 'in'");
        }
        return null;
    }
    
    /** Private method for parsing
     *  letrec MapDef+ in Exp
     */
    private AST parseLetRec(Token token) throws ParseException {
        List<Def> defs_list = new ArrayList<Def>();
        defs_list.add(parseMapDef(in.readToken()));
        while (in.peek() != Lexer.IN)
        {
            defs_list.add(parseMapDef(in.readToken()));
        }
        Token word_in = in.readToken();
        if (word_in == Lexer.IN) {
            AST exp = parseExp(in.readToken());
            return new LetRec(defs_list.toArray(new Def[0]), exp);
        }
        else {
            error(word_in, "invalid let-in expression, missing keyword 'in'");
        }
        return null;
    }

    /** Private method for parsing
     *  map IdList to Exp
     */
    private AST parseMap(Token token) throws ParseException {
        Variable[] vars = parseIdList();
        Token next = in.readToken();
        if (next == Lexer.TO) {
            AST exp = parseExp(in.readToken());
            return new Map(vars, exp);
        }
        else {
            error(next, "invalid map-to expression, missing keyword 'to'");
        }
        return null;
    }

    /** Private method for parsing Block
     *  Block         ::= "{" StatementList "}"
     *  StatementList ::= Exp | Exp ; StatementList
     */
    private AST parseBlock(Token token) throws ParseException {
        if (in.peek() == RightBrace.ONLY) error(in.peek(), "empty Block");
        
        List<AST> exps_list = new ArrayList<AST>();
        exps_list.add(parseExp(in.readToken()));
        while (in.peek() == SemiColon.ONLY)
        {
            in.readToken();
            exps_list.add(parseExp(in.readToken()));
        }
        Token next = in.readToken();
        if (next == RightBrace.ONLY) {
            return new Block(exps_list.toArray(new AST[0]));
        }
        error(next, "unmatched brace of Block");
        return null;
    }

    /** Private method for parsing
     *  Term { Binop Term }
     */
    private AST parseBinOpApp(Token token) throws ParseException {
        AST term = parseTerm(token);
        while (in.peek() instanceof OpToken)
        {
            OpToken op = (OpToken)in.readToken();
            if (! op.isBinOp()) error(op, "requires binary operator");
            AST exp = parseTerm(in.readToken());
            
            //////
            /** expand & and | in terms of if-then-else */
            if (op.toBinOp() instanceof OpAnd)
                term = new If(term, exp, BoolConstant.FALSE);
            else if (op.toBinOp() instanceof OpOr)
                term = new If(term, BoolConstant.TRUE, exp);
            else
                /** normal behavior */
                term = new BinOpApp(op.toBinOp(), term, exp);
        }
        /*
        if (in.peek() instanceof OpToken && ((OpToken)in.peek()).isBinOp()) {
            OpToken op = (OpToken)in.readToken();
            AST exp = parseTerm(in.readToken());
            return new BinOpApp(op.toBinOp(), term, exp);
        }*/
        return term;
    }
    
    /** Private method for parsing Term
     *  Term ::= Unop Term
     *         | Factor { ( ExpList ) }
     *         | Null
     *         | Int
     *         | Bool
     */
    private AST parseTerm(Token token) throws ParseException {
        /** Unop Term */
        if (token instanceof OpToken) {
            OpToken op = (OpToken) token;
            if (! op.isUnOp()) error(op,"requires unary operator");
                return new UnOpApp(op.toUnOp(), parseTerm(in.readToken()));
        }
        
        /** Null | Int | Bool */
        if (token instanceof Constant) return (Constant) token;
        
        /** Factor { ( ExpList ) } */
        AST factor = parseFactor(token);
        Token next = in.peek();
        if (next == LeftParen.ONLY) {
            in.readToken();  // remove next from input stream
            AST[] exps = parseExpList();  // including closing paren
            return new App(factor,exps);
        }
        return factor;
    }
    
    /** Private method for parsing Def
     *  Def ::= Id := Exp ;
     */
    private Def parseDef(Token token) throws ParseException {
        if (token instanceof Variable) {
            Token next = in.readToken();
            if (next == Lexer.BIND) {
                AST exp = parseExp(in.readToken());
                Token word_semicolon = in.readToken();
                if (word_semicolon == SemiColon.ONLY) {
                    return new Def((Variable)token, exp);
                }
                else {
                    error(word_semicolon, "invalid defination, missing keyword ';' at the end");
                }
            }
            else {
                error(next, "invalid defination, missing keyword ':='");
            }
        }
        else {
            error(token, "invalid defination, unexpected Id");
        }
        return null;
    }
    
    /** Private method for parsing MapDef
     *  MapDef ::= Id := Map ;
     */
    private Def parseMapDef(Token token) throws ParseException {
        if (token instanceof Variable) {
            Token next = in.readToken();
            if (next == Lexer.BIND) {
                AST map = parseMap(in.readToken());
                Token word_semicolon = in.readToken();
                if (word_semicolon == SemiColon.ONLY) {
                    return new Def((Variable)token, map);
                }
                else {
                    error(word_semicolon, "invalid map defination, missing keyword ';' at the end");
                }
            }
            else {
                error(next, "invalid map defination, missing keyword ':='");
            }
        }
        else {
            error(token, "invalid map defination, unexpected Id");
        }
        return null;
    }
    
    /** Private method for parsing Factor
     *  Factor ::= ( Exp ) | Prim | Id
     */
    private AST parseFactor(Token token) throws ParseException {
        if (token == LeftParen.ONLY) {
            AST exp = parseExp(in.readToken());
            Token next = in.readToken();
            if(next == RightParen.ONLY) {
                return exp;
            }
            else {
                error(next, "unmatched parentheses in Factor");
                return null;
            }
        }
        
        if (token instanceof PrimFun) {
            return (PrimFun)token;
        }
        
        if (token instanceof Variable){
            return (Variable)token;
        }
        
        error(token, "invalid Factor");
        return null;
    }
    
    /** Private method for parsing IdList
     *  IdList     ::= { PropIdList }
     *  PropIdList ::= Id | Id , PropIdList
     */
    private Variable[] parseIdList() throws ParseException {
        Token token = in.peek();
        List<Variable> vars_list = new ArrayList<Variable>();
        
        if (token instanceof Variable) {
            token = in.readToken();
            vars_list.add((Variable)token);
            while (in.peek() == Comma.ONLY)
            {
                in.readToken();
                Token next = in.readToken();
                if (next instanceof Variable) {
                    vars_list.add((Variable)next);
                }
                else {
                    error(next, "invalid Id after the comma");
                }
            }
            return vars_list.toArray(new Variable[0]);
        }
        
        return new Variable[0];
    }
    
    /** Private method for parsing IdList
     *  ExpList     ::= { PropExpList }
     *  PropExpList ::= Exp | Exp , PropExpList
     */
    private AST[] parseExpList() throws ParseException {
        Token token = in.readToken();
        List<AST> exps_list = new ArrayList<AST>();
        
        if (token == RightParen.ONLY) {
            return new AST[0];
        }
        
        AST exp = parseExp(token);
        exps_list.add(exp);
        while (in.peek() == Comma.ONLY)
        {
            in.readToken();
            exps_list.add(parseExp(in.readToken()));
        }
        
        Token next = in.readToken();
        if (next != RightParen.ONLY) {
            error(next, "unmatched parentheses in Term");
        }
        return exps_list.toArray(new AST[0]);
    }
    
    /** Private method for processing syntax errors
     *  Raises ParseException if there is any syntax error
     */
    private void error (Token token, String msg) throws ParseException
    {
        throw new ParseException("Syntax error at '" + ((token != null)? token.toString() : "EOF") + "': " + msg);
    }
}

