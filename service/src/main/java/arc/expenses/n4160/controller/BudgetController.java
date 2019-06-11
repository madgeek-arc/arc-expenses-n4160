package arc.expenses.n4160.controller;

import arc.athenarc.n4160.domain.*;
import arc.expenses.n4160.domain.OrderByField;
import arc.expenses.n4160.domain.OrderByType;
import arc.expenses.n4160.domain.RequestResponse;
import arc.expenses.n4160.domain.RequestSummary;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
            HttpServletRequest req) throws Exception {



        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation("Reject budget request")
    @RequestMapping(value = "/reject/{budgetId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity reject(
            @PathVariable("budgetId") String budgetId,
            HttpServletRequest req
    ) throws Exception {


        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation("Downgrade budget request")
    @RequestMapping(value = "/downgrade/{budgetId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity downgrade(
            @PathVariable("budgetId") String budgetId,
            HttpServletRequest req
    ) throws Exception {

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation("Edit budget request")
    @RequestMapping(value = "/edit/{budgetId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity edit(
            @PathVariable("budgetId") String budgetId,
            HttpServletRequest req
    ) throws Exception {

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation("Cancel budget request")
    @RequestMapping(value = "/cancel/{budgetId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity cancel(
            @PathVariable("budgetId") String budgetId) throws Exception {


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
            @ApiImplicitParam(name = "technicalReport", value = "The technical report", dataType = "file", paramType = "form")
    })
    @RequestMapping(value = "/add", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Budget addBudget(
            @RequestParam(value = "projectId") String projectId,
            @RequestParam(value = "year") int year,
            @RequestParam(value = "regularAmount", required = false, defaultValue = "0.0") Double regularAmount,
            @RequestParam(value = "contractAmount", required = false, defaultValue = "0.0") Double contractAmount,
            @RequestParam(value = "tripAmount", required = false, defaultValue = "0.0") Double tripAmount,
            @RequestParam(value = "servicesContractAmount", required = false, defaultValue = "0.0") Double servicesContractAmount,
            @RequestParam(value = "boardDecision") Optional<MultipartFile> boardDecision,
            @RequestParam(value = "technicalReport") Optional<MultipartFile> technicalReport
    ) throws Exception {
        return budgetService.add(projectId, year, regularAmount, contractAmount, tripAmount, servicesContractAmount, boardDecision, technicalReport);
    }

//    @RequestMapping(value =  "/getById/{id}", method = RequestMethod.GET)
//    public Request getById(@PathVariable("id") String id) throws ResourceNotFoundException {
//        return null;
//    }

//    @RequestMapping(value =  "/getAll", method = RequestMethod.GET)
//    public Paging<RequestSummary> getAllRequests(@RequestParam(value = "from",required=false,defaultValue = "0") int from,
//                                                 @RequestParam(value = "quantity",required=false,defaultValue = "10") int quantity,
//                                                 @RequestParam(value = "status") List<BaseInfo.Status> status,
//                                                 @RequestParam(value = "type") List<Request.Type> type,
//                                                 @RequestParam(value = "searchField",required=false, defaultValue = "") String searchField,
//                                                 @RequestParam(value = "stage") List<String> stage,
//                                                 @RequestParam(value = "order",required=false,defaultValue = "ASC") OrderByType orderType,
//                                                 @RequestParam(value = "orderField") OrderByField orderField,
//                                                 @RequestParam(value = "editable", required = false, defaultValue = "false") boolean canEdit,
//                                                 @RequestParam(value = "isMine", required = false, defaultValue = "false") boolean isMine,
//                                                 @RequestParam(value = "projectAcronym", required = false, defaultValue = "") String projectAcronym,
//                                                 @RequestParam(value = "institute", required = false, defaultValue = "") String institute,
//                                                 @RequestParam(value = "requester", required = false, defaultValue = "") String requester) {
//
//        return requestService.criteriaSearch(from,quantity,status,type,searchField,stage,orderType,orderField, canEdit, isMine, projectAcronym, institute, requester);
//
//    }

    /*////////////////////////////////////////////////////////////////////////////////////////
                                        OLD STUFF
      ////////////////////////////////////////////////////////////////////////////////////////
     */

}