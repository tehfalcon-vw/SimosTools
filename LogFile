import Foundation

class LogFile {
    static let shared = LogFile()
    private init() {}
    
    private var outputStream: OutputStream?
    private var lastFileURL: URL?
    
    func create(fileName: String, subFolder: String) {
        close()
        
        let fileManager = FileManager.default
        let baseDirectory: URL
        
        switch ConfigSettings.outDirectory {
        case .app:
            baseDirectory = fileManager.urls(for: .applicationSupportDirectory, in: .userDomainMask).first!
        case .public:
            baseDirectory = fileManager.urls(for: .documentDirectory, in: .userDomainMask).first!
        }
        
        let subFolderURL = baseDirectory.appendingPathComponent(subFolder)
        
        do {
            try fileManager.createDirectory(at: subFolderURL, withIntermediateDirectories: true)
        } catch {
            print("Failed to create directory: \(error)")
            return
        }
        
        let fileURL = subFolderURL.appendingPathComponent(fileName)
        
        if !fileManager.fileExists(atPath: fileURL.path) {
            fileManager.createFile(atPath: fileURL.path, contents: nil)
        }
        
        guard let stream = OutputStream(url: fileURL, append: false) else {
            print("Unable to open file for logging: \(fileURL)")
            return
        }
        
        stream.open()
        outputStream = stream
        lastFileURL = fileURL
        print("Log opened: \(fileURL)")
    }
    
    func close() {
        outputStream?.close()
        outputStream = nil
        print("Log closed.")
    }
    
    func add(_ text: String) {
        guard let outputStream = outputStream else { return }
        let data = Data(text.utf8)
        data.withUnsafeBytes { buffer in
            outputStream.write(buffer.baseAddress!, maxLength: data.count)
        }
    }
    
    func addLine(_ text: String) {
        add(text + "\n")
    }
    
    func getLastFile() -> URL? {
        return lastFileURL
    }
    
    func getLastUri() -> URL? {
        return lastFileURL
    }
}

// MARK: - Supporting Types
enum Directory {
    case app
    case public
}

struct ConfigSettings {
    static var outDirectory: Directory = .app
}
