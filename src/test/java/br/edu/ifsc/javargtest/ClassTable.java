/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.ifsc.javargtest;

import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
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
    
    public List<String> getClassFieldTypes(String cname) throws ClassNotFoundException {
        List<String> list = getClassFields(cname).stream()
                .map(f -> f.getGenericType().getTypeName())
                .collect(Collectors.toList());
        
        return list;        
    }
    

   public List<Method> getClassMethods(String cname) throws ClassNotFoundException{
        List<Method> list = new ArrayList<>();

        Class c = Class.forName(cname);

        Method m[] = c.getDeclaredMethods();

        list.addAll(Arrays.asList(m));

        return list;
    }
    
    // Fazer o mesmo para os construtores 
   public List<Constructor> getClassConstructors(String cname) throws ClassNotFoundException{
       List<Constructor> list = new ArrayList<>();
       
       Class c = Class.forName(cname);
       
       Constructor ct[] = c.getDeclaredConstructors();
       
       list.addAll(Arrays.asList(ct));
       
       return list;
       
   }
    
}
