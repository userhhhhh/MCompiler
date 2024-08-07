package AST.Expr;

import AST.ASTVisitor;

public class ConditionalExpr extends Expression {
    public Expression condition;
    public Expression trueBranch;
    public Expression falseBranch;

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
