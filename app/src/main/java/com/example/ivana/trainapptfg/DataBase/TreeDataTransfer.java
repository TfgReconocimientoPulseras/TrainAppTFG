package com.example.ivana.trainapptfg.DataBase;

public class TreeDataTransfer {
    private long id;
    private String tree;

    public TreeDataTransfer(String tree) {
        this.tree = tree;
    }

    public TreeDataTransfer(long id, String tree) {
        this.id = id;
        this.tree = tree;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTree() {
        return tree;
    }

    public void setTree(String tree) {
        this.tree = tree;
    }
}
