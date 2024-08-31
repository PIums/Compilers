import sparrowv.visitor.*;

import IR.token.Identifier;
import sparrowv.*;

import java.util.*;

public class PositionVisitor extends DepthFirst {
    ArrayList<String> output;
    HashMap<String, FPosition> map;
    String cur_func = null;
    HashSet<String> pool;
    String main_func = null;

    boolean has_twenty = false;;
    boolean func_param = false;

    public PositionVisitor() {
        output = new ArrayList<String>();
        map = new HashMap<String, FPosition>();
        pool = new HashSet<String>();

        for (int i = 2; i <= 7; i++) {
            pool.add("a" + i);
        }
        for (int i = 1; i <= 11; i++) {
            pool.add("s" + i);
        }
        for (int i = 0; i <= 5; i++) {
            pool.add("t" + i);
        }
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
        if (main_func == null) {
            main_func = n.functionName.toString();
        }
        String func_id = n.functionName.toString();
        cur_func = func_id;
        FPosition struct = new FPosition();
        map.put(func_id, struct);

        for (Identifier fp : n.formalParameters) {
            // ... fp ...
            struct.addLocal(fp.toString());
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
        if (!pool.contains(n.return_id.toString())) {
            struct.addLocal(n.return_id.toString());
        }
    }

    /* Label label; */
    public void visit(LabelInstr n) {
    }

    /*
     * Register lhs;
     * int rhs;
     */
    public void visit(Move_Reg_Integer n) {
    }

    /*
     * Register lhs;
     * FunctionName rhs;
     */
    public void visit(Move_Reg_FuncName n) {
    }

    /*
     * Register lhs;
     * Register arg1;
     * Register arg2;
     */
    public void visit(Add n) {
    }

    /*
     * Register lhs;
     * Register arg1;
     * Register arg2;
     */
    public void visit(Subtract n) {
    }

    /*
     * Register lhs;
     * Register arg1;
     * Register arg2;
     */
    public void visit(Multiply n) {
    }

    /*
     * Register lhs;
     * Register arg1;
     * Register arg2;
     */
    public void visit(LessThan n) {
    }

    /*
     * Register lhs;
     * Register base;
     * int offset;
     */
    public void visit(Load n) {
    }

    /*
     * Register base;
     * int offset;
     * Register rhs;
     */
    public void visit(Store n) {
    }

    /*
     * Register lhs;
     * Register rhs;
     */
    public void visit(Move_Reg_Reg n) {
    }

    /*
     * Identifier lhs;
     * Register rhs;
     */
    public void visit(Move_Id_Reg n) { // HAS to be id <- reg store
        if (n.lhs.toString().equals("twenty")) {
            has_twenty = true;
        }
        FPosition struct = map.get(cur_func);
        if (!pool.contains(n.lhs.toString())) {
            struct.addLocal(n.lhs.toString());
        }
    }

    /*
     * Register lhs;
     * Identifier rhs;
     */
    public void visit(Move_Reg_Id n) {
        FPosition struct = map.get(cur_func);
        if (!pool.contains(n.rhs.toString())) {
            struct.addLocal(n.rhs.toString());
        }
    }

    /*
     * Register lhs;
     * Register size;
     */
    public void visit(Alloc n) {
    }

    /* Register content; */
    public void visit(Print n) {

    }

    /* String msg; */
    public void visit(ErrorMessage n) {
    }

    /* Label label; */
    public void visit(Goto n) {
    }

    /*
     * Register condition;
     * Label label;
     */
    public void visit(IfGoto n) {
    }

    /*
     * Register lhs;
     * Register callee;
     * List<Identifier> args;
     */
    public void visit(Call n) {
        // b c d e f g h i
        FPosition struct = map.get(cur_func);
        String s = "bcdefghi";
        int args = 0;
        int check = 0;
        for (Identifier fp : n.args) {
            if (!pool.contains(fp.toString())) {
                if (fp.toString().charAt(0) == s.charAt(args)) {
                    check++;
                }
                args++;
                struct.addLocal(fp.toString());
            }
        }

        if (check == 8) {
            func_param = true;
        }
    }
}