package com.promineotech.projects.entity;

public class Step {
    private Integer stepId;
    private String stepDescription;

    // Getters and Setters
    public Integer getStepId() {
        return stepId;
    }

    public void setStepId(Integer stepId) {
        this.stepId = stepId;
    }

    public String getStepDescription() {
        return stepDescription;
    }

    public void setStepDescription(String stepDescription) {
        this.stepDescription = stepDescription;
    }

    @Override
    public String toString() {
        return "Step{" +
               "stepId=" + stepId +
               ", stepDescription='" + stepDescription + '\'' +
               '}';
    }
}
