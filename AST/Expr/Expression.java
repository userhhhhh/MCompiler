package AST.Expr;

import AST.ASTNode;
import Util.Type;
import AST.ASTVisitor;


public class Expression extends ASTNode {
    public Type type;

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
