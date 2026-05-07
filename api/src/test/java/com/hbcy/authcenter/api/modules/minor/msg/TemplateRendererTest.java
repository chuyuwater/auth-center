package com.hbcy.authcenter.api.modules.minor.msg;

import com.hbcy.authcenter.api.modules.minor.msg.engine.TemplateRenderer;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 模板渲染器测试
 */
class TemplateRendererTest {

    @Test
    void render_withVariables_replacesPlaceholders() {
        String template = "您好{name}，您有一条{type}消息待处理";
        Map<String, String> variables = Map.of("name", "张三", "type", "流程审批");
        String result = TemplateRenderer.render(template, variables);
        assertEquals("您好张三，您有一条流程审批消息待处理", result);
    }

    @Test
    void render_withMissingVariable_keepsPlaceholder() {
        String template = "您好{name}，{missing}消息";
        Map<String, String> variables = Map.of("name", "李四");
        String result = TemplateRenderer.render(template, variables);
        assertEquals("您好李四，{missing}消息", result);
    }

    @Test
    void render_withEmptyVariables_returnsOriginal() {
        String template = "您好{name}";
        String result = TemplateRenderer.render(template, Map.of());
        assertEquals(template, result);
    }

    @Test
    void render_withNullVariables_returnsOriginal() {
        String template = "您好{name}";
        String result = TemplateRenderer.render(template, null);
        assertEquals(template, result);
    }

    @Test
    void render_withNullTemplate_returnsNull() {
        assertNull(TemplateRenderer.render(null, Map.of("key", "value")));
    }

    @Test
    void render_withEmptyTemplate_returnsEmpty() {
        assertEquals("", TemplateRenderer.render("", Map.of("key", "value")));
    }

    @Test
    void render_withNoPlaceholders_returnsOriginal() {
        String template = "没有变量的模板";
        assertEquals(template, TemplateRenderer.render(template, Map.of("key", "value")));
    }

    @Test
    void render_withSpecialCharsInValue_escapesCorrectly() {
        String template = "内容：{content}";
        Map<String, String> variables = Map.of("content", "$100 & <html>");
        String result = TemplateRenderer.render(template, variables);
        assertEquals("内容：$100 & <html>", result);
    }

    @Test
    void hasVariables_withPlaceholder_returnsTrue() {
        assertTrue(TemplateRenderer.hasVariables("你好{name}"));
    }

    @Test
    void hasVariables_withoutPlaceholder_returnsFalse() {
        assertFalse(TemplateRenderer.hasVariables("没有变量"));
    }

    @Test
    void hasVariables_withNull_returnsFalse() {
        assertFalse(TemplateRenderer.hasVariables(null));
    }

    @Test
    void hasVariables_withEmpty_returnsFalse() {
        assertFalse(TemplateRenderer.hasVariables(""));
    }
}
