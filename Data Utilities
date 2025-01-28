extension Data {
    var hexString: String {
        map { String(format: "%02x", $0) }.joined()
    }
    
    func findFirst(inner: Data) -> Int {
        guard !inner.isEmpty, count >= inner.count else { return -1 }
        
        for i in 0...(count - inner.count) {
            let subRange = i..<(i + inner.count)
            if self.subdata(in: subRange) == inner {
                return i
            }
        }
        return -1
    }
}

extension FixedWidthInteger {
    var byteArray: [UInt8] {
        withUnsafeBytes(of: self.bigEndian) { Array($0) }
    }
}
