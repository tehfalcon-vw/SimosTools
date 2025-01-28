import Foundation

class UDSFlasher {
    static let shared = UDSFlasher()
    
    private let tag = "UDSflash"
    private var mTask: FlashECUCalSubtask = .none
    private var mCommand = Data()
    private var mLastString = ""
    private var mFullFlash = false
    private var flashConfirmed = false
    private var cancelFlash = false
    private var bin: [Data] = Array(repeating: Data(), count: 6)
    private var workshopCode = Data()
    private var inputBin = Data()
    private var patchBin = Data()
    private var ecuAswVersion = Data()
    private var transferSequence = -1
    private var progress = 0
    private var binAswVersion: CompatibleBoxcodeVersions = .undefined
    private var clearDTCStart = 0
    private var clearDTCcontinue = 0
    private var currentBlockOperation = 0
    private var patchTransferAddress = 0
    private let queue = DispatchQueue(label: "UDSFlasherQueue")
    
    private init() {}
    
    func clear() {
        mCommand = Data()
        inputBin = Data()
        patchBin = Data()
        ecuAswVersion = Data()
        bin = Array(repeating: Data(), count: 6)
    }
    
    func getSubtask() -> FlashECUCalSubtask {
        return mTask
    }
    
    func getFlashConfirmed() -> Bool {
        return flashConfirmed
    }
    
    func setFlashConfirmed(_ input: Bool = false) {
        flashConfirmed = input
    }
    
    func cancelFlash() {
        cancelFlash = true
    }
    
    func getInfo() -> String {
        return mLastString
    }
    
    func getCommand() -> Data {
        return mCommand
    }
    
    func started() -> Bool {
        return mTask != .none
    }
    
    func getProgress() -> Int {
        return progress
    }
    
    func setBinFile(input: InputStream) {
        DebugLog.d(tag, "Received BIN stream from GUI")
        mTask = .none
        flashConfirmed = false
        cancelFlash = false
        progress = 0
        clearDTCStart = 0
        clearDTCcontinue = 0
        
        input.open()
        defer { input.close() }
        
        let bufferSize = 1024
        var buffer = [UInt8](repeating: 0, count: bufferSize)
        var data = Data()
        
        while input.hasBytesAvailable {
            let bytesRead = input.read(&buffer, maxLength: bufferSize)
            data.append(buffer, count: bytesRead)
        }
        
        inputBin = data
        patchBin = Data()
        currentBlockOperation = 0
        workshopCode = Data()
    }
    
    func setFullFlash(_ full: Bool) {
        DebugLog.d(tag, "Set FullFlash to \(full)")
        mFullFlash = full
    }
    
    func getFullFlash() -> Bool {
        return mFullFlash
    }
    
    func startTask(ticks: Int) -> Data {
        if inputBin.count < 500000 {
            mLastString = "Selected file too small..."
            return Data()
        } else if inputBin.count > 500000 && inputBin.count < 4_000_000 {
            mTask = .getEcuBoxCode
            DebugLog.d(tag, "Initiating Calibration Flash subroutine: \(mTask)")
            mLastString = "Initiating calibration flash routines"
            bin[5] = inputBin
            return UDSCommand.readIdentifier.bytes + ECUInfo.partNumber.address
        } else if inputBin.count <= 0x400000 {
            mLastString = "Full flash file selected..."
            mTask = .getEcuBoxCode
            bin = FlashUtilities.splitBinBlocks(inputBin)
            
            if !mFullFlash {
                for i in 0..<5 {
                    bin[i] = Data()
                }
            }
            
            return UDSCommand.readIdentifier.bytes + ECUInfo.partNumber.address
        } else {
            mLastString = "UNLOCK FLASH SELECTED!!!"
            mTask = .getEcuBoxCode
            bin = FlashUtilities.splitBinBlocks(inputBin)
            patchBin = inputBin.subdata(in: 0x400000..<inputBin.count)
            return UDSCommand.readIdentifier.bytes + ECUInfo.partNumber.address
        }
    }
    
    func processFlashCAL(ticks: Int, buff: Data?) -> UDSReturn {
        return queue.sync {
            guard let buff = buff else {
                DebugLog.d(tag, "Flash subroutine: \(mTask) nil buffer")
                return .errorNull
            }
            
            DebugLog.d(tag, "Flash subroutine: \(mTask) \(buff.hexString)")
            
            if checkResponse(buff) == .negativeResponse {
                // Handle negative response
            }
            
            switch mTask {
            case .getEcuBoxCode:
                switch checkResponse(buff) {
                case .readIdentifier:
                    ecuAswVersion = buff.subdata(in: 3..<buff.count)
                    DebugLog.d(tag, "Received ASW version \(ecuAswVersion.hexString) from ecu")
                    mLastString = "Read box code from ECU: \(String(data: ecuAswVersion, encoding: .ascii) ?? "")"
                    mTask = mTask.next()
                    mCommand = UDSCommand.testerPresent.bytes
                    return .commandQueued
                    
                case .positiveResponse:
                    mCommand = UDSCommand.readIdentifier.bytes + ECUInfo.partNumber.address
                    mLastString = "Initiating flash routines"
                    return .commandQueued
                    
                default:
                    DebugLog.d(tag, "Error with ECU Response: \(buff.hexString)")
                    mLastString = "Error with ECU Response: \(String(data: buff, encoding: .ascii) ?? "")"
                    return .errorUnknown
                }
                
            // Implement other cases similarly...
                
            default:
                return .errorUnknown
            }
        }
    }
    
    private func checkResponse(_ input: Data) -> UDSResponse {
        guard !input.isEmpty else { return .noResponse }
        return UDSResponse(rawValue: input[0]) ?? .noResponse
    }
}

// MARK: - Enums and Supporting Types

enum FlashECUCalSubtask {
    case none
    case getEcuBoxCode
    case checkFileCompat
    case confirmProceed
    case checksumBin
    case compressBin
    case encryptBin
    case clearDTC
    case checkProgrammingPrecondition
    case openExtendedDiagnostic
    case sa2SeedKey
    case writeWorkshopLog
    case checksumBlock
    case verifyProgrammingDependencies
    case flashBlock
    case patchBlock
    case resetEcu
    
    func next() -> FlashECUCalSubtask {
        // Implement state transitions
        return self
    }
}

enum UDSResponse: UInt8 {
    case noResponse = 0x00
    case positiveResponse = 0x7F
    case negativeResponse = 0x7E
    case readIdentifier = 0x6A
    case routineAccepted = 0x71
    // Add all other cases...
}

enum UDSReturn {
    case commandQueued
    case errorUnknown
    case errorNull
    case flashConfirm
    case ok
    case aborted
    case errorResponse
    case clearDTCRequest
    case flashComplete
}

struct UDSCommand {
    static let readIdentifier = Command(bytes: Data([0x22]))
    static let testerPresent = Command(bytes: Data([0x3E]))
    // Add all other commands...
    
    struct Command {
        let bytes: Data
    }
}

struct ECUInfo {
    static let partNumber = ECUAddress(address: Data([0xF1, 0x8C]))
    
    struct ECUAddress {
        let address: Data
    }
}

// MARK: - Extensions

extension Data {
    var hexString: String {
        return map { String(format: "%02hhx", $0) }.joined()
    }
}
