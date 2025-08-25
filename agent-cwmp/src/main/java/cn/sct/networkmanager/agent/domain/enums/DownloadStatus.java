package cn.sct.networkmanager.agent.domain.enums;

import lombok.Getter;

public enum DownloadStatus {

        /**
         * 0: 成功
         * 文件已成功下载或上传，并被正确处理（如固件已写入、配置已加载）
         */
        SUCCESS(0, "下载已完成并应用", "Download has completed and been applied"),

        /**
         * 1: 不支持的文件类型
         * CPE 无法识别或不支持 FileType 指定的类型（如 FileType=1 但不支持固件升级）
         */
        UNSUPPORTED_FILE_TYPE(1, "下载尚未完成和应用", "Download has not yet been completed and applied");



        // 枚举属性
        @Getter
        private final int code;
        private final String chineseDesc;
        private final String englishDesc;

        // 构造函数
        DownloadStatus(int code, String chineseDesc, String englishDesc) {
            this.code = code;
            this.chineseDesc = chineseDesc;
            this.englishDesc = englishDesc;
        }
}
