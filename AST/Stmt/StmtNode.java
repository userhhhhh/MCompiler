package AST.Stmt;

import AST.ASTNode;
import AST.ASTVisitor;

public abstract class StmtNode extends ASTNode {
    @Override
    public abstract void accept(ASTVisitor visitor);
}
