(function (window, layui, $) {

    function TreeSelect(options) {
        this.options = $.extend(true, {
            title: '场景选择',
            data: [],
            customName: {},
            needVirtualRoot: false,
            virtualRootName: 'all',
            container: '.oemes-container',
            onSelect: function () {
            }
        }, options);

        this.opened = false;
        this._init();
    }

    TreeSelect.prototype._init = function () {
        this._renderDom();
        this._bindEvent();
        this._renderTree();
    };

    TreeSelect.prototype._renderDom = function () {
        const html = `
            <div class="scene-tree-toggle" id="TreeSelectToggle">
                <i class="layui-icon layui-icon-shrink-right"></i>
            </div>
            <div class="scene-tree-panel" id="TreeSelectPanel">
                <div class="scene-tree-header">${this.options.title}</div>
                <div class="scene-tree-body">
                     <div class="layui-inline">
                        <input type="text" id="tree_name" class="layui-input" autocomplete="off"/>
                    </div>
                    <a class="layui-btn layui-btn-sm layui-btn-primary" id="tree_query">
                        <i class="layui-icon layui-icon-search"></i>
                    </a>
                    <div id="left-tree"></div>
                </div>
            </div>
        `;
        $(this.options.container).append(html);
    };

    TreeSelect.prototype._bindEvent = function () {
        const that = this;

        $('#TreeSelectToggle').on('click', function () {
            that.toggle();
        });

        $('#tree_query').on('click', function () {
            var name = $("#tree_name").val(); //搜索值
            var elem = $("#left-tree").find('.layui-tree-txt').css('color', ''); //搜索文本与设置默认颜色
            if (!name) {
                return; //无搜索值返回
            }
            elem.filter(':contains(' + name + ')').css('color', '#FFB800'); //搜索文本并设置标志颜色
            elem.parents('.layui-tree-pack').prev().find('.layui-tree-iconClick').click(); //展开选项
        });
    };

    TreeSelect.prototype._renderTree = function () {
        const that = this;
        let data;
        if (that.options.needVirtualRoot) {
            let root = {
                title: that.options.virtualRootName,
                children: that.options.data
            };
            if (that.options.customName) {
                if (that.options.customName.title) {
                    root[that.options.customName.title] = root.title
                }
            }
            data = [root];
        } else {
            data = that.options.data
        }
        layui.use(['tree'], function () {
            layui.tree.render({
                elem: '#left-tree',
                data: data,
                onlyIconControl: true,  // 是否仅允许节点左侧图标控制展开收缩
                id: 'left-tree',
                edit: [],
                extEdit: true,
                customName: that.options.customName,
                click: function (obj) {
                    $(".layui-tree-set").removeClass('layui-tree-set-active');
                    obj.elem.addClass('layui-tree-set-active');
                    that.options.onSelect(obj.data);
                }
            });
        });
    };

    /* ===== 对外 API ===== */

    TreeSelect.prototype.toggle = function () {
        this.opened = !this.opened;
        $('#TreeSelectPanel').toggleClass('open', this.opened);
        $('#TreeSelectToggle i')
            .toggleClass('layui-icon-shrink-right', !this.opened)
            .toggleClass('layui-icon-spread-left', this.opened);
    };

    TreeSelect.prototype.open = function () {
        if (!this.opened) this.toggle();
    };

    TreeSelect.prototype.close = function () {
        if (this.opened) this.toggle();
    };

    TreeSelect.prototype.setData = function (data) {
        this.options.data = data;
        this._renderTree();
    };

    // 暴露到全局
    window.TreeSelect = TreeSelect;

})(window, layui, jQuery);
