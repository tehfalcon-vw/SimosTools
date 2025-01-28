import Foundation
import CryptoKit

struct ChecksummedBin {
    var bin: Data
    var fileChecksum: String
    var calculatedChecksum: String
    var updated: Bool
}

enum COMPATIBLE_BOXCODE_VERSIONS: CaseIterable {
    case version1 // Add your actual cases
    
    struct SoftwareInfo {
        let fullBinLocations: [Int]
        let blockLengths: [Int]
    }
    
    var str: String {
        switch self {
        case .version1: return "ABC123"
        }
    }
    
    var software: SoftwareInfo {
        switch self {
        case .version1:
            return SoftwareInfo(
                fullBinLocations: [0, 100, 200, 300, 400, 500],
                blockLengths: [0, 100, 100, 100, 100, 100]
            )
        }
    }
    
    var boxCodeLocation: [Int] {
        switch self {
        case .version1: return [0, 8]
        }
    }
}
