import java.util.ArrayList;
import java.util.List;

/** Class that performs CPS transformation */
class Cps {
    
    /** A counter for naming new variables */
    private int varId;
    
    /** Unshadowed program */
    private AST ast;
    
    /** Constructor */
    public Cps(AST ast) { this.ast = ast; }

    /** Converts Cps[map x to x, M0] to an AST, where M0 is the unshadowed form of the program */
    public AST convert() {
        varId = 0;
        return convertCps(new Map(new Variable[]{new Variable("x")}, new Variable("x")), ast);
    }
    
    /** Converts Cps[k, M] to an AST */
    private AST convertCps(AST k, AST M) {
        
        /** 1. If M is a simple Jam expression S:
         *  Cps[k, S] => k(Rsh[S])
         */
        if (isSimple(M)) {
            return new App(k, new AST[]{convertRsh(M)});
        }

        /** 2. If M is an application (map x1, ..., xn to B)(E1, ...,En), n > 0:
         *  Cps[k, (map x1, ..., xn to B)(E1, ...,En)] => Cps[k, let x1 :=E1; ...; xn :=En; in B]
         */
        if (M instanceof App && ((App) M).rator() instanceof Map && ((App) M).args().length > 0) {
            Map map = (Map)((App) M).rator();
            AST[] args = ((App) M).args();
            List<Def> def_list = new ArrayList<Def>();
            for (int i = 0; i < args.length; i++) {
                def_list.add(new Def(map.vars()[i], args[i]));
            }
            return convertCps(k, new Let(def_list.toArray(new Def[0]), map.body()));
        }

        /** 3. If M is an application (map to B)(),:
         *  Cps[k, (map to B)()] => Cps[k, B]
         */
        if (M instanceof App && ((App) M).rator() instanceof Map && ((App) M).args().length == 0) {
            return convertCps(k, ((Map)((App) M).rator()).body());
        }
        
        /** 4. If M is an application S(S1, ..., Sn), n >= 0:
         *  Cps[k, S(S1, ..., Sn)] => Rsh[S](Rsh[S1], ...,Rsh[Sn], k)
         */
        if (M instanceof App && isSimple(((App) M).rator())) {
            boolean flag = true;
            for (AST arg : ((App) M).args())
                flag = flag && isSimple(arg);
            if (flag) {
                AST rator = convertRsh(((App) M).rator());
                List<AST> arg_list = new ArrayList<AST>();
                for (AST arg : ((App) M).args())
                    arg_list.add(convertRsh(arg));
                arg_list.add(k);
                return new App(rator, arg_list.toArray(new AST[0]));
            }
        }
        if (M instanceof UnOpApp && isSimple(((UnOpApp) M).arg())) {
            UnOp S = ((UnOpApp) M).rator();
            AST S1 = ((UnOpApp) M).arg();
            return new UnOpApp(S, convertRsh(S1));
        }
        if (M instanceof BinOpApp && isSimple(((BinOpApp) M).arg1()) && isSimple(((BinOpApp) M).arg2())) {
            BinOp S = ((BinOpApp) M).rator();
            AST S1 = ((BinOpApp) M).arg1();
            AST S2 = ((BinOpApp) M).arg2();
            return new BinOpApp(S, convertRsh(S1), convertRsh(S2));
        }
        
        /** 5. If M is an application S(E1, ...,En), n > 0:
         *  Cps[k, S(E1, ...,En)] => Cps[k, let v1 :=E1; ... vn :=En; in S(v1, ..., vn)]
         */
        if (M instanceof App && isSimple(((App) M).rator())) {
            AST[] args = ((App) M).args();
            List<Def> def_list = new ArrayList<Def>();
            List<AST> arg_list = new ArrayList<AST>();
            for (int i = 0; i < args.length; i++) {
                String var = ":" + varId++;
                def_list.add(new Def(new Variable(var), args[i]));
                arg_list.add(new Variable(var));
            }
            return convertCps(k, new Let(def_list.toArray(new Def[0]), new App(((App) M).rator(), arg_list.toArray(new AST[0]))));
        }
        if (M instanceof UnOpApp) {
            String v1 = ":" + varId++;
            AST E1 = ((UnOpApp) M).arg();
            UnOp S = ((UnOpApp) M).rator();
            return convertCps(k, new Let(new Def[]{new Def(new Variable(v1), E1)}, new UnOpApp(S, new Variable(v1))));
        }
        if (M instanceof BinOpApp) {
            String v1 = ":" + varId++;
            String v2 = ":" + varId++;
            AST E1 = ((BinOpApp) M).arg1();
            AST E2 = ((BinOpApp) M).arg2();
            BinOp S = ((BinOpApp) M).rator();
            return convertCps(k, new Let(
                    new Def[]{new Def(new Variable(v1), E1), new Def(new Variable(v2), E2)},
                    new BinOpApp(S, new Variable(v1), new Variable(v2))));
        }
        
        /** 6. If M is an application B(E1, ...,En), n >= 0 where B is not simple:
         *  Cps[k, B(E1, ..., En)] => Cps[k, let v :=B; v1 :=E1; ... vn :=En; in v(v1, ..., vn)]
         */
        if (M instanceof App && ! isSimple(((App) M).rator())) {
            AST B = ((App) M).rator();
            AST[] args = ((App) M).args();
            List<Def> def_list = new ArrayList<Def>();
            List<AST> arg_list = new ArrayList<AST>();
            String v = ":" + varId++;
            def_list.add(new Def(new Variable(v), B));
            for (int i = 0; i < args.length; i++) {
                String var = ":" + varId++;
                def_list.add(new Def(new Variable(var), args[i]));
                arg_list.add(new Variable(var));
            }
            return convertCps(k, new Let(def_list.toArray(new Def[0]), new App(new Variable(v), arg_list.toArray(new AST[0]))));
        }
        
        /** 7. If M is a conditional construction if S then A else C:
         *  Cps[k, if S then A else C] => if Rsh[S] then Cps[k, A] else Cps[k, C]
         */
        if (M instanceof If && isSimple(((If) M).test())) {
            AST S = ((If) M).test();
            AST A = ((If) M).conseq();
            AST C = ((If) M).alt();
            return new If(convertRsh(S), convertCps(k, A), convertCps(k, C));
        }
        
        /** 8. If M is a conditional construction if T then A else C:
         *  Cps[k, if T then A else C] => Cps[k, let v := T in if v then A else C]
         */
        if (M instanceof If) {
            AST T = ((If) M).test();
            AST A = ((If) M).conseq();
            AST C = ((If) M).alt();
            String v = ":" + varId++;
            return convertCps(k, new Let(new Def[]{new Def(new Variable(v), T)}, new If(new Variable(v), A, C)));
        }
        
        /** 9. If M is a block {E1; E2; ...; En}, n > 0:
         *  Cps[k, {E1; E2; ...; En}] => Cps[k, (let v1 := E1; ...; vn := En; in vn]
         */
        if (M instanceof Block && ((Block) M).exps().length > 0) {
            AST[] exps = ((Block) M).exps();
            List<Def> def_list = new ArrayList<Def>();
            String var = "";
            for (AST exp : exps) {
                var = ":" + varId++;
                def_list.add(new Def(new Variable(var), exp));
            }
            return convertCps(k, new Let(def_list.toArray(new Def[0]), new Variable(var)));
        }
        
        /** 10. If M is let x1 := S1; in B:
         *  Cps[k, let x1 :=S1; in B] => let x1 :=Rsh[S1]; in Cps[k, B]
         */
        if (M instanceof Let && ((Let) M).defs().length == 1 && isSimple(((Let) M).defs()[0].rhs())) {
            Variable x1 = ((Let) M).defs()[0].lhs();
            AST S1 = ((Let) M).defs()[0].rhs();
            AST B = ((Let) M).body();
            return new Let(new Def[]{new Def(x1, convertRsh(S1))}, convertCps(k, B));
        }
        
        /** 11. If M is let x1 :=S1; x2 := E2; ... xn := En; in B, n > 1:
         *  Cps[k, let x1 :=S1; x2 :=E2; ... xn :=En; in B] => let x1 :=Rsh[S1]; in Cps[k, let x2 := E2; ...; xn := En; in B]
         */
        if (M instanceof Let && ((Let) M).defs().length > 1 && isSimple(((Let) M).defs()[0].rhs())) {
            Def[] defs = ((Let) M).defs();
            AST B = ((Let) M).body();
            Variable x1 = defs[0].lhs();
            AST S1 = defs[0].rhs();
            List<Def> def_list = new ArrayList<Def>();
            for (int i = 1; i < defs.length; i++) {
                def_list.add(new Def(defs[i].lhs(), defs[i].rhs()));
            }
            return new Let(new Def[]{new Def(x1, convertRsh(S1))}, convertCps(k, new Let(def_list.toArray(new Def[0]), B)));
        }
        
        /** 12. If M is let x1 := E1; ... xn := En; in B, n > 0:
         *  Cps[k, let x1 := E1; ... xn := En; in B] => Cps[map v to Cps[k, let x1 := v; ... xn := En; in B], E1]
         */
        if (M instanceof Let && ((Let) M).defs().length > 0) {
            Def[] defs = ((Let) M).defs();
            AST B = ((Let) M).body();
            Variable x1 = defs[0].lhs();
            AST E1 = defs[0].rhs();
            String v = ":" + varId++;
            List<Def> def_list = new ArrayList<Def>();
            def_list.add(new Def(x1, new Variable(v)));
            for (int i = 1; i < defs.length; i++) {
                def_list.add(new Def(defs[i].lhs(), defs[i].rhs()));
            }
            return convertCps(new Map(new Variable[]{new Variable(v)}, convertCps(k, new Let(def_list.toArray(new Def[0]), B))), E1);
        }
        
        /** 13. If M is letrec p1 := map ... to E1; ...; pn := map ... to En; in B:
         *  Cps[k, letrec p1 := map ... to E1; ...; pn := map ... to En; in B]
         *  => letrec p1 := Rsh[map ... to E1]; ...; pn := Rsh[map ... to En]; in Cps[k,B]
         */
        if (M instanceof LetRec) {
            Def[] defs = ((LetRec) M).defs();
            AST B = ((LetRec) M).body();
            List<Def> def_list = new ArrayList<Def>();
            for (int i = 0; i < defs.length; i++) {
                def_list.add(new Def(defs[i].lhs(), convertRsh(defs[i].rhs())));
            }
            return new LetRec(def_list.toArray(new Def[0]), convertCps(k, B));
        }
        
        /** 13. If M is letcc x in B:
         *  Cps[k, letcc x in B] => let x := map v, k1 to k(v) in Cps[k,B]
         */
        if (M instanceof Letcc) {
            Variable x = ((Letcc) M).def();
            AST B = ((Letcc) M).body();
            Variable v = new Variable(":" + varId++);
            Variable k1 = new Variable(":" + varId++);
            return new Let(new Def[]{new Def(x, new Map(new Variable[]{v, k1}, new App(k, new Variable[]{v})))}, convertCps(k, B));
        }
        
        System.out.println("convertCps ends");;
        System.out.println(M);
        
        return M;
    }
    
    private AST convertRsh(AST S) {
        /** 1. If S is a ground constant C (value that is not a map):
         *  Rsh[C] => C
         */
        if (S instanceof BoolConstant || S instanceof IntConstant || S instanceof NullConstant) {
            return S;
        }

        /** 2. If S is a variable x:
         *  Rsh[x] => x
         */
        if (S instanceof Variable) {
            return new Variable(((Variable) S).name());
        }

        /** 3. If S is a primitive application arity(S1):
         *  Rsh[arity(S1)] => arity(Rsh[S1]) - 1
         */
        if (S instanceof App && ((App) S).rator() instanceof ArityPrim && isSimple(((App) S).args()[0])) {
            AST S1 = ((App) S).args()[0];
            return new BinOpApp(BinOpMinus.ONLY, new App(ArityPrim.ONLY, new AST[]{convertRsh(S1)}), new IntConstant(1));
        }

        /** 4. If S is a primitive application f(S1, ..., Sn), n >= 0 where f is not arity:
         *  Rsh[f(S1, ..., Sn)] => f(Rsh[S1], ..., Rsh[Sn])
         */
        if (isPrimitive(S)) {
            if (S instanceof App && ((App) S).rator() instanceof PrimFun && ((App) S).rator() != ArityPrim.ONLY) {
                AST f = ((App) S).rator();
                AST[] args = ((App) S).args();
                List<AST> arg_list = new ArrayList<AST>();
                for (AST arg : args) {
                    arg_list.add(convertRsh(arg));
                }
                return new App(f, arg_list.toArray(new AST[0]));
            }
            if (S instanceof UnOpApp) {
                UnOp f = ((UnOpApp) S).rator();
                AST S1 = ((UnOpApp) S).arg();
                return new UnOpApp(f, convertRsh(S1));
            }
            if (S instanceof BinOpApp) {
                BinOp f = ((BinOpApp) S).rator();
                AST S1 = ((BinOpApp) S).arg1();
                AST S2 = ((BinOpApp) S).arg2();
                return new BinOpApp(f, convertRsh(S1), convertRsh(S2));
            }
        }

        /** 5. If S is map x1, ..., xn to E, n >= 0:
         *  Rsh[map x1, ..., xn to E] => map x1, ..., xn, v to Cps[v, E]
         */
        if (S instanceof Map) {
            Variable[] vars = ((Map) S).vars();
            AST E = ((Map) S).body();
            List<Variable> var_list = new ArrayList<Variable>();
            for (Variable var : vars) {
                var_list.add(var);
            }
            String v = ":" + varId++;
            var_list.add(new Variable(v));
            return new Map(var_list.toArray(new Variable[0]), convertCps(new Variable(v), E));
        }

        /** 6. If S is the primitive function arity:
         *  Rsh[arity] => map x,k to k(arity(x) - 1)
         */
        if (S instanceof ArityPrim) {
            return new Map(
                    new Variable[]{new Variable("x"), new Variable("k")},
                    new App(new Variable("k"), new AST[]{
                            new BinOpApp(
                                    BinOpMinus.ONLY,
                                    new App(ArityPrim.ONLY, new AST[]{new Variable("x")}),
                                    new IntConstant(1))
                    }));
        }

        /** 7. If S is a unary primitive function f other than arity:
         *  Rsh[f] => map x,k to k(f(x))
         */
        if (S instanceof PrimFun && S != ArityPrim.ONLY && S != ConsPrim.ONLY) {
            return new Map(
                    new Variable[]{new Variable("x"), new Variable("k")},
                    new App(new Variable("k"), new AST[]{
                            new App(S, new AST[]{new Variable("x")})
                    }));
        }

        /** 8. If S is a binary primitive function g:
         *  Rsh[g] => map x,y,k to k(g(x,y))
         */
        if (S instanceof ConsPrim) {
            return new Map(
                    new Variable[]{new Variable("x"), new Variable("y"), new Variable("k")},
                    new App(new Variable("k"), new AST[]{
                            new App(S, new AST[]{new Variable("x"), new Variable("y")})
                    }));
        }

        /** 9. If S is a conditional construct if S1 then S2 else S3:
         *  Rsh[if S1 then S2 else S3] => if Rsh[S1] then Rsh[S2] else Rsh[S3]
         */
        if (S instanceof If && isSimple(((If) S).test()) && isSimple(((If) S).conseq()) && isSimple(((If) S).alt())) {
            AST S1 = ((If) S).test();
            AST S2 = ((If) S).conseq();
            AST S3 = ((If) S).alt();
            return new If(convertRsh(S1), convertRsh(S2), convertRsh(S3));
        }

        /** 10. If S is let x1 := S1; ...; xn := Sn; in S, n > 0:
         *  Rsh[let x1 := S1; ...; xn := Sn; in S] => let x1 := Rsh[S1]; ...; xn := Rsh[Sn]; in Rsh[S]
         */
        if (S instanceof Let && isSimple(((Let) S).body())) {
            boolean flag = true;
            for (Def def : ((Let) S).defs())
                flag = flag && isSimple(def.rhs());
            if (flag) {
                AST body = ((Let) S).body();
                Def[] defs = ((Let) S).defs();
                List<Def> def_list = new ArrayList<Def>();
                for (Def def : defs)
                    def_list.add(new Def(def.lhs(), convertRsh(def.rhs())));
                return new Let(def_list.toArray(new Def[0]), convertRsh(body));
            }
        }

        /** 11. If S is letrec p1 := map ... to E1; ...; pn := map ... to En; in S, n > 0:
         *  Rsh[letrec p1 := map ... to E1; ...; pn := map ... to En; in S]
         *  => letrec p1 := Rsh[map... toE1]; ...; pn := Rsh[map... toEn]; in Rsh[S]
         */
        if (S instanceof LetRec && isSimple(((LetRec) S).body())) {
            Def[] defs = ((LetRec) S).defs();
            AST body = ((LetRec) S).body();
            List<Def> def_list = new ArrayList<Def>();
            for (Def def : defs) {
                def_list.add(new Def(def.lhs(), convertRsh(def.rhs())));
            }
            return new LetRec(def_list.toArray(new Def[0]), convertRsh(body));
        }

        /** 12. If S is a block {S1; ...; Sn}, n > 0:
         *  Rsh[{S1; ...; Sn}] => {Rsh[S1]; ...; Rsh[Sn-1]; Rsh[Sn]}
         */
        if (S instanceof Block && ((Block) S).exps().length > 0) {
            boolean flag = true;
            for (AST exp : ((Block) S).exps())
                flag = flag && isSimple(exp);
            if (flag) {
                AST[] exps = ((Block) S).exps();
                List<AST> exp_list = new ArrayList<AST>();
                for (AST exp : exps) {
                    exp_list.add(convertRsh(exp));
                }
                return new Block(exp_list.toArray(new AST[0]));
            }
        }
        
        System.out.println("convertRsh ends");
        System.out.println(S);
        
        return S;
    }
    
    private boolean isPrimitive(AST ast) {
        return CpsSimpleExpVisitor.isPrimitive(ast);
    }
    
    private boolean isSimple(AST ast) {
        return ast.accept(new CpsSimpleExpVisitor());
    }
}

/** Tranverses the AST and decides whether it is simple */
class CpsSimpleExpVisitor implements ASTVisitor<Boolean> {
    
    /** Decides whether an application is primitive */
    public static boolean isPrimitive(AST ast) {
        if(ast instanceof App && ((App) ast).rator() instanceof PrimFun) return true;
        if(ast instanceof UnOpApp) return true;
        if(ast instanceof BinOpApp) return true;
        return false;
    }
    
    /** A bool constant is simple */
    @Override
    public Boolean forBoolConstant(BoolConstant b) { return true; }
    
    /** An int constant is simple */
    @Override
    public Boolean forIntConstant(IntConstant i) { return true; }

    /** A null constant is simple */
    @Override
    public Boolean forNullConstant(NullConstant n) { return true; }

    /** A variable is simple */
    @Override
    public Boolean forVariable(Variable v) { return true; }

    /** A primitive function is simple */
    @Override
    public Boolean forPrimFun(PrimFun f) { return true;}

    /** Recursively decides whether an unary operator appliation is simple */
    @Override
    public Boolean forUnOpApp(UnOpApp u) { return isPrimitive(u) && u.arg().accept(this); }

    /** Recursively decides whether a binary operator appliation is simple */
    @Override
    public Boolean forBinOpApp(BinOpApp b) { return isPrimitive(b) && b.arg1().accept(this) && b.arg2().accept(this); }

    /** An application is simple if all its components as well as itself are simple */
    @Override
    public Boolean forApp(App a) {
        boolean ret = isPrimitive(a) && a.rator().accept(this);
        for (AST arg : a.args())
            ret = ret && arg.accept(this);
        return ret;
    }

    /** A map is simple and is also the end of the traversal */
    @Override
    public Boolean forMap(Map m) { return true; }
    
    /** Recursively decides whether an if-then-else is simple */
    @Override
    public Boolean forIf(If i) { return i.test().accept(this) && i.conseq().accept(this) && i.alt().accept(this); }

    /** Recursively decides whether a Let is simple */
    @Override
    public Boolean forLet(Let l) { return false; }

    /** Recursively decides whether a LetRec is simple */
    @Override
    public Boolean forLetRec(LetRec l) { return false; }

    /** Recursively decides whether a Letcc is simple */
    @Override
    public Boolean forLetcc(Letcc l) { return false; }
    //public Boolean forLetcc(Letcc l) { return l.body().accept(this); }

    /** Recursively decides whether a block is simple */
    @Override
    public Boolean forBlock(Block b) {
        boolean ret = true;
        for(AST exp : b.exps())
            ret = ret && exp.accept(this);
        return ret;
    }
    
}


