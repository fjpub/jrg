/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.ifsc.javargtest;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.Type;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static javafx.scene.input.KeyCode.T;
import javassist.expr.Cast;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;

/**
 *
 * @author lukra
 */
public class JRGCore {
    
    private ClassTable mCT;
    
    private JRGBase mBase;
    
    private Map<String,String> mCtx;
    
    private List<String> mValidNames;
    
    public JRGCore(ClassTable ct , JRGBase base) {
        mCT = ct;
                
        mBase = base;
        
        mCtx =  new HashMap<String, String>() {{
            put("a", "int");
            put("b", "int");
            put("c", "br.edu.ifsc.javargexamples.C");
        }};

        mValidNames = Arrays.asList("a","b","c","d","e","f","g");
        
    }
    
    @Provide
    public  Arbitrary<Expression> genExpression(Type t) {       
        Arbitrary<Expression> e;  
        List<Arbitrary<Expression>> cand = new ArrayList<>();         
        try {
            // Candidatos de tipos primitivos
            if (t.isPrimitiveType()) {
                cand.add(Arbitraries.oneOf(mBase.genPrimitiveType(
                        t.asPrimitiveType())));}           
            // Se não for tipo primitivo
            if (!t.isPrimitiveType()) {                 
                // Candidatos de construtores
                cand.add(Arbitraries.oneOf(genObjectCreation(t)));                
                // Verifica se existem atributos candidatos
                if (!mCT.getCandidateFields(t.asString()).isEmpty()) {
                    cand.add(Arbitraries.oneOf(genAttributeAccess(t)));}                
                // Verifica se existem candidados methods
                if (!mCT.getCandidateMethods(t.asString()).isEmpty()) {
                    cand.add(Arbitraries.oneOf(genMethodInvokation(t)));}                
                // Verifica se existem candidados cast
                if(!mCT.subTypes2(t.asString()).isEmpty()){
                    cand.add(Arbitraries.oneOf(genUpCast(t)));}                
                // Verifica se existem candidados Var
                if(!mCtx.isEmpty()){
                    cand.add(Arbitraries.oneOf(genVar(t)));}                     
            }
        } 
        catch (ClassNotFoundException ex) {
            JRGLog.showMessage(JRGLog.Severity.MSG_ERROR, "genExpression");
        }         
        return Arbitraries.oneOf(cand);
    }
        
    @Provide
    public  Arbitrary<NodeList<Expression>> genExpressionList(List<Type> types) {        
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genExpressionList::inicio");
              
        List<Expression> exs = types.stream()
                .map(t -> genExpression(t))
                .map(e -> e.sample())
                .collect(Collectors.toList());
        
        NodeList<Expression> nodes = new NodeList<>(exs);
        
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genExpressionList::fim");
        
        return Arbitraries.just(nodes);
    }
     
    
    @Provide
    public  Arbitrary<ObjectCreationExpr> genObjectCreation(Type t) {
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, 
                "genObjectCreation::inicio");
        
        List<Constructor> constrs;
        
        System.out.println("genObjectCreation::t = " + 
                t.asClassOrInterfaceType().asString());
        
        try {
             constrs = mCT.getClassConstructors(t.asString());
        }
        catch (ClassNotFoundException e) {
            JRGLog.showMessage(JRGLog.Severity.MSG_ERROR, "genObjectCreation"
                    + "::invalido [" + t.asString() + "] = " + e.getMessage());
            
            return null;
        }
        
        Arbitrary<Constructor> c = Arbitraries.of(constrs);

        Constructor constr = c.sample();
        
        JRGLog.showMessage(JRGLog.Severity.MSG_DEBUG, "genObjectCreation::constr : "
                + constr.toString());
        
        Class[] params = constr.getParameterTypes();
        
        List<Class> ps = Arrays.asList(params);
        
        JRGLog.showMessage(JRGLog.Severity.MSG_DEBUG, "genObjectCreation::ps "  
                + ps);
        
        List<Type> types = ps.stream()
                .map((tname) -> ReflectParserTranslator.reflectToParserType(
                        tname.getName()))
                .collect(Collectors.toList());
        
        JRGLog.showMessage(JRGLog.Severity.MSG_DEBUG, "genObjectCreation::types "
                + "[" + types +"]");
        
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genObjectCreation::fim");
        
        return genExpressionList(types).map(el -> new ObjectCreationExpr(null,
                t.asClassOrInterfaceType(), el));
    }
    
    @Provide
    public  Arbitrary<FieldAccessExpr> genAttributeAccess(Type t)
            throws ClassNotFoundException {
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genAttributeAccess::inicio");
        
        Arbitrary<Field> f = genCandidatesField(t.asString());
        
        Field field = f.sample();
        
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genAttributeAccess::field: "
                + field.getName());
        
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genAttributeAccess::Class: " 
                + field.getDeclaringClass().getName());
        
        Arbitrary<Expression> e = genExpression(ReflectParserTranslator
                .reflectToParserType(field.getDeclaringClass().getName()));
        
        
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genAttributeAccess::fim");
        
        return e.map(obj -> new FieldAccessExpr(obj, field.getName()));
    }
    
    @Provide
    public  Arbitrary<MethodCallExpr> genMethodInvokation(Type t) 
            throws ClassNotFoundException {
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genMethodInvokation::inicio");
        
        List<Method> methods;

        JRGLog.showMessage(JRGLog.Severity.MSG_DEBUG, "genMethodInvokation::t = " 
                + t.asString());
        try {        
            methods = mCT.getClassMethods(t.asString());
            
        }
        catch (ClassNotFoundException e) {
            JRGLog.showMessage(JRGLog.Severity.MSG_ERROR, "genMethodInvokation"
                    + "::invalido ["+ t.asString() + "] = " + e.getMessage());

            return null;
        }

        Arbitrary<Method> m = Arbitraries.of(methods);

        Method method = m.sample();

        Class[] params = method.getParameterTypes();

        List<Class> ps = Arrays.asList(params);

        JRGLog.showMessage(JRGLog.Severity.MSG_DEBUG, "genObjectCreation::method " 
                + method.toString());

        Arbitrary<Expression> e = genExpression(ReflectParserTranslator
        .reflectToParserType(method.getDeclaringClass().toString()));

        List<Type> types = ps.stream()
        .map((tname) -> ReflectParserTranslator.reflectToParserType(
                tname.getName()))
        .collect(Collectors.toList());

        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, 
                "genMethodInvokation::fim");

        return genExpressionList(types).map(el -> new  MethodCallExpr(
                e.sample(),method.getName(),el));
    }       
      
    @Provide    
    public Arbitrary<NameExpr> genVar(Type t) {
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genVar::inicio");                     
                
        List<NameExpr> collect = mCtx.entrySet().stream().filter(
                e -> e.getValue().equals(t.asString())).map(
                x -> new NameExpr(x.getKey())).collect(Collectors.toList());                       
                        
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, 
                "genVar::fim");
        
        return Arbitraries.of(collect);    
    }  
        
    @Provide
    public Arbitrary<CastExpr> genUpCast(Type t) 
            throws ClassNotFoundException {        
        List<Class> st = mCT.subTypes2(t.asString());
        
        Arbitrary<Class> sc = Arbitraries.of(st);
        
        Class c = sc.sample();
        
        Arbitrary<Expression> e = genExpression(ReflectParserTranslator
                .reflectToParserType(c.getName()));
        
        return e.map(obj -> new CastExpr(ReflectParserTranslator
                .reflectToParserType(t.asString()), obj));    
    } 
    
    @Provide 
    public  Arbitrary<Method> genCandidatesMethods(String type) 
            throws ClassNotFoundException {
        List<Method> candidatesMethods;
       
        candidatesMethods = mCT.getCandidateMethods(type);
     
        return Arbitraries.of(candidatesMethods);
    }
    
    @Provide 
    public  Arbitrary<Field> genCandidatesField(String type) 
            throws ClassNotFoundException {
        List<Field> candidatesField;
       
        candidatesField = mCT.getCandidateFields(type);
     
        return Arbitraries.of(candidatesField);
    }
   
    @Provide 
    public  Arbitrary<Constructor> genCandidatesConstructors(String type) 
            throws ClassNotFoundException {
        List<Constructor> candidatesConstructors ;
       
        candidatesConstructors = mCT.getCandidateConstructors(type);
        
        return Arbitraries.of(candidatesConstructors);
    }
    
    @Provide
    public  Arbitrary<Class> genCandidateUpCast(String type)
    throws ClassNotFoundException {
        List<Class> upCast ;
    
        upCast = mCT.subTypes2(type);
    
        return Arbitraries.of(upCast);
    }   
    
}
