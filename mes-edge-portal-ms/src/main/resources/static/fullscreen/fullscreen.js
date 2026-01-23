(function (window, layui, $) {
    function FullScreen(options) {
        this.options = $.extend(true, {
            elem: $('.oemes-container').get(0),
            iconClass: "layui-icon  layui-icon-screen-full"
        }, options);

        this.opened = false;
        this._init();
    }

    FullScreen.prototype._init = function () {
        this._renderDom();
        this._bindEvent();
    };

    FullScreen.prototype._renderDom = function () {
        const that = this;
        const html = `
            <button type="button" class="layui-btn layui-btn-sm fullscreenBtn">
                <i class="${that.options.iconClass}"></i>
            </button>
        `;
        $('body').append(html);
    };

    FullScreen.prototype._bindEvent = function () {
        const that = this;

        let btn = $('.fullscreenBtn')[0]
        let isDragging = false;
        let offsetX = 0;
        let offsetY = 0;

        btn.addEventListener('mousedown', (e) => {
            isDragging = true;
            offsetX = e.clientX - btn.getBoundingClientRect().left;
            offsetY = e.clientY - btn.getBoundingClientRect().top;
            e.preventDefault();
        });

        document.addEventListener('mousemove', (e) => {
            if (!isDragging) return;
            let x = e.clientX - offsetX;
            let y = e.clientY - offsetY;

            // 限制在窗口范围内
            const maxX = window.innerWidth - btn.offsetWidth;
            const maxY = window.innerHeight - btn.offsetHeight;
            x = Math.max(0, Math.min(x, maxX));
            y = Math.max(0, Math.min(y, maxY));

            btn.style.left = x + 'px';
            btn.style.top = y + 'px';
        });

        document.addEventListener('mouseup', () => {
            isDragging = false;
        });

        $('.fullscreenBtn').on('click', function () {
            that.enterFullscreen(that.options.elem);
        });



    };

    FullScreen.prototype.enterFullscreen = function (elem) {
        if (elem.requestFullscreen) {
            elem.requestFullscreen();
        } else if (elem.mozRequestFullScreen) { // Firefox
            elem.mozRequestFullScreen();
        } else if (elem.webkitRequestFullscreen) { // Chrome, Safari, Opera
            elem.webkitRequestFullscreen();
        } else if (elem.msRequestFullscreen) { // IE/Edge
            elem.msRequestFullscreen();
        }
    }

    window.FullScreen = FullScreen;
})(window, layui, jQuery);
