/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.ifsc.javargtest;

import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;

/**
 *
 * @author samuel
 */
public class ReflectParserTranslator {
    
    public static Type reflectToParserType(String tname) {
        Type t = null;
        
        if(tname.equals("int")){
             t = PrimitiveType.intType();
        }else if(tname.equals("float")){
             t = PrimitiveType.floatType();
        }else if(tname.equals("double")){
             t = PrimitiveType.doubleType();
        }else if(tname.equals("boolean")){
             t = PrimitiveType.booleanType();
        }else if(tname.equals("char")){
             t = PrimitiveType.charType();
        }else if(tname.equals("long")){
             t = PrimitiveType.longType();
        }else if(tname.equals("byte")){
             t = PrimitiveType.byteType();
        }else if(tname.equals("short")){
             t = PrimitiveType.shortType();
        }
      
        return t;
    }
    
}
