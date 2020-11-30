
package br.edu.ifsc.javargtest;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.*;
import com.github.javaparser.printer.DotPrinter;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import net.jqwik.api.*;
import br.edu.ifsc.javargtest.JRGLog.Severity;

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
        
        JRGLog.logLevel = Severity.MSG_XDEBUG;
     
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
    boolean checkClassOrInterfaceType (
        @ForAll("classOrInterfaceTypes") ClassOrInterfaceType t
    ) {
        System.out.println("Classe gerada: " + t.asString());
        return true;
    }
    
    //@Example
    boolean checkGenPrimitiveType() {
        Arbitrary<PrimitiveType.Primitive> t = primitiveTypes();
        
        Arbitrary<LiteralExpr> e = t.flatMap(tp -> genPrimitiveType(
                new PrimitiveType(tp)));
        
        System.out.println("Expressão gerada (tipo primitivo): " + 
                e.sample().toString());
 
        return true;        
    }
    
    //@Example
    boolean checkGenPrimitiveString() {        
        Arbitrary<LiteralExpr> s = genPrimitiveString();
        
        System.out.println("Frase gerada: " + s.sample());
        
        return true;
    }
    
    //@Example
    boolean checkGenObjectCreation() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenObjectCreation"
                + "::inicio");
        
        ClassOrInterfaceType c = new ClassOrInterfaceType();
        
        c.setName("br.edu.ifsc.javargexamples.B");              
        
        Arbitrary<ObjectCreationExpr> e = genObjectCreation(c);
        
        if (e != null) {
            System.out.println("ObjectCreation gerado: " + 
                    e.sample().toString());
        }
        else {
            JRGLog.showMessage(Severity.MSG_ERROR, "Não foi possível gerar "
                    + "criação de objeto");
        }
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenObjectCreation::fim");
        
        return true;
    }
    
    @Example
    boolean checkGenMethodInvokation() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenMethodInvokation"
                + "::inicio");
        
        ClassOrInterfaceType c = new ClassOrInterfaceType();
        
        c.setName("br.edu.ifsc.javargexamples.C");
        
        Arbitrary<MethodCallExpr> e = genMethodInvokation(c);
        
        if (e != null) {
            System.out.println("Method gerado: " + e.sample().toString());
            
        }
        else {
            JRGLog.showMessage(Severity.MSG_ERROR, "Não foi possível gerar "
                    + "criação do método");
            
        }
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenMethodInvokation"
                + "::fim");
        
        return true;
    }
    
    //@Example
    boolean checkGenCandidatesMethods() throws ClassNotFoundException {        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenCandidatesMethods"
                + "::inicio");
        
        Arbitrary<Method> b = genCandidatesMethods("int");
        
        System.out.println("Candidatos Methods: " + b.sample());
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenCandidatesMethods"
                + "::fim");
        
        return true;
    } 
    
    //@Example
    boolean checkGenCandidatesFields() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenCandidatesFields"
                + "::inicio");
        
        Arbitrary<Field> b = genCandidatesField("int");
        
        System.out.println("Candidatos Fields: " + b.sample());
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenCandidatesFields:"
                + ":fim");
        
        return true;
    } 
    
    //@Example
    boolean checkGenCandidatesConstructors() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenCandidatesConstructors"
                + "::inicio");
        
        Arbitrary<Constructor> b = genCandidatesConstructors("br.edu.ifsc."
                + "javargexamples.B");
        
        System.out.println("Candidatos Constructors: " + b.sample());
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenCandidatesConstructors"
                + "::fim");
        
        return true;
    } 
    
    //@Example
    boolean checkGenExpression() {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenExpression::inicio");
        
        Arbitrary<Expression> e = genExpression(
                ReflectParserTranslator.reflectToParserType("int"));
        
        System.out.println("Expressão gerada: " + e.sample());
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenExpression::fim");
        
        return true;
    }
    
    //@Example
    boolean checkGenAttributeAccess() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenAtributteAcess"
                + "::inicio");
        
        Arbitrary<FieldAccessExpr> e = genAttributeAccess(
                ReflectParserTranslator.reflectToParserType("int"));
        
        System.out.println("Acesso gerado: " + e.sample());
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenExpression::fim");
        
        return true;
    }
    
    // Generation methods
    @Provide
    Arbitrary<PrimitiveType.Primitive> primitiveTypes() {
        return Arbitraries.of(PrimitiveType.Primitive.values());
    }
    
    @Provide
    Arbitrary<ClassOrInterfaceType> classOrInterfaceTypes() 
            throws ClassNotFoundException {
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
        JRGLog.showMessage(Severity.MSG_XDEBUG, "genExpression::inicio");
        
        Arbitrary<Expression> e;
        
        System.out.println("genExpression::t = " + t.asString());
        
        List<Arbitrary<Expression>> cand = new ArrayList<>();        
        
        try {
            // Candidatos de tipos primitivos
            if (t.isPrimitiveType()) {
                cand.add(Arbitraries.oneOf(genPrimitiveType(
                        t.asPrimitiveType())));
                
            }           
            // Se não for tipo primitivo
            if (!t.isPrimitiveType()) {                 
                // Candidatos de construtores
                cand.add(Arbitraries.oneOf(genObjectCreation(t)));
                
                // Verifica se existem atributos candidatos
                if (!this.ct.getCandidateFields(t.asString()).isEmpty()) {
                    cand.add(Arbitraries.oneOf(genAttributeAccess(t)));
                    
                }
                
                // Verifica se existem canditados methods
                if (!this.ct.getCandidateMethods(t.asString()).isEmpty()) {
                    cand.add(Arbitraries.oneOf(genMethodInvokation(t)));
                    
                }
                       
            }
        } 
        catch (ClassNotFoundException ex) {
            JRGLog.showMessage(Severity.MSG_ERROR, "genExpression");
        }
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "genExpression::fim");
        
        return Arbitraries.oneOf(cand);
    }
    
    
    @Provide
    Arbitrary<NodeList<Expression>> genExpressionList(List<Type> types) {        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "genExpressionList::inicio");
        
        // @TODO: fix it later -- avoid the use of sample()
        List<Expression> exs = types.stream()
                .map(t -> genExpression(t))
                .map(e -> e.sample())
                .collect(Collectors.toList());
        
        NodeList<Expression> nodes = new NodeList<>(exs);
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "genExpressionList::fim");
        
        return Arbitraries.just(nodes);
    }
    
    // Generating primitive types    
    @Provide
    Arbitrary<LiteralExpr> genPrimitiveType(PrimitiveType t) {
        LiteralExpr e = null;
        
        switch (t.getType()) {
            
            case BOOLEAN:                 
                return Arbitraries.of(true, false).map(b -> new 
                        BooleanLiteralExpr(b));
                
            case CHAR:                
                return Arbitraries.chars().ascii().map(c -> new CharLiteralExpr(
                        c));
                
            case DOUBLE:                
                return Arbitraries.doubles().map(d -> new DoubleLiteralExpr(
                        String.valueOf(d)));
                
            case FLOAT:                 
                return Arbitraries.floats().map(f -> new DoubleLiteralExpr(
                        String.valueOf(f)));   
                
            case INT:                
                return Arbitraries.integers().map(i -> new IntegerLiteralExpr(
                        String.valueOf(i)));
                
            case LONG:
                return Arbitraries.longs().map(l -> new LongLiteralExpr(
                        String.valueOf(l)));
                
            case BYTE:                
                return Arbitraries.bytes().map(bt -> new IntegerLiteralExpr(
                        String.valueOf(bt)));
                
            case SHORT:
                return Arbitraries.shorts().map(s -> new IntegerLiteralExpr(
                        String.valueOf(s)));
                
        }
        
        return Arbitraries.just(e);
    }
    
    @Provide
    Arbitrary<LiteralExpr> genPrimitiveString() {
        return Arbitraries.strings().ascii().map(S -> new StringLiteralExpr(
               String.valueOf(S)));
        
    }
    
    // Generating expressions    
    @Provide
    Arbitrary<ObjectCreationExpr> genObjectCreation(Type t) {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "genObjectCreation::inicio");
        
        List<Constructor> constrs;
        
        System.out.println("genObjectCreation::t = " + 
                t.asClassOrInterfaceType().asString());
        
        try {
             constrs = this.ct.getClassConstructors(t.asString());
        }
        catch (ClassNotFoundException e) {
            JRGLog.showMessage(Severity.MSG_ERROR, "genObjectCreation"
                    + "::invalido [" + t.asString() + "] = " + e.getMessage());
            
            return null;
        }
        
        Arbitrary<Constructor> c = Arbitraries.of(constrs);

        // @TODO: fix it later -- avoid the use of sample()
        Constructor constr = c.sample();
        
        JRGLog.showMessage(Severity.MSG_DEBUG, "genObjectCreation::constr "
                + constr.toString());
        
        Class[] params = constr.getParameterTypes();
        
        List<Class> ps = Arrays.asList(params);
        
        JRGLog.showMessage(Severity.MSG_DEBUG, "genObjectCreation::ps "  + ps);
        
        List<Type> types = ps.stream()
                .map((tname) -> ReflectParserTranslator.reflectToParserType(
                        tname.getName()))
                .collect(Collectors.toList());
        
        JRGLog.showMessage(Severity.MSG_DEBUG, "genObjectCreation::types [" 
                + types +"]");
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "genObjectCreation::fim");
        
        return genExpressionList(types).map(el -> new ObjectCreationExpr(null,
                t.asClassOrInterfaceType(), el));
    }
    
    @Provide
    Arbitrary<FieldAccessExpr> genAttributeAccess(Type t)
            throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "genAttributeAccess::inicio");
        
        Arbitrary<Field> f = genCandidatesField(t.asString());
        
        Field field = f.sample();
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "genAttributeAccess::field: "
                + field.getName());
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "genAttributeAccess::Class: " 
                + field.getDeclaringClass().getName());
        
        Arbitrary<Expression> e = genExpression(ReflectParserTranslator
                .reflectToParserType(field.getDeclaringClass().getName()));
        
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "genAttributeAccess::fim");
        
        return e.map(obj -> new FieldAccessExpr(obj, field.getName()));
    }
    
    @Provide
    Arbitrary<MethodCallExpr> genMethodInvokation(Type t) 
            throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "genMethodInvokation::inicio");
        
        List<Method> methods;

        JRGLog.showMessage(Severity.MSG_DEBUG, "genMethodInvokation::t = " 
                + t.asString());
        try {        
            methods = this.ct.getClassMethods(t.asString());
            
        }
        catch (ClassNotFoundException e) {
            JRGLog.showMessage(Severity.MSG_ERROR, "genMethodInvokation"
                    + "::invalido ["+ t.asString() + "] = " + e.getMessage());

            return null;
        }

        Arbitrary<Method> m = Arbitraries.of(methods);

        Method method = m.sample();

        Class[] params = method.getParameterTypes();

        List<Class> ps = Arrays.asList(params);

        JRGLog.showMessage(Severity.MSG_DEBUG, "genObjectCreation::method " 
                + method.toString());

        Arbitrary<Expression> e = genExpression(ReflectParserTranslator
        .reflectToParserType(method.getDeclaringClass().toString()));

        List<Type> types = ps.stream()
        .map((tname) -> ReflectParserTranslator.reflectToParserType(
                tname.getName()))
        .collect(Collectors.toList());

        JRGLog.showMessage(Severity.MSG_XDEBUG, "genMethodInvokation::fim");

        return genExpressionList(types).map(el -> new  MethodCallExpr(
                e.sample(),method.getName(),el));
    }
    
    @Provide 
    Arbitrary<Method> genCandidatesMethods(String type) 
            throws ClassNotFoundException {
        List<Method> candidatesMethods;
       
        candidatesMethods = this.ct.getCandidateMethods(type);
     
        return Arbitraries.of(candidatesMethods);
    }
    
    @Provide 
    Arbitrary<Field> genCandidatesField(String type) 
            throws ClassNotFoundException {
        List<Field> candidatesField;
       
        candidatesField = this.ct.getCandidateFields(type);
     
        return Arbitraries.of(candidatesField);
    }
   
    @Provide 
    Arbitrary<Constructor> genCandidatesConstructors(String type) 
            throws ClassNotFoundException {
        List<Constructor> candidatesConstructors ;
       
        candidatesConstructors = this.ct.getCandidateConstructors(type);
        
        return Arbitraries.of(candidatesConstructors);
    }
}