package com.hbcy.authcenter.api.modules.minor.msg.engine;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 消息模板渲染器
 * 替换 {变量名} 模式
 */
public class TemplateRenderer {
    private static final Pattern VAR_PATTERN = Pattern.compile("\\{(\\w+)}");

    /**
     * 渲染模板，将 {varName} 替换为 variables 中对应的值
     *
     * @param template  模板字符串
     * @param variables 变量映射
     * @return 渲染后的字符串
     */
    public static String render(String template, Map<String, String> variables) {
        if (template == null || template.isEmpty()) {
            return template;
        }
        if (variables == null || variables.isEmpty()) {
            return template;
        }
        Matcher m = VAR_PATTERN.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String varName = m.group(1);
            String replacement = variables.getOrDefault(varName, "{" + varName + "}");
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * 检查模板中是否包含变量占位符
     */
    public static boolean hasVariables(String template) {
        if (template == null || template.isEmpty()) {
            return false;
        }
        return VAR_PATTERN.matcher(template).find();
    }
}
