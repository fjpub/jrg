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
    
    //@Property
    boolean checkPrimitiveType(
        @ForAll("primitiveTypes") PrimitiveType.Primitive t
    ) {
        System.out.println("Tipo primitivo gerado: " + t.asString());
        return true;
    }
    
    //@Property
    boolean checkClassOrInterfaceType(
        @ForAll("classOrInterfaceTypes") ClassOrInterfaceType t
    ) {
        System.out.println("Classe gerada: " + t.asString());
        return true;
    }
    
    //@Example
    boolean checkGenPrimitiveType(){
        Arbitrary<PrimitiveType.Primitive> t = primitiveTypes();
        Arbitrary<LiteralExpr> e = t.flatMap(tp -> genPrimitiveType(new PrimitiveType(tp)));
        
        System.out.println("Expressão gerada (tipo primitivo): " + e.sample().toString());
 
        return true;        
    }
    
    //@Example
    boolean checkGenPrimitiveString(){
        
        Arbitrary<LiteralExpr> s = genPrimitiveString();
        
        System.out.println("Frase gerada: " + s.sample());
        
        return true;
    }
    
    @Example 
    boolean checkGenObjectCreation() throws ClassNotFoundException {
        ClassOrInterfaceType c = new ClassOrInterfaceType();
        c.setName("br.edu.ifsc.javargexamples.C");
        //c.setName("int");
        
        System.out.println("checkGenObjectCreation::inicio");
        
        Arbitrary<ObjectCreationExpr> e = genObjectCreation(c);
        
        if (e != null) {
            System.out.println("ObjectCreation gerado: " + e.sample().toString());
        }
        else {
            System.out.println("Não foi possível gerar criação de objeto.");
        }
        
        System.out.println("checkGenObjectCreation::fim");
        
        return true;
    }
    
    //@Property
    boolean checkGenMethodInvokation() throws ClassNotFoundException {
        ClassOrInterfaceType c = new ClassOrInterfaceType();
        c.setName("br.edu.ifsc.javargexamples.A");
        
        Arbitrary<MethodCallExpr> e = genMethodInvokation(PrimitiveType.intType());

        System.out.println("Method gerado: " + e.sample().toString());

        return true;
    }
    
    //@Example
    boolean checkGenCandidatesMethods() throws ClassNotFoundException{
        
        Arbitrary<Method> b = genCandidatesMethods("int");
        System.out.println("Candidatos Methods: " + b.sample());
        
        return true;
    } 
    
    //@Example
    boolean checkGenCandidatesFields() throws ClassNotFoundException{
        
        Arbitrary<Field> b = genCandidatesField("int");
        System.out.println("Candidatos Fields: " + b.sample());
        
        return true;
    } 
    
    //@Example
    boolean checkGenCandidatesConstructors() throws ClassNotFoundException{
        
        Arbitrary<Constructor> b = genCandidatesConstructors("br.edu.ifsc.javargexamples.C");
        System.out.println("Candidatos Constructors: " + b.sample());
        
        return true;
    } 
    
    //@Example
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
        Arbitrary<Expression> e;
        System.out.println("genExpression::inicio");
        
        System.out.println("genExpression::t = " + t);
        
        List<Arbitrary<Expression>> cand = new ArrayList<>();
        
        try {
        
            // Verifica se existem atributos candidatos
            if (!this.ct.getCandidateFields(t.asString()).isEmpty()) {
                cand.add(Arbitraries.oneOf(genAttributeAccess(t)));
            }
            // Fazer também para métodos

            // Candidatos de tipos primitivos
            if (t.isPrimitiveType()) {
                cand.add(Arbitraries.oneOf(genPrimitiveType(t.asPrimitiveType())));
            }

            // Candidados de construtores
            if (!t.isPrimitiveType()) {
                cand.add(Arbitraries.oneOf(genObjectCreation(t)));
            }
        
        } catch (ClassNotFoundException ex) {
            System.out.println("genExpression::ClassNotFoundException");
        }
        
        System.out.println("genExpression::fim");
        
        return Arbitraries.oneOf(cand);
    }
    
    
    @Provide
    Arbitrary<NodeList<Expression>> genExpressionList(List<Type> types) {
        
        System.out.println("genExpressionList::inicio");
        
        // @TODO: fix it later -- avoid the use of sample()
        List<Expression> exs = types.stream()
                .map(t -> genExpression(t))
                .map(e -> e.sample())
                .collect(Collectors.toList());
        
        NodeList<Expression> nodes = new NodeList<>(exs);
        
        System.out.println("genExpressionList::fim");
        
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
    Arbitrary<ObjectCreationExpr> genObjectCreation(Type t) {
        List<Constructor> constrs;

        System.out.println("genObjectCreation::inicio");
        
        try {
             constrs = this.ct.getClassConstructors(t.asString());
        }
        catch (ClassNotFoundException e) {
            System.out.println("genObjectCreation::Classe inválida [" + t.asString() + "]");
            return null;
        }
        
        
        Arbitrary<Constructor> c = Arbitraries.of(constrs);

        // @TODO: fix it later -- avoid the use of sample()
        Constructor ex = c.sample();
        
        System.out.println("genObjectCreation::ex = " + ex.toString());
        
        Class[] params = ex.getParameterTypes();
        
        System.out.println("genObjectCreation::params = " + params);
        
        List<Class> ps = Arrays.asList(params);
        
        System.out.println("genObjectCreation::ps = " + ps);
        
        List<Type> types = ps.stream()
                .map((tname) -> ReflectParserTranslator.reflectToParserType(tname.getName()))
                .collect(Collectors.toList());
        
        System.out.println("genObjectCreation::types = " + types);
        
        System.out.println("genObjectCreation::fim");
        
        return genExpressionList(types).map(el -> new ObjectCreationExpr(null, t.asClassOrInterfaceType(), el));
    }
    
    @Provide
    Arbitrary<FieldAccessExpr> genAttributeAccess(Type t) throws ClassNotFoundException {
        Arbitrary<Field> f = genCandidatesField(t.asString());
        
        Field field = f.sample();
        
        Arbitrary<Expression> e = genExpression(ReflectParserTranslator
                .reflectToParserType(field.getDeclaringClass().toString()));
        
        
        return e.map(obj -> new FieldAccessExpr(obj, field.getName()));
    }
    
    @Provide
    Arbitrary<MethodCallExpr> genMethodInvokation(Type t) throws ClassNotFoundException {

        Arbitrary<Method> m =  genCandidatesMethods(t.asString());

        Method method = m.sample();

        Arbitrary<Expression> e = genExpression(ReflectParserTranslator
                .reflectToParserType(method.getDeclaringClass().toString()));
        

       // return genExpressionList().map(el -> new  MethodCallExpr(e.sample(),m,el));
       return e.map(obj -> new MethodCallExpr(obj, method.getName()));
    }
    
    @Provide 
   Arbitrary<Method> genCandidatesMethods(String type) throws ClassNotFoundException{
        List<Method> candidatesMethods ;
       
        candidatesMethods = this.ct.getCandidateMethods(type);
     
        return Arbitraries.of(candidatesMethods);
    }
    
   @Provide 
   Arbitrary<Field> genCandidatesField(String type) throws ClassNotFoundException{
        List<Field> candidatesField;
       
        candidatesField = this.ct.getCandidateFields(type);
     
        return Arbitraries.of(candidatesField);
    }
   
   @Provide 
   Arbitrary<Constructor> genCandidatesConstructors(String type) throws ClassNotFoundException{
        List<Constructor> candidatesConstructors ;
       
        candidatesConstructors = this.ct.getCandidateConstructors(type);
        
        return Arbitraries.of(candidatesConstructors);
    }
}
