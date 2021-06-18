package com.ramiro.binder.visitor.model;

import java.math.BigDecimal;

public class Value {

    public static Value VOID = new Value("");

    final Object value;
    
    public Value(Object value) {
        this.value = value;
    }

    public Boolean asBoolean() {
        return (Boolean)value;
    }

    public BigDecimal asDecimal() {
        return (BigDecimal) value;
    }

    public String asString() {
        return String.valueOf(value);
    }

    public boolean isDecimal() {
        return value instanceof BigDecimal;
    }
    
    public boolean isBoolean(){
        return value instanceof Boolean;
    }
    
    public boolean isString(){
        return value instanceof String;
    }

    @Override
    public int hashCode() {

        if(value == null) {
            return 0;
        }

        return this.value.hashCode();
    }

    @Override
    public boolean equals(Object o) {
    	
        if(value == o) {
            return true;
        }

        if(value == null || o == null || o.getClass() != this.getClass()) {
            return false;
        }

        Value that = (Value)o;

        return this.value.equals(that.value);
        
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}