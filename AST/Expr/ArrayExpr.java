package AST.Expr;

import Util.Type;
import AST.ASTVisitor;

public class ArrayExpr extends Expression {
    public Type baseType;
    public Expression size;

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
