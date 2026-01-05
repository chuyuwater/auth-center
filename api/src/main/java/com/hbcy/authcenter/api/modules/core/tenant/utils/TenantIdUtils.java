package com.hbcy.authcenter.api.modules.core.tenant.utils;

import com.hbcy.authcenter.api.common.constants.G;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 按产品要求，租户ID使用类似Excel的列名算法计算
 *
 * @author 姚泰然
 * @date 2025-12-23 09:01
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TenantIdUtils {
    /**
     * 将数字转换为 Excel 列名 (1 -> A, 2 -> B, ..., 27 -> AA)
     */
    public static String convertToTitle(long n) {
        if (n == 1) {
            // 第一个是默认租户，要特殊处理
            return G.DEFAULT_TENANT;
        }
        n--;
        StringBuilder columnTitle = new StringBuilder();
        while (n > 0) {
            // 核心步骤：因为 Excel 是从 1 开始计数，
            // 减 1 是为了将 1-26 映射到 0-25 以便处理 ASCII
            n--;

            // 计算当前位的字符 (A = 65)
            char currentChar = (char) ('A' + (n % 26));
            columnTitle.append(currentChar);

            // 移动到更高的一位
            n /= 26;
        }

        // 因为是从低位向高位计算，最后需要反转
        return columnTitle.reverse().toString();
    }
}
