package com.vitraya.adjudication.engine.controller;

import com.vitraya.adjudication.engine.annotation.SkipResponseLogging;
import com.vitraya.adjudication.engine.dto.enums.VitrayaErrorCodes;
import com.vitraya.adjudication.engine.dto.response.ClaimDetailsResponseDTO;
import com.vitraya.adjudication.engine.dto.response.RestAPIResponse;
import com.vitraya.adjudication.engine.service.ClaimService;
import com.vitraya.adjudication.engine.service.S3FileService;
import com.vitraya.adjudication.engine.utils.EncryptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/document")
public class DocumentController {
    private final EncryptionUtils encryptionUtils;
    private final S3FileService s3FileService;
    private final ClaimService claimService;


    @Value("${API_PARAM_ENCRYPTION_DECRYTION_KEY}")
    private String apiParamEncryptiondecryptionKey;

    // Constructor to inject dependencies
    public DocumentController(EncryptionUtils encryptionUtils, S3FileService s3FileService, ClaimService claimService) {
        this.encryptionUtils = encryptionUtils;
        this.s3FileService = s3FileService;
        this.claimService = claimService;
    }

    /**
     * Method to retrieve a document by its encrypted ID. The method decrypts the document ID,
     * fetches the document from the S3 bucket, and returns it as a PDF response.
     * This allows the document to be displayed in the browser using Angular's pdf.js.
     *
     * @param token The encrypted ID of the document to retrieve.
     * @return A ResponseEntity containing the document content as application/pdf.
     */
    @GetMapping("/fetch/")
    @SkipResponseLogging
    public ResponseEntity<byte[]> fetchDocument(@RequestParam("token") String token) {
        try {
            // Decrypt the document ID to retrieve the S3 URL
            token = URLDecoder.decode(token, "UTF-8").replaceAll(" ", "+");
            String s3Url = URLDecoder.decode(encryptionUtils.decrypt(token, EncryptionUtils.INTERNAL_SECREAT_KEY), "UTF-8");

            // Fetch the document from S3 using the S3 service
            byte[] documentContent = s3FileService.getFileFromS3(s3Url);

            // Prepare the response headers with application/pdf content type
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf");

            // Return the document content in the response
            return new ResponseEntity<>(documentContent, headers, HttpStatus.OK);
        } catch (Exception e) {
            // Log the error and return an internal server error response
            log.error("Error fetching document: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error fetching document: " + e.getMessage()).getBytes());
        }
    }


    public static void main(String[] args) throws UnsupportedEncodingException {
//        String token = "fFFdH9V9dKLYfwc3O1E5PxeDwlJYomDifbgpNSs%2FCA5ZtBIwKRVFi9cWZuCxImAxydj0aZiCWG8bviBCkTfhCqAqw0o7I5njsIauXUXf69NMnTbIDrPSJPvtx4umeSrWmusEzRbcGEc1XlQaFiGj52nDHRJbbAqYwM8nXvRfy0WBFo1MtJ5WXpKzFtritQLne42HM5UOYf4xDobML%2FSa4NR%2BBIwuAtp8sgkBYV%2FTMSNA0UIG8zxTK%2F%2Fvqe1lrkgQxutcrmGiEShxDw3CWTo8bWTd3sir%2Fv07QYPdU70WDbo2v4ZcvWcW0SeQjMJxbMgeASgRuxgut3Zwy8E38sHesRoBsorvosOfe7GYFNV5NiOpY15dvM3ISjQuZosuvn9HYRlTH4tsh0fZZoLtQR51To9nRsfyIZni4A6F29qN3dysaHjhrJI9NwlcsIbHf%2B%2BvIv4uXU%2Bb8jJiQRlfebsnZVVzs1ETNrzyOZiNhvxi3ESVkH2boFwsiq%2B5UAQlbv2%2FSIC73UIXGOPgRbyUxqR5kOlfV8CZpbnscFY8ExFu1Ed1E3h%2BulhnEcl8RE1wePyUvG3GyBO8XY9GI94CPIP1jQ%3D%3D";
        String token = "fFFdH9V9dKLYfwc3O1E5PxeDwlJYomDifbgpNSs/CA5ZtBIwKRVFi9cWZuCxImAxydj0aZiCWG8bviBCkTfhCuQPP1lkPlW5Lq98FdQPx7r/KeSislZWGzkJ2bBe8/nAGn9D6hUogozCBY2aJmo20QXx32IW0lD8g4o7XN+SR50okDnnCfFwauGXoJikVtUIxhQcLzZp6gOCuLKYGL3d7b9/tZOi5vr+M761VHXU2Sh/kqo4peJtzRsLVy6FCmbyovnmwdXCFwB1xVn7kyxo104VyKdH+0bvDkZLyQ0UjEWWMJl7jEL6BRGLm9Xf0NMKQIRRwZaetLdmTeIhq/lVOnhVhsjt57D29eYu/hWhgDziNAGHBfLYo9+0q+kiyx4igfMQkjOsgy0nYk++T2qZdtNjzBYNJLSWPJD9QEWXEjDupYzEU+y97jDis9l7QGWqBWAA/zgmcQMbbsi5WtY9MaDu6gCnd7lX2STqvXoxqH/kq8YTjWzH3HtHenZyCC+EiedZbWG+XjBAW6JiZA+Hr92CgUPJOD7qPrLsavGJ/lMBLqfSJKQ8Z/BEp/S+ofGHKNBXNubBuF+lcoya8p842aHZWG+USZmXZ16/HfQwcUXaxp2iA1o3f7s0GAkSt7GNAIHYN1Z5SmCDc2AaFBMYtw==";
        token = URLDecoder.decode(token, "UTF-8").replaceAll(" ", "+");
        EncryptionUtils encryptionUtils = new EncryptionUtils();
        String s3Url = URLDecoder.decode(encryptionUtils.decrypt(token, EncryptionUtils.INTERNAL_SECREAT_KEY), "UTF-8");
        System.out.println(s3Url);

    }
}
