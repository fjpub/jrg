/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.ifsc.javargexamples;

/**
 *
 * @author samuel
 */
public class C {
    
    public A aInC;
    public B bInC;
    
    public C() {
        
    }
    
    public void setA(A a) {
        this.aInC = a;
    }
    
    public A getA() {
        return aInC;
    }
    
    public void setB(B b) {
        this.bInC = b;
    }
    
    public B getB() {
        return bInC;
    }
}
