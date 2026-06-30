#!/usr/bin/env python3
"""Re-migrate omes-process-server Java sources from 113 with UTF-8 (no BOM)."""

from __future__ import annotations

import re
from pathlib import Path

SRC = Path(r"e:\project\iot\omes\113\113-manage\src\main\java\com\ket\manage\process")
DST = Path(
    r"e:\project\iot\omes\omes\omes-process\omes-process-server\src\main\java\com\ourexists\omes\process"
)

SKIP_FILES = {
    "engine/spi/CollectProcessSignalProvider.java",
    "engine/model/StepEnginePhase.java",
}

KEEP_FILES = {
    "support/ProcessFileStorageService.java",
    "support/ProcessFileProperties.java",
    "support/ProcessFileAccessService.java",
    "support/ProcessFileAccessEnricher.java",
}

TABLE_MAP = {
    "T_BIZ_PROCESS": "t_biz_process",
    "R_BIZ_PROCESS_MOLD": "r_biz_process_mold",
    "R_BIZ_PROCESS_STEP": "r_biz_process_step",
    "R_BIZ_PROCESS_STEP_WIP": "r_biz_process_step_wip",
    "M_BIZ_PROCESS_STEP_EQUIPMENT": "m_biz_process_step_equipment",
    "M_BIZ_PROCESS_STEP_TOOLING": "m_biz_process_step_tooling",
}


def transform_domain(content: str) -> str:
    if "extends EraEntity" not in content:
        return content
    content = re.sub(
        r"import com\.baomidou\.mybatisplus\.annotation\.TableLogic;\r?\n",
        "",
        content,
    )
    content = re.sub(
        r"import lombok\.Data;\r?\nimport lombok\.EqualsAndHashCode;\r?\n",
        "import lombok.Getter;\n"
        "import lombok.Setter;\n"
        "import lombok.experimental.Accessors;\n",
        content,
    )
    content = re.sub(
        r"@Data\r?\n@EqualsAndHashCode\(callSuper = true\)\r?\n",
        "@Getter\n@Setter\n@Accessors(chain = true)\n",
        content,
    )
    return content


def transform_mapper(content: str) -> str:
    if "@Mapper" in content:
        return content
    if "BaseMapper" not in content:
        return content
    content = content.replace(
        "import com.baomidou.mybatisplus.core.mapper.BaseMapper;",
        "import com.baomidou.mybatisplus.core.mapper.BaseMapper;\nimport org.apache.ibatis.annotations.Mapper;",
    )
    content = re.sub(
        r"(\r?\n)(public interface )",
        r"\1@Mapper\n\2",
        content,
        count=1,
    )
    return content


def transform_import_service(content: str) -> str:
    content = re.sub(
        r"import com\.ket\.manage\.file\.dto\.FileVO;\r?\n"
        r"import com\.ket\.manage\.file\.service\.SysFileService;\r?\n",
        "import com.ourexists.omes.process.model.ProcessStoredFileVo;\n"
        "import com.ourexists.omes.process.support.ProcessFileStorageService;\n",
        content,
    )
    content = content.replace("SysFileService", "ProcessFileStorageService")
    content = content.replace("FileVO", "ProcessStoredFileVo")
    return content


def transform_export_service(content: str) -> str:
    content = content.replace(
        "import com.ket.manage.config.FileProperties;",
        "import com.ourexists.omes.process.support.ProcessFileProperties;",
    )
    content = content.replace(
        "private final FileProperties fileProperties;",
        "private final ProcessFileProperties fileProperties;",
    )
    content = content.replace("getStoragePath()", "getRootPath()")
    return content


def transform(content: str, rel: str) -> str:
    content = content.replace("com.ket.manage.process.dto", "com.ourexists.omes.process.model")
    content = content.replace("com.ket.manage.process", "com.ourexists.omes.process")
    content = content.replace(
        "com.ket.manage.common.exception.BizException",
        "com.ourexists.era.framework.core.exceptions.BusinessException",
    )
    content = content.replace("BizException", "BusinessException")
    content = content.replace(
        "com.ket.manage.common.mybatis.BaseEntity",
        "com.ourexists.era.framework.orm.mybatisplus.EraEntity",
    )
    content = content.replace("BaseEntity", "EraEntity")

    for old, new in TABLE_MAP.items():
        content = content.replace(f'"{old}"', f'"{new}"')

    content = content.replace("getCreateTime", "getCreatedTime")
    content = content.replace("getUpdateTime", "getUpdatedTime")

    content = content.replace(
        "import com.ourexists.omes.process.engine.model.StepEnginePhase;",
        "import com.ourexists.omes.process.model.StepEnginePhase;",
    )

    if rel.startswith("domain/"):
        content = transform_domain(content)
    if rel.startswith("mapper/"):
        content = transform_mapper(content)
    if rel == "service/ProcessImportService.java":
        content = transform_import_service(content)
    if rel == "service/ProcessExportService.java":
        content = transform_export_service(content)
    if rel == "engine/model/ProcessStepTickResult.java":
        content = content.replace(
            "import lombok.Builder;",
            "import com.ourexists.omes.process.model.StepEnginePhase;\nimport lombok.Builder;",
        )

    return content


def main() -> None:
    copied = 0
    skipped = 0
    for src_file in sorted(SRC.rglob("*.java")):
        rel = src_file.relative_to(SRC).as_posix()
        if rel.startswith("web/") or rel.startswith("dto/"):
            skipped += 1
            continue
        if rel in SKIP_FILES:
            skipped += 1
            continue
        if rel in KEEP_FILES:
            skipped += 1
            continue

        text = src_file.read_text(encoding="utf-8")
        text = transform(text, rel)

        dst_file = DST / rel
        dst_file.parent.mkdir(parents=True, exist_ok=True)
        dst_file.write_text(text, encoding="utf-8", newline="\n")
        copied += 1

    print(f"Remigrated {copied} files, skipped {skipped}")


if __name__ == "__main__":
    main()
