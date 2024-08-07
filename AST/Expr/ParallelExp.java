package AST.Expr;

import AST.ASTNode;
import java.util.ArrayList;
import AST.ASTVisitor;
import Util.position;

public class ParallelExp extends ASTNode {
    public ArrayList<Expression> expList;

    public ParallelExp(position pos) {
        super(pos);
        expList = new ArrayList<>();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
