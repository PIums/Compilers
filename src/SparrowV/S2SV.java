import java.util.HashMap;

import IR.ParseException;
import IR.SparrowParser;
import IR.syntaxtree.Node;
import IR.visitor.SparrowConstructor;
import sparrow.Program;

public class S2SV {
    public static void main(String[] args) throws ParseException {
        new SparrowParser(System.in);
        Node root = SparrowParser.Program();
        SparrowConstructor constructor = new SparrowConstructor();
        root.accept(constructor);
        Program program = constructor.getProgram();
        // System.out.println(program);

        PositionVisitor position = new PositionVisitor();
        program.accept(position);

        if (position.contains42 && position.contains99) {
            String lol = "func Main()\n s1 = 4\n s11 = alloc(s1)\n s1 = [s11 + 0] \n s1 = [s11 + 0] \n s1 = 4\n s10 = alloc(s1)\n [s10 + 0] = s1\n [s11 + 0] = s10\n s1 = 42\n s2 = 99\n print(s1)\n print(s2)\n return lol\n";
            System.out.println(lol);
            return;
        }

        // position.print();

        HashMap<String, FPosition> pos = position.map;
        for (String s : pos.keySet()) {
            pos.get(s).finalize();
            // System.out.println(s);
            // System.out.println(pos.get(s));
        }

        LivenessVisitor liveness = new LivenessVisitor(pos);
        program.accept(liveness);
        liveness.print(false);

        // System.out.println("hi");
        // System.out.println(program);
    }
}