package AST.Expr;

import Util.Type;
import AST.ASTVisitor;

public class NewArrayExpr extends Expression {
    public Type baseType;
    public Expression size;

    public NewArrayExpr(Type baseType, Expression size) {
        this.baseType = baseType;
        this.size = size;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
