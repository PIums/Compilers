import minijava.syntaxtree.*;
import minijava.visitor.DepthFirstVisitor;
import minijava.visitor.GJNoArguDepthFirst;

import java.util.*;

public class CheckVisitor extends GJNoArguDepthFirst<String> {
    HashMap<String, ClassNode> symbolTable;
    String cur_class = null;
    String cur_method = null;

    public CheckVisitor(HashMap<String, ClassNode> table) {
        symbolTable = table;
    }

    public boolean primitive(String type) {
        // System.out.println("checking prim: " + type);
        if (type.equals("int") || type.equals("int[]") || type.equals("bool")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean subtype(String class1, String class2) {
        // System.out.println("check subtype " + class1 + " " + class2);
        if (primitive(class1) || primitive(class2)) {
            if (class1.equals(class2)) {
                return true;
            } else {
                return false;
            }
        }

        while (true) {
            // System.out.println("cur: " + class1 + " " + class2);
            if (class1.equals(class2)) {
                return true;
            }
            ClassNode class_node = symbolTable.get(class1);
            class1 = class_node.parent_name;
            if (class1 == "null") {
                return false;
            }
        }
    }

    /**
     * f0 -> MainClass()
     * f1 -> ( TypeDeclaration() )*
     * f2 -> <EOF>
     */
    @Override
    public String visit(Goal n) {

        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);

        return "hi1";
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> "public"
     * f4 -> "static"
     * f5 -> "String"
     * f6 -> "main"
     * f7 -> "("
     * f8 -> "String"
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
    public String visit(MainClass n) {
        n.f0.accept(this);
        String id = n.f1.accept(this);
        cur_class = id;
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

        cur_method = "main";
        n.f14.accept(this);
        n.f15.accept(this);
        n.f16.accept(this);
        n.f17.accept(this);

        cur_method = null;
        cur_class = null;
        return "hi2";
    }

    /**
     * f0 -> ClassDeclaration()
     * | ClassExtendsDeclaration()
     */
    @Override
    public String visit(TypeDeclaration n) {
        n.f0.accept(this);

        return "hi3";
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
    public String visit(ClassDeclaration n) {
        n.f0.accept(this);
        String id = n.f1.accept(this);
        cur_class = id;
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        n.f5.accept(this);

        cur_class = null;
        return "OK";
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
    public String visit(ClassExtendsDeclaration n) {
        n.f0.accept(this);
        String id = n.f1.accept(this);
        cur_class = id;
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        n.f5.accept(this);
        n.f6.accept(this);
        n.f7.accept(this);

        cur_class = null;
        return "OK";
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    @Override
    public String visit(VarDeclaration n) {
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);

        return "OK";
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
    public String visit(MethodDeclaration n) {
        n.f0.accept(this);
        String return_type1 = n.f1.accept(this); // pure type, method CLAIMS to be this
        String id = n.f2.accept(this);
        cur_method = id;
        n.f3.accept(this);
        n.f4.accept(this);
        n.f5.accept(this);
        n.f6.accept(this);
        n.f7.accept(this);
        n.f8.accept(this);
        n.f9.accept(this);
        String return_type2 = lookup(n.f10.accept(this)); // pure type OR id, should be a SUBTYPE of the claim type1
        n.f11.accept(this);
        n.f12.accept(this);
        // System.out.println("method decl check: " + id + " " + return_type1 + " " +
        // return_type2);

        if (!subtype(return_type2, return_type1)) {
            abort("return types don't agree " + id);
            return "error";
        }
        // System.out.println("ok");

        cur_method = null;
        return "OK";
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> ( FormalParameterRest() )*
     */
    @Override
    public String visit(FormalParameterList n) {
        n.f0.accept(this);
        n.f1.accept(this);

        return "OK";
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    @Override
    public String visit(FormalParameter n) {
        n.f0.accept(this);
        n.f1.accept(this);

        return "OK";
    }

    /**
     * f0 -> ","
     * f1 -> FormalParameter()
     */
    @Override
    public String visit(FormalParameterRest n) {
        return n.f1.accept(this);
    }

    /**
     * f0 -> ArrayType()
     * | BooleanType()
     * | IntegerType()
     * | Identifier()
     */
    @Override
    public String visit(Type n) {
        return n.f0.accept(this);
    }

    /**
     * f0 -> "int"
     * f1 -> "["
     * f2 -> "]"
     */
    @Override
    public String visit(ArrayType n) {
        return "int[]";
    }

    /**
     * f0 -> "boolean"
     */
    @Override
    public String visit(BooleanType n) {
        return "bool";
    }

    /**
     * f0 -> "int"
     */
    @Override
    public String visit(IntegerType n) {
        return "int";
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
    public String visit(Statement n) {
        n.f0.accept(this);

        return "hi4";
    }

    /**
     * f0 -> "{"
     * f1 -> ( Statement() )*
     * f2 -> "}"
     */
    @Override
    public String visit(Block n) {
        return n.f1.accept(this);
    }

    /**
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    public String lookup(String id) {
        if (id.equals("int") || id.equals("int[]") || id.equals("bool")) { // built in type
            return id;
        } else if (symbolTable.containsKey(id)) { // constructed type
            return id;
        }
        String type = null;
        // System.out.println("assignment statement " + cur_class + " " + cur_method + "
        // " + id);
        // System.out.println("id: " + id);

        ClassNode class_node = symbolTable.get(cur_class);

        String s1 = class_node.getLocal(id, cur_method);
        String s2 = class_node.getField(id);
        // System.out.println("s1, s2: " + s1 + " " + s2);

        if (s1 != null) {
            type = s1;
        } else if (s2 != null) {
            type = s2;
        }

        if (type == null) {
            abort("null type found: " + id);
            return "error";
        }

        return type;
    }

    @Override
    public String visit(AssignmentStatement n) {
        String id = n.f0.accept(this);
        String type1 = lookup(id);

        String type2 = lookup(n.f2.accept(this));
        // System.out.println("assign check: " + id + " " + type1 + " " + type2);

        if (type1 == null || !subtype(type2, type1)) { // type2 should be a subtype of type1
            // System.out.println("assignment statement fail");
            // System.exit(1);
            abort("assignment expression type mismatch: " + id + " " + type1 + " " + type2 + " " + cur_class + " "
                    + cur_method);
            return "error";
        } else {
            return type1;
        }
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
    public String visit(ArrayAssignmentStatement n) {
        // System.out.println("array assignment statement");
        String type1 = lookup(n.f0.accept(this));
        n.f1.accept(this);
        String type2 = lookup(n.f2.accept(this));
        n.f3.accept(this);
        n.f4.accept(this);
        String type3 = lookup(n.f5.accept(this));
        n.f6.accept(this);

        if (!type1.equals("int[]") || !type2.equals("int") || !type3.equals("int")) {
            abort("array assignment error: " + type1 + " " + type2 + " " + type3);
            return "error";
        }

        return "hi5";
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
    public String visit(IfStatement n) {
        // System.out.println("if statement");
        String type = lookup(n.f2.accept(this));
        if (!type.equals("bool")) {
            abort("if statement not given bool");
            return "error";
        }

        n.f4.accept(this);
        n.f6.accept(this);

        return "OK";
    }

    /**
     * f0 -> "while"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     */
    @Override
    public String visit(WhileStatement n) {
        // System.out.println("while statement");
        String type = lookup(n.f2.accept(this));
        if (!type.equals("bool")) {
            abort("while expression " + type);
            return "error";
        }

        return n.f4.accept(this);
    }

    /**
     * f0 -> "System.out.println"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> ";"
     */
    @Override
    public String visit(PrintStatement n) {
        // System.out.println("print statement");
        String type = lookup(n.f2.accept(this));
        if (!type.equals("int")) {
            abort("Print not given int: " + type);
            return "error";
        }

        return "OK";
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
    public String visit(Expression n) {
        return n.f0.accept(this);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "&&"
     * f2 -> PrimaryExpression()
     */
    @Override
    public String visit(AndExpression n) {
        String type1 = lookup(n.f0.accept(this));
        String type2 = lookup(n.f2.accept(this));
        // System.out.println("and expression: " + type1 + " " + type2);
        if (type1.equals("bool") && type2.equals("bool")) {
            return "bool";
        } else {
            abort("and expression");
            return "error";
        }
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    @Override
    public String visit(CompareExpression n) {
        String type1 = lookup(n.f0.accept(this));
        String type2 = lookup(n.f2.accept(this));
        // System.out.println("compare expression: " + type1 + " " + type2);
        if (type1.equals("int") && type2.equals("int")) {
            return "bool";
        } else {
            abort("compare expression");
            return "error";
        }
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    @Override
    public String visit(PlusExpression n) {
        String type1 = lookup(n.f0.accept(this));
        String type2 = lookup(n.f2.accept(this));
        // System.out.println("plus expression: " + type1 + " " + type2);
        if (type1.equals("int") && type2.equals("int")) {
            return "int";
        } else {
            abort("plus expression");
            return "error";
        }
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    @Override
    public String visit(MinusExpression n) {
        String type1 = lookup(n.f0.accept(this));
        String type2 = lookup(n.f2.accept(this));
        // System.out.println("minus expression: " + type1 + " " + type2);
        if (type1.equals("int") && type2.equals("int")) {
            return "int";
        } else {
            abort("minus expression");
            return "error";
        }
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    @Override
    public String visit(TimesExpression n) {
        String type1 = lookup(n.f0.accept(this));
        String type2 = lookup(n.f2.accept(this));
        // System.out.println("times expression: " + type1 + " " + type2);
        if (type1.equals("int") && type2.equals("int")) {
            return "int";
        } else {
            abort("times expression");
            return "error";
        }
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    @Override
    public String visit(ArrayLookup n) {
        String type1 = lookup(n.f0.accept(this));
        String type2 = lookup(n.f2.accept(this));

        if (!type1.equals("int[]") || !type2.equals("int")) {
            abort("array lookup error");
            return "error";
        }

        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    @Override
    public String visit(ArrayLength n) {
        String type = lookup(n.f0.accept(this));
        if (type.equals("int[]")) {
            return "int";
        } else {
            abort("array length");
            return "error";
        }
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
    public String visit(MessageSend n) {
        // two cases:
        // Case 1: it's the class itself: this, new A().run(), new B().f() etc.
        // Case 2: it's an object: a.run(), b.f()
        // How to distinguish?
        String class_name = n.f0.accept(this);
        // Scuffed solution
        if (!symbolTable.containsKey(class_name)) {
            class_name = lookup(class_name);
        }
        String method_name = n.f2.accept(this);
        if (!symbolTable.containsKey(class_name)) {
            abort("message send class does not exist " + class_name);
            return "error";
        }
        ClassNode class_node = symbolTable.get(class_name);
        if (!class_node.methods.containsKey(method_name)) {
            abort("message send method does not exist " + class_name + " , " + method_name);
            return "error";
        }

        MethodNode method_node = class_node.methods.get(method_name);
        // confirm that the method exists
        // System.out.println("break 1");
        ExpressionList exp = (ExpressionList) n.f4.node;
        if (exp == null) {
            if (method_node.formal_params.size() == 0) {
                return method_node.return_type;
            } else {
                abort("method given no args, needed >=1: " + method_name);
                return "error";
            }
        }

        Expression first_arg = exp.f0;
        Vector<Node> args = exp.f1.nodes;
        LinkedHashMap<String, String> formal_params = method_node.formal_params;

        int num = args.size() + 1;
        if (method_node.formal_params.size() != num) {
            abort("method given diff number of params: " + num + " " + formal_params.size() + " "
                    + method_name);
            return "error";
        }

        /* NOTE: NEED TO FIX THIS ISSUE OF ACCEPTING SUPERTYPES FUCK */
        ArrayList<String> types = new ArrayList<String>();
        types.add(lookup(first_arg.accept(this)));
        for (int i = 0; i < num - 1; i++) {
            String type = lookup(args.get(i).accept(this)); // not necessarily pure type without the "lookup"
            types.add(type);
            // System.out.println("i: " + i + " " + type);
        }

        int ind = 0;
        for (String var : formal_params.keySet()) {
            String type1 = types.get(ind);
            String type2 = formal_params.get(var);
            if (!subtype(type1, type2)) {
                abort("mismatch param types: " + ind + " " + type1 + " " + type2);
                return "error";
            }
            ind++;
        }

        // System.out.println(args.size());

        return method_node.return_type;
    }

    /**
     * f0 -> Expression()
     * f1 -> ( ExpressionRest() )*
     */
    @Override
    public String visit(ExpressionList n) {
        String s = n.f0.accept(this);
        if (s.equals("error")) {
            abort("expression list");
            return "error";
        }
        return n.f1.accept(this);
    }

    /**
     * f0 -> ","
     * f1 -> Expression()
     */
    @Override
    public String visit(ExpressionRest n) {
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
    public String visit(PrimaryExpression n) {
        return n.f0.accept(this);
    }

    /**
     * f0 -> <INTEGER_LITERAL>
     */
    @Override
    public String visit(IntegerLiteral n) {
        return "int";
    }

    /**
     * f0 -> "true"
     */
    @Override
    public String visit(TrueLiteral n) {
        return "bool";
    }

    /**
     * f0 -> "false"
     */
    @Override
    public String visit(FalseLiteral n) {
        return "bool";
    }

    /**
     * f0 -> <IDENTIFIER>
     */
    @Override
    public String visit(Identifier n) {
        return n.f0.toString();
    }

    /**
     * f0 -> "this"
     */
    @Override
    public String visit(ThisExpression n) {
        // System.out.println("this expression here: " + cur_class);
        return cur_class;
    }

    /**
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    @Override
    public String visit(ArrayAllocationExpression n) {
        String type = lookup(n.f3.accept(this));

        if (!type.equals("int")) {
            abort("array allocation needs int length");
        }

        return "int[]";
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    @Override
    public String visit(AllocationExpression n) {
        n.f0.accept(this);
        String id = n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);

        return id;
    }

    /**
     * f0 -> "!"
     * f1 -> Expression()
     */
    @Override
    public String visit(NotExpression n) {
        n.f0.accept(this);
        n.f1.accept(this);
        String type = lookup(n.f1.accept(this));
        if (type.equals("bool")) {
            return "bool";
        } else {
            abort("not expression");
            return "error";
        }
    }

    /**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
    @Override
    public String visit(BracketExpression n) {
        return n.f1.accept(this);
    }

    public void abort(String s) {
        System.out.println("Type error");
        System.err.println(s);
        System.exit(1);
    }
}