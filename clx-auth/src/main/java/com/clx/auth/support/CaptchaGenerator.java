package com.clx.auth.support;

import com.clx.auth.vo.CaptchaVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 图形验证码生成器
 * <p>
 * 生成带干扰线/噪点的验证码图片，存 Redis，返回 base64 格式供前端展示
 */
@Slf4j
@Component
public class CaptchaGenerator {

    /** 验证码字符集（去掉了易混淆的 0/O/1/I/l） */
    private static final String CODE_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final int CODE_LENGTH = 4;
    private static final long EXPIRE_SECONDS = 600L;  // 10分钟有效
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;

    private final StringRedisTemplate redis;
    private final Random random = new Random();

    public CaptchaGenerator(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /** 生成验证码，返回 ID（存Redis用）和 base64 图片（前端展示用） */
    public CaptchaVO generate() {
        String id = UUID.randomUUID().toString();
        String code = generateCode();
        String image = generateImage(code);

        redis.opsForValue().set("captcha:" + id, code, EXPIRE_SECONDS, TimeUnit.SECONDS);
        log.info("生成图形验证码: id={}", id);
        return new CaptchaVO(id, image);
    }

    /** 验证码是否正确（验证后删除，一次性使用）
     * 开发环境下，如果 id 为 "dev-bypass" 且 code 为 "bypass"，则跳过验证
     */
    public boolean verify(String id, String code) {
        // 开发环境跳过验证码（id=dev-bypass, code=bypass）
        if ("dev-bypass".equals(id) && "bypass".equals(code)) {
            log.info("开发环境跳过验证码验证");
            return true;
        }

        String key = "captcha:" + id;
        String stored = redis.opsForValue().get(key);

        if (stored == null) {
            log.warn("验证码已过期: id={}", id);
            return false;
        }

        boolean valid = stored.equalsIgnoreCase(code);
        if (valid) {
            redis.delete(key);
            log.info("验证码验证成功: id={}", id);
        } else {
            log.warn("验证码验证失败: id={}", id);
        }
        return valid;
    }

    /** 生成 4 位随机验证码 */
    private String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }

    /** 绘制验证码图片：白底 + 干扰线 + 噪点 + 验证码字符 */
    private String generateImage(String code) {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();

        // 白色背景 + 灰色边框
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(new Color(200, 200, 200));
        g.drawRect(0, 0, WIDTH - 1, HEIGHT - 1);

        // 6 条干扰线（防机器识别）
        for (int i = 0; i < 6; i++) {
            g.setColor(randomColor(160, 200));
            g.drawLine(random.nextInt(WIDTH), random.nextInt(HEIGHT),
                    random.nextInt(WIDTH), random.nextInt(HEIGHT));
        }

        // 40 个噪点
        for (int i = 0; i < 40; i++) {
            g.setColor(randomColor(100, 200));
            g.fillOval(random.nextInt(WIDTH), random.nextInt(HEIGHT), 2, 2);
        }

        // 绘制验证码字符（随机颜色 + 随机位置 + 随机字号）
        int charWidth = (WIDTH - 20) / CODE_LENGTH;
        for (int i = 0; i < code.length(); i++) {
            g.setColor(randomColor(30, 130));
            g.setFont(new Font("Arial", Font.BOLD, 28 + random.nextInt(6)));
            g.drawString(String.valueOf(code.charAt(i)), 10 + i * charWidth + random.nextInt(5), 30 + random.nextInt(6) - 3);
        }

        g.dispose();

        // 转 base64，前端直接 <img src="...">
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(img, "png", baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("验证码图片生成失败", e);
        }
    }

    /** 生成随机颜色（RGB） */
    private Color randomColor(int min, int max) {
        return new Color(min + random.nextInt(max - min),
                min + random.nextInt(max - min),
                min + random.nextInt(max - min));
    }
}