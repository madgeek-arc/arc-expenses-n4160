package arc.expenses.n4160.domain;


import arc.athenarc.n4160.domain.Budget;

public class BudgetSummary {

    private String id;
    private String projectId;
    private String projectAcronym;
    private String instituteName;
    private String submittedByFullName;
    private int year;
    private boolean canEdit;
    private Long creationDate;
    private Budget.BudgetStatus budgetStatus;
    private String stage;


    public BudgetSummary() { }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectAcronym() {
        return projectAcronym;
    }

    public void setProjectAcronym(String projectAcronym) {
        this.projectAcronym = projectAcronym;
    }

    public String getInstituteName() {
        return instituteName;
    }

    public void setInstituteName(String instituteName) {
        this.instituteName = instituteName;
    }

    public String getSubmittedByFullName() {
        return submittedByFullName;
    }

    public void setSubmittedByFullName(String submittedByFullName) {
        this.submittedByFullName = submittedByFullName;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
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
}
