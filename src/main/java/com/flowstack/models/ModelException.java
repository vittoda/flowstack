package com.flowstack.models;

public class ModelException extends Exception{

    public ModelException(String e) {
        super(e);
    }

    public ModelException(Exception e) {
        super(e);
    }
    
}
