#use "lexer.ml"
#use "parser.ml"
#use "oUnit.ml"

let checkString =
  fun output input ->
    assert_equal output (string_of_exp (parse_string input));;

let checkFile =
  fun output input ->
    assert_equal output (string_of_exp (parse_file input));;


let tests = "assign1test" >::: [

   "add" >:: (fun _ -> 
	
	checkString
		"(2 + 3)"
		"2+3"
	
	
	);

	

   "prim  " >:: (fun _ -> 
	
	checkString
		"first"
		"first"
	
	
	);

	

   "parseException" >:: (fun _ -> 
	try
                  
	checkString
		"doh!"
		"map a, to 3"
	with | ParserError(x) -> ()
 
	
	);

	

   "let" >:: (fun _ -> 
	
	checkString
		"let a := 3; in (a + a)"
		"let a:=3; in a + a"
	
	
	);

	

   "map" >:: (fun _ -> 
	
	checkString
		"map f to (map x to f(x(x)))(map x to f(x(x)))"
		"map f to (map x to f( x( x ) ) ) (map x to f(x(x)))"
	
	
	);

	]

let _ = run_test_tt_main tests
