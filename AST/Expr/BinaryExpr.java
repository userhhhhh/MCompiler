package AST.Expr;

import AST.ASTVisitor;

public class BinaryExpr extends Expression {
    public String op;
    public Expression lhs, rhs;

    public BinaryExpr(Expression lhs, String op, Expression rhs) {
        this.lhs = lhs;
        this.op = op;
        this.rhs = rhs;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
