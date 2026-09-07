import type {ClTreeItem} from "@/uni_modules/cool-unix";
import {ref} from "vue";
import {request} from "@/core/service";
import {apiPath} from "@/core/apiRouter/path";
import {t} from "@/locale";
import {parseDataArray} from "@/core/utils/parse";
import type {WorkshopTreeNode} from "@/core/types";

const defaultNode: ClTreeItem = {
    id: -1,
    label: t('全部场景'),
    isExpand: true
};


export class WorkshopTree {

    selectNode = ref<ClTreeItem>(defaultNode);

    tree = ref<ClTreeItem[]>([]);

    workshopLoading: boolean = false;

    isNeedLoading: boolean = true;

    constructor() {
        this.workshopLoading = false;
        this.isNeedLoading = true;
    }

    loadWorkshopTree() {
        if (this.workshopLoading) {
            return
        }
        this.workshopLoading = true;
        try {
            if (!this.isNeedLoading) {
                return;
            }
            request({
                url: apiPath.workshop_tree as string,
                method: "GET"
            })
                .then((res) => {
                    this.isNeedLoading = false;
                    const parent = defaultNode;
                    if (res === null) {
                        this.tree.value = [parent];
                        return;
                    }
                    parent.isChecked = this.selectNode.value.id == -1;
                    const r = parseDataArray<WorkshopTreeNode>(res);
                    if (r != null) {
                        parent.children = this.convertWorkshopTree(r);
                    }
                    this.tree.value = [parent];
                })
        } finally {
            this.workshopLoading = false
        }
    }

    convertWorkshopTree(
        nodes: WorkshopTreeNode[]
    ): ClTreeItem[] {
        return nodes.map(node => ({
            id: node.selfCode,
            label: node.name,
            isExpand: true,
            children: node.children == null ? null : this.convertWorkshopTree(node.children),
        } as ClTreeItem));
    }

    getCheckedNodes(): ClTreeItem[] {
        const result: ClTreeItem[] = []; // 存储选中节点id

        /**
         * 递归收集所有选中节点的id
         * @param nodes 当前遍历的节点数组
         */
        function collectCheckedKeys(nodes: ClTreeItem[]): void {
            for (let i = 0; i < nodes.length; i++) {
                const node = nodes[i];

                if (node.isChecked == true) {
                    result.push(node); // 收集选中节点id
                }

                if (node.children != null) {
                    collectCheckedKeys(node.children); // 递归处理子节点
                }
            }
        }

        collectCheckedKeys(this.tree.value); // 从根节点开始收集
        return result; // 返回所有选中节点id
    }

    selectConfirm() {
        let nodes: ClTreeItem[] = this.getCheckedNodes();
        let node: ClTreeItem | null = null;
        if (nodes.length > 0) {
            node = nodes[0];
            if (node.id == -1) {
                node = null;
            }
        }
        if (node == null) {
            this.selectNode.value = defaultNode;
        } else {
            this.selectNode.value = node;
        }
    }
}

export const workshopTree = new WorkshopTree();