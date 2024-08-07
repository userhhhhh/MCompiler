package AST.Stmt;

import AST.ASTVisitor;
import java.util.ArrayList;
import Util.position;

public class Suite extends StmtNode {
    public ArrayList<StmtNode> stmt;

    public Suite(ArrayList<StmtNode> stmt, position pos) {
        super(pos);
        this.stmt = stmt;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
