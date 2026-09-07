package com.ourexists.omes.portal.oss;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocalFileEntryVo {

    /** 名称（不含路径） */
    private String name;

    /** 相对根目录的路径 */
    private String path;

    /** 是否为目录 */
    private boolean directory;

    /** 文件大小（字节），目录为 null */
    private Long size;

    /** 最后修改时间（毫秒时间戳） */
    private Long lastModified;

    /** 扩展名（小写，不含点），目录为空 */
    private String extension;
}
