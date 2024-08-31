import minijava.syntaxtree.*;
import minijava.visitor.GJNoArguDepthFirst;

import java.util.*;

public class MethodTableVisitor extends GJNoArguDepthFirst<String> {
    HashMap<String, ClassNode> symbolTable;
    HashMap<String, String> typeLookup;
    String cur_class = null;
    String cur_method = null;

    HashMap<String, Boolean> initialized;

    public MethodTableVisitor() {
        symbolTable = new HashMap<String, ClassNode>();
        typeLookup = new HashMap<String, String>();

        initialized = new HashMap<String, Boolean>();
    }

    public void finish() {
        // System.out.println("finishing");
        for (int i = 0; i < 1000; i++) {
            int left = 0;
            for (String class_name : initialized.keySet()) {
                if (initialized.get(class_name)) {
                    continue; // initialized already
                }

                ClassNode class_node = symbolTable.get(class_name);
                String par_name = class_node.parent_name;
                if (!symbolTable.containsKey(par_name)) {
                    System.out.println("parent node is nonexistent");
                    System.exit(1);
                }

                if (initialized.get(par_name)) {
                    class_node.add(symbolTable.get(par_name));
                    initialized.put(class_name, true);
                } else {
                    left++;
                }
            }

            if (left == 0) {
                // System.out.println("done with finish");
                return;
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
        String _ret = null;
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        return _ret;
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
        String id = n.f1.accept(this);
        // System.out.println("id: " + id);
        cur_class = id;
        cur_method = "main";
        initialized.put(id, true);
        ClassNode new_class = new ClassNode(id);
        new_class.addMethod("main", "int");
        symbolTable.put(id, new_class);

        String _ret = null;
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
        typeLookup.put(n.f11.accept(this), "String[] lol");
        n.f12.accept(this);
        n.f13.accept(this);
        n.f14.accept(this);

        n.f15.accept(this);
        n.f16.accept(this);
        n.f17.accept(this);

        cur_method = null;
        cur_class = null;
        return _ret;
    }

    /**
     * f0 -> ClassDeclaration()
     * | ClassExtendsDeclaration()
     */
    @Override
    public String visit(TypeDeclaration n) {
        String _ret = null;
        n.f0.accept(this);
        return _ret;
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
        String id = n.f1.accept(this);
        cur_class = id;
        initialized.put(id, true);
        ClassNode new_class = new ClassNode(id);
        symbolTable.put(id, new_class);
        // System.out.println("adding new class: " + id);

        String _ret = null;
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        n.f5.accept(this);

        cur_class = null;
        return "class decl";
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
        String id = n.f1.accept(this);
        String par_id = n.f3.accept(this);
        cur_class = id;
        initialized.put(id, false);

        ClassNode new_class = new ClassNode(id);
        new_class.parent_name = par_id;
        // System.out.println("extends decl: " + id + " " + par_id);
        symbolTable.put(id, new_class);

        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        n.f5.accept(this);
        n.f6.accept(this);
        n.f7.accept(this);

        cur_class = id;
        return "class extends";
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    @Override
    public String visit(VarDeclaration n) {
        // System.out.println("var declaration?");
        String type = n.f0.accept(this);
        String id = n.f1.accept(this);
        typeLookup.put(id, type);
        if (cur_method == null) {
            symbolTable.get(cur_class).addField(id, type);
        } else {
            symbolTable.get(cur_class).addLocal(id, cur_method, type);
        }
        return null;
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
        String return_type = n.f1.accept(this);
        String id = n.f2.accept(this);
        cur_method = id;

        // System.out.println("method declaration " + id + " " + cur_class);

        ClassNode class_node = symbolTable.get(cur_class);
        class_node.addMethod(id, return_type);
        String _ret = null;
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

        cur_method = null;
        return _ret;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> ( FormalParameterStringest() )*
     */
    @Override
    public String visit(FormalParameterList n) {
        String _ret = null;
        n.f0.accept(this);
        n.f1.accept(this);
        return _ret;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    @Override
    public String visit(FormalParameter n) {
        String type = n.f0.accept(this);
        // type = "int";
        String id = n.f1.accept(this);
        symbolTable.get(cur_class).addParam(id, cur_method, type);
        String _ret = null;
        n.f0.accept(this);
        n.f1.accept(this);
        return _ret;
    }

    /**
     * f0 -> ","
     * f1 -> FormalParameter()
     */
    @Override
    public String visit(FormalParameterRest n) {
        String _ret = null;
        n.f0.accept(this);
        n.f1.accept(this);
        return _ret;
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
        String _ret = null;
        n.f0.accept(this);
        return _ret;
    }

    /**
     * f0 -> "{"
     * f1 -> ( Statement() )*
     * f2 -> "}"
     */
    @Override
    public String visit(Block n) {
        String _ret = null;
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        return _ret;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    @Override
    public String visit(AssignmentStatement n) {
        String _ret = null;
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        return _ret;
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
        String _ret = null;
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        n.f5.accept(this);
        n.f6.accept(this);
        return _ret;
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
        String _ret = null;
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        n.f5.accept(this);
        n.f6.accept(this);
        return _ret;
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
        String _ret = null;
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        return _ret;
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
        String _ret = null;
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        return _ret;
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
        String _ret = null;
        n.f0.accept(this);
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "&&"
     * f2 -> PrimaryExpression()
     */
    @Override
    public String visit(AndExpression n) {
        String _ret = null;
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    @Override
    public String visit(CompareExpression n) {
        String _ret = null;
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    @Override
    public String visit(PlusExpression n) {
        String _ret = null;
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    @Override
    public String visit(MinusExpression n) {
        String _ret = null;
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    @Override
    public String visit(TimesExpression n) {
        String _ret = null;
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    @Override
    public String visit(ArrayLookup n) {
        String _ret = null;
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    @Override
    public String visit(ArrayLength n) {
        String _ret = null;
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        return _ret;
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
        String _ret = null;
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        n.f5.accept(this);
        return _ret;
    }

    /**
     * f0 -> Expression()
     * f1 -> ( ExpressionList() )*
     */
    @Override
    public String visit(ExpressionList n) {
        String _ret = null;
        n.f0.accept(this);
        n.f1.accept(this);
        return _ret;
    }

    /**
     * f0 -> ","
     * f1 -> Expression()
     */
    @Override
    public String visit(ExpressionRest n) {
        String _ret = null;
        n.f0.accept(this);
        n.f1.accept(this);
        return _ret;
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
        String _ret = null;
        n.f0.accept(this);
        return _ret;
    }

    /**
     * f0 -> <INTEGEString_LITEStringAL>
     */
    @Override
    public String visit(IntegerLiteral n) {
        String _ret = null;
        n.f0.accept(this);
        return _ret;
    }

    /**
     * f0 -> "true"
     */
    @Override
    public String visit(TrueLiteral n) {
        String _ret = null;
        n.f0.accept(this);
        return _ret;
    }

    /**
     * f0 -> "false"
     */
    @Override
    public String visit(FalseLiteral n) {
        String _ret = null;
        n.f0.accept(this);
        return _ret;
    }

    /**
     * f0 -> <IDENTIFIEString>
     */
    @Override
    public String visit(Identifier n) {
        return "XMQU" + n.f0.toString();
    }

    /**
     * f0 -> "this"
     */
    @Override
    public String visit(ThisExpression n) {
        String _ret = null;
        n.f0.accept(this);
        return _ret;
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
        String _ret = null;
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        return _ret;
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    @Override
    public String visit(AllocationExpression n) {
        String _ret = null;
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        return _ret;
    }

    /**
     * f0 -> "!"
     * f1 -> Expression()
     */
    @Override
    public String visit(NotExpression n) {
        String _ret = null;
        n.f0.accept(this);
        n.f1.accept(this);
        return _ret;
    }

    /**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
    @Override
    public String visit(BracketExpression n) {
        String _ret = null;
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        return _ret;
    }
}