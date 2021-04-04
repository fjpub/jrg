
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
import net.jqwik.api.*;
import br.edu.ifsc.javargtest.JRGLog.Severity;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.WhileStmt;

/**
 *
 * @author samuel
 */
public class MainTests {
    
    private static final String SKELETON_PATH = 
        "src/main/java/br/edu/ifsc/javarg/MainClass.java";
        
    private CompilationUnit mSkeleton;
    
    private ClassTable mCT;
   
    private JRGBase mBase;
    
    private JRGCore mCore;
    
    private JRGStmt mStmt;
    
    private JRGOperator mOperator;
    
    public MainTests() throws FileNotFoundException, IOException {
        mSkeleton = StaticJavaParser.parse(new File(SKELETON_PATH));
        
        dumpAST();
    
        mCT = new ClassTable(loadImports());
        
        mBase = new JRGBase(mCT);
        
        mCore = new JRGCore(mCT , mBase);       
        
        mStmt = new JRGStmt(mCT , mBase, mCore, mOperator);
        
        mOperator = new JRGOperator(mCT , mBase , mCore, mStmt);
        
        JRGLog.logLevel = Severity.MSG_XDEBUG;
     
    }
    
    // Auxiliary methods    
    private List<String> loadImports() {
        NodeList<ImportDeclaration> imports = mSkeleton.getImports();
        
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
            
            printWriter.print(printer.output(mSkeleton));
        }
    }
           
    //@Example
    boolean checkGenPrimitiveType() {
        Arbitrary<PrimitiveType.Primitive> t = mBase.primitiveTypes();
        
        Arbitrary<LiteralExpr> e = t.flatMap(tp -> mBase.genPrimitiveType(
                new PrimitiveType(tp)));
        
        System.out.println("Expressão gerada (tipo primitivo): " + 
                e.sample().toString());
 
        return true;        
    }
    
    //@Example
    boolean checkGenPrimitiveString() {        
        Arbitrary<LiteralExpr> s = mBase.genPrimitiveString();
        
        System.out.println("Frase gerada: " + s.sample());
        
        return true;
    }
    
    //@Example
    boolean checkGenObjectCreation() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenObjectCreation"
                + "::inicio");
        
        ClassOrInterfaceType c = new ClassOrInterfaceType();
        
        c.setName("br.edu.ifsc.javargexamples.B");              
        
        Arbitrary<ObjectCreationExpr> e = mCore.genObjectCreation(c);
        
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
    
    //@Example
    boolean checkGenMethodInvokation() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenMethodInvokation"
                + "::inicio");
        
        ClassOrInterfaceType c = new ClassOrInterfaceType();
        
        c.setName("br.edu.ifsc.javargexamples.A");
        
        Arbitrary<MethodCallExpr> e = mCore.genMethodInvokation(c);
        
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
        
        Arbitrary<Method> b = mCore.genCandidatesMethods("int");
        
        System.out.println("Candidatos Methods: " + b.sample());
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenCandidatesMethods"
                + "::fim");
        
        return true;
    } 
    
    //@Example
    boolean checkGenCandidatesFields() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenCandidatesFields"
                + "::inicio");
        
        Arbitrary<Field> b = mCore.genCandidatesField("int");
        
        System.out.println("Candidatos Fields: " + b.sample());
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenCandidatesFields:"
                + ":fim");
        
        return true;
    } 
    
   // @Example
    boolean checkGenCandidatesConstructors() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenCandidatesConstructors"
                + "::inicio");
        
        Arbitrary<Constructor> b = mCore.genCandidatesConstructors("br.edu."
                + "ifsc.javargexamples.B");
        
        System.out.println("Candidatos Constructors: " + b.sample());
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenCandidatesConstructors"
                + "::fim");
        
        return true;
    } 
    
    //@Example
    boolean checkGenExpression() {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenExpression::inicio");
        
        Arbitrary<Expression> e = mCore.genExpression(
                ReflectParserTranslator.reflectToParserType("int"));
        
        System.out.println("Expressão gerada: " + e.sample());
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenExpression::fim");
        
        return true;
    }
    
    //@Example
    boolean checkGenAttributeAccess() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenAtributteAcess"
                + "::inicio");
        
        Arbitrary<FieldAccessExpr> e = mCore.genAttributeAccess(
                ReflectParserTranslator.reflectToParserType("int"));
        
        System.out.println("Acesso gerado: " + e.sample());
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenExpression::fim");
        
        return true;
    }
    
    //@Example
    boolean checkGenUpCast() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenUpCast"
                + "::inicio");
        
         Arbitrary<CastExpr> e = mCore.genUpCast(
                 ReflectParserTranslator.reflectToParserType("int"));
        
        System.out.println("CheckGenUpCast: " + e.sample());
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenUpCast"
                + "::final");
        
        return true;
    }
    
    //@Example
    boolean checkGenVar() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenVar"
                + "::inicio");
        
        Arbitrary<NameExpr> e = mCore.genVar(
                ReflectParserTranslator.reflectToParserType("br.edu.ifsc."
                + "javargexamples.C"));
        
        System.out.println("checkGenVar: " + e.sample());
        
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenVar"
                + "::final");
        return true;
    }
    
    //@Example
    boolean checkVariableDeclarator(){
        Arbitrary<VariableDeclarator> v = mStmt.genVarDeclarator(
                PrimitiveType.floatType(),"varA");
        
        System.out.println("Variavel: " + v.sample());
        
        return true;
    }    
   
    //@Example
    boolean checkSuperTypes() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkSuperTypes"
                + "::inicio");
        
        List<Class> b = mCT.superTypes("br.edu.ifsc."
                + "javargexamples.AextendExtend");
        
        b.forEach((i) -> {
            System.out.println("SuperTypes: " + i );
        });
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkSuperTypes"
                + "::final");
        
        return true;
    }
    
    //@Example
    boolean checkSubTypes() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkSubTypes"
                + "::inicio");
        
        List<Class> b = mCT.subTypes("br.edu.ifsc."
                + "javargexamples.A");
        
         b.forEach((i) -> {
            System.out.println("subTypes: " + i.toString() );
        });
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkSubTypes"
                + "::final");
        
        return true;
    }
    
    //@Example
    boolean checkSubTypes2() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkSubTypes"
                + "::inicio");
        
        List<Class> b = mCT.subTypes2("br.edu.ifsc."
                + "javargexamples.A");
        
         b.forEach((i) -> {
            System.out.println("subTypes: " + i.toString() );
        });
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkSubTypes"
                + "::final");
        
        return true;
    }
    
   
    
    //@Example
    boolean checkGenCandidateUpCast() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenCandidateUpCast"
                + "::inicio");
        
        Arbitrary<Class> b = mCore.genCandidateUpCast("br.edu.ifsc."
                + "javargexamples.A");
        
        System.out.println("Candidatos UpCast: " + b.sample().getName());
         
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenCandidateUpCast"
                + "::final");
        
        return true;
    }         
     
    //@Example
    boolean checkGenBlockStmt() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenBlockStmt::inicio");
        
        Arbitrary<BlockStmt> e = mStmt.genBlockStmt(
                ReflectParserTranslator.reflectToParserType("br.edu.ifsc."
                + "javargexamples.B"));
        
        System.out.println("BlockStmt: " + e.sample());
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenBlockStmt::fim");
        
        return true;
    }
    
    //@Example
    boolean checkGenVarDeclaration() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenVarDeclaration::inicio");
        
        Arbitrary<VariableDeclarationExpr> e = mStmt.genVarDeclaration(
                ReflectParserTranslator.reflectToParserType("int"),"varA");
        
        System.out.println("checkGengenVarDeclaration: " + e.sample());
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenVarDeclaration::fim");
        
        return true;
    }
    
    //@Example
    boolean checkGenConditionalExpr() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenVarDeclaration::inicio");
        
        Arbitrary<ConditionalExpr> e = mCore.genConditionalExpr(
                ReflectParserTranslator.reflectToParserType("float"));
        
        System.out.println("checkGengenVarDeclaration: " + e.sample());
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenVarDeclaration::fim");
        
        return true;
    }
    
     @Example 
     boolean checkGenIfStmt() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenIfStmt::inicio");
        
        Arbitrary<IfStmt> e = mStmt.genIfStmt();
        
        System.out.println("checkGenIfStmt: " + e.sample());
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenIfStmt::fim");
        
        return true;
    }
     
    //@Example
    boolean checkWhileStmt() {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkWhileStmt::inicio");
        
        Arbitrary<WhileStmt> e = mStmt.genWhileStmt();
        
        System.out.println("checkWhileStmt: " + e.sample());
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkWhileStmt::fim");
        
        return true;
        
    }
    
    //@Example
    boolean checkGenOpExpression() {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenOpExpression::inicio");
        
        Arbitrary<BinaryExpr> e = mOperator.genOpExpression();
        
        System.out.println("checkGenOpExpression: " + e.sample());
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenOpExpression::fim");
        
        return true;
    }
    
    //@Example
    boolean checkGenAuExpression() {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenAuExpression::inicio");
        
        Arbitrary<BinaryExpr> e = mOperator.genAuExpression();
        
        System.out.println("checkGenAuExpression: " + e.sample());
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenAuExpression::fim");
        
        return true;
    }
    
    //@Example
    boolean checkGenMaExpression() {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenMaExpression::inicio");
        
        Arbitrary<BinaryExpr> e = mOperator.genMaExpression();
        
        System.out.println("checkGenAuExpression: " + e.sample());
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenMaExpression::fim");
        
        return true;
    }
    
}
