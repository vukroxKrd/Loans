package com.virtualpairprogrammers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class LoanCalculatorController {

    @Autowired
    private LoanRepository data;

    @Autowired
    private JavaMailSender mailSender;

    private RestTemplate restTemplate = new RestTemplate();

    // Render the form
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView renderNewLoanForm() {
        LoanApplication loan = new LoanApplication();
        return new ModelAndView("newApplication", "form", loan);
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public ModelAndView processNewLoanApplication(LoanApplication loan) {
        data.save(loan);

        URI location = restTemplate.postForLocation("http://loans.virtualpairprogrammers.com/loanApplication", loan); //this line sends the loan for approval request, which could take up to 24 hours

        //step 1 - calc applicableRate
        BigDecimal applicableRate = calcCorrectApplicableRate(loan);

        //step 2 - calc total sum with interest rates in 2 years
        BigDecimal totalRepayable = totalRepayableCalc(loan, applicableRate);

        //step 3 - calc monthly payment of a customer
        BigDecimal repayment = totalRepayable.divide(new BigDecimal("" + loan.getTermInMonths()), RoundingMode.UP);
        loan.setRepayment(repayment);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(loan.getName());
        message.setSubject("Thank you for your loan application.");
        message.setText("We're currently processing your request, and will send you a further email when we have a decision.");
        mailSender.send(message);

        return new ModelAndView("requestAccepted");
    }

    BigDecimal totalRepayableCalc(LoanApplication loan, BigDecimal applicableRate) {
        BigDecimal result = new BigDecimal(loan.getPrincipal() * Double.parseDouble(applicableRate.toString()));
        return result;
    }

    BigDecimal calcCorrectApplicableRate(LoanApplication appeal) {
        final BigDecimal YEAR_BIGDEC = new BigDecimal("12");
        final BigDecimal ONE_HUNDRED = new BigDecimal("100.0");

        BigDecimal interestRate = appeal.getInterestRate();
        BigDecimal applicableRate = BigDecimal
                .valueOf(appeal.getTermInMonths())
                .divide(YEAR_BIGDEC)
                .multiply(interestRate.divide(ONE_HUNDRED))
                .add(BigDecimal.ONE);
        return applicableRate;
    }


    //Set methods used for testing only
    public void setData(LoanRepository data) {
        this.data = data;
    }

    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


}
