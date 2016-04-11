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
;;.... insert your code here.
;; call below wont work b/c toString6 must be recursive
;; so copy the function above and make your modifications
     (toStringV val)))

(define eagerCheck
  (lambda (output input)
    (assert equal? output (tostring6 ((make-eval-string input))))))
(define cpsCheck
  (lambda (output input)
    (assert equal? output (tostring6 ((make-cps-eval-string input))))))
(define SDEagerCheck
  (lambda (output input)
    (assert equal? output (tostring6 ((make-sd-eval-string input))))))
(define SDCpsCheck
  (lambda (output input)
    (assert equal? output (tostring6 ((make-sd-cps-eval-string input))))))
	
(define allCheck
  (lambda (output input)
    (begin
      (eagerCheck output input)
      (cpsCheck output input)
      (SDEagerCheck output input)
      (SDCpsCheck output input)
)))
(define nonCpsCheck
  (lambda (output input)
    (begin
      (eagerCheck output input)
      (SDEagerCheck output input)
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



(define assign6test
  (make-test-suite
   "assign6test"

   (make-test-case
    "interface: make-eval-string"
    (assert-true (procedure? make-eval-string)))

   (make-test-case
    "interface: make-eval-file"
    (assert-true (procedure? make-eval-file)))
   
   

        
   (make-test-case
       "badLetrec" (with-handlers ([exn:user? (lambda (x)
                           (if (or (regexp-match "(parse|Parse)" (exn-message x))
) #t (raise x))
                         )])
       (allCheck "!"
                    "letrec x:=4; in x" ) ) )

        
   (make-test-case
       "badLet" (with-handlers ([exn:user? (lambda (x)
                           (if (or (regexp-match "syntax" (exn-message x))
) #t (raise x))
                         )])
       (allCheck "!"
                    "let x:= map z to y(z);
             y:= map z to x(z); in x(5)" ) ) )

        
   (make-test-case
       "Uuop" 
       (unshadowConvert "(3 + 3)"
                    "3 + 3" )  )

        
   (make-test-case
       "Suop" 
       (sdConvert "(3 + 3)"
                    "3 + 3" )  )

        
   (make-test-case
       "Cuop" 
       (cpsConvert "(map x to x)((3 + 3))"
                    "3 + 3" )  )

        
   (make-test-case
       "uop" 
       (allCheck "6"
                    "3 + 3" )  )

        
   (make-test-case
       "Udeep" 
       (unshadowConvert "let x:1 := map x:1 to letrec x:2 := map x:3 to x:3; in x:2(x:2); y:1 := let x:1 := 5; in x:1; in x:1(y:1)"
                    "let x:= map x to 
     letrec x:=map x to x; in x(x);
    y:= let x:=5; in x;
  in x(y)" )  )

        
   (make-test-case
       "Sdeep" 
       (sdConvert "let [*2*] map [*1*] to letrec [*1*] map [*1*] to [0,0]; in ([0,0])([0,0]); let [*1*] 5; in [0,0]; in ([0,0])([0,1])"
                    "let x:= map x to 
     letrec x:=map x to x; in x(x);
    y:= let x:=5; in x;
  in x(y)" )  )

        
   (make-test-case
       "Umap" 
       (unshadowConvert "map z:1 to z:1"
                    "map z to z" )  )

        
   (make-test-case
       "Smap" 
       (sdConvert "map [*1*] to [0,0]"
                    "map z to z" )  )

        
   (make-test-case
       "Cmap" 
       (cpsConvert "(map x to x)(map z:1,:0 to :0(z:1))"
                    "map z to z" )  )

        
   (make-test-case
       "Carity" 
       (cpsConvert "(map x to x)(map x,k to k((arity(x) - 1)))"
                    "arity" )  )

        
   (make-test-case
       "Cfirst" 
       (cpsConvert "(map x to x)(map x,k to k(first(x)))"
                    "first" )  )

        
   (make-test-case
       "Ccons" 
       (cpsConvert "(map x to x)(map x,y,k to k(cons(x, y)))"
                    "cons" )  )

        
   (make-test-case
       "Clist" 
       (cpsConvert "(map x to x)(first(rest(rest(cons(1, cons(2, cons(3, null)))))))"
                    "first(rest(rest(cons(1, cons(2, cons(3, null))))))" )  )

        
   (make-test-case
       "Uappend" 
       (unshadowConvert "letrec append:1 := map x:2,y:2 to if (x:2 = null) then y:2 else cons(first(x:2), append:1(rest(x:2), y:2)); in let s:2 := cons(1, cons(2, cons(3, null))); in append:1(s:2, s:2)"
                    "letrec append := map x,y to
          if x = null then y else cons(first(x), append(rest
(x), y));
            in let s := cons(1,cons(2,cons(3,null)));
          in append(s,s)" )  )

        
   (make-test-case
       "Sappend" 
       (sdConvert "letrec [*1*] map [*2*] to if ([0,0] = null) then [0,1] else cons(first([0,0]), ([1,0])(rest([0,0]), [0,1])); in let [*1*] cons(1, cons(2, cons(3, null))); in ([1,0])([0,0], [0,0])"
                    "letrec append := map x,y to
          if x = null then y else cons(first(x), append(rest
(x), y));
            in let s := cons(1,cons(2,cons(3,null)));
          in append(s,s)" )  )

        
   (make-test-case
       "Cappend" 
       (cpsConvert "letrec append:1 := map x:2,y:2,:0 to if (x:2 = null) then :0(y:2) else let :1 := first(x:2); in append:1(rest(x:2), y:2, map :3 to :0(let :2 := :3; in cons(:1, :2))); in let s:2 := cons(1, cons(2, cons(3, null))); in append:1(s:2, s:2, map x to x)"
                    "letrec append := map x,y to
          if x = null then y else cons(first(x), append(rest
(x), y));
            in let s := cons(1,cons(2,cons(3,null)));
          in append(s,s)" )  )

        
   (make-test-case
       "append" 
       (allCheck "(1 2 3 1 2 3)"
                    "letrec append := map x,y to
          if x = null then y else cons(first(x), append(rest
(x), y));
            in let s := cons(1,cons(2,cons(3,null)));
          in append(s,s)" )  )

        
   (make-test-case
       "Uappend1" 
       (unshadowConvert "letrec appendz1:1 := map xz2:2,yz2:2,z0:2 to if (xz2:2 = null) then z0:2(yz2:2) else let z1:3 := first(xz2:2); in appendz1:1(rest(xz2:2), yz2:2, map z3:4 to z0:2(let z2:5 := z3:4; in cons(z1:3, z2:5))); in let sz2:2 := cons(1, cons(2, cons(3, null))); in appendz1:1(sz2:2, sz2:2, map x:3 to x:3)"
                    "letrec appendz1 := map xz2,yz2,z0 to if (xz2 =null) then z0(yz2) else let z1 := first(xz2); in appendz1(rest(xz2), yz2, map z3 to z0(let z2 := z3; in cons(z1, z2))); in let sz2 := cons(1, cons(2, cons(3, null))); in appendz1(sz2, sz2, map x to x)" )  )

        
   (make-test-case
       "Cappend1" 
       (cpsConvert "letrec appendz1:1 := map xz2:2,yz2:2,z0:2,:0 to if (xz2:2 = null) then z0:2(yz2:2, :0) else let z1:3 := first(xz2:2); in appendz1:1(rest(xz2:2), yz2:2, map z3:4,:1 to z0:2(let z2:5 := z3:4; in cons(z1:3, z2:5), :1), :0); in let sz2:2 := cons(1, cons(2, cons(3, null))); in appendz1:1(sz2:2, sz2:2, map x:3,:2 to :2(x:3), map x to x)"
                    "letrec appendz1 := map xz2,yz2,z0 to if (xz2 =null) then z0(yz2) else let z1 := first(xz2); in appendz1(rest(xz2), yz2, map z3 to z0(let z2 := z3; in cons(z1, z2))); in let sz2 := cons(1, cons(2, cons(3, null))); in appendz1(sz2, sz2, map x to x)" )  )

        
   (make-test-case
       "Sfact" 
       (sdConvert "let [*1*] 6; in letrec [*1*] map [*1*] to let [*1*] map [*1*] to ([1,0])(map [*1*] to (([1,0])([1,0]))([0,0])); in ([0,0])([0,0]); in let [*1*] map [*1*] to map [*1*] to if ([0,0] = 0) then 1 else ([0,0] * ([1,0])(([0,0] - 1))); in (([1,0])([0,0]))([2,0])"
                    "let n:= 6; in
   letrec Y := map f to let g := map x to f(map z to (x(x))(z)); in g(g);
   in 
    let 
       FACT := map f to map n to if n = 0 then 1 else n * f(n - 1);
      in (Y(FACT))(n)" )  )

        
   (make-test-case
       "Cfact" 
       (cpsConvert "let Y:1 := map f:1,:0 to let g:2 := map x:2,:1 to f:1(map z:3,:2 to x:2(x:2, map :5 to let :3 := :5; in let :4 := z:3; in :3(:4, :2)), :1); in g:2(g:2, :0); in let FACT:1 := map f:1,:6 to :6(map n:2,:7 to if (n:2 = 0) then :7(1) else let :8 := n:2; in f:1((n:2 - 1), map :10 to :7(let :9 := :10; in (:8 * :9)))); in Y:1(FACT:1, map :13 to let :11 := :13; in let :12 := 6; in :11(:12, map x to x))"
                    "let Y := map f to let g := map x to f(map z to (x(x))(z)); in g(g);
         FACT := map f to map n to if n = 0 then 1 else n * f(n - 1);
      in (Y(FACT))(6)" )  )

        
   (make-test-case
       "fact" 
       (allCheck "720"
                    "let n:= 6; in
   letrec Y := map f to let g := map x to f(map z to (x(x))(z)); in g(g);
   in 
    let 
       FACT := map f to map n to if n = 0 then 1 else n * f(n - 1);
      in (Y(FACT))(n)" )  )

        
   (make-test-case
       "Uletcc" 
       (unshadowConvert "letcc x:1 in if true then x:1(5) else 3"
                    "letcc x in  if true then  x(5)  else 3" )  )

        
   (make-test-case
       "Cletcc" 
       (cpsConvert "let x:1 := map :0,:1 to (map x to x)(:0); in if true then x:1(5, map x to x) else (map x to x)(3)"
                    "letcc x in  if true then  x(5)  else 3" )  )
   
   ))

(require (lib "text-ui.ss" "schemeunit"))
(test/text-ui assign6test)

