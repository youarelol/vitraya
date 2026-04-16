package com.vitraya.adjudication.engine.controller;

import com.google.gson.Gson;
import com.vitraya.adjudication.engine.dto.enums.VitrayaErrorCodes;
import com.vitraya.adjudication.engine.dto.request.ClaimListRequestDTO;
import com.vitraya.adjudication.engine.dto.request.ClaimRerunRequestDTO;
import com.vitraya.adjudication.engine.dto.request.UserRequestDto;
import com.vitraya.adjudication.engine.dto.request.VitrayaInsurerClaimDataDTO;
import com.vitraya.adjudication.engine.dto.response.ClaimDataListDTO;
import com.vitraya.adjudication.engine.dto.response.ClaimDetailsResponseDTO;
import com.vitraya.adjudication.engine.dto.response.RestAPIResponse;
import com.vitraya.adjudication.engine.mysql.entity.Corporate;
import com.vitraya.adjudication.engine.mysql.entity.Users;
import com.vitraya.adjudication.engine.service.*;
import com.vitraya.adjudication.engine.service.factory.ClaimsFactory;
import com.vitraya.adjudication.engine.utils.EncryptionUtils;
import com.vitraya.adjudication.engine.utils.GsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;

@Slf4j
@RestController
@RequestMapping("/api/v1/claim")
public class ClaimController {

    private final ClaimCommonService claimCommonService;
    private final SseEmittersService sseEmittersService;
    @Value("${claim.registration.failed.email.to}")
    private String CLAIM_REGISTRATION_FAILED_EMAIL_TO;

    @Value("${claim.received.email.to}")
    private String CLAIM_RECEIVED_EMAIL_TO;

    private final ClaimsFactory claimsFactory;
    private final UserService userService;
    private final CorporateService corporateService;
    private final ClaimService claimService;
    private final CommunicationService communicationService;
    private final EncryptionUtils encryptionUtils;

    @Value("${API_PARAM_ENCRYPTION_DECRYTION_KEY}")
    private String apiParamEncryptiondecryptionKey;

    public ClaimController(ClaimsFactory claimsFactory, UserService userService, CorporateService corporateService,
                           ClaimService claimService, CommunicationService communicationService, ClaimCommonService claimCommonService, SseEmittersService sseEmittersService) {
        this.claimsFactory = claimsFactory;
        this.userService = userService;
        this.corporateService = corporateService;
        this.claimService = claimService;
        this.communicationService = communicationService;
        this.claimCommonService = claimCommonService;
        this.sseEmittersService = sseEmittersService;
        this.encryptionUtils = new EncryptionUtils();
    }

    /**
     * Creates a new claim.
     *
     * @param claimObject The claim object to be created.
     * @return A success response if the claim is created successfully.
     * @throws IOException    If an I/O error occurs.
     * @throws ParseException If a parsing error occurs.
     */

    @PostMapping("")
    @ResponseStatus(HttpStatus.OK)
    public RestAPIResponse createClaim(@RequestBody Object claimObject,
                                       @RequestAttribute("userDTO") UserRequestDto userRequestDTO) throws IOException, ParseException {
        /*Notify that a claim received first thing so that if in case the claim because of any reason is not processed
        then we can atleast check and process it.*/
        Users user = userRequestDTO.getUserData();
        Corporate corporate = corporateService.getUserCorporate(user.getCorporateId());
        VitrayaInsurerClaimDataDTO vitrayaInsurerClaimData;

        try {
            vitrayaInsurerClaimData = claimsFactory
                    .getClaimsFactory(corporate.getClaimDataParseMapping())
                    .validateAndParseClaimRequest(claimObject, null);
            log.info("parsed claimObject from vhi to vitrayaInsurerClaimData after mapping on db2- {}", new Gson().toJson(vitrayaInsurerClaimData));
            communicationService.sendEmail(claimCommonService.getIntimationNumber(vitrayaInsurerClaimData) +
                            " | New Claim Received (" + vitrayaInsurerClaimData.getRequest().getClaim().getId() + ") | "
                            + vitrayaInsurerClaimData.getRequestType().toString().toUpperCase(),
                    GsonUtils.toJson(vitrayaInsurerClaimData), CLAIM_RECEIVED_EMAIL_TO);
        } catch (Exception e) {
            communicationService.sendEmail("Claim Parsing Error", e.getMessage(), CLAIM_REGISTRATION_FAILED_EMAIL_TO);
            throw e;
        }

        claimService.processExternalClaim(vitrayaInsurerClaimData, corporate, null);
        return RestAPIResponse.buildSuccess("Claim successfully added to the queue");
    }

    // for testing
    @PostMapping(value = "/demo")
    @ResponseStatus(HttpStatus.OK)
    public RestAPIResponse createClaimAlongWthDoc(@RequestParam("claimObject") String claimObject,
                                                  @RequestParam("files") MultipartFile[] files) throws IOException, ParseException {
        /*Notify that a claim received first thing so that if in case the claim because of any reason is not processed
        then we can atleast check and process it.*/
        Users user = userService.getCurrentUser();
        Corporate corporate = corporateService.getUserCorporate(user.getCorporateId());
        VitrayaInsurerClaimDataDTO vitrayaInsurerClaimData = null;
        Object claimObject1 = GsonUtils.fromJson(claimObject, Object.class);
        try {
            vitrayaInsurerClaimData = claimsFactory
                    .getClaimsFactory(corporate.getClaimDataParseMapping())
                    .validateAndParseClaimRequest(claimObject1, files);

            communicationService.sendEmail(claimCommonService.getIntimationNumber(vitrayaInsurerClaimData) +
                            " | New Claim Received (" + vitrayaInsurerClaimData.getRequest().getClaim().getId() + ")",
                    GsonUtils.toJson(vitrayaInsurerClaimData), CLAIM_RECEIVED_EMAIL_TO);
        } catch (Exception e) {
            communicationService.sendEmail("Claim Parsing Error", e.getMessage(), CLAIM_REGISTRATION_FAILED_EMAIL_TO);
            throw e;
        }
        claimService.processExternalClaim(vitrayaInsurerClaimData, corporate, files);
        // claimService.sendNewClaimNotification(user);
        return RestAPIResponse.buildSuccess("Claim successfully added to the queue");
    }

    /**
     * Returns the claim list matching the passed parameters in the request.
     *
     * @param pageNo         Page number to fetch the data. Default value is 1.
     * @param pageSize       Number of records to fetch. Default value is 20.
     * @param insurerId      Insurer ID to fetch the data. Default value is empty and will be used in case when the user is not an insurer.
     * @param startDate      Start date to filter the data. Default value is 30 days back from the current date.
     * @param endDate        End date to filter the data. Default value is the current date.
     * @param attributeName  Attribute name to filter the data. Default value is empty. If empty, no filter will be applied.
     * @param attributeValue Attribute value to filter the data. Default value is empty. If empty, no filter will be applied.
     * @param sortBy         Attribute to sort the data. Default value is dateUpdated.
     * @return A list of claim data matching the passed parameters.
     * @throws ParseException If the date parsing fails.
     */
    @GetMapping("/list")
    @ResponseStatus(HttpStatus.OK)
    public RestAPIResponse getAllClaimList(@RequestParam("pageNo") int pageNo,
                                           @RequestParam("pageSize") int pageSize,
                                           @RequestParam(required = false, name = "insurerId") String insurerId,
                                           @RequestParam(required = false, name = "startDate") String startDate,
                                           @RequestParam(required = false, name = "endDate") String endDate,
                                           @RequestParam(required = false, name = "attributeName") String attributeName,
                                           @RequestParam(required = false, name = "attributeValue") String attributeValue,
                                           @RequestParam(required = false, name = "sortBy") String sortBy) throws ParseException,
            UnsupportedEncodingException {

        ClaimListRequestDTO claimListRequestDTO = claimService.getClaimListRequestDTO(pageNo, pageSize, insurerId, startDate,
                endDate, attributeName, attributeValue, sortBy);
        claimListRequestDTO.checkAndUpdateDateRange();

        Users user = userService.getCurrentUser();
        Corporate corporate = corporateService.getUserCorporate(user.getCorporateId());

        ClaimDataListDTO claimDataListDTO = claimService.fetchAllClaimDetails(corporate, claimListRequestDTO);
        //encrypted logic pass encrypted claim id to the client
        if (claimDataListDTO != null) {
            return RestAPIResponse.buildSuccess(claimDataListDTO);
        } else {
            return RestAPIResponse.buildFail(400, "Invalid credentials",
                    VitrayaErrorCodes.INVALID_CLAIM_DATA_REQUEST.getCode(), "Claims not found for the given criteria");
        }
    }

    /**
     * Returns the details of a specific claim.
     *
     * @param claimId The ID of the claim to retrieve.
     * @return The details of the specified claim.
     */

    @GetMapping("/{claimId}")
    public RestAPIResponse getClaimDetails(@PathVariable("claimId") String encryptedClaimId) {
        // Decrypt the encryptedClaimId
        log.info("Request received to get claim details for claimId: {}", encryptedClaimId);
        encryptedClaimId = encryptionUtils.validateEncrytedString(encryptedClaimId);

        String decryptedClaimIdString = encryptionUtils.decrypt(encryptedClaimId, apiParamEncryptiondecryptionKey);
        long decryptedClaimId = Long.parseLong(decryptedClaimIdString);
        if (decryptedClaimIdString == null || decryptedClaimIdString.isEmpty()) {
            return RestAPIResponse.buildFail(400, "Invalid claim ID",
                    VitrayaErrorCodes.INVALID_CLAIM_DATA_ID.getCode(), "Claim ID cannot be null or empty");
        }
        ClaimDetailsResponseDTO claimDetailsResponseDTO = claimService.prepareClaimData(decryptedClaimId);
        String id = claimDetailsResponseDTO.getId();
        claimDetailsResponseDTO.setId(encryptionUtils.encrypt(id, apiParamEncryptiondecryptionKey).replaceAll("/", "_"));
        if (claimDetailsResponseDTO != null) {
            return RestAPIResponse.buildSuccess(claimDetailsResponseDTO);
        } else {
            return RestAPIResponse.buildFail(400, "Claims not found for the given criteria",
                    VitrayaErrorCodes.INVALID_CLAIM_DATA_REQUEST.getCode(), "Claims not found for the given criteria");
        }
    }


    /**
     * Reruns the claim with the specified claim ID and given module id i.e. either we will run bill / tariff, PML or vNeuron
     * module.
     *
     * @param claimId
     * @param claimRerunRequestDTO
     * @return A success response if the claim is rerun successfully.
     */
    @PostMapping("/rerun/{claimId}")
    public RestAPIResponse rerunClaim(@PathVariable("claimId") String encryptedClaimId,
                                      @RequestBody ClaimRerunRequestDTO claimRerunRequestDTO) throws IOException {
        encryptedClaimId = encryptionUtils.validateEncrytedString(encryptedClaimId);
        String decryptedClaimId = encryptionUtils.decrypt(encryptedClaimId, apiParamEncryptiondecryptionKey);
        if (decryptedClaimId == null || decryptedClaimId.isEmpty()) {
            return RestAPIResponse.buildFail(400, "Invalid claim ID",
                    VitrayaErrorCodes.INVALID_CLAIM_DATA_ID.getCode(), "Claim ID cannot be null or empty");
        }
        claimService.rerunClaim(decryptedClaimId, claimRerunRequestDTO);
        return RestAPIResponse.buildSuccess("Your Request has been submitted successfully For Rerun");
    }

    @GetMapping("/updateClaimData")
    public RestAPIResponse updateClaimData() throws IOException {
        return claimService.checkAndUpdateClaimDecisions() != null
                ? RestAPIResponse.buildSuccess()
                : RestAPIResponse.buildFail(200, null, null, "Not Updated Claim Data Successfully");
    }
}
