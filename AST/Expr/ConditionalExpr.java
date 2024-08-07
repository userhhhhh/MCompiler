package AST.Expr;

import AST.ASTVisitor;
import Util.position;

public class ConditionalExpr extends Expression {
    public Expression condition;
    public Expression trueBranch;
    public Expression falseBranch;

    public ConditionalExpr(Expression condition, Expression trueBranch, Expression falseBranch, position pos) {
        super(pos);
        this.condition = condition;
        this.trueBranch = trueBranch;
        this.falseBranch = falseBranch;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
