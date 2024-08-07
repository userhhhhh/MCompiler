package AST.Stmt;

import AST.ASTVisitor;
import AST.Expr.Expression;

public class IfStmt extends StmtNode {
    public StmtNode thenStmt, elseStmt;
    public Expression condition;

    public IfStmt(Expression condition, StmtNode thenStmt, StmtNode elseStmt) {
        this.condition = condition;
        this.thenStmt = thenStmt;
        this.elseStmt = elseStmt;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
