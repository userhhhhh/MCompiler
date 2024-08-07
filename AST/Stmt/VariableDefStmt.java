package AST.Stmt;

import AST.*;
import AST.Def.VariableDef;
import Util.position;

public class VariableDefStmt extends StmtNode {
    public VariableDef variableDef;

    public VariableDefStmt(VariableDef variableDef, position pos) {
        super(pos);
        this.variableDef = variableDef;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
