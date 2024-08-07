package AST.Expr;

import java.util.ArrayList;
import AST.*;

public class FuncCallExpr extends Expression {
    public String funcName;
    public ArrayList<Expression> args;
    public FuncCallExpr(String funcName, ArrayList<Expression> args) {
        this.funcName = funcName;
        this.args = args;
    }

    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }
}
