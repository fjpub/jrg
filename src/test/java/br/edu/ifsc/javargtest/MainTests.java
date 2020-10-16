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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static javassist.util.proxy.FactoryHelper.primitiveTypes;
import net.jqwik.api.*;
import org.junit.Test;
import static org.junit.internal.MethodSorter.getDeclaredMethods;

/**
 *
 * @author samuel
 */
public class MainTests {
    
    private static final String SKELETON_PATH = 
        "src/main/java/br/edu/ifsc/javarg/MainClass.java";
    private CompilationUnit skeleton;
    private ClassTable ct;
    
    public MainTests() throws FileNotFoundException, IOException {
        this.skeleton = StaticJavaParser.parse(new File(SKELETON_PATH)); 
        this.ct = new ClassTable(loadImports());
        
        //System.out.println(this.skeleton.toString());
        //dumpAST();
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
        System.out.println("Classe gerada: " + t.asString());
        return true;
    }
    
    @Example
    boolean checkGenPrimitiveType(){
        Arbitrary<PrimitiveType.Primitive> t = primitiveTypes();
        Arbitrary<LiteralExpr> e = t.flatMap(tp -> genPrimitiveType(new PrimitiveType(tp)));
        
        System.out.println("Expressão gerada (tipo primitivo): " + e.sample().toString());
 
        return true;        
    }
    
    @Example
    boolean checkGenPrimitiveString(){
        
        Arbitrary<LiteralExpr> s = genPrimitiveString();
        
        System.out.println("Frase gerada: " + s.sample());
        
        return true;
    }
    
    @Example 
    boolean checkGenObjectCreation() throws ClassNotFoundException {
        ClassOrInterfaceType c = new ClassOrInterfaceType();
        c.setName("br.edu.ifsc.javargexamples.A");
        
        Arbitrary<ObjectCreationExpr> e = genObjectCreation(c);
        
        System.out.println("ObjectCreation gerado: " + e.sample().toString());
        
        return true;
    }
    
    @Property
    boolean checkGenMethodInvokation() throws ClassNotFoundException {
        ClassOrInterfaceType c = new ClassOrInterfaceType();
        c.setName("br.edu.ifsc.javargexamples.A");
        
        Arbitrary<MethodCallExpr> e = genMethodInvokation(c ,"getA");

        System.out.println("Method gerado: " + e.sample().toString());

        return true;
    }
    
    @Example
    boolean checkGenCandidatesMethods() throws ClassNotFoundException{
        
        List<Method> b = genCandidatesMethods("int");
        System.out.println("Candidatos Methods: " + b);
        
        return true;
    } 
    
    @Example
    boolean checkGenCandidatesFields() throws ClassNotFoundException{
        
        Arbitrary<Field> b = genCandidatesField("int");
        System.out.println("Candidatos Fields: " + b.sample());
        
        return true;
    } 
    
    @Example
    boolean checkGenCandidatesConstructors() throws ClassNotFoundException{
        
        List<Constructor> b = genCandidatesConstructors("br.edu.ifsc.javargexamples.C");
        System.out.println("Candidatos Constructors: " + b);
        
        return true;
    } 
    
    @Example
    boolean checkGenExpression(){
        Arbitrary<Expression> e = genExpression(ReflectParserTranslator.reflectToParserType("int"));
        
        System.out.println("Expressão gerada: " + e.sample());
        
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
        try {
                   
            return Arbitraries.oneOf(genPrimitiveType(t.asPrimitiveType()),
                    genAttributeAccess(t));
        } catch (ClassNotFoundException ex) {
       
            System.out.println("Erro: " + ex.getMessage());
        }
        return null;
    }
    
    
    @Provide
    Arbitrary<NodeList<Expression>> genExpressionList(List<Type> types) {
        
        // @TODO: fix it later -- avoid the use of sample()
        List<Expression> exs = types.stream()
                .map(t -> genExpression(t))
                .map(e -> e.sample())
                .collect(Collectors.toList());
        
        NodeList<Expression> nodes = new NodeList<>(exs);
        
        return Arbitraries.just(nodes);
    }
    
    // Generating primitive types
    
    @Provide
    Arbitrary<LiteralExpr> genPrimitiveType(PrimitiveType t) {
        LiteralExpr e = null;
        
        switch (t.getType()) {
            case BOOLEAN: 
                return Arbitraries.of(true, false).map(b -> new BooleanLiteralExpr(b));
            case CHAR:
                return Arbitraries.chars().ascii().map(c -> new CharLiteralExpr(c));
            case DOUBLE:
                return Arbitraries.doubles().map(d -> new DoubleLiteralExpr(String.valueOf(d)));
            case FLOAT: 
                return Arbitraries.floats().map(f -> new DoubleLiteralExpr(String.valueOf(f)));   
            case INT:
                return Arbitraries.integers().map(i -> new IntegerLiteralExpr(String.valueOf(i)));
            case LONG:
                return Arbitraries.longs().map(l -> new LongLiteralExpr(String.valueOf(l)));
            case BYTE:
                return Arbitraries.bytes().map(bt -> new IntegerLiteralExpr(String.valueOf(bt)));
            case SHORT:
                return Arbitraries.shorts().map(s -> new IntegerLiteralExpr(String.valueOf(s))); 
        }
        
        return Arbitraries.just(e);
    }
    
    @Provide
    Arbitrary<LiteralExpr> genPrimitiveString() {
       return Arbitraries.strings().ascii().map(S -> new StringLiteralExpr(String.valueOf(S)));
        
    }
    
    // Generating expressions
    
    @Provide
    Arbitrary<ObjectCreationExpr> genObjectCreation(ClassOrInterfaceType t) throws ClassNotFoundException {
        List<Constructor> constrs = this.ct.getClassConstructors(t.getNameAsString());
        Arbitrary<Constructor> c = Arbitraries.of(constrs);

        // @TODO: fix it later -- avoid the use of sample()
        Constructor ex = c.sample();
        
        Class[] params = ex.getParameterTypes();
        List<Class> ps = Arrays.asList(params);
        
        List<Type> types = ps.stream()
                .map((tname) -> ReflectParserTranslator.reflectToParserType(tname.getName()))
                .collect(Collectors.toList());
        
        return genExpressionList(types).map(el -> new ObjectCreationExpr(null, t, el));
    }
    
    @Provide
    Arbitrary<FieldAccessExpr> genAttributeAccess(Type t) throws ClassNotFoundException {
        Arbitrary<Field> f = genCandidatesField(t.asString());
        
        Field field = f.sample();
        
        Arbitrary<Expression> e = genExpression(ReflectParserTranslator.reflectToParserType(field.getDeclaringClass().toString()));
        
        
        return e.map(obj -> new FieldAccessExpr(obj, field.getName()));
    }
    
     @Provide
    Arbitrary<MethodCallExpr> genMethodInvokation(ClassOrInterfaceType t, String m) throws ClassNotFoundException {

        List<Method> method = this.ct.getClassMethods(t.getNameAsString());

        List<Method> ms = method.stream()
        .filter(meth -> meth.getName().equals(m)).collect(Collectors.toList());

        Arbitrary<Method> me= Arbitraries.of(ms);

        Method ex = me.sample();

        Class[] params = ex.getParameterTypes();

        List<Class> ps = Arrays.asList(params);

        List<Type> types = ps.stream()
        .map((tname) -> ReflectParserTranslator.reflectToParserType(tname.getName()))
        .collect(Collectors.toList());
        
        // erro nesta expressão 
        Arbitrary<Expression> e = genExpression(t);

        return genExpressionList(types).map(el -> new  MethodCallExpr(e.sample(),m,el));
    
    }
    
    @Provide 
   List<Method> genCandidatesMethods(String type) throws ClassNotFoundException{
        List<Method> candidatesMethods ;
       
        candidatesMethods = this.ct.getCandidateMethods(type);
     
        return candidatesMethods;
    }
    
   @Provide 
   Arbitrary<Field> genCandidatesField(String type) throws ClassNotFoundException{
        List<Field> candidatesField;
       
        candidatesField = this.ct.getCandidateFields(type);
     
        return Arbitraries.of(candidatesField);
    }
   
   @Provide 
   List<Constructor> genCandidatesConstructors(String type) throws ClassNotFoundException{
        List<Constructor> candidatesConstructors ;
       
        candidatesConstructors = this.ct.getCandidateConstructors(type);
        
        return candidatesConstructors;
    }
}
