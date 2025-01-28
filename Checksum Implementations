extension FlashUtilities {
    static func checksumSimos18(bin: Data, baseAddress: UInt32, checksumLocation: Int) -> ChecksummedBin {
        var mutableBin = bin
        let currentChecksum = bin.subdata(in: checksumLocation..<(checksumLocation + 8))
        
        // Offset calculation
        let start1 = bin.subdata(in: (checksumLocation + 12)..<(checksumLocation + 16)).reversed().withUnsafeBytes { $0.load(as: UInt32.self) } - baseAddress
        let end1 = bin.subdata(in: (checksumLocation + 16)..<(checksumLocation + 20)).reversed().withUnsafeBytes { $0.load(as: UInt32.self) } - baseAddress
        
        var checksumData = bin.subdata(in: Int(start1)...Int(end1))
        
        // Additional range handling
        let start2 = bin.subdata(in: (checksumLocation + 20)..<(checksumLocation + 24)).reversed().withUnsafeBytes { $0.load(as: UInt32.self) } - baseAddress
        let end2 = bin.subdata(in: (checksumLocation + 24)..<(checksumLocation + 28)).reversed().withUnsafeBytes { $0.load(as: UInt32.self) } - baseAddress
        
        if end2 > start2 {
            checksumData.append(bin.subdata(in: Int(start2)...Int(end2)))
        }
        
        // CRC Calculation
        var crc: UInt32 = 0
        let polynomial: UInt32 = 0x4C11DB7
        
        for byte in checksumData {
            for j in (0...7).reversed() {
                let bit = (UInt32(byte) >> j) & 1
                let msb = (crc >> 31) & 1
                crc <<= 1
                
                if (bit ^ msb) != 0 {
                    crc ^= polynomial
                }
            }
            crc &= 0xFFFFFFFF
        }
        
        // Build final checksum
        var checksumCalculated = Data(count: 4)
        checksumCalculated.append(contentsOf: crc.bigEndian.byteArray)
        
        // Update binary
        mutableBin.replaceSubrange(checksumLocation..<(checksumLocation + 8), with: checksumCalculated)
        
        return ChecksummedBin(
            bin: mutableBin,
            fileChecksum: currentChecksum.hexString,
            calculatedChecksum: checksumCalculated.hexString,
            updated: true
        )
    }
}
