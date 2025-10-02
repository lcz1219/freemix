package com.freemix.freemix.util;

import java.util.Random;

public class CaptchaUtil {
    
    /**
     * 生成加减乘除计算验证码
     * @return 验证码结果对象
     */
    public static CaptchaResult generateCaptcha() {
        Random random = new Random();
        
        // 生成两个1-99之间的随机数
        int num1 = random.nextInt(10) + 1;
        int num2 = random.nextInt(10) + 1;
        
        // 随机选择运算符
        String[] operators = {"+", "-", "*", "/"};
        String operator = operators[random.nextInt(operators.length)];
        
        // 计算结果
        int result;
        String expression;
        
        switch (operator) {
            case "+":
                result = num1 + num2;
                expression = num1 + " + " + num2;
                break;
            case "-":
                // 确保结果为正数
                if (num1 < num2) {
                    int temp = num1;
                    num1 = num2;
                    num2 = temp;
                }
                result = num1 - num2;
                expression = num1 + " - " + num2;
                break;
            case "*":
                result = num1 * num2;
                expression = num1 + " × " + num2;
                break;
            case "/":
                // 确保能整除
                result = num1;
                num1 = num1 * num2;
                expression = num1 + " ÷ " + num2;
                break;
            default:
                result = num1 + num2;
                expression = num1 + " + " + num2;
        }
        
        return new CaptchaResult(String.valueOf(result), expression);
    }
    
    /**
     * 验证码结果类
     */
    public static class CaptchaResult {
        private String code;
        private String expression;
        
        public CaptchaResult(String code, String expression) {
            this.code = code;
            this.expression = expression;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getExpression() {
            return expression;
        }
    }
}