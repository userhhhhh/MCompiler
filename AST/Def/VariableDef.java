package AST.Def;

import java.util.ArrayList;
import Util.Type;
import AST.ASTVisitor;
import Util.position;

public class VariableDef extends Definition {
    public Type type;
    public ArrayList<InitVariable> initVariablelist;

    public VariableDef(position pos) {
        super(pos);
        initVariablelist = new ArrayList<>();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
