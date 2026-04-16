package com.vitraya.adjudication.engine.service;

import com.google.gson.*;
import com.vitraya.adjudication.engine.dto.enums.ErrorMsgType;
import com.vitraya.adjudication.engine.dto.enums.ExternalEntity;
import com.vitraya.adjudication.engine.dto.response.NivaResponse;
import com.vitraya.adjudication.engine.dto.response.NivaSettlementResponse;
import com.vitraya.adjudication.engine.mysql.entity.ClaimData;
import com.vitraya.adjudication.engine.mysql.entity.ErrorMessageLogs;
import com.vitraya.adjudication.engine.mysql.entity.NivaPushEvent;
import com.vitraya.adjudication.engine.mysql.repository.ClaimAdjudicationRepository;
import com.vitraya.adjudication.engine.mysql.repository.ErrorMessageLogRepository;
import com.vitraya.adjudication.engine.rulesengine.niva.dto.AuthTokenApiRequestModel;
import com.vitraya.adjudication.engine.rulesengine.niva.dto.AuthenticationResponseDTO;
import com.vitraya.adjudication.engine.rulesengine.niva.dto.PreAuthRequest;
import com.vitraya.adjudication.engine.rulesengine.niva.dto.SettlementRequest;
import com.vitraya.adjudication.engine.service.factory.InsuranceAgencyCallBacks;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@Slf4j
public class NivaCallbackImpl implements InsuranceAgencyCallBacks {
    private final ErrorMessageLogsService errorMessageLogsService;
    @Value("${wdms.username}")
    private String username;

    @Value("${wdms.password}")
    private String password;

    @Value("${wdms.url}")
    private String wdmsUrl;

    @Value("${niva.external.api.base.url}")
    private String nivaExternalApiBaseUrl;

    @Value("${preauth.creation.endpoint}")
    private String preauthCreationEndpoint;

    @Value("${extension.creation.endpoint}")
    private String interimEndpoint;

    @Value("${discharge.creation.endpoint}")
    private String dischargeEndpoint;

    @Value("${reconsideration.creation.endpoint}")
    private String reconsiderEndpoint;

    @Value("${queryreply.creation.endpoint}")
    private String queryReplyEndpoint;

    @Value("${settlement.creation.endpoint}")
    private String settlementEndpoint;

    @Value("${niva.external.api.user.id}")
    private String userID;

    @Value("${niva.external.api.client.id}")
    private String clientID;

    @Value("${is.dev.env}")
    private boolean isDevEnv;

    private final ExternalApiCallBacksHelper externalApiCallBacksHelper;

    private final GoogleChatNotificationService googleChatNotificationService;

    private final RestService restService;

    private final ErrorMessageLogRepository errorMessageLogRepository;

    private final IntegrationLogService integrationLogService;

    public NivaCallbackImpl(ExternalApiCallBacksHelper externalApiCallBacksHelper, GoogleChatNotificationService googleChatNotificationService,
                            RestService restService, ErrorMessageLogRepository errorMessageLogRepository,
                            ErrorMessageLogsService errorMessageLogsService, IntegrationLogService integrationLogService) {
        this.externalApiCallBacksHelper = externalApiCallBacksHelper;
        this.googleChatNotificationService = googleChatNotificationService;
        this.restService = restService;
        this.errorMessageLogRepository = errorMessageLogRepository;
        this.errorMessageLogsService = errorMessageLogsService;
        this.integrationLogService = integrationLogService;
    }

    @Override
    public boolean sendWdmsSoapRequest(String intimationNumber, String activityType, String preAuthId,
                                       ClaimData claimData, NivaPushEvent nivaPushEvent) {
        long startTime = System.currentTimeMillis();
        String partnerUniqueId = intimationNumber;

        log.info("claim -{}sending wdms soap request ", claimData.getId());

        String xml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:req=\"http://ngprocedureselectq.ops.newgen.com/schema/Ngprocedureselect/request\">\n" +
                "    <soapenv:Header/>\n" +
                "    <soapenv:Body>\n" +
                "        <req:ProcedureselectReq>\n" +
                "            <Option>Online_Preauth_Request</Option>\n" +
                "            <Username>" + username + "</Username>\n" +
                "            <Password>" + password + "</Password>\n" +
                "            <req:CriteriaArray>\n" +
                "                <req:Criteria>\n" +
                "                    <FieldName>PARTNER_PREAUTH_XML</FieldName>\n" +
                "                    <FieldValue>\n" +
                "                        <![CDATA[\n" +
                "                           <PARTNER_PREAUTH_XML>" +
                "                               <PARTNER_UNIQUE_ID>" + partnerUniqueId + "</PARTNER_UNIQUE_ID>" +
                "                               <ACTIVITY_TYPE>" + activityType + "</ACTIVITY_TYPE>" +
                "                               <PREAUTH_ID>" + preAuthId + "</PREAUTH_ID>" +
                "                           </PARTNER_PREAUTH_XML>]]>\n" +
                "                    </FieldValue>\n" +
                "                </req:Criteria>\n" +
                "            </req:CriteriaArray>\n" +
                "            <nextSeedValue>?</nextSeedValue>\n" +
                "        </req:ProcedureselectReq>\n" +
                "    </soapenv:Body>\n" +
                "</soapenv:Envelope>";
        boolean response = callSoapService(xml, claimData, startTime, nivaPushEvent);
        log.info("claim -{}response for soap api {} ", claimData.getId(), response);
        return response;
    }

    @Override
    public NivaResponse sendClaimRequestToNiva(Object preAuthRequest,
                                               ClaimData claimData,
                                               boolean isPreAuth,
                                               boolean isInterim,
                                               boolean isDischarge,
                                               boolean isQueryReply,
                                               boolean isReconsideration,
                                               boolean isSettlement,
                                               boolean isRepush,
                                               NivaPushEvent nivaPushEvent,
                                               long preAuthStartTime) {
        String url = nivaExternalApiBaseUrl;
        ErrorMsgType error = resolveErrorType(isPreAuth, isRepush, isInterim, isDischarge, isQueryReply, isSettlement, isReconsideration);
        String failureEngine = error != null ? error.getFailureEngine() : "";
        String failureReason = error != null ? error.getReason() : "";
        try {
            log.info("claim -{} In send pre auth request method", claimData.getId());
            if (isPreAuth && !isRepush) {
                url += preauthCreationEndpoint;
            } else if (isInterim || isRepush) {
                url += interimEndpoint;
            } else if (isDischarge) {
                url += dischargeEndpoint;
            } else if (isQueryReply) {
                url += queryReplyEndpoint;
            } else if (isSettlement) {
                url += settlementEndpoint;
            } else if (isReconsideration) {
                url += reconsiderEndpoint;
            }
            log.info("claim -{} PreAuth Url and request :  {} , {}", claimData.getId(), url, new Gson().toJson(preAuthRequest));

            String token = null;
            boolean isTokenFetchSuccessful = false;

            String authenticationDtostr = getAuthToken();
            isTokenFetchSuccessful = externalApiCallBacksHelper.validateAuthenticationResponse(authenticationDtostr);

            if (isTokenFetchSuccessful) {
                AuthenticationResponseDTO authenticationResponseDTO = new Gson().
                        fromJson(authenticationDtostr, AuthenticationResponseDTO.class);
                token = authenticationResponseDTO.getToken();
                log.info("claim -{} Token fetch Status is successful {}", claimData.getId(), token);

            } else {
                googleChatNotificationService.sendMessage("Error in fetching NIVA token. Claim id = " + claimData.getId());
            }


            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("Authorization", "Bearer " + token);
            headerMap.put("Content-Type", "application/json");

            String preauthRes = restService.post(url, preAuthRequest, String.class, headerMap);

            log.info("claim -{} response from niva case api - {}", claimData.getId(), preauthRes);

            if (isDevEnv) {
                log.info("sendPreAuthRequestToNiva::DEV env.");
                preauthRes = "{\n" +
                        "  \"Status\": \"True\",\n" +
                        "  \"StatusMessage\": \"True\",\n" +
                        "  \"PreauthId\": \"" + generateRandomPreauthId() + "\",\n" +
                        "  \"ErrorList\": null\n" +
                        "}";
            }
            NivaResponse response = new Gson().fromJson(preauthRes, NivaResponse.class);
            String caseApiErrorMsg = extractNivaCaseApiError(preauthRes, claimData.getId());
            if (caseApiErrorMsg != null && !caseApiErrorMsg.trim().isEmpty()) {
                saveNivaAPIErrorLogs(claimData, caseApiErrorMsg, failureEngine, failureReason);
                log.info("Saving Error Message Logs in sending the request to Niva API method for claim data id {}, skipping insert", claimData.getId());
            }
            long endTime = System.currentTimeMillis();
            integrationLogService.logInteraction(nivaPushEvent.getId(), claimData.getIntimationNumber(),
                    ExternalEntity.CASE_API.toString(), url, new Gson().toJson(preAuthRequest), preauthRes, endTime - preAuthStartTime, "Niva " + url  + " API call successful");
            if (isSettlement) {
                return response;
            } else {
                return response;
            }
        } catch (HttpServerErrorException e) {
            log.error("Caught HttpServerErrorException as - {} , for url - {} while sending the request to Niva API for claimId - {} ", e.getMessage(), url, claimData.getId());
            long endTime = System.currentTimeMillis();
            integrationLogService.logInteraction(nivaPushEvent.getId(), claimData.getIntimationNumber(),
                    ExternalEntity.CASE_API.toString(), url, new Gson().toJson(preAuthRequest), e.getMessage(), endTime - preAuthStartTime, "Niva " + url  + " API call failed");
            saveNivaAPIErrorLogs(claimData, e.getMessage(), failureEngine, failureReason);
            log.info("Saving Error Message Logs in sending the request to Niva API method for claim data id {}, skipping insert", claimData.getId());
            return null;
        } catch (Exception e) {
            log.error("Caught an exception as - {} , for url - {} while sending the request to Niva API for claimId - {} ", e.getMessage(), url, claimData.getId());
            long endTime = System.currentTimeMillis();
            integrationLogService.logInteraction(nivaPushEvent.getId(), claimData.getIntimationNumber(),
                    ExternalEntity.CASE_API.toString(), url, new Gson().toJson(preAuthRequest), e.getMessage(), endTime - preAuthStartTime, "Niva " + url  + " API call failed");
            saveNivaAPIErrorLogs(claimData, e.getMessage(), failureEngine, failureReason);
            log.info("Saving Error Message Logs in sending the request to Niva API method for claim data id {}, skipping insert", claimData.getId());
            return null;
        }
    }

    @Override
    public NivaSettlementResponse sendSettlementRequestToNiva(SettlementRequest settlementRequest, ClaimData claimData,
                                                              NivaPushEvent nivaPushEvent, long settlementStartTime) {
        String url = nivaExternalApiBaseUrl;
        ErrorMsgType error = ErrorMsgType.NIVA_SETTLEMENT_API_FAILURE;
        String failureEngine = error.getFailureEngine();
        String failureReason = error.getReason();
        long startTime = System.currentTimeMillis();
        try {
            log.info("claim -{} In send settlement request method", claimData.getId());
            url += settlementEndpoint;
            log.info("claim -{} settlement Url and request :  {} , {}", claimData.getId(), url, new Gson().toJson(settlementRequest));
            String token = null;
            boolean isTokenFetchSuccessful = false;

            String authenticationDtostr = getAuthToken();
            isTokenFetchSuccessful = externalApiCallBacksHelper.validateAuthenticationResponse(authenticationDtostr);

            if (isTokenFetchSuccessful) {
                AuthenticationResponseDTO authenticationResponseDTO = new Gson().
                        fromJson(authenticationDtostr, AuthenticationResponseDTO.class);
                token = authenticationResponseDTO.getToken();
                log.info("claim -{} Token fetch Status is successful {}", claimData.getId(), token);

            } else {
                googleChatNotificationService.sendMessage("Error in fetching NIVA token. Claim id = " + claimData.getId());
            }


            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("Authorization", "Bearer " + token);
            headerMap.put("Content-Type", "application/json");

            String settlementRes = restService.post(url, settlementRequest, String.class, headerMap);

            log.info("claim -{} response from niva case api - {}", claimData.getId(), settlementRes);
            if (isDevEnv) {
                log.info("sendPreAuthRequestToNiva::DEV env.");
                settlementRes = "{\n" +
                        "  \"Status\": \"True\",\n" +
                        "  \"StatusMessage\": \"True\",\n" +
                        "  \"ClaimNumber\": \"" + generateRandomPreauthId() + "\",\n" +
                        "  \"ErrorList\": null\n" +
                        "}";
            }
            try {
                String soapApiErrorMsg = extractNivaCaseApiError(settlementRes, claimData.getId());
                if (soapApiErrorMsg != null && !soapApiErrorMsg.trim().isEmpty()) {
                    saveNivaAPIErrorLogs(claimData, soapApiErrorMsg, failureEngine, failureReason);
                    log.info("Saving Error Message Logs in sending the settlement request method for claim data id {}, skipping insert", claimData.getId());
                }
            } catch (Exception e) {
                log.info("Error while save error message Log for claim data id {}", claimData.getId());
            }
            long endTime = System.currentTimeMillis();
            integrationLogService.logInteraction(nivaPushEvent.getId(), claimData.getIntimationNumber(),
                    ExternalEntity.CASE_API.toString(), url, new Gson().toJson(settlementRequest), settlementRes, endTime - startTime, "Niva Settlement API call successful");
            return new Gson().fromJson(settlementRes, NivaSettlementResponse.class);
        } catch (HttpServerErrorException e) {
            log.error("Caught HttpServerErrorException as - {} , for url - {} while sending the settlement request to Niva API for claimId - {} ", e.getMessage(), url, claimData.getId());
            saveNivaAPIErrorLogs(claimData, e.getMessage(), error.getFailureEngine(), error.getReason());
            log.info("Saving Error Message Logs in sending the settlement request method for claim data id {}, skipping insert", claimData.getId());
            long endTime = System.currentTimeMillis();
            integrationLogService.logInteraction(nivaPushEvent.getId(), claimData.getIntimationNumber(),
                    ExternalEntity.CASE_API.toString(), url, new Gson().toJson(settlementRequest), e.getMessage(), endTime - startTime, "Niva Settlement API call failed");
            return null;
        } catch (Exception e) {
            log.error("Caught an exception as - {} , for url - {} while sending the settlement request to Niva API for claimId - {} ", e.getMessage(), url, claimData.getId());
            saveNivaAPIErrorLogs(claimData, e.getMessage(), error.getFailureEngine(), error.getReason());
            log.info("Saving Error Message Logs in sending the settlement request method for claim data id {}, skipping insert", claimData.getId());
            long endTime = System.currentTimeMillis();
            integrationLogService.logInteraction(nivaPushEvent.getId(), claimData.getIntimationNumber(),
                    ExternalEntity.CASE_API.toString(), url, new Gson().toJson(settlementRequest), e.getMessage(), endTime - startTime, "Niva Settlement API call failed");

            return null;
        }
    }

    public String getAuthToken() {

        log.info("Calling nivabupa api for authentication token.");

        AuthTokenApiRequestModel authTokenApiRequestModel = new AuthTokenApiRequestModel();

        authTokenApiRequestModel.setClient_id(clientID);
        authTokenApiRequestModel.setUserID(userID);

        //if token null do something about it
        String url = nivaExternalApiBaseUrl + "/api/auth/getauthtoken";

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", "application/json");

        return restService.post(url, authTokenApiRequestModel, String.class, headerMap);
    }

    private boolean callSoapService(String soapRequestXml, ClaimData claimData, long startTime, NivaPushEvent nivaPushEvent) {
        String errroMessage = "";
        ErrorMsgType error = ErrorMsgType.NIVA_SOAP_API_FAILURE;
        try {
            String url = wdmsUrl;
            URL obj = new URL(url);

            log.info("claim -{} url for soap api {}", claimData.getId(), url);
            log.info("claim -{} request for soap api {}", claimData.getId(), soapRequestXml);

            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            con.setDoOutput(true);
            con.setConnectTimeout(30000); // 30 sec timeout
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(soapRequestXml);
            log.info("claim -{} DataOutputStream for soap api {}", claimData.getId(), wr);


            wr.flush();
            wr.close();
            String responseStatus = con.getResponseMessage();
            log.info("claim -{} responseStatus {}", claimData.getId(), responseStatus);
            log.info("claim -{} con.getInputStream() {}", claimData.getId(), con.getInputStream().toString());

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            log.info("claim -{} response xml, {}", claimData.getId(), in);

            String inputLine;
            StringBuffer response = new StringBuffer();
            log.info("claim -{} response from soap {}", claimData.getId(), response);


            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            String finalvalue = response.toString();

            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new InputSource(new StringReader(response.toString())));
            log.info("claim -{} doc - {}", claimData.getId(), doc);

            NodeList nodeList = doc.getElementsByTagName("soapenv:Envelope");
            log.info("claim -{} getTextContent - {}", claimData.getId(), nodeList.item(0).getTextContent());
            errroMessage = nodeList.item(0).getTextContent();
            long endTime = System.currentTimeMillis();
            if (nodeList.getLength() > 0) {
                Document doc1 = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                        .parse(new InputSource(new StringReader(nodeList.item(0).getTextContent())));
                NodeList nodeListResponse = doc1.getElementsByTagName("RESPONSE");
                log.info("claim -{} doc1 - {}", claimData.getId(), nodeListResponse);

                log.info("claim -{} getTextContent doc1 - {}", claimData.getId(), nodeListResponse.item(0).getTextContent());

                if (nodeListResponse.item(0).getTextContent().equalsIgnoreCase("Success")) {
                    integrationLogService.logInteraction(nivaPushEvent.getId(), claimData.getIntimationNumber(),
                            ExternalEntity.WDMS_SOAP_API.toString(), "-", sanitizeSoapXml(soapRequestXml), "SUCCESS", endTime - startTime, "WDMS SOAP API call successful");
                    return true;
                } else {
                    try {
                        NodeList failureNode = doc1.getElementsByTagName("FAILURE_REASON");
                        String failureMesssage = null;
                        if (failureNode != null && failureNode.getLength() > 0) {
                            failureMesssage = failureNode.item(0).getTextContent().trim();
                            log.info("claim -{} soap api Failure Reason: {}", claimData.getId(), failureMesssage);
                            log.info("claim -{} soap api Error Response: {}", claimData.getId(), errroMessage);
                            saveNivaAPIErrorLogs(claimData, failureMesssage, error.getFailureEngine(), error.getReason());
                            log.info("Saving Error Message Logs in callSoapService method for claim data id {}, skipping insert", claimData.getId());
                            integrationLogService.logInteraction(nivaPushEvent.getId(), claimData.getIntimationNumber(),
                                    ExternalEntity.WDMS_SOAP_API.toString(), "-", sanitizeSoapXml(soapRequestXml), failureMesssage, endTime - startTime, "WDMS SOAP API call failed");
                        }
                    } catch (Exception e) {
                        integrationLogService.logInteraction(nivaPushEvent.getId(), claimData.getIntimationNumber(),
                                ExternalEntity.WDMS_SOAP_API.toString(), "-", sanitizeSoapXml(soapRequestXml), e.getMessage(), endTime - startTime, "WDMS SOAP API call failed");
                        log.info("Error while save error message Log for claim data id {}", claimData.getId());
                    }
                    return false;
                }
            }
            integrationLogService.logInteraction(nivaPushEvent.getId(), claimData.getIntimationNumber(),
                    ExternalEntity.WDMS_SOAP_API.toString(), "-", sanitizeSoapXml(soapRequestXml), errroMessage, endTime - startTime, "WDMS SOAP API call failed");
            log.info("claim -{} final response - {}", claimData.getId(), finalvalue);
            log.info("claim -{} final - {}", claimData.getId(), finalvalue.substring(finalvalue.indexOf("<RESPONSE>")));
            return false;
        } catch (Exception e) {
            log.error("claim -{} error in soap api", claimData.getId(), e);
            long endTime = System.currentTimeMillis();
            saveNivaAPIErrorLogs(claimData, e.getMessage(), error.getFailureEngine(), error.getReason());
            integrationLogService.logInteraction(nivaPushEvent.getId(), claimData.getIntimationNumber(),
                    ExternalEntity.WDMS_SOAP_API.toString(), "-", sanitizeSoapXml(soapRequestXml), e.getMessage(), endTime - startTime, "WDMS SOAP API call failed");
            log.info("Saving Error Message Logs in callSoapService method for claim data id {}, skipping insert", claimData.getId());
            return false;
        }
    }

    private String generateRandomPreauthId() {
        Random random = new Random();
        // Generate a random number between 10000 and 99999 (5-digit range)
        int randomNumber = random.nextInt(90000) + 10000;
        return "VCC_1234_1975_2_" + randomNumber;
    }

    private void saveNivaAPIErrorLogs(ClaimData claimData, String errorMessage, String failureEngine, String failureReason) {
        try {
            String claimStage = errorMessageLogsService.getClaimStage(claimData.getClaimStatus());
            log.info("claimStage: {} in saveNivaAPIErrorLogs", claimStage);
            ErrorMessageLogs existingLog = errorMessageLogRepository.findByClaimIdErrorReasonClaimStage(claimData.getId(), failureEngine, claimStage).orElse(null);
            if (existingLog == null) {
//                    errorMessage = failureReason + "(" + errorMessage + ") Claim sent via email channel, Kindly process manually";
                errorMessage = failureReason + "(" + errorMessage + ")";
                ErrorMessageLogs errorMessageLogs = ErrorMessageLogs.builder()
                        .claimDataId(claimData.getId())
                        .errorReason(failureReason)
                        .errorMessage(errorMessage)
                        .failureEngine(failureEngine)
                        .claimStage(claimStage)
                        .date_created(new Date())
                        .build();
                errorMessageLogRepository.save(errorMessageLogs);
                log.info("Error Message Logs saved successfully in saveNivaAPIErrorLogs for claim data id: {}", claimData.getId());
            } else {
                log.info("Error Message Logs already exists in saveNivaAPIErrorLogs for claim data id: {} with failure engine: {} and claim stage: {}", claimData.getId(), failureEngine, claimStage);
            }
        } catch (Exception e) {
            log.error("Error :{}  while saving error message logs in saveNivaAPIErrorLogs for :{} claim data id and API Type : {}", e.getMessage(), claimData.getId(), failureEngine);
        }
    }

    private String extractNivaCaseApiError(String settlementRes, Long claimId) {
        String apiErrorMessage = "";
        try {
            if (settlementRes == null || settlementRes.trim().isEmpty()) {
                log.info("settlementRes is null or empty for claim {}", claimId);
                return apiErrorMessage;
            }
            JsonElement element = JsonParser.parseString(settlementRes);
            if (element == null || !element.isJsonObject()) {
                log.info("Json element is null or empty for claim {}", claimId);
                return apiErrorMessage;
            }
            log.info("processing extractNivaCaseApiError for claim {} and request :{}", claimId, settlementRes);
            JsonObject json = element.getAsJsonObject();
            if (json.has("Status")) {
                String status = json.get("Status").getAsString().trim();
                if (status.equalsIgnoreCase("True") || status.equalsIgnoreCase("Success")) {
                    log.info("Status getting : {} for claim {}", status, claimId);
                    return null;
                }
            }
            if (json.has("ErrorList") && json.get("ErrorList").isJsonArray()) {
                JsonArray arr = json.getAsJsonArray("ErrorList");
                if (!arr.isEmpty()) {
                    JsonObject first = arr.get(0).getAsJsonObject();
                    if (first.has("ErrorMsg")) {
                        log.info("ErrorMsg getting from ErrorList : {} for claim {}", arr, claimId);
                        return first.get("ErrorMsg").getAsString();
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to extract Niva Settlement API error for claim {}: {}", claimId, e.getMessage());
        }

        return null;
    }

    private ErrorMsgType resolveErrorType(
            boolean isPreAuth,
            boolean isRepush,
            boolean isInterim,
            boolean isDischarge,
            boolean isQueryReply,
            boolean isSettlement,
            boolean isReconsideration
    ) {
        if (isPreAuth && !isRepush) return ErrorMsgType.NIVA_PREAUTH_CREATION_API_FAILURE;
        if (isInterim || isRepush) return ErrorMsgType.NIVA_INTERIM_API_FAILURE;
        if (isDischarge) return ErrorMsgType.NIVA_DISCHARGE_API_FAILURE;
        if (isQueryReply) return ErrorMsgType.NIVA_QUERY_REPLY_API_FAILURE;
        if (isSettlement) return ErrorMsgType.NIVA_SETTLEMENT_API_FAILURE;
        if (isReconsideration) return ErrorMsgType.NIVA_RECONSIDER_API_FAILURE;

        return null;
    }


    private String sanitizeSoapXml(String xml) {
        return xml
                .replaceAll("<Username>.*?</Username>", "<Username>****</Username>")
                .replaceAll("<Password>.*?</Password>", "<Password>****</Password>");
    }
}
