package com.promineotech.projects.entity;

public class Material {
    private Integer materialId;
    private String materialName;

    // Getters and Setters
    public Integer getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Integer materialId) {
        this.materialId = materialId;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    @Override
    public String toString() {
        return "Material{" +
               "materialId=" + materialId +
               ", materialName='" + materialName + '\'' +
               '}';
    }
}
