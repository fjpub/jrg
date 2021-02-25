
package br.edu.ifsc.javargtest;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.type.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;

public class JRGStmt {
    private ClassTable mCT;
    
    private JRGBase mBase;
    
    private Map<String,String> mCtx;
    
    private List<String> mValidNames;
    
    private JRGCore mCore;
    
    public JRGStmt(ClassTable ct , JRGBase base, JRGCore core) {
        mCT = ct;
                
        mBase = base;
        
        mCore = core;
        
        mCtx =  new HashMap<String, String>() {{
            put("a", "int");
            put("b", "int");
            put("c", "br.edu.ifsc.javargexamples.C");
        }};

        mValidNames = Arrays.asList("a","b","c","d","e","f","g");
        
    }
    
    @Provide
    public  Arbitrary<Statement> genStatement() {
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genStatement::inicio");       
        //Gerar todos o possiveis statement menos o block   
        
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genStatement::fim");   
        
        return Arbitraries.oneOf(genIfStmt(), genWhileStmt());
    } 
    
    @Provide
    public Arbitrary<NodeList<Statement>> genStatementList() {
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genStatementList::inicio");       
                       
        List<Statement> exs = (List<Statement>) genStatement();
        
        NodeList<Statement> nodes = new NodeList<>(exs);
        
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genStatementList::fim");
        
        return Arbitraries.just(nodes);
    }      
    
    @Provide
    public Arbitrary<BlockStmt> genBlockStmt(Type types) 
            throws ClassNotFoundException {
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genBlockStmt::inicio");       
        
        List<Statement> stmt;               
        
        
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genBlockStmt::fim");
        
        return null;
    }
    
    @Provide
    public Arbitrary<VariableDeclarator> genVarDeclarator(Type t, String n) {
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genVarDeclarator::inicio");
        
        Arbitrary<Expression> e = mCore.genExpression(t);
        
       
         
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genVarDeclarator::fim");
        return e.map(obj -> new VariableDeclarator(t, n, obj));
    }    
    
    @Provide
    public Arbitrary<VariableDeclarationExpr> genVarDeclaration(Type t) {
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genVarDeclaration::inicio");       
                
        
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genVarDeclaration::fim"); 
        
        return null;
    }
    
    @Provide
    public Arbitrary<IfStmt> genIfStmt() {
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genIfStmt::inicio");       
                       
        
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genIfStmt::fim"); 
        return null;
    }
    
    @Provide
    public Arbitrary<WhileStmt> genWhileStmt() {
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genWhileStmt::inicio");       
                       
        
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genWhileStmt::fim"); 
        return null;
    }   
    
    
}
