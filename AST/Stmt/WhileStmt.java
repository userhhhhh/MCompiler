package AST.Stmt;

import AST.ASTVisitor;
import AST.Expr.Expression;

public class WhileStmt extends StmtNode {
    public StmtNode body;
    public Expression condition;

    public WhileStmt(Expression condition, StmtNode body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
