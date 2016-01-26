/** Parser for Assignment 2 */

import java.io.*;
import java.util.*;

/** Each parser object in this class contains an embedded lexer which contains an embedded input stream.  The
  * class include a parse() method that will translate the program text in the input stream to the corresponding
  * AST assuming that the program text forms a syntactically valid Jam program.
  */
class Parser {
	
	public static void main(String args[])
	{
		try
		{
			Parser parser = new Parser("src/test_data/hard/08good");
			System.out.println(parser.parse().toString());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	
  
	private Lexer in;
	  
	Parser(Lexer i) {
		in = i;
	    initParser();
	}
	  
	Parser(Reader inputStream) { this(new Lexer(inputStream)); }
	  
	Parser(String fileName) throws IOException { this(new FileReader(fileName)); }
	  
	Lexer lexer() { return in; }
	  
	private void initParser() {}
	
	public AST parse() throws ParseException {
	    AST exp = parseExp(in.readToken());
	    Token token = in.readToken();
	    if (token != null)
	    	error(token, "redundant token after the expression");
	    return exp;
	}
	
	private AST parseExp(Token token) throws ParseException {
		if (token instanceof KeyWord && ((KeyWord) token).getName().equals("if")) {
			AST exp1 = parseExp(in.readToken());
			Token word_then = in.readToken();
			if (word_then instanceof KeyWord && ((KeyWord) word_then).getName().equals("then")) {
				AST exp2 = parseExp(in.readToken());
				Token word_else = in.readToken();
				if (word_else instanceof KeyWord && ((KeyWord) word_else).getName().equals("else")) {
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
		}
		
		if (token instanceof KeyWord && ((KeyWord) token).getName().equals("let")) {
			List<Def> defs_list = new ArrayList<Def>();
			defs_list.add(parseDef(in.readToken()));
			while (!(in.peek() instanceof KeyWord && ((KeyWord)in.peek()).getName().equals("in")))
			{
				defs_list.add(parseDef(in.readToken()));
			}
			Token word_in = in.readToken();
			if (word_in instanceof KeyWord && ((KeyWord) word_in).getName().equals("in")) {
				AST exp = parseExp(in.readToken());
				return new Let(defs_list.toArray(new Def[0]), exp);
			}
			else {
				error(word_in, "invalid let-in expression, missing keyword 'in'");
			}
		}
		
		if (token instanceof KeyWord && ((KeyWord) token).getName().equals("map")) {
			Variable[] vars = parseIdList();
			Token next = in.readToken();
			if (next instanceof KeyWord && ((KeyWord) next).getName().equals("to")) {
				AST exp = parseExp(in.readToken());
				return new Map(vars, exp);
			}
			else {
				error(next, "invalid map-to expression, missing keyword 'to'");
			}
		}
		
		AST term = parseTerm(token);
		if (in.peek() instanceof Op && ((Op)in.peek()).isBinOp()) {
			Op op = (Op)in.readToken();
			AST exp = parseExp(in.readToken());
			return new BinOpApp(op, term, exp);
		}
		return term;
	}

	private AST parseTerm(Token token) throws ParseException {
		if (token instanceof Op) {
			Op op = (Op) token;
			if (! op.isUnOp()) error(op,"requires unary operator");
				return new UnOpApp(op, parseTerm(in.readToken()));
		}
		    
		if (token instanceof Constant) return (Constant) token;
		
		AST factor = parseFactor(token);
		Token next = in.peek();
		if (next == LeftParen.ONLY) {
			in.readToken();  // remove next from input stream
			AST[] exps = parseExpList();  // including closing paren
			return new App(factor,exps);
		}
		return factor;
	}
	
	private Def parseDef(Token token) throws ParseException {
		if (token instanceof Variable) {
			Token next = in.readToken();
			if (next instanceof KeyWord && ((KeyWord) next).getName().equals(":=")) {
				AST exp = parseExp(in.readToken());
				Token word_semicolon = in.readToken();
				if (word_semicolon instanceof SemiColon) {
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
	
	
	//Factor      ::= ( Exp ) | Prim | Id
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
	
	// PropIdList  ::= Id | Id , PropIdList
	private Variable[] parseIdList() throws ParseException {
		Token token = in.peek();
		List<Variable> vars_list = new ArrayList<Variable>();
		
		if (token instanceof Variable) {
			token = in.readToken();
			vars_list.add((Variable)token);
			while (in.peek() instanceof Comma)
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
	
	private AST[] parseExpList() throws ParseException {
		Token token = in.readToken();
		List<AST> exps_list = new ArrayList<AST>();
		
		if (token == RightParen.ONLY) {
			return new AST[0];
		}
		
		AST exp = parseExp(token);
		exps_list.add(exp);
		while (in.peek() instanceof Comma)
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
	
	private void error (Token token, String msg) throws ParseException
	{
		throw new ParseException("Syntax error at '" + ((token != null)? token.toString() : "EOF") + "': " + msg);
	}
}

