package AST.Expr;

import AST.ASTVisitor;

public class UnaryExpr extends Expression {
    public Expression expr;
    public String op;

    public UnaryExpr(Expression expr, String op) {
        this.expr = expr;
        this.op = op;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
