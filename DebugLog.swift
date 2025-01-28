import Foundation

class DebugLog {
    static let shared = DebugLog()
    private let tag = "DebugLog"
    
    private var writer: FileHandle?
    private var logQueue = DispatchQueue(label: "DebugLogQueue")
    private var flags: Options = []
    
    private lazy var dateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd HH:mm:ss.SSS"
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.timeZone = TimeZone.current
        return formatter
    }()
    
    struct Options: OptionSet {
        let rawValue: Int
        
        static let none = Options(rawValue: 0)
        static let info = Options(rawValue: 1 << 0)
        static let warning = Options(rawValue: 1 << 1)
        static let debug = Options(rawValue: 1 << 2)
        static let exception = Options(rawValue: 1 << 3)
        static let communications = Options(rawValue: 1 << 4)
        
        static let all: Options = [.info, .warning, .debug, .exception, .communications]
    }
    
    private init() {}
    
    func setFlags(_ options: Options) {
        logQueue.sync {
            flags = options
            d(tag, "Set debug flags to: \(options.rawValue)")
        }
    }
    
    func getFlags() -> Options {
        return logQueue.sync { flags }
    }
    
    func create(filename: String) {
        logQueue.sync {
            close()
            
            let fileManager = FileManager.default
            guard let docsURL = fileManager.urls(for: .documentDirectory, in: .userDomainMask).first else {
                print("Error getting documents directory")
                return
            }
            
            let logFileURL = docsURL.appendingPathComponent(filename)
            
            do {
                if !fileManager.fileExists(atPath: logFileURL.path) {
                    fileManager.createFile(atPath: logFileURL.path, contents: nil)
                }
                writer = try FileHandle(forWritingTo: logFileURL)
                writer?.seekToEndOfFile()
                i(tag, "Log open")
            } catch {
                print("Error opening debug log: \(error)")
            }
        }
    }
    
    func close() {
        logQueue.sync {
            i(tag, "Closing log")
            do {
                try writer?.close()
            } catch {
                print("Error closing log file: \(error)")
            }
            writer = nil
        }
    }
    
    func i(_ tag: String, _ text: String) {
        print("[INFO] \(tag): \(text)")
        logQueue.async { [weak self] in
            guard let self = self else { return }
            guard self.flags.contains(.info) else { return }
            self.writeLog("[I] \(tag): \(text)")
        }
    }
    
    func w(_ tag: String, _ text: String) {
        print("[WARN] \(tag): \(text)")
        logQueue.async { [weak self] in
            guard let self = self else { return }
            guard self.flags.contains(.warning) else { return }
            self.writeLog("[W] \(tag): \(text)")
        }
    }
    
    func d(_ tag: String, _ text: String) {
        print("[DEBUG] \(tag): \(text)")
        logQueue.async { [weak self] in
            guard let self = self else { return }
            guard self.flags.contains(.debug) else { return }
            self.writeLog("[D] \(tag): \(text)")
        }
    }
    
    func e(_ tag: String, _ text: String, error: Error) {
        print("[ERROR] \(tag): \(text) - \(error.localizedDescription)")
        logQueue.async { [weak self] in
            guard let self = self else { return }
            guard self.flags.contains(.exception) else { return }
            self.writeLog("[E] \(tag): \(text)\nError: \(error.localizedDescription)")
        }
    }
    
    func c(_ tag: String, data: Data?, from: Bool) {
        guard let data = data else { return }
        logQueue.async { [weak self] in
            guard let self = self else { return }
            guard self.flags.contains(.communications) else { return }
            
            let direction = from ? "->" : "<-"
            let hexString = data.hexEncodedString()
            self.writeLog("[C] \(tag): [\(data.count) \(direction)] \(hexString)")
        }
    }
    
    private func writeLog(_ message: String) {
        let timestamp = dateFormatter.string(from: Date())
        let fullMessage = "\(timestamp) \(message)\n"
        
        do {
            try writer?.write(contentsOf: fullMessage.data(using: .utf8) ?? Data())
        } catch {
            print("Error writing to log: \(error)")
        }
    }
}

extension Data {
    func hexEncodedString() -> String {
        return map { String(format: "%02hhx", $0) }.joined()
    }
}

---- usage example ----

// Initialize
DebugLog.shared.create(filename: "app.log")
DebugLog.shared.setFlags([.info, .warning, .error])

// Logging
DebugLog.shared.i("Network", "Connection established")
DebugLog.shared.c("Comm", data: someData, from: true)
