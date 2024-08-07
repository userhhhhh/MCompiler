package AST.Expr;

import AST.ASTVisitor;
import Util.position;

public class NewVarExpr extends Expression {
    public String varName;

    public NewVarExpr(String varName, position pos) {
        super(pos);
        this.varName = varName;
    }

    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }

}
