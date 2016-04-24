(require (lib "test.ss" "schemeunit"))
(load "eval.ss")

;; testing framework for typed jam
;;
;;

(define sa string-append)  
(define los2 
  (lambda (list pre in post)
    (letrec [(helper (lambda (list pre in) 
                       (if (null? list)  ""
                           (sa pre (car list) 
                               (helper (cdr list) in in)))))]
      (sa pre (helper list "" in) post))))
  
(define toStringV
  (lambda (val)
    (cond [(number? val) (number->string val)]
          [(list? val) (los2 (map toStringV val) "(" " " ")")]
          [(symbol? val) (symbol->string val)]
          [(boolean? val) (if val
                              "true"
                              "false")]
          [(box? val) (sa "(ref " (toStringV (unbox val)) ")")]
          [else (tostring val)]
          )))

(define toString6
   (lambda (val)
;.... insert your code here.
; call below wont work b/c toString6 must be recursive
; so copy the function above and make your modifications
     (toStringV val)))

(define eagerCheck
  (lambda (output input)
    (assert equal? output (tostring6 ((make-eval-string input))))))
(define cpsCheck
  (lambda (output input)
    (assert equal? output (tostring6 ((make-cps-eval-string input))))))
;(define SDEagerCheck
;  (lambda (output input)
;    (assert equal? output (tostring6 ((make-sd-eval-string input))))))

(define SDCpsCheck
  (lambda (output input)
     (SDCpsFullCheck output input 50000 'value)))

(define SDCpsFullCheck
  (lambda (output input size mode)
    (assert equal? output (tostring6 ((make-sd-cps-eval-string input size) mode)))))
	
(define allCheck
  (lambda (output input)
    (begin
      (eagerCheck output input)
      (cpsCheck output input)
;      (SDEagerCheck output input)
      (SDCpsCheck output input)
)))
(define nonCpsCheck
  (lambda (output input)
    (begin
      (eagerCheck output input)
 ;     (SDEagerCheck output input)
)))
(define unshadowConvert 
  (lambda (output input)
    (assert equal? output (tostring6 (unshadow (parse-string input))))))

(define cpsConvert
  (lambda (output input)
    (assert equal? output (tostring6 (convert-to-cps (parse-string input))))))

(define sdConvert
  (lambda (output input)
    (assert equal? output (tostring6 (convert-to-sd (parse-string input))))))



(define assign7test
  (make-test-suite
   "assign7test"

   (make-test-case
    "interface: make-eval-string"
    (assert-true (procedure? make-eval-string)))

   (make-test-case
    "interface: make-eval-file"
    (assert-true (procedure? make-eval-file)))
   
   

        
   (make-test-case
       "badLetrec" (with-handlers ([exn:user? (lambda (x)
                           (if (or (regexp-match "parse" (exn-message x))
) #t (raise x))
                         )])
       (allCheck "!"
                    "letrec x:=4; in x") ) )

        
   (make-test-case
       "badLet" (with-handlers ([exn:user? (lambda (x)
                           (if (or (regexp-match "syntax" (exn-message x))
) #t (raise x))
                         )])
       (allCheck "!"
                    "let x:= map z to y(z);
             y:= map z to x(z); in x(5)") ) )

        
   (make-test-case
       "append" 
       (allCheck "(1 2 3 1 2 3)"
                    "letrec append := map x,y to
          if x = null then y else cons(first(x), append(rest
(x), y));
            in let s := cons(1,cons(2,cons(3,null)));
          in append(s,s)")  )

        
   (make-test-case
       "bigfib" 
       (allCheck "((0 1) (1 1) (2 2) (3 3) (4 5) (5 8) (6 13) (7 21) (8 34) (9 55) (10 89) (11 144) (12 233) (13 377) (14 610) (15 987) (16 1597) (17 2584) (18 4181) (19 6765) (20 10946) (21 17711) (22 28657) (23 46368) (24 75025) (25 121393) (26 196418) (27 317811) (28 514229) (29 832040) (30 1346269) (31 2178309) (32 3524578) (33 5702887) (34 9227465) (35 14930352) (36 24157817) (37 39088169) (38 63245986) (39 102334155) (40 165580141) (41 267914296) (42 433494437) (43 701408733) (44 1134903170) (45 1836311903))"
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
")  )
   
   ))

(require (lib "text-ui.ss" "schemeunit"))
(test/text-ui assign7test)

