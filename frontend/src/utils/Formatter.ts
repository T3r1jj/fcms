export default class Formatter {
    public static formatBytes(bytes: number, decimals: number = 0): string {
        if (0 === bytes) {
            return "0 Bytes"
        }
        const c = 1024
        const e = ["Bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"]
        const f = Math.floor(Math.log(bytes) / Math.log(c))
        return parseFloat((bytes / Math.pow(c, f)).toFixed(decimals)) + " " + e[f]
    }
}