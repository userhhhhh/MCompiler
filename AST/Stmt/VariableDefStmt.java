package AST.Stmt;

import AST.*;
import AST.Def.VariableDef;

public class VariableDefStmt extends StmtNode {
    public VariableDef variableDef;

    public VariableDefStmt(VariableDef variableDef) {
        this.variableDef = variableDef;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
