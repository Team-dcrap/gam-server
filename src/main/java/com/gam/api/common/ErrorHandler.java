package com.gam.api.common;


import com.gam.api.common.exception.AuthException;
import com.gam.api.common.exception.AwsException;
import com.gam.api.common.exception.BlockException;
import com.gam.api.common.exception.ReportException;
import com.gam.api.common.exception.ScrapException;
import com.gam.api.common.exception.WorkException;
import com.gam.api.common.message.ExceptionMessage;
import com.gam.api.config.SlackConfig;
import com.slack.api.Slack;
import java.time.LocalDateTime;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.persistence.EntityNotFoundException;

@RestControllerAdvice
@RequiredArgsConstructor
public class ErrorHandler {

    private final Slack slack;
    private final SlackConfig slackConfig;

    /** Internal Server Error + Slack Alert **/
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception exception, HttpServletRequest request) {
        val errorDTO = requestToDTO(exception, request);
        sendSlackAlarm(errorDTO.toString());

        ApiResponse response = ApiResponse.serverError(errorDTO);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException exception, HttpServletRequest request) {
        val errorDTO = requestToDTO(exception, request);
        sendSlackAlarm(errorDTO.toString());

        ApiResponse response = ApiResponse.serverError(errorDTO);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    /** Custom Error + 4__ Error Handler **/
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse> handleEntityNotFoundException(EntityNotFoundException exception) {
        ApiResponse response = ApiResponse.fail(exception.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgumentException(IllegalArgumentException exception) {
        ApiResponse response = ApiResponse.fail(exception.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception){
        ApiResponse response = ApiResponse.fail(exception.getBindingResult().getAllErrors().get(0).getDefaultMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiResponse> authException(AuthException exception){
        ApiResponse response = ApiResponse.fail(exception.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AwsException.class)
    public ResponseEntity<ApiResponse> awsException(AwsException exception){
        ApiResponse response = ApiResponse.fail(exception.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(WorkException.class)
    public ResponseEntity<ApiResponse> workException(WorkException exception){
        ApiResponse response = ApiResponse.fail(exception.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BlockException.class)
    public ResponseEntity<ApiResponse> BlockException(BlockException exception){
        ApiResponse response = ApiResponse.fail(exception.getMessage());
        return new ResponseEntity<>(response, exception.getStatusCode());
    }

    @ExceptionHandler(ScrapException.class)
    public ResponseEntity<ApiResponse> ScrapException(ScrapException exception) {
        ApiResponse response = ApiResponse.fail(exception.getMessage());
        return new ResponseEntity<>(response, exception.getStatusCode());
    }

    @ExceptionHandler(ReportException.class)
    public ResponseEntity<ApiResponse> ReportException(ReportException exception){
        ApiResponse response = ApiResponse.fail(exception.getMessage());
        return new ResponseEntity<>(response, exception.getStatusCode());
    }

    private InternalServerErrorDTO requestToDTO(Exception exception, HttpServletRequest request) {
        val header = InternalServerErrorDTO.extractHeaders(request);
        return InternalServerErrorDTO.of(header, request.getMethod(),  request.getRequestURL().toString(),
                exception.getMessage(), exception.getClass().getName(), LocalDateTime.now());
    }

    private void sendSlackAlarm(String dto) {
        try{
            val slackResponse = slack.send(slackConfig.getUrl(), SlackErrorPayload.of(dto)).getBody();

            if(!slackResponse.equals("ok")) {
                throw new Exception(ExceptionMessage.NOT_POST_SLACK_ALARM.getMessage());// todo log -> 슬랙알림 안감
            }
        }catch (Exception exception) {
            System.out.println(exception.toString()); // todo log
        }
    }

}