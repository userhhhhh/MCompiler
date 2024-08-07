package AST.Expr;

import Util.Type;
import AST.ASTVisitor;
import Util.position;

public class ArrayExpr extends Expression {
    public Type baseType;
    public Expression size;

    public ArrayExpr(Type baseType, Expression size, position pos) {
        super(pos);
        this.baseType = baseType;
        this.size = size;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
