import java.util.*;

public class FPosition {
    LinkedHashMap<String, String> locals;
    int num_vars;
    // max size: ~ num_vars + 2 (fp + return addr) + 8 (max number of arguments? or
    // 12 to be safe)

    public FPosition() {
        locals = new LinkedHashMap<String, String>();
        num_vars = 0;
    }

    public void addLocal(String id) {
        if (locals.containsKey(id))
            return;
        locals.put(id, "-" + 4 * (num_vars + 3) + "(fp)");
        num_vars++;
    }

    public String toString() {
        String ret = "numvars: " + num_vars + "\n";
        for (String s : locals.keySet()) {
            ret += (s + " " + locals.get(s) + "\n");
        }
        return ret;
    }
}
