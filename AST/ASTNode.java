package AST;

import Util.position;

public class ASTNode {
    public position pos;

    abstract public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
