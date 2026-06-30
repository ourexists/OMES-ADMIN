/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.process.support;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ourexists.omes.process.model.ProcessImportParseResult;
import com.ourexists.omes.process.model.ProcessListVO;
import com.ourexists.omes.process.model.ProcessVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProcessFileAccessEnricher {

    private final ProcessFileAccessService fileAccessService;

    public void enrichProcess(ProcessListVO vo) {
        if (vo == null) {
            return;
        }
        vo.setProcessImageAccessUrl(
                fileAccessService.buildAccessUrlByStoragePath(vo.getProcessImageUrl()));
    }

    public void enrichProcess(ProcessVO vo) {
        enrichProcess((ProcessListVO) vo);
    }

    public void enrichProcessList(List<? extends ProcessListVO> list) {
        if (list == null) {
            return;
        }
        list.forEach(this::enrichProcess);
    }

    public void enrichProcessPage(IPage<ProcessVO> page) {
        if (page != null) {
            enrichProcessList(page.getRecords());
        }
    }

    public void enrichProcessImport(ProcessImportParseResult result) {
        if (result == null) {
            return;
        }
        result.setProcessImageAccessUrl(
                fileAccessService.buildAccessUrlByStoragePath(result.getProcessImageUrl()));
    }
}
