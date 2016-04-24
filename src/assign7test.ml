#use "lexer.ml"
#use "parser.ml"
#use "eval.ml"
#use "oUnit.ml"

(* You should have this stuff in eval.ml
 *
type value =
   VNum of int
 | VList of value list
 | VBool of bool
 | VPrim of string
 | ... (* add your own types here *)
;;
*)
(* need to define function string_of_value: value -> string *)
let heapSize = 50000
let assert_equal = assert_equal ~printer:(fun x -> x)

let eagerCheck =
  fun output input ->
    assert_equal output (string_of_value (eval_string input));;

let cpsCheck =
  fun output input ->
    assert_equal output (string_of_value (cps_eval_string input));;

let sdCpsCheck =
  fun output input ->
    let result = (sd_cps_eval_string input heapSize RETVALUE) in
      match result with
      | RValue v -> assert_equal output (string_of_value v)
      | _ -> failwith "should be an RValue";;

let allCheck output input = 
  begin
    eagerCheck output input;
    cpsCheck output input;
    sdCpsCheck output input;
  end
;;

let nonCPSCheck output input = 
  begin
    eagerCheck output input;
  end
;;

let unshadowConvert =
  fun output input ->
    assert_equal output (string_of_exp (unshadow (parse_string input)));;

let cpsConvert =
  fun output input ->
    assert_equal output (string_of_exp (convert_to_cps (parse_string input)));;

let sdConvert =
  fun output input ->
    assert_equal output (string_of_exp (convert_to_sd (parse_string input)));;



let tests = "assign7test" >::: [

   "badLetrec" >:: (fun _ -> 
	try
                  
	allCheck
		"!"
		"letrec x:=4; in x"
	with | ParserError(x) -> ()
 
	
	);

	

   "badLet" >:: (fun _ -> 
	try
                  
	allCheck
		"!"
		"let x:= map z to y(z);
             y:= map z to x(z); in x(5)"
	with | SyntaxError(x) -> ()
 
	
	);

	

   "append" >:: (fun _ -> 
	
	allCheck
		"(1 2 3 1 2 3)"
		"letrec append := map x,y to
          if x = null then y else cons(first(x), append(rest
(x), y));
            in let s := cons(1,cons(2,cons(3,null)));
          in append(s,s)"
	
	
	);

	

   "bigfib" >:: (fun _ -> 
	
	allCheck
		"((0 1) (1 1) (2 2) (3 3) (4 5) (5 8) (6 13) (7 21) (8 34) (9 55) (10 89) (11 144) (12 233) (13 377) (14 610) (15 987) (16 1597) (17 2584) (18 4181) (19 6765) (20 10946) (21 17711) (22 28657) (23 46368) (24 75025) (25 121393) (26 196418) (27 317811) (28 514229) (29 832040) (30 1346269) (31 2178309) (32 3524578) (33 5702887) (34 9227465) (35 14930352) (36 24157817) (37 39088169) (38 63245986) (39 102334155) (40 165580141) (41 267914296) (42 433494437) (43 701408733) (44 1134903170) (45 1836311903))"
		"
letrec fib :=  map n to if n <= 1 then 1 else fib(n - 1) + fib(n - 2);
       fibhelp := map k,fn,fnm1 to
                    if k = 0 then fn
                    else fibhelp(k - 1, fn + fnm1, fn);
       pair := map x,y to cons(x, cons(y, null));
in let ffib := map n to if n = 0 then 1 else fibhelp(n - 1,1,1);
   in letrec fibs :=  map k,l to 
                        if 0 <= k then 
                        fibs(k - 1, cons(pair(k,ffib(k)), l))
	                else l;
      in fibs(45, null)
"
	
	
	);

	]

let _ = run_test_tt ~verbose:true tests

