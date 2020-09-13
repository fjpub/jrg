/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.ifsc.javargtest;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.*;
import java.io.*;
import java.util.*;
import net.jqwik.api.*;

/**
 *
 * @author samuel
 */
public class MainTests {
    
    private static final String SKELETON_PATH = 
        "src/main/java/br/edu/ifsc/javarg/MainClass.java";
    private CompilationUnit skeleton;
    private ClassTable ct;
    
    public MainTests() throws FileNotFoundException {
        this.skeleton = StaticJavaParser.parse(new File(SKELETON_PATH)); 
        this.ct = new ClassTable(loadImports());
        
        System.out.println(this.skeleton.toString());
    }
    
    private List<String> loadImports() {
        NodeList<ImportDeclaration> imports = this.skeleton.getImports();
        List<String> list = new ArrayList<>();
        
        Iterator<ImportDeclaration> it = imports.iterator();
        
        while (it.hasNext()) {
            ImportDeclaration i = it.next();
            list.add(i.getName().asString());
        }
        
        return list;
    }
    
    @Property
    boolean checkPrimitiveType(
        @ForAll("primitiveTypes") PrimitiveType.Primitive t
    ) {
        System.out.println("Tipo primitivo gerado: " + t.asString());
        return true;
    }
    
    @Property
    boolean checkClassOrInterfaceType(
        @ForAll("classOrInterfaceTypes") ClassOrInterfaceType t
    ) {
        System.out.println("Tipo gerado: " + t.asString());
        return true;
    }
    
    @Provide
    Arbitrary<PrimitiveType.Primitive> primitiveTypes() {
        return Arbitraries.of(PrimitiveType.Primitive.values());
    }
    
    @Provide
    Arbitrary<ClassOrInterfaceType> classOrInterfaceTypes() throws ClassNotFoundException {
        List<ClassOrInterfaceType> list = new ArrayList<>();
        
        for (String s : this.ct.getTypes()) {
            ClassOrInterfaceType c = new ClassOrInterfaceType();
            c.setName(s);
            list.add(c);
        }
        
        return Arbitraries.of(list);
    }
    
    @Provide
    Arbitrary<BlockStmt> attributeAccess(Type t, String f) {
        BlockStmt b = new BlockStmt();        
        
        // Gerar um bloco: acesso do campo f em um objeto do tipo t
        
        return Arbitraries.just(b);
    }
    
    @Provide
    Arbitrary<BlockStmt> objectCreation(Type t) {
        BlockStmt b = new BlockStmt();
        
        // Gerar um block: criação de um objeto to tipo t
        
        return Arbitraries.just(b);
    }
}
