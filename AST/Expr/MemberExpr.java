package AST.Expr;

import AST.ASTVisitor;

public class MemberExpr extends Expression {
    public Expression base;
    public String memberName;

    public MemberExpr(Expression base, String memberName) {
        this.base = base;
        this.memberName = memberName;
    }

    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }
}
