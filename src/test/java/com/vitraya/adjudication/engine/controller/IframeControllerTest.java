//package com.vitraya.adjudication.engine.controller;
//
//import com.vitraya.adjudication.engine.dto.request.InsurerEncryptedClaimRequest;
//import com.vitraya.adjudication.engine.dto.request.TariffLineItemUpdateRequest;
//import com.vitraya.adjudication.engine.dto.request.UpdateDecisionRequest;
//import com.vitraya.adjudication.engine.dto.response.ClaimDetailsResponseDTO;
//import com.vitraya.adjudication.engine.dto.response.RestAPIResponse;
//import com.vitraya.adjudication.engine.dto.response.TariffLineItemResponseDTO;
//import com.vitraya.adjudication.engine.helper.VitrayaException;
//import com.vitraya.adjudication.engine.service.ClaimCommonService;
//import com.vitraya.adjudication.engine.service.ClaimService;
//import com.vitraya.adjudication.engine.service.IframeService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.http.HttpStatus;
//
//import java.io.IOException;
//import java.util.Collections;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.anyString;
//import static org.mockito.Mockito.when;
//
//public class IframeControllerTest {
//    @Mock
//    private ClaimService claimService;
//
//    @Mock
//    private IframeService iframeService;
//
//    @Mock
//    private ClaimCommonService claimCommonService;
//
//    @InjectMocks
//    private IframeController iframeController;
//
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    /*@Test
//    void testGetClaimDetails_Success() throws Exception {
//        String encryptedData = "encryptedData";
//        InsurerEncryptedClaimRequest request = new InsurerEncryptedClaimRequest();
//        request.setVitrayaClaimId("claimId");
//        ClaimDetailsResponseDTO responseDTO = new ClaimDetailsResponseDTO();
//        when(iframeService.getDecryptedIframeData(anyString())).thenReturn(request);
//        when(claimService.prepareClaimDataByIntimationNumber(anyString())).thenReturn(responseDTO);
//        RestAPIResponse response = iframeController.getClaimDetails(encryptedData);
//        assertEquals(HttpStatus.OK.value(), response.getStatus());
//        assertEquals(responseDTO, response.getData());
//    }*/
//
//    @Test
//    void testGetClaimDetails_Fail() throws Exception {
//        String encryptedData = "encryptedData";
//        InsurerEncryptedClaimRequest request = new InsurerEncryptedClaimRequest();
//        request.setVitrayaClaimId("claimId");
//        when(iframeService.getDecryptedIframeData(anyString())).thenReturn(request);
//        when(claimService.prepareClaimDataByIntimationNumber(anyString())).thenReturn(null);
//        RestAPIResponse response = iframeController.getClaimDetails(encryptedData);
//        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
//    }
//
//    @Test
//    void testUpdateClaimTariffDataById_Success() throws VitrayaException, IOException {
//        TariffLineItemUpdateRequest request = new TariffLineItemUpdateRequest();
//        List<TariffLineItemResponseDTO> responseDTOList = Collections.singletonList(new TariffLineItemResponseDTO());
//        when(claimService.addUpdateTariffLineItems(any(TariffLineItemUpdateRequest.class))).thenReturn(responseDTOList);
//        RestAPIResponse response = iframeController.updateClaimTariffDataById(request);
//        assertEquals(HttpStatus.OK.value(), response.getStatus());
//        assertEquals(responseDTOList, response.getData());
//    }
//
//    @Test
//    void testUpdateClaimTariffDataById_Fail() throws VitrayaException, IOException {
//        TariffLineItemUpdateRequest request = new TariffLineItemUpdateRequest();
//        when(claimService.addUpdateTariffLineItems(any(TariffLineItemUpdateRequest.class))).thenReturn(Collections.emptyList());
//        RestAPIResponse response = iframeController.updateClaimTariffDataById(request);
//        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
//    }
//
//    @Test
//    void testUpdateDecision_Success() throws VitrayaException {
//        UpdateDecisionRequest request = new UpdateDecisionRequest();
//        request.setClaimDataId(1L);
//        when(claimService.updateDecision(any(UpdateDecisionRequest.class))).thenReturn(true);
//        RestAPIResponse response = iframeController.updateDecision(request);
//        assertEquals(HttpStatus.OK.value(), response.getStatus());
//    }
//
//    @Test
//    void testUpdateDecision_Fail() throws VitrayaException {
//        UpdateDecisionRequest request = new UpdateDecisionRequest();
//        request.setClaimDataId(1L);
//        when(claimService.updateDecision(any(UpdateDecisionRequest.class))).thenReturn(false);
//        RestAPIResponse response = iframeController.updateDecision(request);
//        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
//    }
//
//    @Test
//    void testPushDecisionToInsurer_Success() throws VitrayaException {
//        long claimId = 1L;
//        when(claimService.pushClaimDecision(claimId)).thenReturn(true);
//        RestAPIResponse response = iframeController.pushDecisionToInsurer(String.valueOf(claimId));
//        assertEquals(HttpStatus.OK.value(), response.getStatus());
//    }
//
//    @Test
//    void testPushDecisionToInsurer_Fail() throws VitrayaException {
//        long claimId = 1L;
//        when(claimService.pushClaimDecision(claimId)).thenReturn(false);
//        RestAPIResponse response = iframeController.pushDecisionToInsurer(String.valueOf(claimId));
//        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
//    }
//}
