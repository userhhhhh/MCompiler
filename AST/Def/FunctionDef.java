package AST.Def;

import AST.ASTVisitor;
import java.util.ArrayList;

public class FunctionDef {
    public String name;
    public String returnType;
    public ArrayList<String> parameterNames = new ArrayList<>();
    public ArrayList<String> parameters;
    public Suite body;

    public FunctionDef(String name, String returnType, ArrayList<String> parameters, Suite body) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
        this.body = body;
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
