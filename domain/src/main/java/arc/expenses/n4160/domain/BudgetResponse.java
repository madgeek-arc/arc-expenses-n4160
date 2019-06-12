package arc.expenses.n4160.domain;

import arc.athenarc.n4160.domain.*;

import java.util.Map;

public class BudgetResponse {

    private String id;
    private String projectAcronym;
    private String projectId;
    private String instituteName;
    private int year;
    private User submittedBy;
    private Long creationDate;
    private Budget.BudgetStatus budgetStatus;
    private String stage;
    private Double regularAmount;
    private Double contractAmount;
    private Double tripAmount;
    private Double servicesContractAmount;
    private String comment;
    private Stage2 stage2;
    private Stage4 stage4;
    private Stage5a stage5a;
    private Stage6 stage6;
    private Attachment boardDecision;
    private Attachment technicalReport;
    private boolean canEdit;
    private boolean canEditPrevious;

    public BudgetResponse() { }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectAcronym() {
        return projectAcronym;
    }

    public void setProjectAcronym(String projectAcronym) {
        this.projectAcronym = projectAcronym;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getInstituteName() {
        return instituteName;
    }

    public void setInstituteName(String instituteName) {
        this.instituteName = instituteName;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public User getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(User submittedBy) {
        this.submittedBy = submittedBy;
    }

    public Long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
    }

    public Budget.BudgetStatus getBudgetStatus() {
        return budgetStatus;
    }

    public void setBudgetStatus(Budget.BudgetStatus budgetStatus) {
        this.budgetStatus = budgetStatus;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public Double getRegularAmount() {
        return regularAmount;
    }

    public void setRegularAmount(Double regularAmount) {
        this.regularAmount = regularAmount;
    }

    public Double getContractAmount() {
        return contractAmount;
    }

    public void setContractAmount(Double contractAmount) {
        this.contractAmount = contractAmount;
    }

    public Double getTripAmount() {
        return tripAmount;
    }

    public void setTripAmount(Double tripAmount) {
        this.tripAmount = tripAmount;
    }

    public Double getServicesContractAmount() {
        return servicesContractAmount;
    }

    public void setServicesContractAmount(Double servicesContractAmount) {
        this.servicesContractAmount = servicesContractAmount;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Stage2 getStage2() {
        return stage2;
    }

    public void setStage2(Stage2 stage2) {
        this.stage2 = stage2;
    }

    public Stage4 getStage4() {
        return stage4;
    }

    public void setStage4(Stage4 stage4) {
        this.stage4 = stage4;
    }

    public Stage5a getStage5a() {
        return stage5a;
    }

    public void setStage5a(Stage5a stage5a) {
        this.stage5a = stage5a;
    }

    public Stage6 getStage6() {
        return stage6;
    }

    public void setStage6(Stage6 stage6) {
        this.stage6 = stage6;
    }

    public Attachment getBoardDecision() {
        return boardDecision;
    }

    public void setBoardDecision(Attachment boardDecision) {
        this.boardDecision = boardDecision;
    }

    public Attachment getTechnicalReport() {
        return technicalReport;
    }

    public void setTechnicalReport(Attachment technicalReport) {
        this.technicalReport = technicalReport;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    public boolean isCanEditPrevious() {
        return canEditPrevious;
    }

    public void setCanEditPrevious(boolean canEditPrevious) {
        this.canEditPrevious = canEditPrevious;
    }
}
