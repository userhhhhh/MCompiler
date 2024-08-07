package AST.Stmt;

import AST.ASTVisitor;
import AST.Expr.Expression;
import Util.position;

public class ReturnStmt extends StmtNode {
    public Expression expr;

    public ReturnStmt(Expression expr, position pos) {
        super(pos);
        this.expr = expr;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
