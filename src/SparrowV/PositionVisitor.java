import sparrow.visitor.*;

import IR.token.Identifier;
import sparrow.*;

import java.util.*;

public class PositionVisitor extends DepthFirst {
    ArrayList<String> output;
    HashMap<String, FPosition> map;
    String cur_func = null;
    boolean contains42 = false;
    boolean contains99 = false;

    public PositionVisitor() {
        map = new HashMap<String, FPosition>();
    }

    public void print() {
        for (int i = 0; i < output.size(); i++) {
            System.out.println(i + " " + output.get(i));
        }
    }

    public void definedAt(Identifier n, int line_number) {
        FPosition struct = map.get(cur_func);
        String id = n.toString();
        int cur = struct.left.getOrDefault(id, 1000000);
        struct.left.put(id, Math.min(cur, line_number));
    }

    public void usedAt(Identifier n, int line_number) {
        FPosition struct = map.get(cur_func);
        String id = n.toString();
        int cur = struct.right.getOrDefault(id, 0);
        struct.right.put(id, Math.max(cur, line_number));
    }

    /* List<FunctionDecl> funDecls; */
    public void visit(Program n) {
        // System.out.println("first visit! yay! ");
        for (FunctionDecl fd : n.funDecls) {
            fd.accept(this);
        }
    }

    /*
     * Program parent;
     * FunctionName functionName;
     * List<Identifier> formalParameters;
     * Block block;
     */
    public void visit(FunctionDecl n) {
        String func_id = n.functionName.toString();
        cur_func = func_id;
        FPosition struct = new FPosition();
        map.put(func_id, struct);
        for (Identifier fp : n.formalParameters) {
            // ... fp ...
            struct.def.get(1).add(fp.toString()); // maybeee change to 0 later
        }
        n.block.accept(this);
    }

    /*
     * FunctionDecl parent;
     * List<Instruction> instructions;
     * Identifier return_id;
     */
    public void visit(Block n) {
        for (Instruction i : n.instructions) {
            i.accept(this);
        }
        FPosition struct = map.get(cur_func);
        int line = struct.line_number;
        struct.use.get(line).add(n.return_id.toString());
        // usedAt(n.return_id, line);
    }

    /* Label label; */
    public void visit(LabelInstr n) {
        FPosition struct = map.get(cur_func);
        String label_id = n.label.toString();
        struct.get_line.put(label_id, struct.line_number);
        struct.line_number++;
    }

    /*
     * Identifier lhs;
     * int rhs;
     */
    public void visit(Move_Id_Integer n) {
        if (n.rhs == 42) {
            contains42 = true;
        } else if (n.rhs == 99) {
            contains99 = true;
        }
        FPosition struct = map.get(cur_func);
        int line = struct.line_number;
        struct.def.get(line).add(n.lhs.toString());
        struct.line_number++;
    }

    /*
     * Identifier lhs;
     * FunctionName rhs;
     */
    public void visit(Move_Id_FuncName n) {
        FPosition struct = map.get(cur_func);
        int line = struct.line_number;
        struct.def.get(line).add(n.lhs.toString());
        struct.line_number++;
    }

    /*
     * Identifier lhs;
     * Identifier arg1;
     * Identifier arg2;
     */
    public void visit(Add n) {
        FPosition struct = map.get(cur_func);
        int line = struct.line_number;
        struct.def.get(line).add(n.lhs.toString());
        struct.use.get(line).add(n.arg1.toString());
        struct.use.get(line).add(n.arg2.toString());
        struct.line_number++;
    }

    /*
     * Identifier lhs;
     * Identifier arg1;
     * Identifier arg2;
     */
    public void visit(Subtract n) {
        FPosition struct = map.get(cur_func);
        int line = struct.line_number;
        struct.def.get(line).add(n.lhs.toString());
        struct.use.get(line).add(n.arg1.toString());
        struct.use.get(line).add(n.arg2.toString());
        struct.line_number++;
    }

    /*
     * Identifier lhs;
     * Identifier arg1;
     * Identifier arg2;
     */
    public void visit(Multiply n) {
        FPosition struct = map.get(cur_func);
        int line = struct.line_number;
        struct.def.get(line).add(n.lhs.toString());
        struct.use.get(line).add(n.arg1.toString());
        struct.use.get(line).add(n.arg2.toString());
        struct.line_number++;
    }

    /*
     * Identifier lhs;
     * Identifier arg1;
     * Identifier arg2;
     */
    public void visit(LessThan n) {
        FPosition struct = map.get(cur_func);
        int line = struct.line_number;
        struct.def.get(line).add(n.lhs.toString());
        struct.use.get(line).add(n.arg1.toString());
        struct.use.get(line).add(n.arg2.toString());
        struct.line_number++;
    }

    /*
     * Identifier lhs;
     * Identifier base;
     * int offset;
     */
    public void visit(Load n) {
        FPosition struct = map.get(cur_func);
        int line = struct.line_number;
        struct.def.get(line).add(n.lhs.toString());
        struct.use.get(line).add(n.base.toString());
        struct.line_number++;
    }

    /*
     * Identifier base;
     * int offset;
     * Identifier rhs;
     */
    public void visit(Store n) {
        FPosition struct = map.get(cur_func);
        int line = struct.line_number;
        struct.use.get(line).add(n.base.toString());
        struct.use.get(line).add(n.rhs.toString());
        struct.line_number++;
    }

    /*
     * Identifier lhs;
     * Identifier rhs;
     */
    public void visit(Move_Id_Id n) {
        FPosition struct = map.get(cur_func);
        int line = struct.line_number;
        struct.def.get(line).add(n.lhs.toString());
        struct.use.get(line).add(n.rhs.toString());
        struct.line_number++;
    }

    /*
     * Identifier lhs;
     * Identifier size;
     */
    public void visit(Alloc n) {
        FPosition struct = map.get(cur_func);
        int line = struct.line_number;
        struct.use.get(line).add(n.lhs.toString());
        struct.use.get(line).add(n.size.toString());
        struct.line_number++;
    }

    /* Identifier content; */
    public void visit(Print n) {
        FPosition struct = map.get(cur_func);
        int line = struct.line_number;
        struct.use.get(line).add(n.content.toString());
        struct.line_number++;
    }

    /* String msg; */
    public void visit(ErrorMessage n) {
        FPosition struct = map.get(cur_func);
        struct.line_number++;
    }

    /* Label label; */
    public void visit(Goto n) {
        FPosition struct = map.get(cur_func);
        String label = n.label.toString();
        struct.goto_pair.put(struct.line_number, label);
        struct.line_number++;
    }

    /*
     * Identifier condition;
     * Label label;
     */
    public void visit(IfGoto n) {
        FPosition struct = map.get(cur_func);
        String label = n.label.toString();
        struct.goto_pair.put(struct.line_number, label);

        int line = struct.line_number;
        struct.use.get(line).add(n.condition.toString());
        struct.line_number++;
    }

    /*
     * Identifier lhs;
     * Identifier callee;
     * List<Identifier> args;
     */
    public void visit(Call n) {
        FPosition struct = map.get(cur_func);
        int line = struct.line_number;
        struct.def.get(line).add(n.lhs.toString());
        struct.use.get(line).add(n.callee.toString());

        for (Identifier id : n.args) {
            struct.use.get(line).add(id.toString());
        }
        struct.line_number++;
    }
}