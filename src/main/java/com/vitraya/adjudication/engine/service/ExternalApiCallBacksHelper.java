package com.vitraya.adjudication.engine.service;

import com.google.gson.Gson;
import com.vitraya.adjudication.engine.rulesengine.niva.dto.AuthenticationResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.util.Date;

@Service
public class ExternalApiCallBacksHelper {

    @Value("${token.retry.count:4}")
    private Integer tokenRetryCount;

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalApiCallBacksHelper.class);

//    public ExternalApiRequestModel buildInitialRequestModel(Claim claim, InsuranceAgencyCallBacks insuranceAgencyCallBacks) {
//
//        try {
//
//            String token = externalApiCallBacksHelper.isTokenAvailable();
//            ExternalApiRequestModel externalApiRequestModel = new ExternalApiRequestModel();
//
//            if (token != null) {
//                LOGGER.info("Token has not expired yet , using old token.");
//                externalApiRequestModel.setToken(token);
//                return externalApiRequestModel;
//            }
//
//            String authenticationDtostr = null;
//            boolean isTokenFetchSuccessful = false;
//            int numberOfRetriesRemaining = tokenRetryCount;
//            while (!isTokenFetchSuccessful && numberOfRetriesRemaining > 0) {
//                LOGGER.info("Token fetch Status , Allowed Retries - {} numberoFRetries Remaining - {}", tokenRetryCount, numberOfRetriesRemaining);
//                authenticationDtostr = insuranceAgencyCallBacks.getAuthToken();
//                isTokenFetchSuccessful = validateAuthenticationResponse(authenticationDtostr);
//                if (isTokenFetchSuccessful) {
//                    LOGGER.info("Token fetch Status is successful Allowed Retries - {} numberoFRetries Remaining - {}",
//                            tokenRetryCount, numberOfRetriesRemaining);
//                }
//                numberOfRetriesRemaining = numberOfRetriesRemaining - 1;
//            }
//
//
//            if (authenticationDtostr == null) {
//                LOGGER.info("Authentication token is null cant proceed further");
//
//                if (claim != null) {
//                    exceptionLogsService.saveErrorThread(claim,
//                            exceptionLogsService.buildErrorThreadModel("Authentication_token_null", "Authentication_token_null",
//                                    VitrayaAdjudicationFlowStages.EXTERNAL_API_CALL_FOR_MEMBER_DETAILS.toString(),
//                                    ClaimErrorThread.FailureType.UNABLE_TO_FETCH_DATA));
//                }
//
//                return null;
//            }
//
//            AuthenticationResponseDTO authenticationResponseDTO = new Gson().
//                    fromJson(authenticationDtostr, AuthenticationResponseDTO.class);
//
//            if (!authenticationResponseDTO.getStatus().equalsIgnoreCase("Success")) {
//
//                LOGGER.info("Niva external token api request not successfull. error : {}", authenticationResponseDTO.getErrorMsg());
//
//                if (claim != null) {
//                    exceptionLogsService.saveErrorThread(claim,
//                            exceptionLogsService.buildErrorThreadModel("Authentication_token_error", authenticationResponseDTO.getErrorMsg(),
//                                    VitrayaAdjudicationFlowStages.EXTERNAL_API_CALL_FOR_MEMBER_DETAILS.toString(),
//                                    ClaimErrorThread.FailureType.INSURER_API_ERROR));
//                }
//
//                return null;
//            }
//
//            externalApiRequestModel.setToken(authenticationResponseDTO.getToken());
//
//            saveToken(authenticationResponseDTO.getToken());
//
//            return externalApiRequestModel;
//        } catch (Exception e) {
//            LOGGER.info("Exception occured in calling insurer api for authentication token");
//            e.printStackTrace();
//            if (claim != null) {
//                exceptionLogsService.saveErrorThread(claim,
//                        exceptionLogsService.buildErrorThreadModel("Authentication_token_exception", e.getLocalizedMessage(),
//                                VitrayaAdjudicationFlowStages.EXTERNAL_API_CALL_FOR_MEMBER_DETAILS.toString(),
//                                ClaimErrorThread.FailureType.CODE_EXCEPTION));
//            }
//            return null;
//        }
//
//
//    }

    boolean validateAuthenticationResponse(String rawResponse) {
        if (rawResponse == null) {
            return Boolean.FALSE;
        }
        AuthenticationResponseDTO authenticationResponseDTO = new Gson().
                fromJson(rawResponse, AuthenticationResponseDTO.class);

        if (!authenticationResponseDTO.getStatus().equalsIgnoreCase("Success")) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

//    public MemberListDTO getMemberList(ExternalApiRequestModel externalApiRequestModel, InsuranceAgencyCallBacks insuranceAgencyCallBacks) {
//
//        try {
//            String memberList = insuranceAgencyCallBacks.getMemberList(externalApiRequestModel);
//
//            try {
//                Gson gson = new Gson();
//                requestResponseLogService.saveRequestAndResponseLog(gson.toJson(externalApiRequestModel),
//                        gson.toJson(memberList), externalApiRequestModel.getMemberRecordId(),
//                        RequestResponseLogDTO.RequestResponseIdentifierEnum.GET_MEMBER_LIST);
//            } catch (Exception e) {
//                LOGGER.info("Caught exception while saving the memberList api response as {}", e);
//            }
//            if (memberList == null) {
//                LOGGER.info("Memeber list is null for this unique id :{}", memberList);
////                exceptionLogsService.saveErrorThread(claim,
////                        exceptionLogsService.buildErrorThreadModel("Member_list_null","Member_list_null",
////                                VitrayaAdjudicationFlowStages.EXTERNAL_API_CALL_FOR_MEMBER_DETAILS.toString(),
////                                ClaimErrorThread.FailureType.INSURER_API_ERROR));
//            }
//
//
//            MemberListDTO memberListDTO = null;
//            try {
//                memberListDTO = new Gson().fromJson(memberList, MemberListDTO.class);
//            } catch (Exception e) {
//                LOGGER.info("Niva external Member list api request not successfull. not able to parse");
//                return null;
//            }
//            if (!memberListDTO.getStatus().equalsIgnoreCase("true")) {
//                LOGGER.info("Niva external Member list api request not successfull. error : {}", memberListDTO.getStatusMessage());
//
////                exceptionLogsService.saveErrorThread(claim,
////                        exceptionLogsService.buildErrorThreadModel("Fetch_Member_list_error",memberListDTO.getErrorMsg(),
////                                VitrayaAdjudicationFlowStages.EXTERNAL_API_CALL_FOR_MEMBER_DETAILS.toString(),
////                                ClaimErrorThread.FailureType.INSURER_API_ERROR));
//                return null;
//            }
//
//            return memberListDTO;
//        } catch (Exception e) {
//            LOGGER.info("Exception occured in calling insurer api for policy data");
//            e.printStackTrace();
//
////            exceptionLogsService.saveErrorThread(claim,
////                    exceptionLogsService.buildErrorThreadModel("Fetch_Member_list_exception",e.getLocalizedMessage(),
////                            VitrayaAdjudicationFlowStages.EXTERNAL_API_CALL_FOR_MEMBER_DETAILS.toString(),
////                            ClaimErrorThread.FailureType.CODE_EXCEPTION));
//            return null;
//        }
//    }
//
//    public NivaPolicyDataDTO getPolicyData(Claim claim, ExternalApiRequestModel externalApiRequestModel,
//                                           InsuranceAgencyCallBacks insuranceAgencyCallBacks) throws FileNotFoundException {
//
//        try {
//            String policyDatastr = insuranceAgencyCallBacks.getPolicyData(externalApiRequestModel);
//
//            if (policyDatastr == null) {
//                exceptionLogsService.saveErrorThread(claim,
//                        exceptionLogsService.buildErrorThreadModel("Policy_data_null", "Policy_data_null",
//                                VitrayaAdjudicationFlowStages.EXTERNAL_API_CALL_FOR_MEMBER_DETAILS.toString(),
//                                ClaimErrorThread.FailureType.INSURER_API_ERROR));
//                return null;
//
//            }
//
//            NivaPolicyDataDTO policyDataDTO = null;
//            try {
//                policyDataDTO = new Gson().fromJson(policyDatastr, NivaPolicyDataDTO.class);
//            } catch (Exception e) {
//                LOGGER.info("Niva external policy data api request not successfull. not able to parse ");
//                exceptionLogsService.saveErrorThread(claim,
//                        exceptionLogsService.buildErrorThreadModel("Policy_data_error", policyDatastr,
//                                VitrayaAdjudicationFlowStages.EXTERNAL_API_CALL_FOR_MEMBER_DETAILS.toString(),
//                                ClaimErrorThread.FailureType.INSURER_API_ERROR));
//                return null;
//            }
//
//            if (!policyDataDTO.getStatus().equalsIgnoreCase("true")) {
//                LOGGER.info("Niva external policy data api request not successfull. error : {}", policyDataDTO.getStatusMessage());
//                exceptionLogsService.saveErrorThread(claim,
//                        exceptionLogsService.buildErrorThreadModel("Policy_data_error", policyDataDTO.getStatusMessage(),
//                                VitrayaAdjudicationFlowStages.EXTERNAL_API_CALL_FOR_MEMBER_DETAILS.toString(),
//                                ClaimErrorThread.FailureType.INSURER_API_ERROR));
//
//                return null;
//            }
//
//            return policyDataDTO;
//        } catch (Exception e) {
//            LOGGER.info("Exception occured in calling insurer api for policy data");
//            e.printStackTrace();
//            exceptionLogsService.saveErrorThread(claim,
//                    exceptionLogsService.buildErrorThreadModel("Policy_data_exception", e.getLocalizedMessage(),
//                            VitrayaAdjudicationFlowStages.EXTERNAL_API_CALL_FOR_MEMBER_DETAILS.toString(),
//                            ClaimErrorThread.FailureType.CODE_EXCEPTION));
//
//            return null;
//        }
//
//    }
//
//    public ClaimHistoryDTO getClaimHistory(Claim claim, ExternalApiRequestModel externalApiRequestModel,
//                                           InsuranceAgencyCallBacks insuranceAgencyCallBacks) throws FileNotFoundException {
//        try {
//            String claimHistorystr = insuranceAgencyCallBacks.getClaimHistory(externalApiRequestModel);
//
//            if (claimHistorystr == null) {
//                exceptionLogsService.saveErrorThread(claim,
//                        exceptionLogsService.buildErrorThreadModel("Claim_history_null", "Claim_history_null",
//                                VitrayaAdjudicationFlowStages.EXTERNAL_API_CALL_FOR_MEMBER_DETAILS.toString(),
//                                ClaimErrorThread.FailureType.INSURER_API_ERROR));
//                return null;
//
//            }
//
//            ClaimHistoryDTO claimHistoryDTO =null;
//            try {
//                claimHistoryDTO = new Gson().fromJson(claimHistorystr, ClaimHistoryDTO.class);
//            }catch (Exception e){
//                LOGGER.info("Niva external claim history data api request not successfull. not able to parse");
//                exceptionLogsService.saveErrorThread(claim,
//                        exceptionLogsService.buildErrorThreadModel("Claim_history_error", claimHistorystr,
//                                VitrayaAdjudicationFlowStages.EXTERNAL_API_CALL_FOR_MEMBER_DETAILS.toString(),
//                                ClaimErrorThread.FailureType.INSURER_API_ERROR));
//                return null;
//
//            }
//
//            if (!claimHistoryDTO.getStatus().equalsIgnoreCase("true")) {
//                LOGGER.info("Niva external claim history data api request not successfull. error : {}", claimHistoryDTO.getStatusMessage());
//
//                exceptionLogsService.saveErrorThread(claim,
//                        exceptionLogsService.buildErrorThreadModel("Claim_history_error", claimHistoryDTO.getStatusMessage(),
//                                VitrayaAdjudicationFlowStages.EXTERNAL_API_CALL_FOR_MEMBER_DETAILS.toString(),
//                                ClaimErrorThread.FailureType.INSURER_API_ERROR));
//                return null;
//            }
//
//            return claimHistoryDTO;
//
//        } catch (Exception e) {
//            LOGGER.info("Exception occured in calling insurer api for claim history data");
//            e.printStackTrace();
//            exceptionLogsService.saveErrorThread(claim,
//                    exceptionLogsService.buildErrorThreadModel("Claim_history_exception", e.getLocalizedMessage(),
//                            VitrayaAdjudicationFlowStages.EXTERNAL_API_CALL_FOR_MEMBER_DETAILS.toString(),
//                            ClaimErrorThread.FailureType.CODE_EXCEPTION));
//            return null;
//        }
//
//    }
//
//    public String isTokenAvailable() {
//        JwtTokenEntity jwtTokenEntity = claimTransactionService.getLatestAccessToken();
//
//        if (jwtTokenEntity != null) {
//            String latestAccessToken = jwtTokenEntity.getAccessToken();
//            if (checkTokenValidity(latestAccessToken)) {
//                return latestAccessToken;
//            }
//        }
//
//        return null;
//    }
//
//    public boolean checkTokenValidity(String token) {
//
//        Date now = new Date();
//
//        try {
//            DecodedJWT jwt = JWT.decode(token);
//
//            long exp = jwt.getExpiresAt().getTime();
//
//            long diff = exp - now.getTime();
//
//            if (diff > 60000) {
//
//                return true;
//            } else {
//
//                return false;
//            }
//        } catch (Exception exception) {
//            return false;
//        }
//    }
//
//    public void saveToken(String token) {
//        JwtTokenEntity jwtTokenEntity = new JwtTokenEntity();
//        jwtTokenEntity.setAccessToken(token);
//        try {
//            DecodedJWT jwt = JWT.decode(token);
//            long exp = jwt.getExpiresAt().getTime();
//            jwtTokenEntity.setExpiryTime(exp);
//            System.out.println(jwtTokenEntity);
//            claimTransactionService.saveAccessToken(jwtTokenEntity);
//        } catch (Exception e) {
//            LOGGER.info("Exception in saving access token");
//            return;
//        }
//    }
//
//    public NivaResponseForPolicyLink linkPolicyWithNhcx(PolicyLinkRequestModel policyLinkRequestModel, InsuranceAgencyCallBacks insuranceAgencyCallBacks) throws Exception{
//        try {
//            NivaResponseForPolicyLink nivaResponseForPolicyLink = insuranceAgencyCallBacks.linkPolicyWithNhcx(policyLinkRequestModel);
//
////            if (memberList == null) {
////                LOGGER.info("Memeber list is null for this unique id :{}", memberList);
////                exceptionLogsService.saveErrorThread(claim,
////                        exceptionLogsService.buildErrorThreadModel("Member_list_null","Member_list_null",
////                                VitrayaAdjudicationFlowStages.EXTERNAL_API_CALL_FOR_MEMBER_DETAILS.toString(),
////                                ClaimErrorThread.FailureType.INSURER_API_ERROR));
////            }
//
//
////            NivaResponseForPolicyLink nivaResponseForPolicyLink = null;
//            try {
////                nivaResponseForPolicyLink = new Gson().fromJson(memberList, NivaResponseForPolicyLink.class);
//                return nivaResponseForPolicyLink;
//            } catch (Exception e) {
//                LOGGER.info("Niva external api linking not successfull");
//                return null;
//            }
////            if (!memberListDTO.getStatus().equalsIgnoreCase("true")) {
////                LOGGER.info("Niva external Member list api request not successfull. error : {}", memberListDTO.getStatusMessage());
//
////                exceptionLogsService.saveErrorThread(claim,
////                        exceptionLogsService.buildErrorThreadModel("Fetch_Member_list_error",memberListDTO.getErrorMsg(),
////                                VitrayaAdjudicationFlowStages.EXTERNAL_API_CALL_FOR_MEMBER_DETAILS.toString(),
////                                ClaimErrorThread.FailureType.INSURER_API_ERROR));
////                return null;
////            }
//
//        } catch (Exception e) {
//            LOGGER.info("Niva external api linking not successfull");
//            e.printStackTrace();
//
////            exceptionLogsService.saveErrorThread(claim,
////                    exceptionLogsService.buildErrorThreadModel("Fetch_Member_list_exception",e.getLocalizedMessage(),
////                            VitrayaAdjudicationFlowStages.EXTERNAL_API_CALL_FOR_MEMBER_DETAILS.toString(),
////                            ClaimErrorThread.FailureType.CODE_EXCEPTION));
//            return null;
//        }
//    }
}
