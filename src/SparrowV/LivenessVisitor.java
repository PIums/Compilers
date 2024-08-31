import sparrow.visitor.*;

import IR.token.Identifier;
import sparrow.*;

import java.util.*;

public class LivenessVisitor extends DepthFirst {
    ArrayList<String> output;
    HashMap<String, FPosition> map;
    HashSet<String> init;
    String cur_func = null;
    int num_calls = 0;
    int cur_line = 1;

    public LivenessVisitor(HashMap<String, FPosition> map) {
        this.map = map;
        init = new HashSet<String>();
        output = new ArrayList<String>();
    }

    public void print(boolean lines) {
        for (int i = 0; i < output.size(); i++) {
            if (lines) {
                System.out.print((i + 1) + " ");
            }
            System.out.println(output.get(i));
        }
    }

    int par = 1;
    int mod = 2;

    public boolean inMemory(Identifier id) {
        FPosition struct = map.get(cur_func);
        String reg = struct.perm.get(id.toString());
        return (reg.equals("Memory"));
    }

    public String load(Identifier id) {
        FPosition struct = map.get(cur_func);
        String reg = struct.perm.get(id.toString());
        if (!reg.equals("Memory")) {
            return reg;
        }

        String s = "t" + par;
        par = (par + 1) % mod;
        output.add(s + " = " + id);
        return s;
    }

    public String store(Identifier id) {
        FPosition struct = map.get(cur_func);
        String reg = struct.perm.get(id.toString());
        if (!reg.equals("Memory")) {
            return reg;
        }

        String s = "t" + par;
        par = (par + 1) % mod;

        // do after return
        return s;
    }

    /* List<FunctionDecl> funDecls; */
    public void visit(Program n) {
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
        cur_line = 1;
        init.clear();
        String func_id = n.functionName.toString();
        cur_func = func_id;
        FPosition struct = map.get(cur_func);
        String formal_params = "";
        int cur = 2;
        HashMap<String, String> get_a = new HashMap<String, String>();
        for (Identifier fp : n.formalParameters) {
            init.add(fp.toString());
            if (cur <= 7) {
                get_a.put(fp.toString(), "a" + cur);
                cur++;
            } else {
                formal_params += fp + " ";
            }
        }
        output.add("func " + func_id + "(" + formal_params + ")");
        for (Identifier fp : n.formalParameters) {
            if (get_a.containsKey(fp.toString())) {
                String reg = load(fp);
                output.add(reg + " = " + get_a.get(fp.toString()));
            } else {
                if (!inMemory(fp)) {
                    String reg = struct.perm.get(fp.toString());
                    output.add(reg + " = " + fp);
                } else {

                }
            }
        }
        n.block.accept(this);
    }

    /*
     * FunctionDecl parent;
     * List<Instruction> instructions;
     * Identifier return_id;
     */
    public void visit(Block n) {
        FPosition struct = map.get(cur_func);
        for (Instruction i : n.instructions) {
            i.accept(this);
        }

        String reg = struct.perm.get(n.return_id.toString());
        if (!inMemory(n.return_id)) {
            output.add(n.return_id + " = " + reg);
        }
        // System.out.println("total num of lines: " + cur_func + " " + cur_line);
        output.add("return " + n.return_id);
    }

    /* Label label; */
    public void visit(LabelInstr n) {
        output.add(n.toString());
        cur_line++;
    }

    /*
     * Identifier lhs;
     * int rhs;
     */
    public void visit(Move_Id_Integer n) {
        String store = store(n.lhs);
        init.add(n.lhs.toString());
        output.add(store + " = " + n.rhs);

        if (store.charAt(0) == 't') {
            output.add(n.lhs + " = " + store);
        }
        cur_line++;
    }

    /*
     * Identifier lhs;
     * FunctionName rhs;
     */
    public void visit(Move_Id_FuncName n) {
        String store = store(n.lhs);
        output.add(store + " = @" + n.rhs);

        if (store.charAt(0) == 't') {
            output.add(n.lhs + " = " + store);
        }
        cur_line++;
    }

    /*
     * Identifier lhs;
     * Identifier arg1;
     * Identifier arg2;
     */
    public void visit(Add n) {
        String reg1 = load(n.arg1);
        String reg2 = load(n.arg2);
        String store = store(n.lhs);

        init.add(n.lhs.toString());
        output.add(store + " = " + reg1 + " + " + reg2);

        if (store.charAt(0) == 't') {
            output.add(n.lhs + " = " + store);
        }
        cur_line++;
    }

    /*
     * Identifier lhs;
     * Identifier arg1;
     * Identifier arg2;
     */
    public void visit(Subtract n) {
        String reg1 = load(n.arg1);
        String reg2 = load(n.arg2);
        String store = store(n.lhs);

        init.add(n.lhs.toString());
        output.add(store + " = " + reg1 + " - " + reg2);

        if (store.charAt(0) == 't') {
            output.add(n.lhs + " = " + store);
        }
        cur_line++;
    }

    /*
     * Identifier lhs;
     * Identifier arg1;
     * Identifier arg2;
     */
    public void visit(Multiply n) {
        String reg1 = load(n.arg1);
        String reg2 = load(n.arg2);
        String store = store(n.lhs);

        init.add(n.lhs.toString());
        output.add(store + " = " + reg1 + " * " + reg2);

        if (store.charAt(0) == 't') {
            output.add(n.lhs + " = " + store);
        }
        cur_line++;
    }

    /*
     * Identifier lhs;
     * Identifier arg1;
     * Identifier arg2;
     */
    public void visit(LessThan n) {
        String reg1 = load(n.arg1);
        String reg2 = load(n.arg2);
        String store = store(n.lhs);

        init.add(n.lhs.toString());
        output.add(store + " = " + reg1 + " < " + reg2);

        if (store.charAt(0) == 't') {
            output.add(n.lhs + " = " + store);
        }
        cur_line++;
    }

    /*
     * Identifier lhs;
     * Identifier base;
     * int offset;
     */
    public void visit(Load n) {
        String reg = load(n.base);
        String store = store(n.lhs);

        init.add(n.lhs.toString());
        output.add(store + " = [" + reg + " + " + n.offset + "]");

        if (store.charAt(0) == 't') {
            output.add(n.lhs + " = " + store);
        }
        cur_line++;
    }

    /*
     * Identifier base;
     * int offset;
     * Identifier rhs;
     */
    public void visit(Store n) {
        String reg1 = load(n.base);
        String reg2 = load(n.rhs);
        output.add("[" + reg1 + " + " + n.offset + "] = " + reg2);
        cur_line++;
    }

    /*
     * Identifier lhs;
     * Identifier rhs;
     */
    public void visit(Move_Id_Id n) {
        String store = store(n.lhs);
        String reg = load(n.rhs);
        init.add(n.lhs.toString());
        output.add(store + " = " + reg);

        if (store.charAt(0) == 't') {
            output.add(n.lhs + " = " + store);
        }
        cur_line++;
    }

    /*
     * Identifier lhs;
     * Identifier size;
     */
    public void visit(Alloc n) {
        String reg = load(n.size);
        String store = store(n.lhs);
        init.add(n.lhs.toString());
        output.add(store + " = alloc(" + reg + ")");

        if (store.charAt(0) == 't') {
            output.add(n.lhs + " = " + store);
        }
        cur_line++;
    }

    /* Identifier content; */
    public void visit(Print n) {
        String reg = load(n.content);
        output.add("print(" + reg + ")");
        cur_line++;
    }

    /* String msg; */
    public void visit(ErrorMessage n) {
        output.add(n.toString());
        cur_line++;
    }

    /* Label label; */
    public void visit(Goto n) {
        output.add(n.toString());
        cur_line++;
    }

    /*
     * Identifier condition;
     * Label label;
     */
    public void visit(IfGoto n) {
        String reg = load(n.condition);
        output.add("if0 " + reg + " goto " + n.label);
        cur_line++;
    }

    /*
     * Identifier lhs;
     * Identifier callee;
     * List<Identifier> args;
     */
    public void visit(Call n) {
        init.add(n.lhs.toString());
        FPosition struct = map.get(cur_func);
        HashMap<String, String> store_for_later = new HashMap<String, String>();
        for (String s : struct.perm.keySet()) {
            if (!init.contains(s))
                continue;

            int last_line_used = struct.right.get(s);
            if (last_line_used <= cur_line) {
                continue;
            }

            String reg = struct.perm.get(s);
            if (!reg.equals("Memory")) {
                if (store_for_later.containsKey(reg)) {
                    String cur = store_for_later.get(reg);
                    int t1 = struct.right.get(cur);
                    int t2 = struct.right.get(s);
                    if (t2 >= t1) {
                        store_for_later.put(reg, s);
                    }
                } else {
                    store_for_later.put(reg, s);
                }
            }
        }

        HashSet<String> stored_strings = new HashSet<String>();
        for (String s : store_for_later.keySet()) {
            output.add(store_for_later.get(s) + " = " + s);
            stored_strings.add(store_for_later.get(s));
        }

        String store = store(n.lhs);
        String reg = load(n.callee);

        String formal_params = "";
        int cur = 2;
        for (Identifier fp : n.args) {
            if (cur <= 7) {
                if (inMemory(fp)) {
                    output.add("a" + cur + " = " + fp);
                } else {
                    String param_reg = struct.perm.get(fp.toString());
                    output.add("a" + cur + " = " + param_reg);
                }
                cur++;
            } else {
                formal_params += fp + " ";
                if (inMemory(fp) || stored_strings.contains(fp.toString())) {
                    // nothing
                } else {
                    // retrieve it and put it in memory
                    output.add(fp.toString() + " = " + struct.perm.get(fp.toString()));
                }
            }
        }
        output.add(store + " = call " + reg + "(" + formal_params + ")");
        if (store.charAt(0) == 't') {
            output.add(n.lhs + " = " + store);
        }

        for (String s : store_for_later.keySet()) {
            if (s.equals(store)) {
                continue;
            }
            output.add(s + " = " + store_for_later.get(s));
        }
        cur_line++;
    }
}
