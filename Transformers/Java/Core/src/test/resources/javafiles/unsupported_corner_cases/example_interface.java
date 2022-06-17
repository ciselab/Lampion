public interface Example{

    public void method_A();

    public default int doSomething(){
        return 5;
    }

}