package AST.Expr;

import AST.ASTVisitor;
import Util.position;

public class PostfixExpr extends Expression {
    public Expression expr;
    public String op;

    public PostfixExpr(Expression expr, String op, position pos) {
        super(pos);
        this.expr = expr;
        this.op = op;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
