package AST.Expr;

import Util.Type;
import AST.ASTVisitor;
import Util.position;
import java.util.ArrayList;
import AST.Literal;

public class NewArrayExpr extends Expression {
    public Type baseType;
    public ArrayList<Expression> size;
    public int dim;
    public ArrayList<Literal> literal;

    public NewArrayExpr(position pos) {
        super(pos);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
