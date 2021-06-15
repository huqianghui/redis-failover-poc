package com.idreamshen.demo.redis.property;

public class RedisProperty {

    public String getNodes() {
        return nodes;
    }

    public void setNodes(String nodes) {
        this.nodes = nodes;
    }

    private String nodes;

    private String password;

    public String getPassword(){
        return password;
    }

    public void setPassword(String password){
        this.password = password;
    }
}
