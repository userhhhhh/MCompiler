package IR;

import AST.ASTVisitor;
import AST.Def.*;
import AST.Expr.*;
import AST.Program;
import AST.Stmt.*;
import IR.Util.Counter;
import IR.Util.LoopScope;
import IR.Util.ReNamer;
import IR.definition.IRClassDef;
import IR.definition.IRFuncDef;
import IR.definition.IRGlobalVariDef;
import IR.entity.*;
import IR.instruction.*;
import Util.Scope;
import Util.error.semanticError;
import Util.infor.ClassInfor;
import Util.infor.FuncInfor;
import Util.type.IRType;
import Util.type.Type;

import java.util.ArrayList;
import java.util.Objects;

public class IRBuilder implements ASTVisitor {

    public IRProgram irProgram = new IRProgram();
    public Scope gScope;
    public LoopScope currentScope;
    public ReNamer currentReNamer;
    public IRBlock currentBlock;
    public String currentClassName = null;
    public IREntity currentEntity = null;
    public IRVariable currentPtr = null;
    public IRFuncDef currentFunc = null;
    public Counter counter; // 记录各种变量的数量以免重名

    // 凡是dim > 0的类型，都是ptr类型
    public IRType Type_To_IRType(Type type){
        IRType irType = new IRType();
        if(type.dim > 0 || type.isString || type.isNull || type.isClass){
            irType.setPtr();
        }else if(type.isVoid){
            irType.setVoid();
        }else if(type.isInt){
            irType.setI32();
        }else if(type.isBool){
            irType.setI1();
        }
        return irType;
    }

    public IRBuilder(IRProgram irProgram, Scope gScope) {
        this.irProgram = irProgram;
        this.gScope = gScope;
        currentScope = new LoopScope(null);
        currentReNamer = new ReNamer(null);
        counter = new Counter();
    }

    @Override public void visit(Program it) {
        for(var def : it.defList) {
            def.accept(this);
        }
    }

    @Override public void visit(Definition it) {
        if(it == null) return;
        if(it.funcDef != null) {
            it.funcDef.accept(this);
        } else if(it.classDef != null) {
            it.classDef.accept(this);
        } else if(it.variDef != null) {
            it.variDef.accept(this);
        }
    }

    @Override public void visit(ClassTypeDef it) {
        currentClassName = it.name;
        currentReNamer = new ReNamer(currentReNamer);

        IRClassDef irClassDef = new IRClassDef(it.name);
        irProgram.classDefMap.put(it.name, irClassDef);
        irClassDef.classSize = it.varList.size() * 4;
        it.varList.forEach(vd -> {
            vd.accept(this);
            irClassDef.variableDefList.add(vd);
        });
        it.funcList.forEach(fd -> fd.accept(this));
        if(it.constructor != null) {
            it.constructor.accept(this);
        }

        currentReNamer = currentReNamer.getParent();
        currentClassName = null;
    }

    @Override public void visit(Constructor it){
        currentReNamer = new ReNamer(currentReNamer);
        IRFuncDef irFuncDef = new IRFuncDef(Type_To_IRType(new Type("void", 0)));
        irFuncDef.functionName = "@" + currentClassName + ".." + currentClassName;
        currentBlock = irFuncDef.addBlock(new IRBlock(irFuncDef, "entry"));

        it.suite.stmt.forEach(stmt -> stmt.accept(this));

        irProgram.funcDefMap.put(irFuncDef.functionName, irFuncDef);
        currentReNamer = currentReNamer.getParent();
        currentBlock = null;
    }

    @Override public void visit(FunctionDef it) {
        FuncInfor func = new FuncInfor(it);
        IRFuncDef irFuncDef = new IRFuncDef(Type_To_IRType(it.returnType));
        if(currentClassName != null) {
            irFuncDef.functionName = currentClassName + ".." + it.name;
        } else {
            irFuncDef.functionName = it.name;
        }
        currentBlock = irFuncDef.addBlock(new IRBlock(irFuncDef, "entry"));
        currentReNamer = new ReNamer(currentReNamer);
        it.body.stmt.forEach(stmt -> stmt.accept(this));
        boolean hasReturn = false;
        for(int i = 0; i < it.body.stmt.size(); i++) {
            if(it.body.stmt.get(i) instanceof ReturnStmt) {
                hasReturn = true;
                break;
            }
        }
        if(!hasReturn){
            if(it.returnType.isVoid){
                currentBlock.instructions.add(new RetInstr(currentBlock, null, new IRType("void")));
            } else {
                if(it.name.equals("main")) {
                    currentBlock.instructions.add(new RetInstr(currentBlock, new IRIntLiteral(0), new IRType("i32")));
                }
            }
        }
        irProgram.funcDefMap.put(irFuncDef.functionName, irFuncDef);
        currentReNamer = currentReNamer.getParent();
        currentBlock = null;
    }

    @Override public void visit(VariableDef it) {
        it.initVariablelist.forEach(iv -> iv.accept(this));
    }

    @Override public void visit(InitVariable it) {
        if(it.init != null) {
            it.init.accept(this);
        }
        if(it.type.isString) {
            int num = counter.strCounter++;
            String actualName = "@.str.." + num;
            IRGlobalVariDef irGlobalVariDef = new IRGlobalVariDef(actualName);
            irGlobalVariDef.irType = new IRType("ptr");
            if(currentClassName == null && currentBlock == null) {
                irProgram.globalVarDefMap.put(it.name, irGlobalVariDef);
                if(it.init == null) {
                    irGlobalVariDef.result = new IROtherLiteral("null", new IRType("ptr"));
                    return;
                }
                if(it.init instanceof FmtString fmtString) {
                    it.init.accept(this);
                } else {
                    irGlobalVariDef.result = new IROtherLiteral(it.init.toString(), new IRType("ptr"));
                }
            } else {
                irGlobalVariDef.result = new IROtherLiteral(it.init.toString(), new IRType("ptr"));
                int count = counter.varCounter++;
                IRVariable irVariable = new IRVariable("%var" + String.valueOf(count), new IRType("ptr"));
                currentBlock.instructions.add(new IR.instruction.AllocInstr(currentBlock, new IRType("ptr"), irVariable));
                currentBlock.instructions.add(new IR.instruction.StoreInstr(currentBlock, irVariable, new IRType("ptr"), new IRVariable(actualName)));
            }
        }
        // 全局变量
        if(currentClassName == null && currentBlock == null) {
            IRGlobalVariDef irGlobalVariDef = new IRGlobalVariDef("@" + it.name);
            irGlobalVariDef.irType = Type_To_IRType(it.type);
            if(it.init == null){
                if(it.type.isInt) irGlobalVariDef.result = new IRIntLiteral(0);
                if(it.type.isBool) irGlobalVariDef.result = new IRBoolLiteral(false);
                irProgram.globalVarDefMap.put(it.name, irGlobalVariDef);
                return;
            }
            if(it.init instanceof PrimaryExpr primaryExpr) {
                irProgram.globalVarDefMap.put(it.name, irGlobalVariDef);
            } else {
                if(irProgram.funcDefMap.containsKey(irGlobalVariDef.funcDef.functionName)) {
                    irGlobalVariDef.funcDef = irProgram.funcDefMap.get(irGlobalVariDef.funcDef.functionName);
                } else {
                    irGlobalVariDef.funcDef = new IRFuncDef(Type_To_IRType(it.type), "@._init", new ArrayList<>(), new ArrayList<>());
                }
                irGlobalVariDef.funcDef.returnType = new IRType("void");
                currentBlock = irGlobalVariDef.funcDef.addBlock(new IRBlock(irGlobalVariDef.funcDef, "entry"));
                it.init.accept(this);
                IRVariable addr = new IRVariable(it.name + ".addr", irGlobalVariDef.irType);
                currentBlock.instructions.add(new IR.instruction.StoreInstr(currentBlock, addr, irGlobalVariDef.irType, currentEntity));
                irProgram.funcDefMap.put(irGlobalVariDef.funcDef.functionName, irGlobalVariDef.funcDef);
                irProgram.globalVarDefMap.put(it.name, irGlobalVariDef);
                currentBlock = null;
            }
        } else {   // 局部变量
            int num = currentReNamer.getVarNum(it.name, false);
            if(num == -1) {
                currentReNamer.addVar(it.name);
                num = 1;
            } else {
                num++;
            }
            IRVariable irVariable = new IRVariable("%" + it.name + "." + num, Type_To_IRType(it.type));
            currentBlock.instructions.add(new IR.instruction.AllocInstr(currentBlock, irVariable.type, irVariable));
            if(it.init != null) {
                currentBlock.instructions.add(new IR.instruction.StoreInstr(currentBlock, irVariable, irVariable.type, currentEntity));
            }
            currentEntity = irVariable;
        }
    }

    @Override public void visit(ArrayExpr it) {
        it.baseType.accept(this);
        IRVariable result = new IRVariable("%var" + String.valueOf(counter.varCounter++), new IRType("ptr"));
        String actualName = it.baseType.type.typeName;
        LoadInstr loadInstr = new LoadInstr(currentBlock, new IRVariable(actualName), result, Type_To_IRType(it.baseType.type));
        currentBlock.instructions.add(loadInstr);
        it.size.accept(this);
        int size = 0;
        if(currentEntity instanceof IRIntLiteral literal) {
            size = literal.value;
        } else {
            throw new semanticError("ArrayExpr size is not a int", it.pos);
        }

        IRVariable retValue = new IRVariable("%var" + String.valueOf(counter.varCounter++), new IRType("ptr"));
        GeteleptrInstr geteleptrInstr = new GeteleptrInstr(currentBlock, retValue, actualName, result);
        geteleptrInstr.idxList.add(new IRIntLiteral(size));
        currentBlock.instructions.add(geteleptrInstr);
        currentEntity = retValue;

        IRVariable result1 = new IRVariable("%var" + String.valueOf(counter.varCounter++), new IRType("ptr"));
        LoadInstr loadInstr1 = new LoadInstr(currentBlock, retValue, result1, Type_To_IRType(it.type));
        currentBlock.instructions.add(loadInstr1);

        currentPtr = loadInstr1.result;
    }

    @Override public void visit(AssignExpr it) {
        it.lhs.accept(this);
        var lhsValue = currentPtr;
        it.rhs.accept(this);
        var rhsValue = currentEntity;

        StoreInstr storeInstr = new StoreInstr(currentBlock, lhsValue, Type_To_IRType(it.lhs.type), rhsValue);
        currentBlock.instructions.add(storeInstr);
        currentPtr = null;
    }

    @Override public void visit(BinaryExpr it) {
        it.lhs.accept(this);
        var lhsValue = currentEntity;

        if(it.op.equals("&&") || it.op.equals("||")){
            int num = counter.landCounter++;
            IRBlock thenBlock = new IRBlock(currentBlock.parent, "land.then.." + num);
            IRBlock elseBlock = new IRBlock(currentBlock.parent, "land.else.." + num);
            IRBlock endBlock = new IRBlock(currentBlock.parent, "land.end.." + num);
            currentBlock.instructions.add(new IR.instruction.BrInstr(currentBlock, lhsValue, thenBlock, elseBlock));

            currentBlock.parent.addBlock(thenBlock);
            currentBlock = thenBlock;
            if(it.op.equals("&&")) it.rhs.accept(this);
            currentBlock.instructions.add(new JumpInstr(currentBlock, endBlock));
            IRBlock block1 = currentBlock;

            currentBlock.parent.addBlock(elseBlock);
            currentBlock = elseBlock;
            if(it.op.equals("||")) it.rhs.accept(this);
            currentBlock.instructions.add(new JumpInstr(currentBlock, endBlock));
            IRBlock block2 = currentBlock;

            currentBlock.parent.addBlock(endBlock);

            IRVariable retValue = new IRVariable("%var" + String.valueOf(counter.varCounter++), new IRType("i1"));
            currentBlock = endBlock;
            PhiInstr phiInstr = new PhiInstr(currentBlock, retValue, new IRType("i1"));
            currentBlock.instructions.add(phiInstr);

            if(it.op.equals("&&")) {
                phiInstr.addBranch(currentEntity, block1);
                phiInstr.addBranch(lhsValue, block2);
            } else {
                phiInstr.addBranch(lhsValue, thenBlock);
                phiInstr.addBranch(currentEntity, endBlock);
            }
            currentEntity = retValue;
            return;
        }

        it.rhs.accept(this);
        var rhsValue = currentEntity;

        IRVariable retValue = new IRVariable("%var" + String.valueOf(counter.varCounter++));
        switch (it.op){
            case "<", ">", "<=", ">=", "==", "!=" -> {
                if(it.lhs.type.isString && it.rhs.type.isString) {
                    ArrayList<IRType> stringTypes = new ArrayList<>();
                    stringTypes.add(new IRType("ptr"));
                    stringTypes.add(new IRType("ptr"));
                    ArrayList<IREntity> stringArgs = new ArrayList<>();
                    stringArgs.add(lhsValue);
                    stringArgs.add(rhsValue);
                    switch (it.op) {
                        case "<" -> {
                            retValue.type = new IRType("i1");
                            CallInstr callInstr = new CallInstr(currentBlock, "string.less", retValue);
                            callInstr.argTypes.addAll(stringTypes);
                            callInstr.args.addAll(stringArgs);
                            currentBlock.instructions.add(callInstr);
                        }
                        case ">" -> {
                            retValue.type = new IRType("i1");
                            CallInstr callInstr = new CallInstr(currentBlock, "string.greater", retValue);
                            callInstr.argTypes.addAll(stringTypes);
                            callInstr.args.addAll(stringArgs);
                            currentBlock.instructions.add(callInstr);
                        }
                        case "<=" -> {
                            retValue.type = new IRType("i1");
                            CallInstr callInstr = new CallInstr(currentBlock, "string.lessOrEqual", retValue);
                            callInstr.argTypes.addAll(stringTypes);
                            callInstr.args.addAll(stringArgs);
                            currentBlock.instructions.add(callInstr);
                        }
                        case ">=" -> {
                            retValue.type = new IRType("i1");
                            CallInstr callInstr = new CallInstr(currentBlock, "string.greaterOrEqual", retValue);
                            callInstr.argTypes.addAll(stringTypes);
                            callInstr.args.addAll(stringArgs);
                            currentBlock.instructions.add(callInstr);
                        }
                        case "==" -> {
                            retValue.type = new IRType("i1");
                            CallInstr callInstr = new CallInstr(currentBlock, "string.equal", retValue);
                            callInstr.argTypes.addAll(stringTypes);
                            callInstr.args.addAll(stringArgs);
                            currentBlock.instructions.add(callInstr);
                        }
                        case "!=" -> {
                            retValue.type = new IRType("i1");
                            CallInstr callInstr = new CallInstr(currentBlock, "string.notEqual", retValue);
                            callInstr.argTypes.addAll(stringTypes);
                            callInstr.args.addAll(stringArgs);
                            currentBlock.instructions.add(callInstr);
                        }
                    }
                } else {
                    retValue.type = new IRType("i1");
                    currentBlock.instructions.add(new IR.instruction.IcmpInstr(currentBlock, retValue, lhsValue, it.op, rhsValue));
                }
            }
            case "+" -> {
                if(it.lhs.type.isString && it.rhs.type.isString) {
                    retValue.type = new IRType("ptr");
                    CallInstr callInstr = new CallInstr(currentBlock, "string.add", retValue);
                    callInstr.argTypes.add(new IRType("ptr"));
                    callInstr.argTypes.add(new IRType("ptr"));
                    callInstr.args.add(lhsValue);
                    callInstr.args.add(rhsValue);
                    currentBlock.instructions.add(callInstr);
                } else {
                    retValue.type = new IRType("i32");
                    currentBlock.instructions.add(new IR.instruction.BinaryInstr(currentBlock, retValue, it.op, lhsValue, rhsValue));
                }
            }
            case "*", "/", "%", "-", ">>", "<<", "&", "|", "^" -> {
                retValue.type = new IRType("i32");
                currentBlock.instructions.add(new IR.instruction.BinaryInstr(currentBlock, retValue, it.op, lhsValue, rhsValue));
            }
        }
        currentEntity = retValue;
    }

    @Override public void visit(ConditionalExpr it) {
        it.condition.accept(this);
        if(currentEntity instanceof IRBoolLiteral literal) {
            if(literal.value) {
                it.trueBranch.accept(this);
            } else {
                it.falseBranch.accept(this);
            }
            return;
        } else {
            IRBlock thenBlock = new IRBlock(currentBlock.parent, "then");
            IRBlock elseBlock = new IRBlock(currentBlock.parent, "else");
            IRBlock backBlock = new IRBlock(currentBlock.parent, "back");
            currentBlock.parent.addBlock(thenBlock);
            currentBlock = thenBlock;
            it.trueBranch.accept(this);
            thenBlock.instructions.add(new IR.instruction.JumpInstr(thenBlock, backBlock));
            currentBlock = backBlock;
            currentBlock.parent.addBlock(elseBlock);
            currentBlock = elseBlock;
            it.falseBranch.accept(this);
            elseBlock.instructions.add(new IR.instruction.JumpInstr(elseBlock, backBlock));
            currentBlock = backBlock;
            currentBlock.parent.addBlock(backBlock);
        }
    }

    @Override public void visit(Expression it) {
        it.accept(this);
    }

    @Override public void visit(FuncCallExpr it) {
        String actualFuncName = it.funcName;
        IRVariable result = new IRVariable("%var" + String.valueOf(counter.varCounter++), Type_To_IRType(it.type));
        CallInstr callInstr = new CallInstr(currentBlock, actualFuncName, result);
        IRFuncDef funcDef = irProgram.getFuncDef(actualFuncName);
        callInstr.argTypes.addAll(funcDef.parameterTypeList);
        for(int i = 0; i < it.callExpList.expList.size(); i++) {
            it.callExpList.expList.get(i).accept(this);
            callInstr.args.add(currentEntity);
        }
        currentBlock.instructions.add(callInstr);
        currentEntity = callInstr.result;
    }

    @Override public void visit(MemberExpr it) {
        it.base.accept(this);
        IRVariable ptr = currentPtr;
        ClassInfor struct = gScope.getClassInfo(it.base.type.typeName);
        IRVariable result = new IRVariable("%var" + String.valueOf(counter.varCounter++), Type_To_IRType(it.type));
        String className = "class.." + it.base.type.typeName;
        GeteleptrInstr geteleptrInstr = new GeteleptrInstr(currentBlock, result, className, ptr);
        geteleptrInstr.idxList.add(new IRIntLiteral(0));
        struct.varNames.forEach(varName -> {
            if(varName.equals(it.memberName)){
                geteleptrInstr.idxList.add(new IRIntLiteral(struct.varNames.indexOf(varName)));
            }
        });
        currentBlock.instructions.add(geteleptrInstr);
        currentPtr = geteleptrInstr.result;
    }

    @Override public void visit(MethodCallExpr it) {
        it.base.accept(this);
        IRVariable ptr = currentPtr;
        String className = ptr.type.className;
        String funcName = className + ".." + it.methodName;
        IRFuncDef funcDef = irProgram.getFuncDef(funcName);
        CallInstr callInstr = new CallInstr(currentBlock, funcName, ptr);
        callInstr.argTypes.addAll(funcDef.parameterTypeList);
        callInstr.args.add(ptr);
        if(it.callExpList != null){
            it.callExpList.expList.forEach(e -> {
                e.accept(this);
                callInstr.args.add(currentPtr);
            });
        }
        if(it.type.isVoid){
            currentBlock.instructions.add(callInstr);
        } else {
            IRVariable retValue = new IRVariable("%var" + String.valueOf(counter.varCounter++), Type_To_IRType(it.type));
            callInstr.result = retValue;
            currentBlock.instructions.add(callInstr);
            currentEntity = retValue;
        }
    }

    @Override public void visit(NewArrayExpr it) {
        IRVariable retValue = new IRVariable("%var" + String.valueOf(counter.varCounter++), new IRType("ptr"));
        CallInstr callInstr = new CallInstr(currentBlock, "__array.alloca_inside", retValue);
        ArrayList<IREntity> sizeList = new ArrayList<>();
        it.size.forEach(e -> {
            e.accept(this);
            callInstr.args.add(currentEntity);
            callInstr.argTypes.add(new IRType("i32"));
        });
        currentBlock.instructions.add(callInstr);
        currentEntity = retValue;
    }

    @Override public void visit(Arrayconst it) {
        IRVariable retValue = new IRVariable("%var" + String.valueOf(counter.varCounter++), new IRType("ptr"));
        CallInstr callInstr = new CallInstr(currentBlock, "malloc", retValue);
        callInstr.argTypes.add(new IRType("i32"));
        callInstr.args.add(new IRIntLiteral(it.literal.size() * 4));
        for(int i = 0; i < it.literal.size(); i++) {
            it.literal.get(i).accept(this);
            IREntity entity = currentEntity;
            IRVariable retValue1 = new IRVariable("%var" + String.valueOf(counter.varCounter++), new IRType("ptr"));
            GeteleptrInstr geteleptrInstr = new GeteleptrInstr(currentBlock, retValue1, String.valueOf(i), retValue);
            currentBlock.instructions.add(geteleptrInstr);
            currentBlock.instructions.add(new StoreInstr(currentBlock, retValue1, new IRType("ptr"), entity));
        }
        currentBlock.instructions.add(callInstr);
    }

    @Override public void visit(Literal it) {
        if(it.arrayconst == null) {
            IRVariable retValue = new IRVariable("%var" + String.valueOf(counter.varCounter++), Type_To_IRType(it.type));
            if(it.isInt){
                CallInstr callInstr = new CallInstr(currentBlock, "malloc", retValue);
                callInstr.argTypes.add(new IRType("i32"));
                callInstr.args.add(new IRIntLiteral(4));
            } else if(it.isTrue || it.isFalse){
                CallInstr callInstr = new CallInstr(currentBlock, "malloc", retValue);
                callInstr.argTypes.add(new IRType("i32"));
                callInstr.args.add(new IRIntLiteral(1));
            } else if(it.isString){
                int num = counter.strCounter++;
                String actualName = "@.str.." + num;
                IRGlobalVariDef irGlobalVariDef = new IRGlobalVariDef(actualName);
                irGlobalVariDef.irType = new IRType("ptr");
                irGlobalVariDef.result = new IROtherLiteral(actualName, new IRType("ptr"));
                irProgram.globalVarDefMap.put(actualName, irGlobalVariDef);
                CallInstr callInstr = new CallInstr(currentBlock, "malloc", retValue);
                callInstr.argTypes.add(new IRType("ptr"));
                callInstr.args.add(new IRIntLiteral(4));
            } else if(it.isNull){
                // TODO
                CallInstr callInstr = new CallInstr(currentBlock, "malloc", retValue);
                callInstr.argTypes.add(new IRType("i32"));
                callInstr.args.add(new IRIntLiteral(4));
            }
            currentEntity = retValue;
        }
        IRVariable irVariable = new IRVariable("%var" + String.valueOf(counter.varCounter++), Type_To_IRType(it.type));
        CallInstr callInstr = new CallInstr(currentBlock, "malloc", irVariable);
        callInstr.argTypes.add(new IRType("ptr"));
        callInstr.args.add(new IRIntLiteral(it.arrayconst.literal.size() * 4));
        for(int i = 0; i < it.arrayconst.literal.size(); i++) {
            it.arrayconst.literal.get(i).accept(this);
            IREntity entity = currentEntity;
            IRVariable retValue = new IRVariable("%var" + String.valueOf(counter.varCounter++), new IRType("ptr"));
            GeteleptrInstr geteleptrInstr = new GeteleptrInstr(currentBlock, retValue, String.valueOf(i), entity);
            currentBlock.instructions.add(geteleptrInstr);
            currentBlock.instructions.add(new StoreInstr(currentBlock, irVariable, new IRType("ptr"), retValue));
        }
        currentBlock.instructions.add(callInstr);
    }

    @Override public void visit(NewVarExpr it) {
        IRVariable retValue = new IRVariable("%var" + String.valueOf(counter.varCounter++), new IRType("ptr"));
        CallInstr callInstr = new CallInstr(currentBlock, "malloc", retValue);
        int size = 0;
        if(it.type.isClass){
            size = irProgram.getClassDef(it.type.typeName).classSize;
        } else if(it.type.isInt || it.type.isBool){
            size = 4;
        }
        callInstr.argTypes.add(new IRType("i32"));
        callInstr.args.add(new IRIntLiteral(size));
        currentBlock.instructions.add(callInstr);
        currentEntity = retValue;
    }

    @Override public void visit(ParallelExp it) {
        it.expList.forEach(e -> e.accept(this));
    }
    @Override public void visit(ParenExpr it) {
        it.expr.accept(this);
    }
    @Override public void visit(PostfixExpr it) {
        it.expr.accept(this);
        String option = "";
        if(it.op.equals("++")) option = "+";
        if(it.op.equals("--")) option = "-";
        IRVariable retValue1 = new IRVariable("%var" + String.valueOf(counter.varCounter++), new IRType("i32"));
        currentBlock.instructions.add(new IR.instruction.BinaryInstr(currentBlock, retValue1, option, currentEntity, new IRIntLiteral(1)));
        currentBlock.instructions.add(new IR.instruction.StoreInstr(currentBlock, currentPtr, new IRType("i32"), retValue1));
    }

    @Override public void visit(PrefixExpr it) {
        it.expr.accept(this);
        String option = "";
        if(it.op.equals("++")) option = "+";
        if(it.op.equals("--")) option = "-";
        IRVariable retValue1 = new IRVariable("%var" + String.valueOf(counter.varCounter++), new IRType("i32"));
        currentBlock.instructions.add(new IR.instruction.BinaryInstr(currentBlock, retValue1, option, currentEntity, new IRIntLiteral(1)));
        currentBlock.instructions.add(new IR.instruction.StoreInstr(currentBlock, currentPtr, new IRType("i32"), retValue1));
        currentEntity = retValue1;
    }

    @Override public void visit(PrimaryExpr it) {
        if(it.isIdentifier){
            // then表示这个值是一个类，else表示这个值是一个变量
            if(gScope.containsClass(it.identifier, true)){
                currentEntity = new IROtherLiteral(it.identifier, new IRType("ptr"));
            } else {
                int num = currentReNamer.getVarNum(it.identifier, false);
                String actualName = it.identifier;
                if(num > 0) { // 局部变量
                    actualName = "%" + it.identifier + "." + num;
                } else { // 全局变量 & 类变量
                    // TODO
                    if(currentClassName != null) {
                        currentEntity = new IRVariable("%this", new IRType("ptr"));
                    } else {
                        actualName = "@" + it.identifier;
                    }
                }
                IRVariable ptr = new IRVariable(actualName);
                IRVariable result = new IRVariable("%var" + String.valueOf(counter.varCounter++), Type_To_IRType(it.type));
                currentBlock.instructions.add(new IR.instruction.LoadInstr(currentBlock, ptr, result, Type_To_IRType(it.type)));
                currentEntity = result;
                currentPtr = ptr;
            }
        } else if(it.isTrue) {
            currentEntity = new IRBoolLiteral(true);
            currentEntity.type = new IRType("i1");
        } else if(it.isFalse) {
            currentEntity = new IRBoolLiteral(false);
            currentEntity.type = new IRType("i1");
        } else if(it.isIntLiteral) {
            currentEntity = new IRIntLiteral(Integer.parseInt(it.intLiteral));
            currentEntity.type = new IRType("i32");
        } else if(it.isStringLiteral){
            int num = counter.strCounter++;
            String actualName = "@.str.." + num;
            IRGlobalVariDef irGlobalVariDef = new IRGlobalVariDef(actualName);
            irGlobalVariDef.irType = new IRType("ptr");
            irGlobalVariDef.result = new IROtherLiteral(it.stringLiteral, new IRType("ptr"));
            irProgram.globalVarDefMap.put(it.stringLiteral, irGlobalVariDef);
            currentEntity = new IRVariable(actualName);
        } else if(it.isFmtString){
            it.fmtString.accept(this);
        } else if(it.isLiteral){
            it.literal.accept(this);
        }
    }

    @Override public void visit(FmtString it) {
        int num = counter.strCounter;
        String actualName = "@.str.." + num;
        IRGlobalVariDef irGlobalVariDef = new IRGlobalVariDef(actualName);
        irGlobalVariDef.irType = new IRType("ptr");
        if(it.stringList != null){
            irGlobalVariDef.result = new IROtherLiteral(it.stringList.getFirst(), new IRType("ptr"));
            irProgram.globalVarDefMap.put(it.stringList.getFirst(), irGlobalVariDef);
            currentEntity = new IRVariable(actualName);
        }
        for(int i = 0; i < it.exprList.size(); i++) {
            IREntity irEntity1 = currentEntity;
            it.exprList.get(i).accept(this);
            IREntity irEntity2 = currentEntity;
            IRVariable retValue = new IRVariable("%var" + String.valueOf(counter.varCounter++), new IRType("ptr"));
            CallInstr callInstr = new CallInstr(currentBlock, "string.add", retValue);
            callInstr.argTypes.add(new IRType("ptr"));
            callInstr.argTypes.add(new IRType("ptr"));
            callInstr.args.add(irEntity1);
            callInstr.args.add(irEntity2);
            currentBlock.instructions.add(callInstr);

            int count = counter.varCounter++;
            String actualName1 = "@.str.." + count;
            IRGlobalVariDef irGlobalVariDef1 = new IRGlobalVariDef(actualName1);
            irGlobalVariDef1.irType = new IRType("ptr");
            irGlobalVariDef1.result = new IROtherLiteral(it.stringList.get(i), new IRType("ptr"));
            irProgram.globalVarDefMap.put(it.stringList.get(i), irGlobalVariDef1);

            IRVariable retValue1 = new IRVariable("%var" + String.valueOf(counter.varCounter++), new IRType("ptr"));
            CallInstr callInstr1 = new CallInstr(currentBlock, "string.add", retValue1);
            callInstr1.argTypes.add(new IRType("ptr"));
            callInstr1.argTypes.add(new IRType("ptr"));
            callInstr1.args.add(retValue);
            callInstr1.args.add(new IRVariable(actualName1));
            currentBlock.instructions.add(callInstr1);
            currentEntity = retValue1;
            currentPtr = retValue1;
        }
    }

    @Override public void visit(UnaryExpr it) {
        it.expr.accept(this);
        IRVariable retValue = new IRVariable("%var" + String.valueOf(counter.varCounter++), new IRType("i32"));
        switch (it.op){
            case "+", "-" -> {
                currentBlock.instructions.add(new IR.instruction.BinaryInstr(currentBlock, retValue, it.op, new IRIntLiteral(0), currentEntity));
            }
            case "~" -> {
                currentBlock.instructions.add(new IR.instruction.BinaryInstr(currentBlock, retValue, "^", currentEntity, new IRIntLiteral(-1)));
            }
            case "!" -> {
                currentBlock.instructions.add(new IR.instruction.BinaryInstr(currentBlock, retValue, "^", currentEntity, new IRBoolLiteral(true)));
            }
        }
        currentEntity = retValue;
    }

    @Override public void visit(BreakStmt it) {
        currentBlock.instructions.add(new IR.instruction.JumpInstr(currentBlock, currentScope.loopEnd));
    }
    @Override public void visit(ContinueStmt it) {
        currentBlock.instructions.add(new IR.instruction.JumpInstr(currentBlock, currentScope.loopNext));
    }
    @Override public void visit(ExprStmt it) {
        it.expr.accept(this);
    }
    @Override public void visit(ForStmt it) {
        currentScope = new LoopScope(currentScope);
        currentReNamer = new ReNamer(currentReNamer);
        if(it.parallelExp != null) it.parallelExp.accept(this);
        if(it.variableDef != null) it.variableDef.accept(this);

        int currentNum = counter.forCounter++;
        IRBlock backBlock = new IRBlock(currentBlock.parent, "for.back." + currentNum);
        IRBlock condBlock = new IRBlock(currentBlock.parent, "for.cond." + currentNum);
        IRBlock bodyBlock = new IRBlock(currentBlock.parent, "for.body." + currentNum);
        IRBlock stepBlock = new IRBlock(currentBlock.parent, "for.step." + currentNum);
        currentScope.loopNext = bodyBlock;
        currentScope.loopEnd = backBlock;
        currentBlock.instructions.add(new IR.instruction.JumpInstr(currentBlock, condBlock));

        if(it.conditionExp != null){
            currentBlock.parent.addBlock(condBlock);
            currentBlock = condBlock;
            it.conditionExp.accept(this);
            condBlock.instructions.add(new IR.instruction.BrInstr(condBlock, (IRVariable)currentEntity, bodyBlock, stepBlock));
        } else {
            currentBlock.parent.addBlock(condBlock);
            currentBlock.instructions.add(new IR.instruction.JumpInstr(currentBlock, condBlock));
            currentBlock = condBlock;
            condBlock.instructions.add(new IR.instruction.JumpInstr(condBlock, bodyBlock));
        }

        if(it.stmt != null){
            currentBlock.parent.addBlock(bodyBlock);
            currentBlock = bodyBlock;
            it.stmt.accept(this);
            currentBlock.instructions.add(new IR.instruction.JumpInstr(currentBlock, stepBlock));
        } else {
            currentBlock.parent.addBlock(bodyBlock);
            currentBlock = bodyBlock;
            currentBlock.instructions.add(new IR.instruction.JumpInstr(currentBlock, stepBlock));
        }

        if(it.stepExp != null){
            currentBlock.parent.addBlock(stepBlock);
            currentBlock = stepBlock;
            it.stepExp.accept(this);
            currentBlock.instructions.add(new IR.instruction.JumpInstr(currentBlock, condBlock));
        } else {
            currentBlock.parent.addBlock(stepBlock);
            currentBlock = stepBlock;
            currentBlock.instructions.add(new IR.instruction.JumpInstr(currentBlock, condBlock));
        }

        currentBlock.parent.addBlock(backBlock);
        currentBlock = backBlock;
        currentScope = currentScope.parentScope();
        currentReNamer = currentReNamer.getParent();
    }
    @Override public void visit(IfStmt it) {
        it.condition.accept(this);

        IRBlock thenBlock = new IRBlock(currentBlock.parent, "if.then");
        IRBlock elseBlock = new IRBlock(currentBlock.parent, "if.else");
        IRBlock backBlock = new IRBlock(currentBlock.parent, "if.back");

        // 两种情况，一种是直接的bool值，一种是变量
        if(currentEntity instanceof IRBoolLiteral literal) {
            if(literal.value) {
                currentBlock.instructions.add(new IR.instruction.JumpInstr(currentBlock, thenBlock));
            } else {
                currentBlock.instructions.add(new IR.instruction.JumpInstr(currentBlock, elseBlock));
            }
        } else {
            currentBlock.instructions.add(new IR.instruction.BrInstr(currentBlock, (IRVariable)currentEntity, thenBlock, elseBlock));
        }

        if(it.thenStmt != null) {
            currentBlock.parent.addBlock(thenBlock);
            currentBlock = thenBlock;
            currentReNamer = new ReNamer(currentReNamer);
            it.thenStmt.accept(this);
            thenBlock.instructions.add(new IR.instruction.JumpInstr(thenBlock, backBlock));
            currentReNamer = currentReNamer.getParent();
            currentBlock = backBlock;
        } else {
            currentBlock.parent.addBlock(thenBlock);
            currentBlock = thenBlock;
            thenBlock.instructions.add(new IR.instruction.JumpInstr(thenBlock, backBlock));
            currentBlock = backBlock;
        }

        if(it.elseStmt != null) {
            currentBlock.parent.addBlock(elseBlock);
            currentBlock = elseBlock;
            currentReNamer = new ReNamer(currentReNamer);
            it.elseStmt.accept(this);
            elseBlock.instructions.add(new IR.instruction.JumpInstr(elseBlock, backBlock));
            currentReNamer = currentReNamer.getParent();
            currentBlock = backBlock;
        } else{
            currentBlock.parent.addBlock(elseBlock);
            currentBlock = elseBlock;
            elseBlock.instructions.add(new IR.instruction.JumpInstr(elseBlock, backBlock));
            currentBlock = backBlock;
        }
        currentBlock.parent.addBlock(backBlock);
    }
    @Override public void visit(ReturnStmt it) {
        if(it.expr != null) {
            it.expr.accept(this);
            currentBlock.instructions.add(new IR.instruction.RetInstr(currentBlock, currentEntity, Type_To_IRType(it.expr.type)));
        } else {
            currentBlock.instructions.add(new IR.instruction.RetInstr(currentBlock, null, new IRType("void")));
        }
    }
    @Override public void visit(Suite it) {
        currentReNamer = new ReNamer(currentReNamer);
        it.stmt.forEach(stmt -> stmt.accept(this));
        currentReNamer = currentReNamer.getParent();
    }
    @Override public void visit(VariableDefStmt it) {
        it.variableDef.accept(this);
    }
    @Override public void visit(WhileStmt it) {
        it.condition.accept(this);
        currentScope = new LoopScope(currentScope);

        IRBlock backBlock = new IRBlock(currentBlock.parent, "for.back");
        IRBlock condBlock = new IRBlock(currentBlock.parent, "for.cond");
        IRBlock bodyBlock = new IRBlock(currentBlock.parent, "for.body");
        currentScope.loopNext = bodyBlock;
        currentScope.loopEnd = backBlock;

        if(it.condition != null){
            currentBlock.parent.addBlock(condBlock);
            currentBlock.instructions.add(new IR.instruction.JumpInstr(currentBlock, condBlock));
            currentBlock = condBlock;
            it.condition.accept(this);
            condBlock.instructions.add(new IR.instruction.BrInstr(condBlock, (IRVariable)currentEntity, bodyBlock, backBlock));
        }

        if(it.body != null){
            currentBlock.parent.addBlock(bodyBlock);
            currentBlock.instructions.add(new IR.instruction.JumpInstr(currentBlock, bodyBlock));
            currentBlock = bodyBlock;
            currentReNamer = new ReNamer(currentReNamer);
            it.body.accept(this);
            currentReNamer = currentReNamer.getParent();
            currentBlock.instructions.add(new IR.instruction.JumpInstr(currentBlock, condBlock));
        }

        currentBlock.parent.addBlock(backBlock);
        currentScope = currentScope.parentScope();
    }
    @Override public void visit(EmptyStmt it) {}

}
