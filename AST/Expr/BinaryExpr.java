package AST.Expr;

import AST.ASTVisitor;
import Util.position;

public class BinaryExpr extends Expression {
    public String op;
    public Expression lhs, rhs;

    public BinaryExpr(Expression lhs, String op, Expression rhs, position pos) {
        super(pos);
        this.lhs = lhs;
        this.op = op;
        this.rhs = rhs;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
