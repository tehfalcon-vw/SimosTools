class UDSInfo {
    static let shared = UDSInfo()
    
    private let TAG = "UDSInfo"
    private var mLastString: String = ""
    private var mTimeoutCounter: Int = TIME_OUT_INFO
    
    func getInfo() -> String {
        return mLastString
    }
    
    func getStartCount() -> Int {
        return ECUInfo.allCases.count
    }
    
    func startTask(index: Int) -> Data {
        let bleHeader = BLEHeader()
        bleHeader.cmdSize = 1 + ECUInfo.allCases[index].address.count
        bleHeader.cmdFlags = BLECommandFlags.PER_CLEAR.rawValue
        
        var data = bleHeader.toData()
        data.append(0x22)
        data.append(ECUInfo.allCases[index].address)
        return data
    }
    
    func processPacket(ticks: Int, buff: Data?) -> UDSReturn {
        if let buff = buff {
            resetTimeout()
            
            if buff.count >= 11 && buff[8] == 0x62 {
                let responseData = buff.subdata(in: 11..<buff.count)
                mLastString = "\(ECUInfo.allCases[ticks].str): \(ECUInfo.allCases[ticks].parseResponse(data: responseData))"
                
                return .OK
            }
            return .ERROR_UNKNOWN
        }
        
        return addTimeout()
    }
    
    private func addTimeout() -> UDSReturn {
        mTimeoutCounter -= 1
        if mTimeoutCounter == 0 {
            return .ERROR_TIME_OUT
        }
        return .OK
    }
    
    private func resetTimeout() {
        mTimeoutCounter = TIME_OUT_INFO
    }
}

// Supporting types and constants:

enum UDSReturn {
    case OK
    case ERROR_UNKNOWN
    case ERROR_TIME_OUT
}

// Assuming these are defined elsewhere:
let TIME_OUT_INFO: Int = /* appropriate value */
enum BLECommandFlags: UInt8 {
    case PER_CLEAR = /* appropriate value */
}
class BLEHeader {
    var cmdSize: Int = 0
    var cmdFlags: UInt8 = 0
    func toData() -> Data { /* implementation */ return Data() }
}
enum ECUInfo: CaseIterable {
    case /* your cases */
    var address: Data { /* implementation */ return Data() }
    var str: String { /* implementation */ return "" }
    func parseResponse(data: Data) -> String { /* implementation */ return "" }
}
