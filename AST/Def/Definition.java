package AST.Def;

import AST.ASTVisitor;

public class Definition {
    public ClassTypeDef classDef;
    public FunctionDef funcDef;
    public VariableDef variDef;

    abstract void accept(ASTVisitor visitor);
}
