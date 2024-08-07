package AST.Expr;

import AST.ASTVisitor;

public class ParenExpr extends Expression {
    public Expression expr;

    public ParenExpr(Expression expr) {
        this.expr = expr;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
