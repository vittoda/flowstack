package com.flowstack.models;

public class ModelNonRecoverableException extends ModelException {

    public ModelNonRecoverableException(String e) {
        super(e);
    }

     public ModelNonRecoverableException(Exception e) {
        super(e);
    }
    
}
