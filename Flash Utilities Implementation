struct FlashUtilities {
    static let TAG = "FlashUtilities"
    
    // MARK: - Core Functionality
    static func splitBinBlocks(bin: Data) -> [Data] {
        var splitBlocks = Array(repeating: Data(), count: 6)
        guard let boxCode = getBoxCodeFromBin(bin: bin) else { return splitBlocks }
        
        for i in 1...5 {
            let start = boxCode.software.fullBinLocations[i]
            let end = start + boxCode.software.blockLengths[i]
            splitBlocks[i] = bin.subdata(in: start..<end)
        }
        return splitBlocks
    }
    
    static func getBoxCodeFromBin(bin: Data) -> COMPATIBLE_BOXCODE_VERSIONS? {
        if bin.count >= 4_000_000 {
            for version in COMPATIBLE_BOXCODE_VERSIONS.allCases {
                let start = version.software.fullBinLocations[5] + version.boxCodeLocation[0]
                let end = version.software.fullBinLocations[5] + version.boxCodeLocation[1]
                let boxData = bin.subdata(in: start..<end)
                guard let boxStr = String(data: boxData, encoding: .ascii)?.trimmingCharacters(in: .whitespaces) else { continue }
                
                if version.str == boxStr {
                    return version
                }
            }
        } else {
            // CAL file handling
        }
        return nil
    }
}
