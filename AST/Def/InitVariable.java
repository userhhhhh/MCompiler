package AST.Def;

import AST.ASTVisitor;
import AST.Expr.Expression;
import Util.Type;
import AST.ASTNode;

public class InitVariable extends ASTNode{
    public Type type;
    public String name;
    public Expression init;

    public InitVariable(Type type, String name, Expression init) {
        this.type = type;
        this.name = name;
        this.init = init;
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
