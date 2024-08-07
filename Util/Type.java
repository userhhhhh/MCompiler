package Util;

import java.util.HashMap;

public class Type {
    public boolean isInt = false, isBool = false;
    public boolean isString = false, isNull = false, isVoid = false;
    public boolean isClass = false;
    public String typeName = null;
    public int dim = 0;

    public Type(String typeName, int dim) {
        this.typeName = typeName;
        this.dim = dim;
        if (typeName.equals("int")) {
            isInt = true;
        } else if (typeName.equals("bool")) {
            isBool = true;
        } else if (typeName.equals("string")) {
            isString = true;
        } else if (typeName.equals("void")) {
            isVoid = true;
        } else if (typeName.equals("null")) {
            isNull = true;
        } else {
            isClass = true;
        }
    }

    public Type(Type type) {
        this.isInt = type.isInt;
        this.isBool = type.isBool;
        this.isString = type.isString;
        this.isNull = type.isNull;
        this.isVoid = type.isVoid;
        this.isClass = type.isClass;
        this.typeName = type.typeName;
        this.dim = type.dim;
    }

    public boolean equals(Type other) {
        if (other.isNull) {
            if (isNull) return true;
            if (isVoid) return false;
            if (dim > 0) return true;
            return isClass;
        }
        if (isVoid && other.isVoid) return true;
        return !isVoid && !other.isVoid && typeName.equals(other.typeName) && dim == other.dim;
    }
}
