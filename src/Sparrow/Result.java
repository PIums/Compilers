public class Result {
    public String code;
    public int c;

    public Result(String code, int c) {
        this.code = code;
        this.c = c;
    }

    public String toString() {
        String s = "code: " + code + " c: " + c + "\n";
        return s;
    }
}
