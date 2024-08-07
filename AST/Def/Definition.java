package AST.Def;

import AST.ASTVisitor;
import AST.ASTNode;
import Util.position;

public class Definition extends ASTNode {
    public ClassTypeDef classDef;
    public FunctionDef funcDef;
    public VariableDef variDef;

    public Definition(position pos) {
        super(pos);
        classDef = null;
        funcDef = null;
        variDef = null;
    }

    @Override
    public void accept(ASTVisitor visitor){
        visitor.visit(this);
    }
}
