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
     *  Exp ::= Term { Binop Exp }
     *        | if Exp then Exp else Exp
     *        | let Def+ in Exp
     *        | map TypedIdList to Exp
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
        
        /** map IdList to Exp */
        if (token == Lexer.MAP) {
            return parseMap(token);
        }
        
        /** Block */
        if (token == LeftBrace.ONLY) {
            return parseBlock(token);
        }
        
        /** Term { Binop Exp } */
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
        defs_list.add(parseDef());
        while (in.peek() != Lexer.IN)
        {
            defs_list.add(parseDef());
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
     *  map TypedIdList to Exp
     */
    private AST parseMap(Token token) throws ParseException {
        Variable[] vars = parseTypedIdList();
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
     *  Term { Binop Exp }
     */
    private AST parseBinOpApp(Token token) throws ParseException {
        AST term = parseTerm(token);
        while (in.peek() instanceof OpToken)
        {
            OpToken op = (OpToken)in.readToken();
            if (! op.isBinOp()) error(op, "requires binary operator");
            AST exp = parseTerm(in.readToken());
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
               | Prim ( ExpList )
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
        
        /** Null */
        if (token == Lexer.NULL) {
            Token next = in.readToken();
            if (next == Colon.ONLY) {
                Type dataType = parseType();
                //return new NullConstant(dataType);
                return new NullConstant(new ListType(dataType));
            }
            else {
                error(next, "':' expected after 'null'");
            }
        }
        
        /** Int | Bool */
        if (token instanceof Constant) {
            return (Constant) token;
        }
        
        /** Prim ( ExpList ) */
        if (token instanceof PrimFun) {
            Token next = in.peek();
            if (next == LeftParen.ONLY) {
                in.readToken();  // remove next from input stream
                AST[] exps = parseExpList();  // including closing paren
                return new App((PrimFun)token,exps);
            }
            else {
                error(next, "expected '(' after primitive function");
            }
        }
        
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
    private Def parseDef() throws ParseException {
        Token token = in.peek();
        Variable typedId = parseTypedId();
        if (typedId != null) {
            Token next = in.readToken();
            if (next == Lexer.BIND) {
                AST exp = parseExp(in.readToken());
                Token word_semicolon = in.readToken();
                if (word_semicolon == SemiColon.ONLY) {
                    return new Def(typedId, exp);
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
    
    /** Private method for parsing Factor
     *  Factor ::= ( Exp ) | Id
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
        
        if (token instanceof Variable){
            return (Variable)token;
        }
        
        error(token, "invalid Factor");
        return null;
    }
    
    /** Private method for parsing TypedIdList
     *  TypedIdList     ::= { PropTypedIdList }
     *  PropTypedIdList ::= Id | Id , PropTypedIdList
     */
    private Variable[] parseTypedIdList() throws ParseException {
        Variable typedId = parseTypedId();
        List<Variable> vars_list = new ArrayList<Variable>();
        
        if (typedId != null) {
            vars_list.add(typedId);
            while (in.peek() == Comma.ONLY)
            {
                in.readToken();
                Token next = in.peek();
                typedId = parseTypedId();
                if (typedId != null) {
                    vars_list.add(typedId);
                }
                else {
                    error(next, "invalid type after the comma");
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
    
    /**
     *  Private method for parsing TypedId
     *  TypedId           ::= Id : Type
     */
    private Variable parseTypedId() throws ParseException {
        Token token = in.peek();
        if (token instanceof Variable) {
            in.readToken();
            Token next = in.readToken();
            if (next == Colon.ONLY) {
                Type type = parseType();
                Variable typedId = (Variable) token;
                typedId.setDataType(type);
                return typedId;
            }
            else {
                error(next, "expected ':' after Id");
            }
        }
        return null;
    }
    
    /**
     *  Private method for parsing Type
     *  Type        ::= unit
     *                | int
     *                | bool
     *                | list Type
     *                | ref Type
     *                | (TypeList -> Type)
     * TypeList     ::= { PropTypeList }
     * PropTypeList ::= Type | Type, PropTypeList
     */
    private Type parseType() throws ParseException {
        Token token = in.readToken();
        if (token == Lexer.UNIT) return UnitType.ONLY;
        if (token == Lexer.INT) return IntType.ONLY;
        if (token == Lexer.BOOL) return BoolType.ONLY;
        if (token == Lexer.LIST) return new ListType(parseType());
        if (token == Lexer.REF) return new RefType(parseType());
        if (token == LeftParen.ONLY) {
            Token next = in.peek();
            List<Type> args = new ArrayList<Type>();
            while (next != Lexer.RETURN) {
                args.add(parseType());
                next = in.peek();
                if (next == Lexer.RETURN) break;
                if (next == Comma.ONLY) {
                    in.readToken();
                    continue;
                }
                error (next, "expected ',' after type");
            }
            in.readToken();
            Type ret = parseType();
            if (in.readToken() != RightParen.ONLY)
                error(next, "unmatched parentheses");
            return new ClosureType(args.toArray(new Type[0]), ret);
        }
        error(token, "invalid type after ':'");
        return null;
    }
    
    /** Private method for processing syntax errors
     *  Raises ParseException if there is any syntax error
     */
    private void error (Token token, String msg) throws ParseException
    {
        throw new ParseException("Syntax error at '" + ((token != null)? token.toString() : "EOF") + "': " + msg);
    }
}

