package AST.Expr;

import Util.Type;
import AST.ASTVisitor;
import Util.position;

public class NewArrayExpr extends Expression {
    public Type baseType;
    public Expression size;
    // TODO
    // 数组

    public NewArrayExpr(Type baseType, Expression size, position pos) {
        super(pos);
        this.baseType = baseType;
        this.size = size;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
