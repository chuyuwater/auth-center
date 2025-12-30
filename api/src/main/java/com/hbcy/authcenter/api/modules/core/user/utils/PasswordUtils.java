package com.hbcy.authcenter.api.modules.core.user.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author 姚泰然
 * @date 2025-12-29 12:59
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PasswordUtils {

    /**
     * 密码强度校验
     *
     * @param password 待校验的密码
     * @return 是否满足密码强度要求
     */
    public static boolean isValid(String password) {
        // 1. 长度校验
        if (password == null || password.length() < 8) {
            return false;
        }

        // 使用标志位记录是否出现过某种类型
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        char[] chars = password.toCharArray();
        for (char c : chars) {
            if (c >= 'A' && c <= 'Z') {
                hasUpper = true;
            } else if (c >= 'a' && c <= 'z') {
                hasLower = true;
            } else if (c >= '0' && c <= '9') {
                hasDigit = true;
            } else {
                // 剩余字符视为特殊符号（包括标点、空格等）
                hasSpecial = true;
            }
            // 优化：如果四种都已经凑齐，可以提前结束遍历
            if (hasUpper && hasLower && hasDigit && hasSpecial) {
                break;
            }
        }

        // 统计命中的种类数量
        int count = 0;
        if (hasUpper) count++;
        if (hasLower) count++;
        if (hasDigit) count++;
        if (hasSpecial) count++;

        return count == 4;
    }
}
