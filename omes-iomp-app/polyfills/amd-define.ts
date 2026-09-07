/**
 * 微信小程序：appservice 无 AMD `define`，部分 UMD 产物顶层会引用 define，导致 ReferenceError。
 * 仅由 main.ts 在 MP-WEIXIN 条件编译下导入；勿使用 globalThis 与 Record 字符串索引映射（UTS→Kotlin 会报错）。
 */
declare const wx: any;

(function installAmdDefineStub(): void {
    const root = wx;
    if (root == null) {
        return;
    }
    if (typeof root.define === 'function') {
        return;
    }
    const stub = function defineStub(): void {
        /* noop */
    };
    (stub as any).amd = {};
    root.define = stub;
})();
