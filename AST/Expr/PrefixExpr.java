package AST.Expr;

import AST.ASTVisitor;
import Util.position;

public class PrefixExpr extends Expression {
    public String op;
    public Expression expr;

    public PrefixExpr(String op, Expression expr, position pos) {
        super(pos);
        this.op = op;
        this.expr = expr;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
