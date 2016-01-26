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
		if (token instanceof If) {
			If iff = (If) token;
				return new parseExp()
			
		}
	}
	
	
	/**Term  ::= Unop Term
     *       | Factor { ( ExpList ) }
     *       | Null
     *       | Int
     *       | Bool
     */

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
	
	private Def parseDef(Token token) throws ParseException {
		if (token instanceof Variable) {
			Token next = in.readToken();
			if (next instanceof KeyWord) {
				if (((KeyWord) next).getName().equals(":=")) {
					AST exp = parseExp(in.readToken());
					return new Def((Variable)token, exp);
				}
			}
		}
		
		error(token, "failed to parse definations");
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
				error(next, "non-enclosed factor");
				return null;
			}
		}
		
		if (token instanceof PrimFun) {
			return (PrimFun)token;
		}
		
		if (token instanceof Variable){
			return (Variable)token;
		}
		
		error(token, "unknown factor element");
		return null;
	}
	
	private AST[] parseExpList(Token token) throws ParseException {
		
	}
	
	// PropIdList  ::= Id | Id , PropIdList
	private AST[] parseIdList(Token token) throws ParseException {
		ArrayList<Variable> list = new ArrayList<Variable>();
		
		if(token instanceof Variable){
			Variable temp = (Variable) token;
			list.add(temp);
			
			while((token = in.readToken()) != null){
				if(token instanceof Comma){
					token = in.readToken();
					if (token instanceof Variable){
						  list.add((Variable) token);
						  token = in.readToken();
					  } else {
						  error(token,"missing id");
					  }
				}else{
					error(token,"missing comma");
					return null;
				}
			}
			Variable[] arr = new Variable[list.size()];
			list.toArray(arr);
			return arr;
			
		}else{
			error(token,"invalid idList at the beginning");
			return null;
		}
		
		
	}
	
	private AST[] parseArgs() throws ParseException {
		Token next;
		List<AST> args = new ArrayList();
		while ((next = in.readToken()) != RightParen.ONLY)
		{
			args.add(parseExp(next));
			if (in.peek() instanceof Comma)
				in.readToken();
		}
		return args.toArray(new AST[0]);
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

