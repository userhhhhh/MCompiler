package AST.Stmt;

import AST.ASTVisitor;
import AST.Expr.Expression;

public class ReturnStmt extends StmtNode {
    public Expression expr;

    public ReturnStmt(Expression expr) {
        this.expr = expr;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
