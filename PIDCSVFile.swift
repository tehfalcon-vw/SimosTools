import Foundation

struct PIDStruct {
    let address: Int64
    let length: Int
    let isSigned: Bool
    let progMin: Float
    let progMax: Float
    let warnMin: Float
    let warnMax: Float
    let smoothing: Float
    let value: Float
    let equation: String
    let format: String
    let name: String
    let unit: String
    let isEnabled: Bool
    let tabs: String
    let assignTo: String
}

enum CSVItem: Int, CaseIterable {
    case name
    case unit
    case equation
    case format
    case address
    case length
    case signed
    case progMin
    case progMax
    case warnMin
    case warnMax
    case smoothing
    case enabled
    case tabs
    case assignTo
    
    var header: String {
        switch self {
        case .name: return "Name"
        case .unit: return "Unit"
        case .equation: return "Equation"
        case .format: return "Format"
        case .address: return "Address"
        case .length: return "Length"
        case .signed: return "Signed"
        case .progMin: return "Prog Min"
        case .progMax: return "Prog Max"
        case .warnMin: return "Warn Min"
        case .warnMax: return "Warn Max"
        case .smoothing: return "Smoothing"
        case .enabled: return "Enabled"
        case .tabs: return "Tabs"
        case .assignTo: return "Assign To"
        }
    }
}

struct PIDCSVFile {
    private static let TAG = "PIDCSVFile"
    private static let MAX_PIDS = 256 // Adjust as needed
    
    static func read(fileName: String, addressMin: Int64, addressMax: Int64) -> [PIDStruct]? {
        let fileManager = FileManager.default
        guard let documentsDir = fileManager.urls(for: .documentDirectory, in: .userDomainMask).first else {
            return nil
        }
        
        let fileURL = documentsDir.appendingPathComponent(fileName)
        guard fileManager.fileExists(atPath: fileURL.path) else {
            return nil
        }
        
        guard let stream = InputStream(url: fileURL) else { return nil }
        return readStream(stream, addressMin: addressMin, addressMax: addressMax)
    }
    
    private static func readStream(_ stream: InputStream, addressMin: Int64, addressMax: Int64) -> [PIDStruct]? {
        stream.open()
        defer { stream.close() }
        
        guard let lines = try? readLines(from: stream) else { return nil }
        guard !lines.isEmpty else { return nil }
        
        // Check header (original logic may be flawed)
        let expectedHeader = CSVItem.allCases[0].header + "\n"
        guard lines[0] != expectedHeader else {
            return nil
        }
        
        var pidList = [PIDStruct]()
        for (index, line) in lines.dropFirst().enumerated() {
            guard index < MAX_PIDS else { break }
            let components = line.components(separatedBy: ",")
            guard components.count == CSVItem.allCases.count else { continue }
            
            do {
                let address = try parseAddress(components[CSVItem.address.rawValue], addressMin, addressMax)
                let length = try parseLength(components[CSVItem.length.rawValue])
                let signed = try parseBool(components[CSVItem.signed.rawValue])
                let progMin = try parseFloat(components[CSVItem.progMin.rawValue])
                let progMax = try parseFloat(components[CSVItem.progMax.rawValue])
                let warnMin = try parseFloat(components[CSVItem.warnMin.rawValue])
                let warnMax = try parseFloat(components[CSVItem.warnMax.rawValue])
                let smoothing = try parseFloat(components[CSVItem.smoothing.rawValue])
                let enabled = try parseBool(components[CSVItem.enabled.rawValue])
                
                let pid = PIDStruct(
                    address: address,
                    length: length,
                    isSigned: signed,
                    progMin: progMin,
                    progMax: progMax,
                    warnMin: warnMin,
                    warnMax: warnMax,
                    smoothing: smoothing,
                    value: 0.0,
                    equation: components[CSVItem.equation.rawValue],
                    format: components[CSVItem.format.rawValue],
                    name: components[CSVItem.name.rawValue],
                    unit: components[CSVItem.unit.rawValue],
                    isEnabled: enabled,
                    tabs: components[CSVItem.tabs.rawValue],
                    assignTo: components[CSVItem.assignTo.rawValue]
                )
                pidList.append(pid)
            } catch {
                return nil
            }
        }
        
        return pidList
    }
    
    static func write(fileName: String, pidList: [PIDStruct], overwrite: Bool) -> Bool {
        let fileManager = FileManager.default
        guard let documentsDir = fileManager.urls(for: .documentDirectory, in: .userDomainMask).first else {
            return false
        }
        
        let fileURL = documentsDir.appendingPathComponent(fileName)
        if fileManager.fileExists(atPath: fileURL.path) {
            if overwrite {
                do {
                    try fileManager.removeItem(at: fileURL)
                } catch {
                    return false
                }
            } else {
                return false
            }
        }
        
        fileManager.createFile(atPath: fileURL.path, contents: nil, attributes: nil)
        guard let outputStream = OutputStream(url: fileURL, append: false) else { return false }
        outputStream.open()
        defer { outputStream.close() }
        
        // Write header (original logic may be flawed)
        let header = CSVItem.allCases[0].header + "\n"
        guard writeData(header.data(using: .utf8), to: outputStream) else { return false }
        
        for pid in pidList {
            let addressString = formatAddress(pid.address)
            let line = [
                pid.name,
                pid.unit,
                pid.equation,
                pid.format,
                addressString,
                String(pid.length),
                String(pid.isSigned),
                String(pid.progMin),
                String(pid.progMax),
                String(pid.warnMin),
                String(pid.warnMax),
                String(pid.smoothing),
                String(pid.isEnabled),
                pid.tabs,
                pid.assignTo
            ].joined(separator: ",") + "\n"
            
            guard writeData(line.data(using: .utf8), to: outputStream) else { return false }
        }
        
        return true
    }
    
    // Helper functions
    private static func readLines(from stream: InputStream) throws -> [String] {
        var data = Data()
        let bufferSize = 1024
        let buffer = UnsafeMutablePointer<UInt8>.allocate(capacity: bufferSize)
        defer { buffer.deallocate() }
        
        while stream.hasBytesAvailable {
            let count = stream.read(buffer, maxLength: bufferSize)
            if count > 0 {
                data.append(buffer, count: count)
            }
        }
        
        guard let string = String(data: data, encoding: .utf8) else { throw NSError(domain: "PIDCSVFile", code: 1, userInfo: nil) }
        return string.components(separatedBy: .newlines)
    }
    
    private static func parseAddress(_ str: String, _ min: Int64, _ max: Int64) throws -> Int64 {
        let hexString = str.replacingOccurrences(of: "0x", with: "", options: .caseInsensitive)
        guard let address = Int64(hexString, radix: 16) else {
            throw NSError(domain: "PIDCSVFile", code: 2, userInfo: nil)
        }
        guard address >= min && address <= max else {
            throw NSError(domain: "PIDCSVFile", code: 3, userInfo: nil)
        }
        return address
    }
    
    private static func parseLength(_ str: String) throws -> Int {
        guard let length = Int(str), [1, 2, 4].contains(length) else {
            throw NSError(domain: "PIDCSVFile", code: 4, userInfo: nil)
        }
        return length
    }
    
    private static func parseBool(_ str: String) throws -> Bool {
        guard let bool = Bool(str) else {
            throw NSError(domain: "PIDCSVFile", code: 5, userInfo: nil)
        }
        return bool
    }
    
    private static func parseFloat(_ str: String) throws -> Float {
        guard let float = Float(str) else {
            throw NSError(domain: "PIDCSVFile", code: 6, userInfo: nil)
        }
        return float
    }
    
    private static func formatAddress(_ address: Int64) -> String {
        if (address & 0xFFFF0000) != 0 {
            return String(format: "0x%llX", address)
        } else {
            return String(format: "0x%04X", UInt16(address))
        }
    }
    
    private static func writeData(_ data: Data?, to stream: OutputStream) -> Bool {
        guard let data = data else { return false }
        let result = data.withUnsafeBytes {
            stream.write($0.bindMemory(to: UInt8.self).baseAddress!, maxLength: data.count)
        }
        return result == data.count
    }
}
