package AST.Def;

import java.util.ArrayList;

public class VariableDef {
    public String type;
    public ArrayList<InitVariable> initVariablelist;

    public VariableDef() {
        initVariablelist = new ArrayList<>();
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
