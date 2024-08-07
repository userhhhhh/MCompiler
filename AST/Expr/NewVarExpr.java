package AST.Expr;

import AST.ASTVisitor;
import Util.position;

public class NewVarExpr extends Expression {
    public String varName;

    public NewVarExpr(position pos) {
        super(pos);
    }

    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }

}
