package AST.Def;

import java.util.ArrayList;
import Util.Type;
import AST.ASTVisitor;

public class VariableDef extends Definition {
    public Type type;
    public ArrayList<InitVariable> initVariablelist;

    public VariableDef() {
        initVariablelist = new ArrayList<>();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
