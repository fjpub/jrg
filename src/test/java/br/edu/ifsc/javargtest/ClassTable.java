/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.ifsc.javargtest;

import java.util.List;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author samuel
 */
public class ClassTable {
    
    private List<String> imports;
    
    public ClassTable(List<String> imports) {
        this.imports = imports;
        
        System.out.println("Imports: " + this.imports.toString());
    }
    
    public List<String> getTypes() throws ClassNotFoundException {
        List<String> list = new ArrayList<>();
        
        for (String s : this.imports) {
            list.add(Class.forName(s).getSimpleName());
        }
        
        return list;
    }
    
    public List<Field> getClassFields(String cname) throws ClassNotFoundException {
        List<Field> list = new ArrayList<>();
        
        Class c = Class.forName(cname);
        
        Field f[] = c.getFields();
        
        list.addAll(Arrays.asList(f));
        
        return list;
    }
}
