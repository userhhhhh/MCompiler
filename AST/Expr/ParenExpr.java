package AST.Expr;

import AST.ASTVisitor;
import Util.position;

public class ParenExpr extends Expression {
    public Expression expr;

    public ParenExpr(Expression expr, position pos) {
        super(pos);
        this.expr = expr;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
