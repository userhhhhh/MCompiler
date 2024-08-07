package AST.Def;

import AST.ASTNode;
import AST.ASTVisitor;
import java.util.ArrayList;


public class ClassTypeDef extends ASTNode {
    public String name;
    public ArrayList<VariableDef> varList;
    public ArrayList<FunctionDef> funcList;

    public ClassTypeDef(String name) {
        this.name = name;
        varList = new ArrayList<>();
        funcList = new ArrayList<>();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
