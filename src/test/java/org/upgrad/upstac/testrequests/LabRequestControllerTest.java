package org.upgrad.upstac.testrequests;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.web.server.ResponseStatusException;
import org.upgrad.upstac.testrequests.lab.CreateLabResult;
import org.upgrad.upstac.testrequests.lab.LabRequestController;
import org.upgrad.upstac.testrequests.lab.TestStatus;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest
@Slf4j
class LabRequestControllerTest {


    @Autowired
    LabRequestController labRequestController;




    @Autowired
    TestRequestQueryService testRequestQueryService;


    @Test
    @WithUserDetails(value = "tester")
    public void calling_assignForLabTest_with_valid_test_request_id_should_update_the_request_status(){

        TestRequest testRequest = getTestRequestByStatus(RequestStatus.INITIATED);

        TestRequest updateTestResultRequest = labRequestController.assignForLabTest(testRequest.getRequestId());
        assertThat(updateTestResultRequest.getRequestId(), equalTo(testRequest.getRequestId()));
        assertThat(updateTestResultRequest.getStatus(),equalTo(RequestStatus.LAB_TEST_IN_PROGRESS));
        assertNotNull(updateTestResultRequest.getLabResult());
    }

    public TestRequest getTestRequestByStatus(RequestStatus status) {
        return testRequestQueryService.findBy(status).stream().findFirst().get();
    }

    @Test
    @WithUserDetails(value = "tester")
    public void calling_assignForLabTest_with_valid_test_request_id_should_throw_exception(){

        Long InvalidRequestId= -34L;

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,()->{
            labRequestController.assignForLabTest(InvalidRequestId);
        });
        assertThat(exception.getMessage(),containsString("Invalid ID"));
    }

    @Test
    @WithUserDetails(value = "tester")
    public void calling_updateLabTest_with_valid_test_request_id_should_update_the_request_status_and_update_test_request_details(){

        TestRequest testRequest = getTestRequestByStatus(RequestStatus.LAB_TEST_IN_PROGRESS);
        CreateLabResult labResult = getCreateLabResult(testRequest);

        TestRequest updateTestRequest = labRequestController.updateLabTest(testRequest.getRequestId(),labResult);
        assertThat(updateTestRequest.getRequestId(),equalTo(testRequest.getRequestId()));
        assertThat(updateTestRequest.getStatus(),equalTo(RequestStatus.LAB_TEST_COMPLETED));
        assertThat(updateTestRequest.getLabResult().getResult(),equalTo(labResult.getResult()));
    }


    @Test
    @WithUserDetails(value = "tester")
    public void calling_updateLabTest_with_invalid_test_request_id_should_throw_exception(){

        TestRequest testRequest = getTestRequestByStatus(RequestStatus.LAB_TEST_IN_PROGRESS);
        CreateLabResult labResult = getCreateLabResult(testRequest);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,()->{
            labRequestController.updateLabTest(-1L,labResult);
        });
        assertThat(exception.getMessage(),containsString("Invalid ID"));
    }

    @Test
    @WithUserDetails(value = "tester")
    public void calling_updateLabTest_with_invalid_empty_status_should_throw_exception(){

        TestRequest testRequest = getTestRequestByStatus(RequestStatus.LAB_TEST_IN_PROGRESS);
        CreateLabResult labResult = getCreateLabResult(testRequest);
        labResult.setResult(null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,()->{
            labRequestController.updateLabTest(testRequest.getRequestId(),labResult);
        });
        assertThat(exception.getMessage(),containsString("ConstraintViolationException"));
    }

    public CreateLabResult getCreateLabResult(TestRequest testRequest) {

        CreateLabResult labResult = new CreateLabResult();
        labResult.setBloodPressure("120/92");
        labResult.setComments("Asymptomatic. No detection.");
        labResult.setHeartBeat("92/94");
        labResult.setOxygenLevel("87-94");

        labResult.setTemperature("103");
        labResult.setComments("Patient is healthy");
        labResult.setResult(TestStatus.NEGATIVE);

        return labResult; // Replace this line with your code
    }

}