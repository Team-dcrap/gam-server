package com.gam.api.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InternalServerErrorDTO {
    private String header = null;
    private String httpMethod = null;
    private String URL = null ;
    private String message = null;

    private String errorClass = null;

    private String dateTime = "";

    @Builder
    private InternalServerErrorDTO(String header, String httpMethod, String URL, String message,
                                   String errorClass, String dateTime) {
        this.header = header;
        this.httpMethod = httpMethod;
        this.URL = URL;
        this.message = message;
        this.errorClass = errorClass;
        this.dateTime = dateTime;
    }

    public static InternalServerErrorDTO of(String header, String httpMethod, String URL, String message,
                                            String errorClass, LocalDateTime nowDateTime) {
        if(Objects.isNull(nowDateTime)) {
            nowDateTime = LocalDateTime.now();
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String now = nowDateTime.format(formatter);

        return InternalServerErrorDTO.builder()
                .header(header)
                .httpMethod(httpMethod)
                .URL(URL)
                .message(message)
                .errorClass(errorClass)
                .dateTime(now)
                .build();
    }

    public static String extractHeaders(HttpServletRequest request) {
        StringBuilder headers = new StringBuilder();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            headers.append(headerName).append(": ").append(headerValue);
        }
        return headers.toString();
    }

    @Override
    public String toString() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
        String jsonString = null;
        try {
            jsonString = writer.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e); //todo Log
        }
        return jsonString;
    }

}
