package arc.expenses.n4160.domain;

import arc.athenarc.n4160.domain.*;
import arc.athenarc.n4160.domain.Request;

import java.util.Map;

public class RequestResponse {

    private BaseInfo baseInfo;
    private Request.RequesterPosition requesterPosition;
    private Request.Type type;
    private Request.RequestStatus requestStatus;
    private Map<String, Stage> stages;
    private Double total;
    private Double paid;

    private String projectAcronym;
    private String instituteName;
    private String requesterEmail;
    private String requesterFullName;
    private String onBehalfFullName;
    private String onBehalfEmail;
    private String tripDestination;
    private String budgetId;
    private boolean canEdit;
    private boolean canEditPrevious;

    public RequestResponse() { }

    public BaseInfo getBaseInfo() {
        return baseInfo;
    }

    public void setBaseInfo(BaseInfo baseInfo) {
        this.baseInfo = baseInfo;
    }

    public Request.RequesterPosition getRequesterPosition() {
        return requesterPosition;
    }

    public void setRequesterPosition(Request.RequesterPosition requesterPosition) {
        this.requesterPosition = requesterPosition;
    }

    public Map<String, Stage> getStages() {
        return stages;
    }

    public void setStages(Map<String, Stage> stages) {
        this.stages = stages;
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

    public String getRequesterFullName() {
        return requesterFullName;
    }

    public void setRequesterFullName(String requesterFullName) {
        this.requesterFullName = requesterFullName;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    public Request.Type getType() {
        return type;
    }

    public void setType(Request.Type type) {
        this.type = type;
    }

    public Request.RequestStatus getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(Request.RequestStatus requestStatus) {
        this.requestStatus = requestStatus;
    }

    public String getOnBehalfFullName() {
        return onBehalfFullName;
    }

    public void setOnBehalfFullName(String onBehalfFullName) {
        this.onBehalfFullName = onBehalfFullName;
    }

    public String getTripDestination() {
        return tripDestination;
    }

    public void setTripDestination(String tripDestination) {
        this.tripDestination = tripDestination;
    }

    public String getOnBehalfEmail() {
        return onBehalfEmail;
    }

    public void setOnBehalfEmail(String onBehalfEmail) {
        this.onBehalfEmail = onBehalfEmail;
    }

    public String getRequesterEmail() {
        return requesterEmail;
    }

    public void setRequesterEmail(String requesterEmail) {
        this.requesterEmail = requesterEmail;
    }

    public boolean isCanEditPrevious() {
        return canEditPrevious;
    }

    public void setCanEditPrevious(boolean canEditPrevious) {
        this.canEditPrevious = canEditPrevious;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public Double getPaid() {
        return paid;
    }

    public void setPaid(Double paid) {
        this.paid = paid;
    }

    public String getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(String budgetId) {
        this.budgetId = budgetId;
    }
}
