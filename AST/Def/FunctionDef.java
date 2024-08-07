package AST.Def;

import AST.ASTVisitor;
import java.util.ArrayList;
import AST.Stmt.Suite;
import Util.position;

public class FunctionDef extends Definition {
    public String name;
    public String returnType;
    public ArrayList<String> parameterNames = new ArrayList<>();
    public ArrayList<String> parameters;
    public Suite body;

    public FunctionDef(String name, String returnType, ArrayList<String> parameters, Suite body, position pos) {
        super(pos);
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
        this.body = body;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
