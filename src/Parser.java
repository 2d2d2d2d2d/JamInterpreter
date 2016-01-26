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
			Parser parser = new Parser("/Users/wjy/Documents/Rice/COMP 511/Assignment 1/tests/in.txt");
			System.out.println(parser.parse().toString());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	
	
  
	private Lexer in;
	
	  //. . .
	  
	Parser(Lexer i) {
		in = i;
	    initParser();
	}
	  
	Parser(Reader inputStream) { this(new Lexer(inputStream)); }
	  
	Parser(String fileName) throws IOException { this(new FileReader(fileName)); }
	  
	Lexer lexer() { return in; }
	  
	private void initParser() {
	    //. . .
	}
	  
	/** Parses the program text in the lexer bound to 'in' and returns the corresponding AST. 
	  * @throws ParseException if a syntax error is encountered (including lexical errors). 
	  */
	public AST parse() throws ParseException {
	    
		return null;
	}
	  
	/** Parses:
	  *     <exp> :: = if <exp> then <exp> else <exp>
	  *              | let <prop-def-list> in <exp>
	  *              | map <id-list> to <exp>
	  *              | <term> { <biop> <exp> }
	  * 
	  * @return  the corresponding AST.
	  */
	private AST parseExp(Token token) throws ParseException {
	    
	}
	
	private AST parseTerm(Token token) throws ParseException {
		if (token instanceof Op) {
			Op op = (Op) token;
			if (! op.isUnOp()) error(op,"unary operator");
				return new UnOpApp(op, parseTerm(in.readToken()));
		}
		    
		if (token instanceof Constant) return (Constant) token;
			AST factor = parseFactor(token);
			Token next = in.peek();
			if (next == LeftParen.ONLY) {
				in.readToken();  // remove next from input stream
				AST[] exps = parseArgs();  // including closing paren
				return new App(factor,exps);
			}
			return factor;
		}
	
	private AST parseDef(Token token) throws ParseException {
		
	}
	
	private AST parseFactor(Token token) throws ParseException {
		if (token == LeftParen.ONLY) {
			AST exp = parseExp(in.readToken());
			Token next = in.readToken();
			if(next == RightParen.ONLY) {
				return new PrimFun(exp.toString());
			}
			else {
				error(next, "non-enclosed factor");
			}
		}
		
		if (token instanceof PrimFun) {
			return (PrimFun)token;
		}
		
		if ()
	}
	
	private AST parseDef(Token token) throws ParseException {
		
	}
	
	private AST parseExpList(Token token) throws ParseException {
		
	}
	
	private AST parseIdList(Token token) throws ParseException {
		
	}
	
	private AST[] parseArgs() throws ParseException {
		
	}
	
	class ParseException extends Exception
	{
		public ParseException(String message)
		{
			super(message);
		}
	}
	
	private void error (Token token, String msg) throws ParseException
	{
		throw new ParseException(msg);
	}
}

