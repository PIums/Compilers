import java.util.*;

public class ClassNode {
    public String class_name;
    public String parent_name;
    // public LinkedHashMap<String, String> fields;
    public ArrayList<String> fields;
    public ArrayList<String> field_id;
    public LinkedHashMap<String, MethodNode> methods;
    public HashMap<String, String> origin;
    public HashMap<String, Integer> method_id;

    ArrayList<String> keyset(ArrayList<String> arr) {
        HashSet<String> st = new HashSet<String>();
        ArrayList<String> ret = new ArrayList<String>();
        for (String s : arr) {
            if (st.contains(s))
                continue;
            ret.add(s);
            st.add(s);
        }
        return ret;
    }

    public ClassNode(String class_name) {
        this.class_name = class_name;
        this.parent_name = null;
        // fields = new LinkedHashMap<String, String>();// assuming parent fields are
        // same type
        // field_id = new HashMap<String, Integer>();
        fields = new ArrayList<String>();
        field_id = new ArrayList<String>();
        methods = new LinkedHashMap<String, MethodNode>();
        origin = new HashMap<String, String>();
        method_id = new HashMap<String, Integer>();
    }

    public void add(ClassNode parentNode) {
        // System.out.println("adding: " + class_name + " " + parentNode.class_name);

        ArrayList<String> new_field_id = new ArrayList<String>();
        ArrayList<String> new_fields = new ArrayList<String>();
        for (String s : parentNode.fields) {
            new_fields.add(s);
        }
        for (String s : parentNode.field_id) {
            new_field_id.add(s);
        }
        for (String s : fields) {
            new_fields.add(s);
        }
        for (String s : field_id) {
            new_field_id.add(s);
        }

        fields = new_fields;
        field_id = new_field_id;

        LinkedHashMap<String, MethodNode> new_methods = new LinkedHashMap<String, MethodNode>();
        LinkedHashMap<String, Integer> new_method_id = new LinkedHashMap<String, Integer>();
        for (String s : parentNode.methods.keySet()) {
            if (methods.containsKey(s)) {
                MethodNode method_node = methods.get(s);
                new_methods.put(s, method_node);
                new_method_id.put(s, parentNode.method_id.get(s));
            } else {
                MethodNode method_node = parentNode.methods.get(s);
                new_methods.put(s, method_node);
                new_method_id.put(s, parentNode.method_id.get(s));
                origin.put(s, parentNode.origin.get(s));
            }
        }

        for (String s : methods.keySet()) {
            if (new_methods.containsKey(s))
                continue;
            MethodNode method_node = methods.get(s);
            new_methods.put(s, method_node);
            new_method_id.put(s, new_method_id.size());
        }

        methods = new_methods;
        method_id = new_method_id;
    }

    public int getInd(String id) {
        for (int i = field_id.size() - 1; i >= 0; i--) {
            if (field_id.get(i).equals(id)) {
                return i;
            }
        }
        return 1000;
    }

    public String getType(String id, String method) {
        // check method first
        // System.out.println("checking the type of " + id);
        MethodNode cur = methods.get(method);
        if (cur == null) {
            return null;
        }
        if (cur.formal_params.containsKey(id)) {
            return cur.formal_params.get(id);
        } else if (cur.local_vars.containsKey(id)) {
            return cur.local_vars.get(id);
        } else if (getInd(id) != 1000) {
            return fields.get(getInd(id));
        } else {
            return null;
        }
    }

    public void addField(String id, String type) {
        // fields.put(id, type);
        // field_id.put(id, field_id.size());
        fields.add(type);
        field_id.add(id);
    }

    public void addParam(String id, String method, String type) {
        MethodNode cur = methods.get(method);
        cur.addParam(id, type);
    }

    public void addLocal(String id, String method, String type) {
        MethodNode cur = methods.get(method);
        cur.addLocal(id, type);
    }

    public ArrayList<String> initialize(String id) {
        ArrayList<String> output = new ArrayList<String>();
        int v0 = 4 * (field_id.size() + 1);
        int v2 = 4 * (methods.size());

        String vmt = "vmt_" + class_name;

        // String ret = "vx0 = " + v0 + "\nvx1 = alloc(vx0)\nvx2 = " + v2 + "\n" + vmt +
        // " = alloc(vx2)\n";
        output.add("vx0 = " + v0);
        output.add("vx1 = alloc(vx0)");
        output.add("vx2 = " + v2);
        output.add(vmt + " = alloc(vx2)");

        int cnt = 0;
        for (String s : methods.keySet()) {
            String code1 = "vx" + (cnt + 3) + " = @" + origin.get(s) + s;
            String code2 = "[" + vmt + " + " + (4 * cnt) + "] = " + "vx" + (cnt + 3);

            // ret += (code1 + code2);
            output.add(code1);
            output.add(code2);
            cnt++;
        }

        String code3 = "[vx1 + 0] = " + vmt;
        // ret += code3;
        output.add(code3);
        // ret += id + " = vx1\n";
        output.add(id + " = vx1");

        return output;
    }

    public ArrayList<String> initializeMethod(String method_id) {
        // implement shadowing
        ArrayList<String> output = new ArrayList<String>();
        MethodNode method_node = methods.get(method_id);
        for (String s : keyset(field_id)) {
            if (method_node.local_vars.containsKey(s) || method_node.formal_params.containsKey(s)) {
                continue;
            }

            output.add(s + " = [this + " + 4 * (getInd(s) + 1) + "]");
        }

        return output;
    }

    public ArrayList<String> storeMethod(String method_id) {
        ArrayList<String> output = new ArrayList<String>();
        MethodNode method_node = methods.get(method_id);
        for (String s : keyset(field_id)) {
            if (method_node.local_vars.containsKey(s) || method_node.formal_params.containsKey(s)) {
                continue;
            }
            output.add("[this + " + 4 * (getInd(s) + 1) + "] = " + s);
        }

        return output;
    }

    public void addMethod(String id, String return_type) {
        MethodNode new_method = new MethodNode(id);
        new_method.return_type = return_type;
        new_method.method_name = id;
        methods.put(id, new_method);
        origin.put(id, class_name);
        method_id.put(id, method_id.size());
    }

    public String toString() {
        String ret = "<=><=><=><=><=>[  Class: " + class_name + " (" + parent_name + ")  ]<=><=><=><=><=>\n";
        ret += "fields: ";
        for (int i = 0; i < field_id.size(); i++) {
            ret += ("{" + (i + 1) + ": " + field_id.get(i) + " " + fields.get(i) + "} ");
        }
        ret += "\n";
        for (String m : methods.keySet()) {
            ret += method_id.get(m) + " : " + methods.get(m).toString() + "\n";
        }
        return ret;
    }
}

class MethodNode {
    public String method_name;
    public LinkedHashMap<String, String> formal_params;
    public LinkedHashMap<String, String> local_vars;
    public String return_type;

    public MethodNode(String method_name) {
        // System.out.println("constructing new method: " + method_name);
        this.method_name = method_name;
        this.formal_params = new LinkedHashMap<String, String>();
        this.local_vars = new LinkedHashMap<String, String>();
        this.return_type = "null";
    }

    public MethodNode(MethodNode other) {
        this.method_name = other.method_name;
        this.formal_params = other.formal_params;
        this.local_vars = new LinkedHashMap<String, String>();
        this.return_type = other.return_type;
    }

    public void addParam(String id, String type) {
        formal_params.put(id, type);
    }

    public void addLocal(String id, String type) {
        local_vars.put(id, type);
    }

    public String toString() {
        String ret = "-=-=-=-=[ Method: " + method_name + " (" + return_type + ") ]-=-=-=-=-=-\n";
        ret += "formal params: " + formal_params.toString() + "\n";
        ret += "local vars: " + local_vars.toString() + "\n";
        return ret;
    }
}
