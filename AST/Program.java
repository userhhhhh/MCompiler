package AST;

import AST.Def.Definition;
import java.util.ArrayList;

public class Program extends ASTNode {
    public ArrayList<Definition> defList;

    Program() {
        defList = new ArrayList<>();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
