import minijava.syntaxtree.*;
import minijava.visitor.GJNoArguDepthFirst;

import java.util.*;

public class TranslateVisitor extends GJNoArguDepthFirst<Result> {
    HashMap<String, ClassNode> symbolTable;
    HashMap<String, String> typeLookup;
    int counter = 0;
    int flags = 0;
    int counter1 = 0;
    // String output = "";
    ArrayList<String> output;
    String cur_class = null;
    String cur_method = null;
    String top_class = null;
    String this_change = null;

    public TranslateVisitor(HashMap<String, ClassNode> table, HashMap<String, String> typeLookup) {
        this.symbolTable = table;
        this.typeLookup = typeLookup;
        output = new ArrayList<String>();
    }

    public void prettyprint(boolean lines) {
        for (int i = 0; i < output.size(); i++) {
            if (lines) {
                System.out.print((i + 1) + ": ");
            }
            System.out.println(output.get(i));
        }
    }

    String getType(String id) {
        // actual id, not v_i stuff
        // ignore ids: class, function names,
        if (cur_class == null || cur_method == null) {
            return "ntype";
        }
        ClassNode class_node = symbolTable.get(cur_class);
        String type = class_node.getType(id, cur_method);
        if (type == null) {
            return "ntype";
        }
        return type;
    }

    public int k() {
        counter++;
        return counter;
    }

    public int kno() {
        return counter;
    }

    /**
     * f0 -> MainClass()
     * f1 -> ( TypeDeclaration() )*
     * f2 -> <EOF>
     */
    @Override
    public Result visit(Goal n) {
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        return new Result("goal", -1);
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> "public"
     * f4 -> "static"
     * f5 -> "void"
     * f6 -> "main"
     * f7 -> "("
     * f8 -> "Result"
     * f9 -> "["
     * f10 -> "]"
     * f11 -> Identifier()
     * f12 -> ")"
     * f13 -> "{"
     * f14 -> ( VarDeclaration() )*
     * f15 -> ( Statement() )*
     * f16 -> "}"
     * f17 -> "}"
     */
    @Override
    public Result visit(MainClass n) {
        String id = "XMQU" + n.f1.f0.toString();
        cur_method = "main";
        cur_class = id;
        output.add("func Main()");

        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        n.f5.accept(this);
        n.f6.accept(this);
        n.f7.accept(this);
        n.f8.accept(this);
        n.f9.accept(this);
        n.f10.accept(this);
        n.f11.accept(this);
        n.f12.accept(this);
        n.f13.accept(this);
        n.f14.accept(this);
        n.f15.accept(this);
        n.f16.accept(this);
        n.f17.accept(this);
        output.add("return v1");

        cur_method = null;
        cur_class = null;
        return new Result("goal", -1);
    }

    /**
     * f0 -> ClassDeclaration()
     * | ClassExtendsDeclaration()
     */
    @Override
    public Result visit(TypeDeclaration n) {
        n.f0.accept(this);
        return new Result("goal", -1);
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    @Override
    public Result visit(ClassDeclaration n) {
        String id = "XMQU" + n.f1.f0.toString();
        cur_class = id;

        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        n.f5.accept(this);

        cur_class = null;
        return new Result("goal", -1);
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "extends"
     * f3 -> Identifier()
     * f4 -> "{"
     * f5 -> ( VarDeclaration() )*
     * f6 -> ( MethodDeclaration() )*
     * f7 -> "}"
     */
    @Override
    public Result visit(ClassExtendsDeclaration n) {
        String id = "XMQU" + n.f1.f0.toString();
        cur_class = id;

        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        n.f5.accept(this);
        n.f6.accept(this);
        n.f7.accept(this);

        cur_class = null;
        return new Result("goal", -1);
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    @Override
    public Result visit(VarDeclaration n) {
        return new Result("vardeclaration", -1);
    }

    /**
     * f0 -> "public"
     * f1 -> Type()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )?
     * f5 -> ")"
     * f6 -> "{"
     * f7 -> ( VarDeclaration() )*
     * f8 -> ( Statement() )*
     * f9 -> "return"
     * f10 -> Expression()
     * f11 -> ";"
     * f12 -> "}"
     */
    @Override
    public Result visit(MethodDeclaration n) {
        String id = "XMQU" + n.f2.f0.toString();
        cur_method = id;
        MethodNode method_node = symbolTable.get(cur_class).methods.get(id);
        String name = cur_class + id;
        String s = "func " + name + "(this ";
        for (String param : method_node.formal_params.keySet()) {
            s += param + " ";
        }
        s += ")";
        // output += s;
        output.add(""); // empty line
        output.add(s);

        // initialize field variables to their locations

        ArrayList<String> init = symbolTable.get(cur_class).initializeMethod(id);
        for (String line : init) {
            output.add(line);
        }

        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        n.f5.accept(this);
        n.f6.accept(this);
        n.f7.accept(this);
        n.f8.accept(this);
        n.f9.accept(this);
        n.f10.accept(this);
        n.f11.accept(this);
        n.f12.accept(this);

        ArrayList<String> store = symbolTable.get(cur_class).storeMethod(id);
        for (String line : store) {
            output.add(line);
        }

        int k = kno();
        // counter++;
        // System.out.println("before return");
        Result r = n.f10.accept(this);
        output.add("return v" + k);

        cur_method = null;
        return new Result(s, k + 1);
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> ( FormalParameterResultest() )*
     */
    @Override
    public Result visit(FormalParameterList n) {
        n.f0.accept(this);
        n.f1.accept(this);
        return new Result("goal", -1);
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    @Override
    public Result visit(FormalParameter n) {
        n.f0.accept(this);
        n.f1.accept(this);
        return new Result("goal", -1);
    }

    /**
     * f0 -> ","
     * f1 -> FormalParameter()
     */
    @Override
    public Result visit(FormalParameterRest n) {
        n.f0.accept(this);
        n.f1.accept(this);
        return new Result("goal", -1);
    }

    /**
     * f0 -> ArrayType()
     * | BooleanType()
     * | IntegerType()
     * | Identifier()
     */
    @Override
    public Result visit(Type n) {
        return new Result("type", -1);
    }

    /**
     * f0 -> "int"
     * f1 -> "["
     * f2 -> "]"
     */
    @Override
    public Result visit(ArrayType n) {
        return new Result("arr", -1);
    }

    /**
     * f0 -> "boolean"
     */
    @Override
    public Result visit(BooleanType n) {
        return new Result("bool", -1);
    }

    /**
     * f0 -> "int"
     */
    @Override
    public Result visit(IntegerType n) {
        return new Result("int", -1);
    }

    /**
     * f0 -> Block()
     * | AssignmentStatement()
     * | ArrayAssignmentStatement()
     * | IfStatement()
     * | WhileStatement()
     * | PrintStatement()
     */
    @Override
    public Result visit(Statement n) {
        // System.out.println("INTERMEDIARY STATEMENT");
        Result r = n.f0.accept(this);
        // System.out.println("statement: " + r);
        return r;
    }

    /**
     * f0 -> "{"
     * f1 -> ( Statement() )*
     * f2 -> "}"
     */
    @Override
    public Result visit(Block n) {
        int k = kno();
        for (Node statement : n.f1.nodes) {
            Result r = statement.accept(this);
            k = r.c;
        }
        // System.out.println("block: " + k);
        return new Result("block", k);
    }

    HashSet<String> initialized = new HashSet<String>();

    String lookup(Identifier n) {
        String id = "XMQU" + n.f0.toString();
        // output.add("id: " + id);

        if (symbolTable.containsKey(id)) {
            return null;
        }

        // check if field variable... do shadowing later fml
        ClassNode class_node = symbolTable.get(cur_class);

        return id; // kind of sus.
    }

    String lookupval(Identifier n) {
        String id = "XMQU" + n.f0.toString();
        // output.add("id: " + id);

        // check if field variable... do shadowing later fml
        ClassNode class_node = symbolTable.get(cur_class);

        return id; // kind of sus.
    }

    /**
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    @Override
    public Result visit(AssignmentStatement n) {
        int k = kno();
        String id = lookup(n.f0);
        Result r = n.f2.accept(this);
        String s = id + " = " + "v" + k + "\n";
        // output += s;
        output.add(id + " = " + "v" + k);
        // System.out.println("res: " + r.code + s + " " + r.c);
        return new Result(r.code + s, r.c);
    }

    /**
     * f0 -> Identifier()
     * f1 -> "["
     * f2 -> Expression()
     * f3 -> "]"
     * f4 -> "="
     * f5 -> Expression()
     * f6 -> ";"
     */
    @Override
    public Result visit(ArrayAssignmentStatement n) {
        String id = lookup(n.f0); // sketch
        int k = kno();

        Result r1 = n.f2.accept(this);
        Result r2 = n.f5.accept(this);
        checkbound(id, "v" + k);

        sizeup("v" + r2.c, "v" + k);
        counter++;

        output.add("index = " + id + " + v" + r2.c);
        output.add("[index + 0] = v" + r1.c);
        return new Result("array assignment", r2.c + 1);
    }

    /**
     * f0 -> "if"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     * f5 -> "else"
     * f6 -> Statement()
     */
    @Override
    public Result visit(IfStatement n) {
        int k = kno();
        Result r1 = n.f2.accept(this);
        String elseflag = "else" + flags;
        String endflag = "end" + flags;
        flags++;

        String s = "if0 v" + k + " goto " + elseflag;
        // output += s;
        output.add(s);

        Result r2 = n.f4.accept(this);
        output.add("goto " + endflag);
        output.add(elseflag + ":");

        Result r3 = n.f6.accept(this);
        output.add(endflag + ":");
        return new Result("yo what the", r3.c);
    }

    /**
     * f0 -> "while"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     */
    @Override
    public Result visit(WhileStatement n) {
        int k = kno();

        String loopflag = "loop" + flags;
        String endflag = "end" + flags;
        flags++;
        output.add(loopflag + ":");

        Result r1 = n.f2.accept(this);
        output.add("if0 v" + k + " goto " + endflag);

        Result r2 = n.f4.accept(this);

        output.add("goto " + loopflag);

        output.add(endflag + ":");

        return new Result("idkwhile", r2.c);
    }

    /**
     * f0 -> "System.out.println"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> ";"
     */
    @Override
    public Result visit(PrintStatement n) {
        int k = kno();

        Result r = n.f2.accept(this);
        // System.out.println("cur counter: " + counter);

        String s = "print(" + "v" + k + ")";
        output.add(s);
        return new Result(s, r.c);
    }

    /**
     * f0 -> AndExpression()
     * | CompareExpression()
     * | PlusExpression()
     * | MinusExpression()
     * | TimesExpression()
     * | ArrayLookup()
     * | ArrayLength()
     * | MessageSend()
     * | PrimaryExpression()
     */
    @Override
    public Result visit(Expression n) {
        return n.f0.accept(this);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "&&"
     * f2 -> PrimaryExpression()
     */
    @Override
    public Result visit(AndExpression n) {
        // System.out.println("and expression");
        int k = kno();
        counter++;
        String notflag = "not" + flags;
        String endflag = "end" + flags;
        flags++;

        Result r1 = n.f0.accept(this);
        // String s = "if0 v" + (k + 1) + " goto " + endflag + "\n";
        output.add("if0 v" + (k + 1) + " goto " + notflag);

        Result r2 = n.f2.accept(this);
        output.add("if0 v" + (r1.c) + " goto " + notflag);

        output.add("v" + k + " = 1");
        output.add("goto " + endflag);

        output.add(notflag + ":");
        output.add("v" + k + " = 0");

        output.add(endflag + ":");

        return new Result("ooga booga", r2.c);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    @Override
    public Result visit(CompareExpression n) {
        // System.out.println("compare expression");
        int k = kno();
        counter++;
        Result r1 = n.f0.accept(this);
        Result r2 = n.f2.accept(this);

        String s = "v" + k + " = v" + (k + 1) + " < v" + r1.c;
        output.add(s);
        return new Result(r1.code + r2.code + s, r2.c);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    @Override
    public Result visit(PlusExpression n) {
        // System.out.println("plus expression");
        int k = kno();
        counter++;
        Result r1 = n.f0.accept(this);
        Result r2 = n.f2.accept(this);

        String s = "v" + k + " = v" + (k + 1) + " + v" + r1.c;
        // output += s;
        output.add(s);
        return new Result(r1.code + r2.code + s, r2.c);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    @Override
    public Result visit(MinusExpression n) {
        // System.out.println("minus expression");
        int k = kno();
        counter++;
        Result r1 = n.f0.accept(this);
        Result r2 = n.f2.accept(this);

        String s = "v" + k + " = v" + (k + 1) + " - v" + r1.c;
        // output += s;
        output.add(s);

        // System.out.println("minus: " + r1 + "\n" + r2);
        return new Result(r1.code + r2.code + s, r2.c);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    @Override
    public Result visit(TimesExpression n) {
        int k = kno();
        counter++;
        Result r1 = n.f0.accept(this);
        Result r2 = n.f2.accept(this);

        String s = "v" + k + " = v" + (k + 1) + " * v" + r1.c;
        output.add(s);

        return new Result(r1.code + r2.code + s, r2.c);
    }

    void sizeup(String id1, String id2) {
        output.add("one = 1");
        output.add("four = 4");
        output.add(id1 + " = one + " + id2);
        output.add(id1 + " = four * " + id1);
    }

    void checkbound(String arr, String ind) {
        // output.add("checking bounds on " + arr + " " + ind);
        String okflag = "ok" + flags;
        String okkflag = "okk" + flags;
        // String endflag = "end" + flags;
        flags++;

        output.add("one = 1");
        output.add("zero = 0");

        output.add("length = [" + arr + " + 0]");
        output.add("comp = " + ind + " < length");
        output.add("negcomp = one - comp");
        output.add("if0 negcomp goto " + okflag);
        output.add("error(\"array index out of bounds\")");
        output.add(okflag + ":");
        output.add("neg1 = zero - one");
        output.add("comp = neg1 < " + ind);
        output.add("negcomp = one - comp");
        output.add("if0 negcomp goto " + okkflag);
        output.add("error(\"array index out of bounds\")");
        output.add(okkflag + ":");
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    @Override
    public Result visit(ArrayLookup n) {
        int k = kno();
        counter++;

        Result r1 = n.f0.accept(this);
        Result r2 = n.f2.accept(this);

        checkbound("v" + (k + 1), "v" + r1.c);

        sizeup("v" + r2.c, "v" + r1.c);
        counter++;
        output.add("index = v" + (k + 1) + " + v" + r2.c);
        output.add("v" + k + " = [index + 0]");

        return new Result("array lookup", r2.c + 1);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    @Override
    public Result visit(ArrayLength n) {
        int k = kno();
        counter++;
        Result r = n.f0.accept(this);
        String s = "v" + k + " = [v" + (k + 1) + " + 0]";
        // output += s;
        output.add(s);

        return new Result(s, k + 1);
    }

    void checknull(String id) {
        String errorflag = "error" + flags;
        String endflag = "end" + flags;
        flags++;
        output.add("if0 " + id + " goto " + errorflag);
        output.add("goto " + endflag);
        output.add(errorflag + ":");
        output.add("error(\"null pointer\")");
        output.add(endflag + ":");
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */
    @Override
    public Result visit(MessageSend n) {
        ArrayList<String> store = symbolTable.get(cur_class).storeMethod(cur_method);
        for (String s : store) {
            output.add(s);
        }
        int k = kno();
        counter++;
        Result r1 = n.f0.accept(this);
        // Result r2 = n.f4.accept(this);

        String get_class = top_class;
        String get_method = "XMQU" + n.f2.f0.toString();
        int ind = symbolTable.get(get_class).method_id.get(get_method);

        ArrayList<String> ids = new ArrayList<String>();
        ids.add("v" + (k + 1));
        checknull("v" + (k + 1));

        ExpressionList exp = (ExpressionList) n.f4.node;
        if (exp != null) {
            Expression first_arg = exp.f0;
            ids.add("v" + r1.c);
            r1 = first_arg.accept(this);

            Vector<Node> args = exp.f1.nodes;

            int num = args.size() + 1;
            for (int i = 0; i < num - 1; i++) {
                ids.add("v" + (r1.c));
                r1 = args.get(i).accept(this);
            }
        }

        output.add("v" + r1.c + " = [v" + (k + 1) + " + 0]");
        output.add("v" + (r1.c + 1) + " = [v" + r1.c + " + " + (4 * ind) + "]");
        counter++;
        counter++;
        String s1 = "v" + k + " = call v" + (r1.c + 1) + " (";
        for (int i = 0; i < ids.size(); i++) {
            s1 += ids.get(i);
            if (i != ids.size() - 1)
                s1 += " ";
        }
        s1 += ")";
        // output += s1;
        output.add(s1);

        // System.out.println("done with message send");
        ArrayList<String> init = symbolTable.get(cur_class).initializeMethod(cur_method);
        for (String s : init) {
            output.add(s);
        }
        return new Result(s1, (r1.c + 2));
    }

    /**
     * f0 -> Expression()
     * f1 -> ( ExpressionList() )*
     */
    @Override
    public Result visit(ExpressionList n) { // change this
        Result r1 = n.f0.accept(this);
        Result r2 = n.f1.accept(this);
        // System.out.println("here at expression list");

        if (r2 == null) {
            return new Result(r1.code, r1.c);
        } else {
            return new Result(r1.code + r2.code, r2.c);
        }
    }

    /**
     * f0 -> ","
     * f1 -> Expression()
     */
    @Override
    public Result visit(ExpressionRest n) {
        return n.f1.accept(this);
    }

    /**
     * f0 -> IntegerLiteral()
     * | TrueLiteral()
     * | FalseLiteral()
     * | Identifier()
     * | ThisExpression()
     * | ArrayAllocationExpression()
     * | AllocationExpression()
     * | NotExpression()
     * | BracketExpression()
     */
    @Override
    public Result visit(PrimaryExpression n) {
        // System.out.println("primary expression: " + r);
        return n.f0.accept(this);
    }

    /**
     * f0 -> <INTEGEResult_LITEResultAL>
     */
    @Override
    public Result visit(IntegerLiteral n) {
        // output.add("new literal here: " + counter);
        int k = kno();
        counter++;
        String s = "v" + k + " = " + n.f0.tokenImage;
        // output += s;
        output.add(s);
        return new Result(s, kno());
    }

    /**
     * f0 -> "true"
     */
    @Override
    public Result visit(TrueLiteral n) {
        int k = kno();
        counter++;
        String s = "v" + k + " = 1";
        // output += s;
        output.add(s);
        return new Result(s, kno());
    }

    /**
     * f0 -> "false"
     */
    @Override
    public Result visit(FalseLiteral n) {
        int k = kno();
        counter++;
        String s = "v" + k + " = 0";
        // output += s;
        output.add(s);
        return new Result(s, kno());
    }

    /**
     * f0 -> <IDENTIFIEResult>
     */
    @Override
    public Result visit(Identifier n) {
        String id = "XMQU" + n.f0.toString();
        // System.out.println("at identifier: " + id);
        String type = getType(id);
        if (type.equals("ntype")) {
            return null;
        } else {
            // System.out.println("setting top_class to " + type + " " + id);
            top_class = type;
        }
        int k = kno();
        counter++;
        String s = "v" + k + " = " + id;
        // output += s;
        output.add(s);
        // System.out.println("iden returning: " + id);
        return new Result("XMQU" + n.f0.toString(), k + 1);
    }

    /**
     * f0 -> "this"
     */
    @Override
    public Result visit(ThisExpression n) {
        int k = kno();
        counter++;
        String s = "v" + k + " = this";
        // output += s;
        output.add(s);
        top_class = cur_class;
        // System.out.println("setting top_class to this: " + top_class);
        return new Result("this\n", k + 1);
    }

    /**
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    @Override
    public Result visit(ArrayAllocationExpression n) {
        int k = kno();
        counter++;
        Result r = n.f3.accept(this);

        sizeup("v" + r.c, "v" + (k + 1));
        counter++;
        output.add("v" + k + " = alloc(v" + r.c + ")");
        output.add("[v" + k + " + 0] = v" + (k + 1));
        return new Result("array allocation", r.c + 1);
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    @Override
    public Result visit(AllocationExpression n) {
        int k = kno();
        counter++;
        String id = "XMQU" + n.f1.f0.toString();
        // String init = symbolTable.get(id).initialize("v" + k);
        ArrayList<String> init = symbolTable.get(id).initialize("v" + k);

        for (String s : init) {
            output.add(s);
        }
        top_class = id;
        return new Result("allocation expression", k + 1);
    }

    /**
     * f0 -> "!"
     * f1 -> Expression()
     */
    @Override
    public Result visit(NotExpression n) {
        int k = kno();
        counter += 2; // going to save k, k+1 here. remember this style oh man.

        Result r = n.f1.accept(this);
        // String s = "v" + (k + 1) + " = 1\n";
        // s += "v" + k + " = v" + (k + 1) + " - v" + (k + 2) + "\n";
        // output += s;
        output.add("v" + (k + 1) + " = 1");
        output.add("v" + k + " = v" + (k + 1) + " - v" + (k + 2));
        return new Result("oma gosh", r.c);
    }

    /**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
    @Override
    public Result visit(BracketExpression n) {
        return n.f1.accept(this);
    }

}