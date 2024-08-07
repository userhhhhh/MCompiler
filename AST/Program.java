package AST;

import AST.ASTNode;

public class Program extends ASTNode {
    public List<Definition> defList;

    Program() {
        defList = new ArrayList<>();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
