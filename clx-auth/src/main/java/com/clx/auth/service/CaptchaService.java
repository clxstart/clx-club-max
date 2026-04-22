package com.clx.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 图形验证码服务。
 *
 * <p>生成 4 位随机验证码图片，存储到 Redis，返回 base64 格式图片。
 *
 * <p>功能：
 * <ul>
 *   <li>生成 4 位验证码（数字+字母）</li>
 *   <li>绘制干扰线、噪点</li>
 *   <li>输出 base64 格式 PNG 图片</li>
 *   <li>验证码存储到 Redis，5 分钟过期</li>
 * </ul>
 */
@Slf4j
@Service
public class CaptchaService {

    private final StringRedisTemplate redisTemplate;

    /** 验证码字符集（去除了易混淆的 0/O、1/I/l） */
    private static final String CODE_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";

    /** 验证码长度 */
    private static final int CODE_LENGTH = 4;

    /** 验证码有效期（秒） */
    private static final long CAPTCHA_EXPIRE_SECONDS = 600L;

    /** 图片宽度 */
    private static final int IMAGE_WIDTH = 120;

    /** 图片高度 */
    private static final int IMAGE_HEIGHT = 40;

    /** 干扰线数量 */
    private static final int INTERFERENCE_LINE_COUNT = 6;

    /** 噪点数量 */
    private static final int NOISE_DOT_COUNT = 40;

    private final Random random = new Random();

    public CaptchaService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 生成图形验证码。
     *
     * @return 验证码 ID 和 base64 图片
     */
    public CaptchaResult generateCaptcha() {
        String captchaId = UUID.randomUUID().toString();
        String code = generateCode();
        String captchaImage = generateCaptchaImage(code);

        // 保存到 Redis
        String key = getCaptchaKey(captchaId);
        redisTemplate.opsForValue().set(key, code, CAPTCHA_EXPIRE_SECONDS, TimeUnit.SECONDS);

        log.info("生成图形验证码: captchaId={}", captchaId);

        return new CaptchaResult(captchaId, captchaImage);
    }

    /**
     * 验证图形验证码。
     *
     * @param captchaId 验证码ID
     * @param code 用户输入的验证码
     * @return true 如果验证码正确且未过期
     */
    public boolean verifyCaptchaCode(String captchaId, String code) {
        String key = getCaptchaKey(captchaId);
        String storedCode = redisTemplate.opsForValue().get(key);

        if (storedCode == null) {
            log.warn("图形验证码已过期或不存在: captchaId={}", captchaId);
            return false;
        }

        boolean valid = storedCode.equalsIgnoreCase(code);
        if (valid) {
            redisTemplate.delete(key);
            log.info("图形验证码验证成功: captchaId={}", captchaId);
        } else {
            log.warn("图形验证码验证失败: captchaId={}", captchaId);
        }

        return valid;
    }

    /**
     * 生成随机验证码。
     */
    private String generateCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
        }
        return code.toString();
    }

    /**
     * 生成验证码图片并转为 base64。
     */
    private String generateCaptchaImage(String code) {
        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 背景
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);

        // 边框
        g.setColor(new Color(200, 200, 200));
        g.drawRect(0, 0, IMAGE_WIDTH - 1, IMAGE_HEIGHT - 1);

        // 干扰线
        for (int i = 0; i < INTERFERENCE_LINE_COUNT; i++) {
            g.setColor(randomColor(160, 200));
            g.drawLine(
                    random.nextInt(IMAGE_WIDTH),
                    random.nextInt(IMAGE_HEIGHT),
                    random.nextInt(IMAGE_WIDTH),
                    random.nextInt(IMAGE_HEIGHT)
            );
        }

        // 噪点
        for (int i = 0; i < NOISE_DOT_COUNT; i++) {
            g.setColor(randomColor(100, 200));
            g.fillOval(
                    random.nextInt(IMAGE_WIDTH),
                    random.nextInt(IMAGE_HEIGHT),
                    2, 2
            );
        }

        // 绘制验证码字符
        int charWidth = (IMAGE_WIDTH - 20) / CODE_LENGTH;
        for (int i = 0; i < code.length(); i++) {
            g.setColor(randomColor(30, 130));
            g.setFont(new Font("Arial", Font.BOLD, 28 + random.nextInt(6)));
            int x = 10 + i * charWidth + random.nextInt(5);
            int y = 30 + random.nextInt(6) - 3;
            g.drawString(String.valueOf(code.charAt(i)), x, y);
        }

        g.dispose();

        // 转 base64
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            log.error("验证码图片生成失败", e);
            throw new RuntimeException("验证码图片生成失败", e);
        }
    }

    /**
     * 生成随机颜色。
     */
    private Color randomColor(int min, int max) {
        int r = min + random.nextInt(max - min);
        int g = min + random.nextInt(max - min);
        int b = min + random.nextInt(max - min);
        return new Color(r, g, b);
    }

    /**
     * 获取验证码 Redis key。
     */
    private String getCaptchaKey(String captchaId) {
        return "captcha:" + captchaId;
    }

    /**
     * 验证码记录类。
     */
    public record CaptchaResult(String captchaId, String captchaImage) {}
}
