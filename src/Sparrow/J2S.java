
//import minijava.MiniJavaParser;
//import minijava.syntaxtree.Goal;
import minijava.MiniJavaParser;
import minijava.syntaxtree.Goal;
import java.io.*;
import java.util.*;

public class J2S {
    public static void main(String[] arg) throws Exception {
        Goal root;
        try {
            // System.out.println("starting");
            root = new MiniJavaParser(System.in).Goal();

            MethodTableVisitor mtv = new MethodTableVisitor();
            root.accept(mtv);
            mtv.finish();
            HashMap<String, ClassNode> table = mtv.symbolTable;
            HashMap<String, String> lookup = mtv.typeLookup;

            for (String s : table.keySet()) {
                // System.out.println(table.get(s));
            }

            /*
             * System.out.println("Lookup: ");
             * for (String s : lookup.keySet()) {
             * System.out.println(s + " " + lookup.get(s));
             * }
             */

            TranslateVisitor tv = new TranslateVisitor(table, lookup);
            root.accept(tv);

            // System.out.println("YAAAY");
            // System.out.println(tv.output);
            tv.prettyprint(false);

        } catch (minijava.ParseException e) {
            System.out.println("You messed up buddy.");
            System.exit(1);
        }

    }
}