package AST.Def;

public class InitVariable {
    public String type;
    public String name;
    public Expr init;

    public InitVariable(String type, String name, Expr init) {
        this.type = type;
        this.name = name;
        this.init = init;
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
