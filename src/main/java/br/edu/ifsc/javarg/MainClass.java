package br.edu.ifsc.javarg;

import br.edu.ifsc.javargexamples.A;
import br.edu.ifsc.javargexamples.B;
import br.edu.ifsc.javargexamples.C;

public class MainClass {
    
    public int fieldExample1;
    public A fieldExample2;
    
    public static void main(String args[]) {
        A aObj = new A();
        int a = aObj.getA();
        int b = new B().b;
        B bOjb = new C().getB();
        int a1 = new C().getA().a;
        int num = 10;
    }
    
}
