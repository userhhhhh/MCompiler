package AST;

import AST.Def.Definition;
import java.util.ArrayList;
import Util.position;

public class Program extends ASTNode {
    public ArrayList<Definition> defList;

    public Program(position pos) {
        super(pos);
        defList = new ArrayList<>();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
