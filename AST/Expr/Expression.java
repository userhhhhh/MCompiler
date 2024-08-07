package AST.Expr;

import AST.ASTNode;
import Util.Type;
import AST.ASTVisitor;
import Util.position;


public class Expression extends ASTNode {
    public Type type;

    public Expression(position pos) {
        super(pos);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
