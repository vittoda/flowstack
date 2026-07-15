package com.flowstack.models;

public class GithubOpenAI extends OpenAI {

    public GithubOpenAI(String modelName) {
        super(modelName);
    }

    @Override
    protected String getCredKey() {
        return "flowstack.github.openai";
    }

}
