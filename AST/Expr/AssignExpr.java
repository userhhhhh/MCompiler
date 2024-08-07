package AST.Expr;

import AST.ASTVisitor;

public class AssignExpr extends Expression {
    public Expression lhs, rhs;

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
