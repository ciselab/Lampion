public class Outer {

    public int outerDoSomething(int o){
        int con = 5;
        return o + con;
    }

    private class Inner {

        public int innerDoSomething(int i){
            int cin = 15;
            return i + cin;
        }

    }

}