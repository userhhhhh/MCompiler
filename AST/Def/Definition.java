package AST.Def;

import AST.ASTVisitor;
import AST.ASTNode;

public class Definition extends ASTNode {
    public ClassTypeDef classDef;
    public FunctionDef funcDef;
    public VariableDef variDef;

    @Override
    public void accept(ASTVisitor visitor){
        visitor.visit(this);
    }
}
