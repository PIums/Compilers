import java.util.*;

public class FPosition {
    ArrayList<ArrayList<Integer>> adj;
    HashMap<String, Integer> get_line;
    HashMap<Integer, String> goto_pair;

    HashMap<Integer, HashSet<String>> in;
    HashMap<Integer, HashSet<String>> out;
    HashMap<Integer, HashSet<String>> use;
    HashMap<Integer, HashSet<String>> def;

    HashMap<String, Integer> left;
    HashMap<String, Integer> right;

    ArrayList<Interval> intervals;

    HashMap<String, String> temp;
    HashMap<String, String> perm;
    PriorityQueue<String> pool;

    int line_number;
    static int MAXN = 10000;

    public FPosition() {
        adj = new ArrayList<ArrayList<Integer>>();
        get_line = new HashMap<String, Integer>();
        goto_pair = new HashMap<Integer, String>();

        in = new HashMap<Integer, HashSet<String>>();
        out = new HashMap<Integer, HashSet<String>>();
        use = new HashMap<Integer, HashSet<String>>();
        // def = new HashMap<Integer, String>();
        def = new HashMap<Integer, HashSet<String>>();

        left = new HashMap<String, Integer>();
        right = new HashMap<String, Integer>();

        intervals = new ArrayList<Interval>();

        temp = new HashMap<String, String>();
        perm = new HashMap<String, String>();
        pool = new PriorityQueue<String>();
        for (int i = 1; i <= 11; i++) {
            pool.add("s" + i);
        }
        for (int i = 2; i <= 5; i++) {
            pool.add("t" + i);
        }

        // System.out.println(pool.peek());
        line_number = 1;

        for (int i = 0; i < MAXN; i++) {
            adj.add(new ArrayList<Integer>());
            in.put(i, new HashSet<String>());
            out.put(i, new HashSet<String>());
            def.put(i, new HashSet<String>());
            use.put(i, new HashSet<String>());
        }
    }

    public void finalize() {

        for (int i = 1; i < line_number; i++) {
            adj.get(i).add(i + 1);
            if (goto_pair.containsKey(i)) {
                String label_id = goto_pair.get(i);
                adj.get(i).add(get_line.get(label_id));
            }
        }

        for (int i = 1; i <= line_number; i++) {
            for (String s : def.get(i)) {
                left.put(s, i);
                right.put(s, i);
            }
        }

        for (int ii = 0; ii < 100; ii++) {
            for (int i = 1; i <= line_number; i++) {
                HashSet<String> setdiff = new HashSet<String>(out.get(i));
                setdiff.removeAll(def.get(i));
                setdiff.addAll(use.get(i));

                in.put(i, setdiff);
                for (String s : setdiff) {
                    right.put(s, Math.max(right.getOrDefault(s, -1000000), i));
                }
            }

            for (int i = 1; i <= line_number; i++) {
                HashSet<String> union = new HashSet<String>();
                union.addAll(def.get(i));
                for (int j : adj.get(i)) {
                    union.addAll(in.get(j));
                }

                out.put(i, union);
                for (String s : union) {
                    left.put(s, Math.min(left.getOrDefault(s, 1000000), i));
                }
            }
        }

        for (String s : left.keySet()) {
            Interval range = new Interval(s, left.get(s), right.get(s));
            intervals.add(range);
        }

        Collections.sort(intervals);

        assignReg();
    }

    void assignReg() {
        // System.out.println("assigning regs");
        for (Interval range : intervals) {
            String id = range.id;

            HashSet<String> retire = new HashSet<String>();
            for (String s : temp.keySet()) {
                if (right.get(s) < left.get(id)) {
                    retire.add(s);
                }
            }
            for (String s : retire) {
                // put this into perm
                perm.put(s, temp.get(s));
                // perm.put(s, "Memory");
                pool.add(temp.get(s));
                temp.remove(s);
            }

            if (pool.size() > 0) {
                String reg = pool.peek();
                temp.put(id, reg);
                pool.remove(reg);
                // System.out.println("assigning: " + id + " " + reg);
            } else {
                int latest = 0;
                String remove = "joe mama";
                for (String s : temp.keySet()) {
                    if (right.get(s) > latest) {
                        latest = right.get(s);
                        remove = s;
                    }
                }

                if (latest <= right.get(id)) {
                    perm.put(id, "Memory");
                } else {
                    temp.put(id, temp.get(remove)); // send the latest interval into memory
                    temp.remove(remove);
                    perm.put(remove, "Memory");
                }
            }
        }

        for (String s : temp.keySet()) {
            perm.put(s, temp.get(s));
            // perm.put(s, "Memory");
            // System.out.println("perming: " + s + " " + perm.get(s));
        }
    }

    public String toString() {
        String s = "total lines: " + line_number + "\n";

        s += "def / use: \n";
        for (int i = 1; i <= line_number; i++) {
            s += i + ": ";
            for (String str : def.get(i)) {
                s += str + " ";
            }
            s += "|";
            for (String str : use.get(i)) {
                s += str + " ";
            }
            s += "\n";
        }

        s += "intervals:\n";
        for (Interval range : intervals) {
            s += range.id + " (" + range.left + "," + range.right + ")\n";
        }

        for (String str : perm.keySet()) {
            s += str + " " + perm.get(str) + "\n";
        }

        return s;
    }
}

class Interval implements Comparable<Interval> {
    String id;
    int left;
    int right;

    public Interval(String id, int left, int right) {
        this.id = id;
        this.left = left;
        this.right = right;
    }

    public int compareTo(Interval other) {
        if (left == other.left) {
            return Integer.compare(right, other.right);
        } else {
            return Integer.compare(left, other.left);
        }
    }
}
