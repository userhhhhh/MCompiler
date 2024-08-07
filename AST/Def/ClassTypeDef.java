package AST.Def;

import AST.ASTVisitor;
import java.util.ArrayList;
import Util.position;


public class ClassTypeDef extends Definition {
    public String name;
    public ArrayList<VariableDef> varList;
    public ArrayList<FunctionDef> funcList;

    public ClassTypeDef(String name, position pos) {
        super(pos);
        this.name = name;
        varList = new ArrayList<>();
        funcList = new ArrayList<>();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
