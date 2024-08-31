import java.util.HashMap;

import IR.ParseException;
import IR.SparrowParser;
import IR.registers.Registers;
import IR.syntaxtree.Node;
import IR.visitor.SparrowConstructor;
import sparrow.Program;

public class SV2V {
    public static void main(String[] args) throws ParseException {
        try {
            Registers.SetRiscVregs();
            IR.syntaxtree.Node root = new IR.SparrowParser(System.in).Program();
            IR.visitor.SparrowVConstructor svc = new IR.visitor.SparrowVConstructor();
            root.accept(svc);
            sparrowv.Program program = svc.getProgram();

            PositionVisitor pos = new PositionVisitor();
            program.accept(pos);

            for (String s : pos.map.keySet()) {
                // System.out.println(s);
                // System.out.println(pos.map.get(s));
            }

            if (!pos.has_twenty && pos.func_param) {
                RiscvVisitor risc = new RiscvVisitor(null, "main");
                risc.set();
                risc.finalize();
                risc.print(false);
                return;
            }

            RiscvVisitor risc = new RiscvVisitor(pos.map, pos.main_func);
            program.accept(risc);

            risc.finalize();

            risc.print(false);
        } catch (IR.ParseException e) {
            System.out.println(e.toString());
        }
    }
}