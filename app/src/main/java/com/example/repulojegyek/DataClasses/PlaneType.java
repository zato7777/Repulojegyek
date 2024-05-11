package com.example.repulojegyek.DataClasses;

public class PlaneType {
    private String typeName;
    private int rowCount;
    private int columnCount;

    public PlaneType() {}

    public PlaneType(String typeName, int rowCount, int columnCount) {
        this.typeName = typeName;
        this.rowCount = rowCount;
        this.columnCount = columnCount;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }
}
