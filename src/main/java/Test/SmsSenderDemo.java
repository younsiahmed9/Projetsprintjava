package Test;

import Models.Budget;
import Services.SmsService;

public class SmsSenderDemo {
    public static void main(String[] args) {
        Budget budget = new Budget();
        budget.setIdBudget(1);
        budget.setNomBudget("Demo Budget");
        budget.setMontantTotal(50.0);

        SmsService smsService = new SmsService();
        SmsService.SmsResult result = smsService.sendBudgetExceededSms(budget, 120.0);
        System.out.println("SMS sent: " + result.isSent() + " | " + result.getUserMessage());
    }
}

