package br.edu.ifsc.javarg;

import br.edu.ifsc.javargexamples.A;
import br.edu.ifsc.javargexamples.B;
import br.edu.ifsc.javargexamples.C;

public class MainClass {
    
    public int fieldExample1;
    public A fieldExample2;
    
    public static void main(String args[]) {
        A aObj = new A(5, 2);
        int a = aObj.getA1();
        int b = new B().b;
        //B bOjb = new C().getB();
        //int a1 = new C().getA().a1;
        int num = 10;
    }
    
}
