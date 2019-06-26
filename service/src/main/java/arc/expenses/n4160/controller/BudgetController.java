package arc.expenses.n4160.controller;

import arc.athenarc.n4160.domain.*;
import arc.expenses.n4160.domain.*;
import arc.expenses.n4160.service.BudgetServiceImpl;
import arc.expenses.n4160.service.RequestApprovalServiceImpl;
import arc.expenses.n4160.service.RequestPaymentServiceImpl;
import arc.expenses.n4160.service.RequestServiceImpl;
import eu.openminted.registry.core.domain.Browsing;
import eu.openminted.registry.core.domain.Paging;
import eu.openminted.registry.core.exception.ResourceNotFoundException;
import eu.openminted.registry.core.service.ParserPool;
import eu.openminted.registry.core.service.SearchService;
import eu.openminted.registry.core.service.ServiceException;
import eu.openminted.store.restclient.StoreRESTClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/budget")
@Api(description = "Budget API  ",  tags = {"Manage budget requests"})
public class BudgetController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BudgetController.class);

    @Autowired
    private BudgetServiceImpl budgetService;


    @ApiOperation("Approve budget request")
    @RequestMapping(value = "/approve/{budgetId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity approve(
            @PathVariable("budgetId") String budgetId,
            HttpServletRequest req
    ){

        Budget budget = budgetService.get(budgetId);
        if(budget == null)
            throw new ServiceException("Budget not found");

        budgetService.approve(budget,req);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation("Reject budget request")
    @RequestMapping(value = "/reject/{budgetId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity reject(
            @PathVariable("budgetId") String budgetId,
            HttpServletRequest req
    ){

        Budget budget = budgetService.get(budgetId);
        if(budget == null)
            throw new ServiceException("Budget not found");

        budgetService.reject(budget,req);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation("Downgrade budget request")
    @RequestMapping(value = "/downgrade/{budgetId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity downgrade(
            @PathVariable("budgetId") String budgetId,
            HttpServletRequest req
    ){

        Budget budget = budgetService.get(budgetId);
        if(budget == null)
            throw new ServiceException("Budget not found");

        budgetService.downgrade(budget,req);


        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation("Edit budget request")
    @RequestMapping(value = "/edit/{budgetId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity edit(
            @PathVariable("budgetId") String budgetId,
            HttpServletRequest req
    ){

        Budget budget = budgetService.get(budgetId);
        if(budget == null)
            throw new ServiceException("Budget not found");

        budgetService.edit(budget,req);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation("Cancel budget request")
    @RequestMapping(value = "/cancel/{budgetId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity cancel(
            @PathVariable("budgetId") String budgetId) throws Exception {

        Budget budget = budgetService.get(budgetId);
        if(budget == null)
            throw new ServiceException("Budget not found");

        budgetService.cancel(budget);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation("Add budget")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectId", value = "Id of project of request", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "year", value = "Year of the requested budget", required = true, dataType = "number", paramType = "form"),
            @ApiImplicitParam(name = "regularAmount", value = "Amount to be spent on regular requests", required = true, dataType = "number", paramType = "form"),
            @ApiImplicitParam(name = "contractAmount", value = "Amount to be spent on contracts", required = true, dataType = "number", paramType = "form"),
            @ApiImplicitParam(name = "tripAmount", value = "Amount to be spend on trips", required = true, dataType = "number", paramType = "form"),
            @ApiImplicitParam(name = "servicesContractAmount", value = "Amount to be spent on services contracts", required = true, dataType = "number", paramType = "form"),
            @ApiImplicitParam(name = "boardDecision", value = "The board decision", dataType = "file", paramType = "form"),
            @ApiImplicitParam(name = "technicalReport", value = "The technical report", dataType = "file", paramType = "form"),
            @ApiImplicitParam(name = "comment", value = "Any comment regarding the budget request", dataType = "string", paramType = "form")
    })
    @PreAuthorize("hasRole('ROLE_OPERATOR') or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/add", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Budget addBudget(
            @RequestParam(value = "projectId") String projectId,
            @RequestParam(value = "comment", required = false,defaultValue = "") String comment,
            @RequestParam(value = "year") int year,
            @RequestParam(value = "regularAmount", required = false, defaultValue = "0.0") Double regularAmount,
            @RequestParam(value = "contractAmount", required = false, defaultValue = "0.0") Double contractAmount,
            @RequestParam(value = "tripAmount", required = false, defaultValue = "0.0") Double tripAmount,
            @RequestParam(value = "servicesContractAmount", required = false, defaultValue = "0.0") Double servicesContractAmount,
            @RequestParam(value = "boardDecision") Optional<MultipartFile> boardDecision,
            @RequestParam(value = "technicalReport") Optional<MultipartFile> technicalReport
    ) throws Exception {
        return budgetService.add(projectId, year, regularAmount, contractAmount, tripAmount, servicesContractAmount, boardDecision, technicalReport, comment);
    }

    @PreAuthorize("hasRole('ROLE_EXECUTIVE') or hasRole('ROLE_ADMIN')")
    @RequestMapping(value =  "/getAll", method = RequestMethod.GET)
    public Paging<BudgetSummary> getAllRequests(@RequestParam(value = "from",required=false,defaultValue = "0") int from,
                                                 @RequestParam(value = "quantity",required=false,defaultValue = "10") int quantity,
                                                 @RequestParam(value = "status") List<Budget.BudgetStatus> status,
                                                 @RequestParam(value = "searchField",required=false, defaultValue = "") String searchField,
                                                 @RequestParam(value = "stage") List<String> stage,
                                                 @RequestParam(value = "order",required=false,defaultValue = "ASC") OrderByType orderType,
                                                 @RequestParam(value = "orderField") OrderByField orderField,
                                                 @RequestParam(value = "editable", required = false, defaultValue = "false") boolean canEdit,
                                                 @RequestParam(value = "isMine", required = false, defaultValue = "false") boolean isMine,
                                                 @RequestParam(value = "projectAcronym", required = false, defaultValue = "") String projectAcronym,
                                                 @RequestParam(value = "institute", required = false, defaultValue = "") String institute,
                                                 @RequestParam(value = "requester", required = false, defaultValue = "") String requester) {

        return budgetService.criteriaSearch(from,quantity,status,searchField,stage,orderType,orderField, canEdit, isMine, projectAcronym, institute, requester);

    }


    @RequestMapping(value =  "/getById/{id}", method = RequestMethod.GET)
    public BudgetResponse getById(@PathVariable("id") String id) throws Exception {
        Budget budget = budgetService.get(id);
        if(budget == null)
            throw new ResourceNotFoundException();
        return budgetService.getBudgetResponse(budget);
    }


    @RequestMapping(value =  "/getAmounts/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getAmountsOfBudget(@PathVariable("id") String id) throws Exception {
        Budget budget = budgetService.get(id);
        if(budget == null)
            throw new ResourceNotFoundException();
        return new ResponseEntity(budgetService.amountsOfBudget(budget).toString(), HttpStatus.OK);
    }


    @RequestMapping(value = "/store/uploadFile", method = RequestMethod.POST)
    public ResponseEntity<Object> uploadFile(@RequestParam("archiveID") String archiveID,
                                             @RequestParam("stage") String stage,
                                             @RequestParam("file") MultipartFile file) throws IOException {
        return budgetService.upLoadFile(archiveID,stage,file);
    }

    @RequestMapping(value = "/store", method = RequestMethod.GET)
    @ResponseBody
    public void downloadFile(@RequestParam("archiveId") String archiveId,
                             @RequestParam("id") String objectId,
                             HttpServletResponse response) throws Exception {
        Budget budget = budgetService.get(objectId);

        if(budget == null)
            throw new ServiceException("Budget not found");

        Attachment attachment = budgetService.getAttachmentsFromBudget(budget,archiveId);
        if(attachment == null)
            throw new ServiceException("Attachment not found");
        File temp = File.createTempFile(attachment.getFilename(), "tmp");
        temp = budgetService.downloadFile(temp,budget,archiveId);
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''"+ UriUtils.encode(attachment.getFilename(),"UTF-8") +"");
        IOUtils.copyLarge(new FileInputStream(temp), response.getOutputStream());
    }

    @RequestMapping(value = "/store", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteFile(@RequestParam("archiveId") String archiveId,
                           @RequestParam("id") String objectId) {

        Budget budget = budgetService.get(objectId);
        if (budget== null)
            throw new ServiceException("Budget not found");
        budgetService.deleteFile(budget, archiveId);
    }

}