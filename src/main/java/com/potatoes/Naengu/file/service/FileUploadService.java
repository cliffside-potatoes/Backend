package com.potatoes.Naengu.file.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.potatoes.Naengu.file.dto.PresignedUrlResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadService {
    @Autowired
    private AmazonS3 amazonS3;

    // Presigned URL을 통해 업로드되는 파일의 이름을 저장하는 변수
    private String useOnlyOneFileName;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket; // S3 버킷 이름

    @Value("${cloud.aws.region.static}")
    private String location; // S3 리전 (Region)

    /**
     * Presigned URL을 생성하는 메서드
     * @param prefix 파일을 저장할 경로 (예: "images", "documents")
     * @param fileName 사용자가 업로드하려는 원본 파일명
     * @return Presigned URL 문자열 (PUT 요청)
     */
    public PresignedUrlResponseDTO getPreSignedUrl(String prefix, String fileName) {

        // 파일 이름을 UUID를 추가한 유니크한 이름으로 변환
        String s3Key = onlyOneFileName(fileName); //

        // prefix(폴더 경로)가 있을 경우 경로를 포함한 파일명 설정
        if (!prefix.equals("")) {
            s3Key = prefix + "/" + s3Key;
        }

        // Presigned URL 생성 요청 객체 생성
        GeneratePresignedUrlRequest generatePresignedUrlRequest = getGeneratePreSignedUrlRequest(bucket, s3Key);

        URL presignedUrl = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);

        // AWS S3에서 Presigned URL 생성 후 반환
        return new PresignedUrlResponseDTO(presignedUrl.toString() , s3Key);
    }

    /**
     * Presigned URL 요청을 생성하는 메서드
     * @param bucket 버킷 이름
     * @param fileName 업로드될 파일의 S3 경로
     * @return GeneratePresignedUrlRequest 객체
     */
    private GeneratePresignedUrlRequest getGeneratePreSignedUrlRequest(String bucket, String fileName) {
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucket, fileName)
                        .withMethod(HttpMethod.PUT) // PUT 요청으로 업로드 가능
                        .withExpiration(getPreSignedUrlExpiration()); // Presigned URL 만료 시간 설정


        // 최대 파일 크기 제한 설정 (바이트 단위)
        generatePresignedUrlRequest.addRequestParameter("Content-Length", String.valueOf(20 * 1024 * 1024));

        return generatePresignedUrlRequest;
    }

    /**
     * Presigned URL의 만료 시간을 설정하는 메서드 (현재 시간 기준 2분 후 만료)
     * @return 만료 시간 (Date 객체)
     */
    private Date getPreSignedUrlExpiration() {
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * 2; // 현재 시간에서 2분 추가
        expiration.setTime(expTimeMillis);
        log.info(expiration.toString()); // 로그로 만료 시간 출력
        return expiration;
    }


    /**
     * 파일명을 UUID를 추가한 유니크한 이름으로 변환하는 메서드
     * @param filename 원본 파일명
     * @return 변경된 파일명 (UUID + 원본 파일명)
     */
    private String onlyOneFileName(String filename) {
        return UUID.randomUUID().toString() + filename;
    }


}
