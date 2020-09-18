/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.ifsc.javargtest;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.*;
import com.github.javaparser.ast.type.PrimitiveType.Primitive;
import com.github.javaparser.printer.DotPrinter;
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
    private Type desiredType;
    
    public MainTests() throws FileNotFoundException, IOException {
        this.skeleton = StaticJavaParser.parse(new File(SKELETON_PATH)); 
        this.ct = new ClassTable(loadImports());
        
        System.out.println(this.skeleton.toString());
        dumpAST();
        
        // @TODO: Modificar isso depois
        this.desiredType = new PrimitiveType(Primitive.CHAR);
    }
    
    // Auxiliary methods
    
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
    
    private void dumpAST() throws IOException {
        DotPrinter printer = new DotPrinter(true);
        try (FileWriter fileWriter = new FileWriter("ast.dot");
            PrintWriter printWriter = new PrintWriter(fileWriter)) {
            printWriter.print(printer.output(this.skeleton));
        }
    }
    
    // Properties to be tested
    
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
    
    @Property
    boolean checkGenPrimitiveType(
        @ForAll("genPrimitiveType") LiteralExpr e
    ) {
        
        // Example: dá para usar coisas dessa forma (aparentemente)
        //LiteralExpr e = genPrimitiveType().sample();
        
        System.out.println("Expressão gerada: " + e.toString());
        
        return true;        
    }
    
    // Generation methods
    
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
        
    // Generating expressions
    
    @Provide
    Arbitrary<Expression> genExpression(Type t) {
        Expression e = null;
        
        // implementar
        
        return Arbitraries.just(e);
    }
    
    // Generating primitive types
    
    @Provide
    Arbitrary<LiteralExpr> genPrimitiveType() {
        LiteralExpr e = null;
        
        System.out.println("IntegerLiteral[1]");
        
        switch (this.desiredType.asPrimitiveType().getType()) {
            case BOOLEAN: 
                // @TODO: Descobrir como fazer
                e = new BooleanLiteralExpr(false);
                break;
            case CHAR:
                e = new CharLiteralExpr(Arbitraries.chars().ascii().sample());
                break;
            case DOUBLE:
                // implementar
                break;
            case FLOAT: 
                // implementar 
                break;
            case INT:
                e = new IntegerLiteralExpr(String.valueOf(Arbitraries.integers().sample()));
                break;
        }
        
        System.out.println("IntegerLiteral[2]: " + e.toString());
        
        return Arbitraries.just(e);
    }
    
    @Provide
    Arbitrary<LiteralExpr> genPrimitiveString() {
        LiteralExpr e = null;
        
        // Implementar 
        
        return Arbitraries.just(e);
    }
    
    // Generating expressions
    
    @Provide
    Arbitrary<ObjectCreationExpr> genObjectCreation(Type t) {
        ObjectCreationExpr e = new ObjectCreationExpr();
        
        // criação de um objeto to tipo t
        
        return Arbitraries.just(e);
    }
    
    @Provide
    Arbitrary<FieldAccessExpr> genAttributeAccess(Type t) {
        FieldAccessExpr e = new FieldAccessExpr();        
        
        // acesso a um atributo
        
        return Arbitraries.just(e);
    }
    
    @Provide
    Arbitrary<MethodCallExpr> genMethodInvokation(Type t) {
        MethodCallExpr e = new MethodCallExpr();
        
        // invocação de método
        
        return Arbitraries.just(e);
    }
    
}