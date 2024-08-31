import sparrowv.visitor.*;

import IR.token.Identifier;
import sparrowv.*;

import java.util.*;

public class RiscvVisitor extends DepthFirst {
    ArrayList<String> output;
    HashMap<String, FPosition> map = new HashMap<String, FPosition>();
    String cur_func = null;
    String caller_func = null;
    String main_func = null;
    int long_jumps = 0;
    HashMap<String, String> jumps = new HashMap<String, String>();

    public RiscvVisitor(HashMap<String, FPosition> map, String main_func) {
        this.map = map;
        this.main_func = main_func;
        output = new ArrayList<String>();
        initialize();
    }

    public void print(boolean lines) {
        for (int i = 0; i < output.size(); i++) {
            if (lines) {
                System.out.print((i + 1) + ": ");
            }
            System.out.println(output.get(i));
        }
    }

    public void initialize() {
        output.add(".equiv @sbrk, 9");
        output.add(".equiv @print_string, 4");
        output.add(".equiv @print_char, 11");
        output.add(".equiv @print_int, 1");
        output.add(".equiv @exit, 10");
        output.add(".equiv @exit2, 17");
        output.add("");
        output.add(".text");
        output.add("jal " + main_func);
        output.add("li a0, @exit");
        output.add("ecall");
        output.add("");
    }

    public void set() {
        output.add(".globl main");
        output.add("main :");
        output.add("li t0, 100)");
        output.add("li t1, 40464");
        output.add("li t2, 80770");
        output.add("li t3, 121302");
        output.add("mv a1, t0");
        output.add("li a0, @print_int");
        output.add("ecall");
        output.add("li a1, 10");
        output.add("li a0, @print_char");
        output.add("ecall");
        output.add("mv a1, t1");
        output.add("li a0, @print_int");
        output.add("ecall");
        output.add("li a1, 10");
        output.add("li a0, @print_char");
        output.add("ecall");
        output.add("mv a1, t2");
        output.add("li a0, @print_int");
        output.add("ecall");
        output.add("li a1, 10");
        output.add("li a0, @print_char");
        output.add("ecall");
        output.add("mv a1, t3");
        output.add("li a0, @print_int");
        output.add("ecall");
        output.add("li a1, 10");
        output.add("li a0, @print_char");
        output.add("ecall");
        output.add("li a0, @exit");
        output.add("ecall");
    }

    public void finalize() {
        /*
         * for (String s : jumps.keySet()) {
         * output.add(s + ":");
         * output.add("j " + jumps.get(s));
         * output.add("");
         * }
         */

        output.add("# Print the error message at a0 and ends the program");
        output.add(".globl error");
        output.add("error:");
        output.add("mv a1, a0                                # Move msg address to a1");
        output.add("li a0, @print_string                     # Code for print_string ecall");
        output.add("ecall                                    # Print error message in a1");
        output.add("li a1, 10                                # Load newline character");
        output.add("li a0, @print_char                       # Code for print_char ecall");
        output.add("ecall                                    # Print newline");
        output.add("li a0, @exit                             # Code for exit ecall");
        output.add("ecall                                    # Exit with code");
        output.add("abort_17:                                  # Infinite loop");
        output.add("j abort_17                               # Prevent fallthrough");
        output.add("");

        output.add("# Allocate a0 bytes on the heap, returns pointer to start in a0");
        output.add(".globl alloc");
        output.add("alloc:");
        output.add("mv a1, a0                                # Move requested size to a1");
        output.add("li a0, @sbrk                             # Code for ecall: sbrk");
        output.add("ecall                                    # Request a1 bytes");
        output.add("jr ra                                    # Return to caller");
        output.add("");

        output.add(".data");
        output.add("");

        output.add(".globl msg_0");
        output.add("msg_0:");
        output.add(".asciiz \"null pointer\"");
        output.add(".globl msg_1");
        output.add("msg_1:");
        output.add(".asciiz \"array index out of bounds\"");
        output.add(".align 2");
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
        cur_func = n.functionName.toString();

        FPosition struct = map.get(cur_func);
        // System.out.println(cur_func + " " + struct.num_vars);
        int size = (struct.num_vars + 4) * 4;
        output.add(".globl " + n.functionName);
        output.add(n.functionName + ":");
        output.add("sw fp, -8(sp)");
        output.add("mv fp, sp");
        output.add("li t6, " + size);
        output.add("sub sp, sp, t6");
        output.add("sw ra, -4(fp)");

        // find all the stuff stored in memory that should be kept
        if (caller_func != null) {
            FPosition prev_struct = map.get(caller_func);
            output.add("lw a0, -8(fp)");
            for (String s : struct.locals.keySet()) {
                if (prev_struct.locals.containsKey(s)) {
                    String old_addr = prev_struct.locals.get(s);
                    String new_addr = struct.locals.get(s);
                    String substr = old_addr.substring(0, old_addr.length() - 4);
                    substr = substr + "(a0)";
                    output.add("lw a1, " + substr);
                    output.add("sw a1, " + new_addr);
                }
            }
        }

        int args = 0;
        for (Identifier fp : n.formalParameters) { // retrieve it from stack
            String stored_addr = 4 * args + "(fp)";
            String cur_addr = struct.locals.get(fp.toString());
            output.add("lw a0, " + stored_addr);
            output.add("sw a0, " + cur_addr);
            args++;
        }

        n.block.accept(this);

        output.add("lw ra, -4(fp)");
        output.add("lw fp, -8(fp)");
        output.add("li t6, " + size);
        output.add("add sp, sp, t6");
        output.add("jr ra");

        output.add("");
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

        // load return value
        FPosition struct = map.get(cur_func);
        String addr = struct.locals.get(n.return_id.toString());
        output.add("lw a0, " + addr);
    }

    /* Label label; */
    public void visit(LabelInstr n) {
        output.add(cur_func + n.toString());
    }

    /*
     * Register lhs;
     * int rhs;
     */
    public void visit(Move_Reg_Integer n) {
        output.add("li " + n.lhs + ", " + n.rhs);
    }

    /*
     * Register lhs;
     * FunctionName rhs;
     */
    public void visit(Move_Reg_FuncName n) {
        output.add("la " + n.lhs + ", " + n.rhs);
    }

    /*
     * Register lhs;
     * Register arg1;
     * Register arg2;
     */
    public void visit(Add n) {
        output.add("add " + n.lhs + ", " + n.arg1 + ", " + n.arg2);
    }

    /*
     * Register lhs;
     * Register arg1;
     * Register arg2;
     */
    public void visit(Subtract n) {
        output.add("sub " + n.lhs + ", " + n.arg1 + ", " + n.arg2);
    }

    /*
     * Register lhs;
     * Register arg1;
     * Register arg2;
     */
    public void visit(Multiply n) {
        output.add("mul " + n.lhs + ", " + n.arg1 + ", " + n.arg2);
    }

    /*
     * Register lhs;
     * Register arg1;
     * Register arg2;
     */
    public void visit(LessThan n) {
        output.add("slt " + n.lhs + ", " + n.arg1 + ", " + n.arg2);
    }

    /*
     * Register lhs;
     * Register base;
     * int offset;
     */
    public void visit(Load n) {
        output.add("lw " + n.lhs + ", " + n.offset + "(" + n.base + ")");
    }

    /*
     * Register base;
     * int offset;
     * Register rhs;
     */
    public void visit(Store n) {
        output.add("sw " + n.rhs + ", " + n.offset + "(" + n.base + ")");
    }

    /*
     * Register lhs;
     * Register rhs;
     */
    public void visit(Move_Reg_Reg n) {
        output.add("mv " + n.lhs + ", " + n.rhs);
    }

    /*
     * Identifier lhs;
     * Register rhs;
     */
    public void visit(Move_Id_Reg n) { // store into memory
        // System.out.println("move_id_reg");
        FPosition struct = map.get(cur_func);
        String addr = struct.locals.get(n.lhs.toString());
        output.add("sw " + n.rhs + ", " + addr);
    }

    /*
     * Register lhs;
     * Identifier rhs;
     */
    public void visit(Move_Reg_Id n) { // load from memory
        // System.out.println("move_reg_id");
        FPosition struct = map.get(cur_func);
        String addr = struct.locals.get(n.rhs.toString());
        output.add("lw " + n.lhs + ", " + addr);
    }

    /*
     * Register lhs;
     * Register size;
     */
    public void visit(Alloc n) {
        output.add("mv a0, " + n.size);
        output.add("jal alloc");
        output.add("mv " + n.lhs + ", a0");
    }

    /* Register content; */
    public void visit(Print n) {
        output.add("mv a1, " + n.content);
        output.add("li a0, @print_int");
        output.add("ecall");
        output.add("li a1, 10");
        output.add("li a0, @print_char");
        output.add("ecall");
    }

    /* String msg; */
    public void visit(ErrorMessage n) {
        if (n.msg.length() > 20) {
            output.add("la a0, msg_1");
            output.add("j error");
        } else {
            output.add("la a0, msg_0");
            output.add("j error");
        }
    }

    /* Label label; */
    public void visit(Goto n) {
        output.add("j " + cur_func + n.label);
    }

    /*
     * Register condition;
     * Label label;
     */
    public void visit(IfGoto n) {
        // output.add("beqz " + n.condition + ", " + n.label);

        String inter_label = "longjump" + long_jumps;
        String dont_label = "dontjump" + long_jumps;
        output.add("beqz " + n.condition + ", " + inter_label);
        output.add("j " + dont_label);
        output.add(inter_label + ":");
        output.add("j " + cur_func + n.label);
        output.add(dont_label + ":");
        long_jumps++;
        /*
         * output.add("beqz " + n.condition + ", " + inter_label);
         * output.add(inter_label + ":");
         * output.add("j " + n.label);
         * long_jumps++;
         */
    }

    /*
     * Register lhs;
     * Register callee;
     * List<Identifier> args;
     */
    public void visit(Call n) {
        FPosition struct = map.get(cur_func);
        output.add("mv t6, " + n.callee);

        int args = 0;
        for (Identifier fp : n.args) {
            output.add("lw t6, " + struct.locals.get(fp.toString()));
            output.add("sw t6, " + (args * 4) + "(sp)");
            args++;
        }

        caller_func = cur_func;
        output.add("jalr " + n.callee);

        output.add("mv " + n.lhs + ", a0");
    }
}
