package com.virtualpairprogrammers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Spy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

public class PaymentsAndCalculationTests {

    @Spy
    LoanApplication loanApplication;
    LoanCalculatorController controller;

    @Before
    public void setup() {
        /**
         * dummy -> to create dummies methods of which we will be allowed to rewrite
         * */
        loanApplication = spy(new LoanApplication());
        controller = new LoanCalculatorController();

        //mock -> to create dummies
        LoanRepository repository = mock(LoanRepository.class);
        JavaMailSender mailSender = mock(JavaMailSender.class);
        RestTemplate restTemplate = mock(RestTemplate.class);

        controller.setData(repository);
        controller.setMailSender(mailSender);
        controller.setRestTemplate(restTemplate);
    }

    @Test
    public void test1YearLoanWholePounds() {

        loanApplication.setId(1);
        loanApplication.setName("two years loan");
        loanApplication.setPrincipal(1200);
        loanApplication.setTermInMonths(12);
        //to override only 1 method using Spy annot
        doReturn(new BigDecimal("10.0")).when(loanApplication).getInterestRate();

        controller.processNewLoanApplication(loanApplication);

        Assert.assertEquals(new BigDecimal("110"), loanApplication.getRepayment());
    }

    @Test
    public void test2YearLoanWholePounds() {
        loanApplication.setId(1);
        loanApplication.setName("two years loan");
        loanApplication.setPrincipal(1200);
        loanApplication.setTermInMonths(24);
        //to override only 1 method using Spy annot
        doReturn(new BigDecimal("10.0")).when(loanApplication).getInterestRate();

        controller.processNewLoanApplication(loanApplication);

        Assert.assertEquals(new BigDecimal("60"), loanApplication.getRepayment());
    }

    @Test
    public void test5YearLoanWholePounds() {
        loanApplication.setId(1);
        loanApplication.setName("two years loan");
        loanApplication.setPrincipal(5000);
        loanApplication.setTermInMonths(60);
        //to override only 1 method using Spy annot
        doReturn(new BigDecimal("6.5")).when(loanApplication).getInterestRate();

        controller.processNewLoanApplication(loanApplication);

        Assert.assertEquals(new BigDecimal("111"), loanApplication.getRepayment());
    }

    @Test
    public void calculateCorrectLoanRepaymentAmountCalculationTest() {

        loanApplication.setId(1);
        loanApplication.setName("two years loan");
        loanApplication.setPrincipal(1000);
        loanApplication.setTermInMonths(24);
        //to override only 1 method using Spy annot
        doReturn(new BigDecimal("7.0")).when(loanApplication).getInterestRate();

        BigDecimal applicableRate = controller.calcCorrectApplicableRate(loanApplication);
        BigDecimal total = controller.totalRepayableCalc(loanApplication, applicableRate);
        Assert.assertEquals(new BigDecimal("1140"), total);
    }

    @Test
    public void calculateCorrectApplicableRateTest() {
        loanApplication.setId(1);
        loanApplication.setName("two years loan");
        loanApplication.setPrincipal(1000);
        loanApplication.setTermInMonths(24);
        doReturn(new BigDecimal("7.0")).when(loanApplication).getInterestRate();

        BigDecimal applicableRate = controller.calcCorrectApplicableRate(loanApplication);
        Assert.assertEquals(new BigDecimal("1.14"), applicableRate);

    }
}
