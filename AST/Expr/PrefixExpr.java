package AST.Expr;

import AST.ASTVisitor;

public class PrefixExpr extends Expression {
    public String op;
    public Expression expr;

    public PrefixExpr(String op, Expression expr) {
        this.op = op;
        this.expr = expr;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
