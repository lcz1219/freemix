package com.freemix.freemix.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;

public class CaptchaUtil {
    
    private static final int WIDTH = 120; // 验证码图片宽度
    private static final int HEIGHT = 40; // 验证码图片高度
    private static final int CODE_COUNT = 4; // 验证码字符个数
    private static final int FONT_SIZE = 20; // 字体大小
    private static final char[] CODE_SEQUENCE = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
        'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
        'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3',
        '4', '5', '6', '7', '8', '9'
    };
    
    /**
     * 生成验证码图片和文本
     * @return 验证码结果对象
     */
    public static CaptchaResult generateCaptcha() {
        BufferedImage buffImg = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = buffImg.createGraphics();
        
        // 将图像填充为白色
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        
        // 创建字体
        Font font = new Font("Fixedsys", Font.BOLD, FONT_SIZE);
        g.setFont(font);
        
        // 绘制边框
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, WIDTH - 1, HEIGHT - 1);
        
        // 随机产生40条干扰线
        g.setColor(Color.BLACK);
        Random random = new Random();
        for (int i = 0; i < 40; i++) {
            int x = random.nextInt(WIDTH);
            int y = random.nextInt(HEIGHT);
            int xl = random.nextInt(12);
            int yl = random.nextInt(12);
            g.drawLine(x, y, x + xl, y + yl);
        }
        
        // 生成随机验证码
        StringBuffer randomCode = new StringBuffer();
        for (int i = 0; i < CODE_COUNT; i++) {
            String strRand = String.valueOf(CODE_SEQUENCE[random.nextInt(CODE_SEQUENCE.length)]);
            g.setColor(new Color(20 + random.nextInt(110), 20 + random.nextInt(110), 20 + random.nextInt(110)));
            g.drawString(strRand, 25 * i + 10, 25);
            randomCode.append(strRand);
        }
        
        g.dispose();
        
        // 转换为Base64字符串
        String base64String = "";
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(buffImg, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            base64String = Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return new CaptchaResult(randomCode.toString(), base64String);
    }
    
    /**
     * 验证码结果类
     */
    public static class CaptchaResult {
        private String code;
        private String imageBase64;
        
        public CaptchaResult(String code, String imageBase64) {
            this.code = code;
            this.imageBase64 = imageBase64;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getImageBase64() {
            return imageBase64;
        }
    }
}