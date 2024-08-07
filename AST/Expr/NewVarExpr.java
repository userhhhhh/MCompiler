package AST.Expr;

import AST.ASTVisitor;

public class NewVarExpr extends Expression {
    public String varName;

    public NewVarExpr(String varName) {
        this.varName = varName;
    }

    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }

}
