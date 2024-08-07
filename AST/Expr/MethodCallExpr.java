package AST.Expr;

import java.util.ArrayList;
import AST.*;

public class MethodCallExpr extends Expression {
    public Expression base;
    public String methodName;
    public ArrayList<Expression> parameters;

    public MethodCallExpr(Expression base, String methodName, ArrayList<Expression> parameters) {
        this.base = base;
        this.methodName = methodName;
        this.parameters = parameters;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
