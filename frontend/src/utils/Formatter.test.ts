import Formatter from "./Formatter";

describe("utils", () => {
    it("formats bytes 900 Bytes", () => {
        expect(Formatter.formatBytes(900)).toEqual("900 Bytes")
    })
    it("formats bytes 1KB", () => {
        expect(Formatter.formatBytes(1024)).toEqual("1 KB")
    })
    it("formats bytes 1.1KB = 1KB", () => {
        expect(Formatter.formatBytes(1124, 0)).toEqual("1 KB")
    })
    it("formats bytes 1.1KB = 1.1KB", () => {
        expect(Formatter.formatBytes(1124, 1)).toEqual("1.1 KB")
    })
    it("formats bytes 1MB", () => {
        expect(Formatter.formatBytes(1024 * 1024, 1)).toEqual("1 MB")
    })
})