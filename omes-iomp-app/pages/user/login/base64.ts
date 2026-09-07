export function base64Encode(str: string): string {
    // #ifdef MP
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=';
    let output = '';
    let i = 0;

    while (i < str.length) {
        const chr1 = str.charCodeAt(i++)!;
        const chr2 = str.charCodeAt(i++)!;
        const chr3 = str.charCodeAt(i++)!;

        const enc1 = chr1 >> 2;
        const enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
        const enc3 = isNaN(chr2) ? 64 : ((chr2 & 15) << 2) | (chr3 >> 6);
        const enc4 = isNaN(chr3) ? 64 : chr3 & 63;

        output += chars.charAt(enc1) + chars.charAt(enc2) + chars.charAt(enc3) + chars.charAt(enc4);
    }

    return output;
    // #endif

    // #ifndef MP
    return btoa(str)
    // #endif
}