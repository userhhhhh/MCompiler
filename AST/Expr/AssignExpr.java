package AST.Expr;

import AST.ASTVisitor;
import Util.position;

public class AssignExpr extends Expression {
    public Expression lhs, rhs;

    public AssignExpr(Expression lhs, Expression rhs, position pos) {
        super(pos);
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
