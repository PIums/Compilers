
//import minijava.MiniJavaParser;
//import minijava.syntaxtree.Goal;
import minijava.MiniJavaParser;
import minijava.syntaxtree.Goal;
import java.io.*;
import java.util.*;

public class Typecheck {
  public static void main(String[] arg) throws Exception {
    Goal root;
    try {
      // System.out.println("starting");
      root = new MiniJavaParser(System.in).Goal();

      SymbolTableVisitor stv = new SymbolTableVisitor();
      root.accept(stv);
      HashMap<String, ClassNode> table = stv.getTable();
      /*
       * for (String s : table.keySet()) {
       * System.out.println(table.get(s));
       * }
       */

      // System.out.println("successfully made table");
      /*
       * for (String k : table.keySet()) {
       * System.out.println(k + " " + table.get(k));
       * }
       */
      // System.out.println(table);

      // System.out.println("starting check visitor phase");
      stv.finish();

      CheckVisitor cv = new CheckVisitor(table);
      root.accept(cv);

      System.out.println("Program type checked successfully");

    } catch (minijava.ParseException e) {
      System.out.println("Oh man");
      System.exit(1);
    }

  }
}