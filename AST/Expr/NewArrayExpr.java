package AST.Expr;

import Util.Type;
import AST.ASTVisitor;
import Util.position;

public class NewArrayExpr extends Expression {
    public Type baseType;
    public Expression size;
    public
    // TODO
    // 数组

    public NewArrayExpr(position pos) {
        super(pos);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
