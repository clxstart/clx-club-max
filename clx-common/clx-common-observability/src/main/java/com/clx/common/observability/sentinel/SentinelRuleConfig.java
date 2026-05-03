package com.clx.common.observability.sentinel;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel 规则配置示例。
 *
 * <p>生产环境建议使用 Sentinel Dashboard 动态配置规则，
 * 此配置类仅作为代码配置示例参考。
 *
 * <p>如需启用，取消 @Configuration 注释。
 */
// @Configuration
@ConditionalOnClass(name = "com.alibaba.csp.sentinel.SphU")
public class SentinelRuleConfig {

    /**
     * 流控规则示例。
     *
     * <p>配置方式建议：
     * <ul>
     *   <li>开发环境：代码配置或配置文件</li>
     *   <li>生产环境：Sentinel Dashboard 动态配置</li>
     * </ul>
     */
    // @Bean
    public List<FlowRule> flowRules() {
        List<FlowRule> rules = new ArrayList<>();

        // 示例：API 接口 QPS 限制为 100
        FlowRule rule = new FlowRule();
        rule.setResource("api_limit");
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(100);
        rules.add(rule);

        return rules;
    }

    /**
     * 熔断规则示例。
     *
     * <p>熔断策略：
     * <ul>
     *   <li>慢调用比例：响应时间超过阈值</li>
     *   <li>异常比例：异常数超过阈值</li>
     *   <li>异常数：异常数超过阈值</li>
     * </ul>
     */
    // @Bean
    public List<DegradeRule> degradeRules() {
        List<DegradeRule> rules = new ArrayList<>();

        // 示例：异常比例熔断
        DegradeRule rule = new DegradeRule();
        rule.setResource("service_degrade");
        rule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        rule.setCount(0.5);  // 异常比例 50%
        rule.setTimeWindow(10);  // 熔断时长 10 秒
        rules.add(rule);

        return rules;
    }
}
