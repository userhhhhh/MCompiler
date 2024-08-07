package AST.Def;

import AST.ASTVisitor;
import AST.Expr.Expression;
import Util.Type;
import AST.ASTNode;
import Util.position;

public class InitVariable extends ASTNode{
    public String name;
    public Expression init;

    public InitVariable(position pos) {
        super(pos);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
