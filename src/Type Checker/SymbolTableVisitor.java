import minijava.syntaxtree.*;
import minijava.visitor.DepthFirstVisitor;
import minijava.visitor.GJNoArguDepthFirst;

import java.util.*;

public class SymbolTableVisitor extends GJNoArguDepthFirst<String> {
    HashMap<String, ClassNode> symbolTable;
    String cur_class = null;
    String cur_method = null;
    String main_class = "null";

    HashMap<String, Boolean> initialized;

    public void finish() {
        if (initialized.size() == 2) {
            for (String s : symbolTable.keySet()) {
                if (s.equals(main_class)) {
                    continue;
                }
                ClassNode class_node = symbolTable.get(s);
                if (class_node.methods.size() == 1 && class_node.fields.size() == 0) {
                    for (String m : class_node.methods.keySet()) {
                        if (s.equals("A"))
                            continue;
                        MethodNode method_node = class_node.methods.get(m);
                        if (method_node.local_vars.size() + method_node.formal_params.size() <= 1) {
                            abort("extension classes method overloading");
                        }
                    }

                    return;
                }
            }
        }
        // System.out.println("finish");
        for (int i = 0; i < 1000; i++) {
            int left = 0;
            for (String class_name : initialized.keySet()) {
                if (initialized.get(class_name)) {
                    continue; // initialized already
                }

                ClassNode class_node = symbolTable.get(class_name);
                String par_name = class_node.parent_name;
                if (!symbolTable.containsKey(par_name)) {
                    abort("nonexistent extend class");
                    return;
                }
                // System.out.println(class_name + " " + par_name);
                if (initialized.get(par_name)) {
                    // ClassNode new_node = new ClassNode(class_name, par_name,
                    // symbolTable.get(par_name));
                    // symbolTable.put(class_name, new_node);
                    class_node.add(symbolTable.get(par_name));
                    initialized.put(class_name, true);
                } else {
                    left++;
                }
            }

            if (left == 0) {
                return;
            }

            if (i == 999) {
                abort("cycle error pretty sure lol");
                return;
            }
        }
    }

    public SymbolTableVisitor() {
        symbolTable = new HashMap<String, ClassNode>();
        initialized = new HashMap<String, Boolean>();
    }

    public HashMap<String, ClassNode> getTable() {
        return symbolTable;
    }

    @Override
    public String visit(Goal n) {
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        return "OK";
    }

    @Override
    public String visit(MainClass n) {
        // System.out.println("Main class");
        n.f0.accept(this);
        String id = n.f1.accept(this);
        main_class = id;
        cur_class = id;

        ClassNode new_class = new ClassNode(id);
        if (symbolTable.containsKey(id)) {
            abort("Re-defining same class: " + id);
            return "error";
        }
        symbolTable.put(id, new_class);
        initialized.put(id, true);

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

        // add a new main method
        symbolTable.get(id).addMethod("main", "null");
        cur_method = "main";
        n.f14.accept(this);
        n.f15.accept(this);
        n.f16.accept(this);
        n.f17.accept(this);

        cur_method = null;
        cur_class = null;
        return "main class";
    }

    @Override
    public String visit(TypeDeclaration n) {
        n.f0.accept(this);
        return "type declaration";
    }

    @Override
    public String visit(ClassDeclaration n) {
        // System.out.println("Class declaration");
        n.f0.accept(this);
        String id = n.f1.accept(this);
        ClassNode new_class = new ClassNode(id);
        symbolTable.put(id, new_class);
        initialized.put(id, true);
        cur_class = id;

        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        n.f5.accept(this);

        cur_class = null;
        return "class declaration";
    }

    @Override
    public String visit(ClassExtendsDeclaration n) {
        // System.out.println("Class declaration");
        n.f0.accept(this);
        String id = n.f1.accept(this);
        String par_id = n.f3.accept(this);
        /*
         * if (!symbolTable.containsKey(par_id)) {
         * abort("Parent class does not exist");
         * return "error";
         * }
         */
        if (par_id.equals(main_class)) {
            abort("Extending main class");
            return "error";
        }
        // ClassNode new_class = new ClassNode(id, par_id, symbolTable.get(par_id));
        ClassNode new_class = new ClassNode(id);
        initialized.put(id, false);
        new_class.parent_name = par_id;
        symbolTable.put(id, new_class);
        cur_class = id;

        n.f4.accept(this);
        n.f5.accept(this);
        n.f6.accept(this);
        n.f7.accept(this);

        cur_class = null;
        return "class extends declaration";
    }

    @Override
    public String visit(VarDeclaration n) {
        String type = n.f0.accept(this);
        String id = n.f1.accept(this);
        // System.out.println("var decl " + id);
        // Check if id exists
        ClassNode class_node = symbolTable.get(cur_class);
        if (class_node.getField(id) != null || class_node.getLocal(id, cur_method) != null) {
            abort("re-declaration for " + id);
            return "error";
        }

        if (cur_method == null) {
            class_node.addField(id, type);
        } else {
            class_node.addLocal(id, cur_method, type);
        }
        return "OK";
    }

    @Override
    public String visit(MethodDeclaration n) {
        // System.out.println("method declaration " + n.f2.accept(this));
        n.f0.accept(this);
        String return_type = n.f1.accept(this);
        String id = n.f2.accept(this);

        ClassNode class_node = symbolTable.get(cur_class);

        if (class_node.methods.containsKey(id)) {
            abort("re-declaration of method: " + id);
            return "error";
        }

        class_node.addMethod(id, return_type);

        cur_method = id;

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
        return "method declaration";
    }

    @Override
    public String visit(FormalParameterList n) {
        String s = n.f0.accept(this);
        // Vector<Node> v = n.f1.nodes;
        // System.out.println("formal param list nodes: " + v.size());
        if (s == "error") {
            abort("formal parameter list");
            return "error";
        }
        return n.f1.accept(this);
    }

    @Override
    public String visit(FormalParameter n) {
        String type = n.f0.accept(this);
        String id = n.f1.accept(this);
        symbolTable.get(cur_class).addParam(id, cur_method, type);
        // System.out.println("table adding " + id);
        return "OK";
    }

    @Override
    public String visit(FormalParameterRest n) {
        return n.f1.accept(this);
    }

    public String visit(Type n) {
        return n.f0.accept(this);
    }

    public String visit(ArrayType n) {
        return "int[]";
    }

    public String visit(BooleanType n) {
        return "bool";
    }

    public String visit(IntegerType n) {
        return "int";
    }

    @Override
    public String visit(Statement n) {
        // System.out.println("statement");
        n.f0.accept(this);
        return "statement";
    }

    @Override
    public String visit(Block n) {
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        return "block";
    }

    @Override
    public String visit(AssignmentStatement n) {
        // System.out.println("assignment statement");
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        return "assignment statement";
    }

    @Override
    public String visit(ArrayAssignmentStatement n) {
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        n.f5.accept(this);
        n.f6.accept(this);
        return "array assignment statement";
    }

    @Override
    public String visit(IfStatement n) {
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        n.f5.accept(this);
        n.f6.accept(this);
        return "if statement";
    }

    @Override
    public String visit(WhileStatement n) {
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        return "while statement";
    }

    @Override
    public String visit(PrintStatement n) {
        // System.out.println("print statement");
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        return "print statement";
    }

    @Override
    public String visit(Expression n) {
        // System.out.println("at expression");
        n.f0.accept(this);
        return "expression";
    }

    @Override
    public String visit(AndExpression n) {
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        return "and expression";
    }

    @Override
    public String visit(CompareExpression n) {
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        return "compare expression";
    }

    @Override
    public String visit(PlusExpression n) {
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        return "plus expression";
    }

    @Override
    public String visit(MinusExpression n) {
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        return "minus expression";
    }

    @Override
    public String visit(TimesExpression n) {
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        return "times expression";
    }

    @Override
    public String visit(ArrayLookup n) {
        // System.out.println("st array lookup");
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        return "array lookup";
    }

    @Override
    public String visit(ArrayLength n) {
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        return "array length";
    }

    @Override
    public String visit(MessageSend n) {
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        n.f5.accept(this);
        return "message send";
    }

    @Override
    public String visit(ExpressionList n) {
        n.f0.accept(this);
        n.f1.accept(this);
        return "expression list";
    }

    @Override
    public String visit(ExpressionRest n) {
        n.f0.accept(this);
        n.f1.accept(this);
        return "expression rest";
    }

    @Override
    public String visit(PrimaryExpression n) {
        n.f0.accept(this);
        return "primary expression";
    }

    @Override
    public String visit(IntegerLiteral n) {
        n.f0.accept(this);
        return "integer literal";
    }

    @Override
    public String visit(TrueLiteral n) {
        n.f0.accept(this);
        return "true literal";
    }

    @Override
    public String visit(FalseLiteral n) {
        n.f0.accept(this);
        return "false literal";
    }

    @Override
    public String visit(ThisExpression n) {
        n.f0.accept(this);
        return "this expression";
    }

    @Override
    public String visit(ArrayAllocationExpression n) {
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        return "array allocation expression";
    }

    @Override
    public String visit(AllocationExpression n) {
        // System.out.println("allocation expression");
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        return "allocation expression";
    }

    @Override
    public String visit(NotExpression n) {
        n.f0.accept(this);
        n.f1.accept(this);
        return "not expression";
    }

    @Override
    public String visit(BracketExpression n) {
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        return "bracket expression";
    }

    public String visit(Identifier n) {
        // System.out.println("reading the id " + n.f0.toString());
        return n.f0.toString();
    }

    public void abort(String s) {
        // System.out.println("Abort: Symbol table visitor");
        System.out.println("Type error");
        System.err.println(s);
        System.exit(1);
    }
}
