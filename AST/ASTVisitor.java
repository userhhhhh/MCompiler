package AST;

import AST.Def.*;
import AST.Expr.*;
import AST.Stmt.*;

public interface ASTVisitor {
    void visit(Program node);

    void visit(ClassTypeDef node);
    void visit(FunctionDef node);
    void visit(VariableDef node);
    void visit(InitVariable node);
    void visit(Definition node);
    void visit(Expression node);
    void visit(ArrayExpr node);
    void visit(BinaryExpr node);
    void visit(ParallelExp node);
    void visit(FuncCallExpr node);

    void visit(BreakStmt node);
    void visit(ContinueStmt node);
    void visit(ExprStmt node);
    void visit(ForStmt node);
    void visit(IfStmt node);
    void visit(ReturnStmt node);
    void visit(Suite node);
    void visit(VariableDefStmt node);
    void visit(WhileStmt node);
}
