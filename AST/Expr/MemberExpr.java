package AST.Expr;

import AST.ASTVisitor;
import Util.position;

public class MemberExpr extends Expression {
    public Expression base;
    public String memberName;

    public MemberExpr(Expression base, String memberName, position pos) {
        super(pos);
        this.base = base;
        this.memberName = memberName;
    }

    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }
}
