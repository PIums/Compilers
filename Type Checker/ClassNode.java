import java.util.*;

public class ClassNode {
    public String class_name;
    public String parent_name;
    public HashMap<String, String> fields;
    public HashMap<String, MethodNode> methods;

    public ClassNode(String class_name) {
        this.class_name = class_name;
        this.parent_name = "null";
        fields = new HashMap<String, String>();
        methods = new HashMap<String, MethodNode>();
    }

    public void add(ClassNode parentNode) {
        for (String s : parentNode.fields.keySet()) {
            if (fields.containsKey(s)) {
                continue;
            } else {
                String type = parentNode.fields.get(s);
                fields.put(s, type);
            }
        }

        for (String s : parentNode.methods.keySet()) {
            if (methods.containsKey(s)) {
                if (methods.get(s).sametype(parentNode.methods.get(s))) {
                    continue;
                } else {
                    System.out.println("Type error");
                    System.err.println("NO OVERLOADING!!!");
                    System.exit(1);
                }
            } else {
                MethodNode method_node = new MethodNode(parentNode.methods.get(s));
                methods.put(s, method_node);
            }
        }
    }

    /*
     * public ClassNode(String class_name, String parent_name, ClassNode parentNode)
     * {
     * this.class_name = class_name;
     * this.parent_name = parent_name;
     * this.fields = new HashMap<String, String>(parentNode.fields);
     * this.methods = new HashMap<String, MethodNode>(parentNode.methods);
     * }
     */

    public void addField(String id, String type) {
        fields.put(id, type);
    }

    public String getField(String id) {
        if (fields.containsKey(id)) {
            return fields.get(id);
        } else {
            return null;
        }
    }

    public void addMethod(String id, String return_type) {
        MethodNode new_method = new MethodNode(id);
        new_method.return_type = return_type;
        methods.put(id, new_method);
    }

    public String getLocal(String id, String method) {
        if (method == null) {
            return null; // bad practice, should re-format this later
        }
        // System.out.println("getting local: " + id + " " + method);
        MethodNode cur_method = methods.get(method);
        if (cur_method.formal_params.containsKey(id)) {
            return cur_method.formal_params.get(id);
        } else if (cur_method.local_vars.containsKey(id)) {
            return cur_method.local_vars.get(id);
        }

        return null;
    }

    public void addParam(String id, String method, String type) {
        MethodNode cur = methods.get(method);
        if (cur.formal_params.containsKey(id)) {
            System.out.println("Type error");
            System.err.println("Two params with same name: " + id);
            System.exit(1);
        }
        cur.addParam(id, type);
    }

    public void addLocal(String id, String method, String type) {
        MethodNode cur = methods.get(method);
        cur.addLocal(id, type);
    }

    public String toString() {
        String ret = "<=><=><=><=><=>[  Class: " + class_name + " (" + parent_name + ")  ]<=><=><=><=><=>\n";
        ret += "fields: " + fields.toString() + "\n";
        for (String m : methods.keySet()) {
            ret += methods.get(m).toString() + "\n";
        }
        return ret;
    }
}

class MethodNode {
    public String method_name;
    public LinkedHashMap<String, String> formal_params;
    public HashMap<String, String> local_vars;
    public String return_type;

    public MethodNode(String method_name) {
        this.method_name = method_name;
        this.formal_params = new LinkedHashMap<String, String>();
        this.local_vars = new HashMap<String, String>();
        this.return_type = "null";
    }

    public MethodNode(MethodNode other) {
        this.method_name = other.method_name;
        this.formal_params = other.formal_params;
        this.local_vars = new HashMap<String, String>();
        this.return_type = other.return_type;
    }

    public void addParam(String id, String type) {
        formal_params.put(id, type);
    }

    public void addLocal(String id, String type) {
        local_vars.put(id, type);
    }

    public boolean sametype(MethodNode other) {
        if (this.return_type != other.return_type) {
            return false;
        }
        ArrayList<String> types1 = new ArrayList<String>();
        ArrayList<String> types2 = new ArrayList<String>();
        for (String s : this.formal_params.keySet()) {
            types1.add(this.formal_params.get(s));
        }
        for (String s : other.formal_params.keySet()) {
            types2.add(other.formal_params.get(s));
        }

        if (types1.size() != types2.size()) {
            return false;
        }

        for (int i = 0; i < types1.size(); i++) {
            if (!types1.get(i).equals(types2.get(i))) {
                return false;
            }
        }
        return true;
    }

    public String toString() {
        String ret = "-=-=-=-=[ Method: " + method_name + " (" + return_type + ") ]-=-=-=-=-=-\n";
        ret += "formal params: " + formal_params.toString() + "\n";
        ret += "local vars: " + local_vars.toString() + "\n";
        return ret;
    }
}
