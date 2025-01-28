import CoreBluetooth
import os

// MARK: - Constants
let BLE_SERVICE_UUID = CBUUID(string: "0000XXXX-0000-1000-8000-00805F9B34FB") // Replace with actual UUID
let BLE_DATA_RX_UUID = CBUUID(string: "0000XXXX-0000-1000-8000-00805F9B34FB") // Replace
let BLE_DATA_TX_UUID = CBUUID(string: "0000XXXX-0000-1000-8000-00805F9B34FB") // Replace
let BLE_CCCD_UUID = CBUUID(string: "00002902-0000-1000-8000-00805F9B34FB")

// MARK: - BLEHeader Struct
struct BLEHeader {
    var hdID: UInt8 = 0
    var cmdFlags: UInt8 = 0
    var rxID: UInt16 = 0
    var txID: UInt16 = 0
    var cmdSize: UInt16 = 0
    var tickCount: UInt32 = 0
    
    init() {}
    
    init?(data: Data) {
        guard data.count >= 8 else { return nil }
        
        hdID = data[0]
        cmdFlags = data[1]
        rxID = UInt16(data[2]) | UInt16(data[3]) << 8
        txID = UInt16(data[4]) | UInt16(data[5]) << 8
        cmdSize = UInt16(data[6]) | UInt16(data[7]) << 8
        tickCount = (UInt32(rxID) << 16) | UInt32(txID)
    }
    
    func toData() -> Data {
        var data = Data()
        data.append(hdID)
        data.append(cmdFlags)
        data.append(contentsOf: rxID.bytes)
        data.append(contentsOf: txID.bytes)
        data.append(contentsOf: cmdSize.bytes)
        return data
    }
}

// MARK: - BLE Manager
class BLEManager: NSObject, CBCentralManagerDelegate, CBPeripheralDelegate {
    static let shared = BLEManager()
    
    private var centralManager: CBCentralManager!
    private var connectedPeripheral: CBPeripheral?
    private var dataRxCharacteristic: CBCharacteristic?
    private var dataTxCharacteristic: CBCharacteristic?
    
    private let writeQueue = DispatchQueue(label: "com.youapp.writeQueue")
    private let semaphore = DispatchSemaphore(value: 1)
    private var isScanning = false
    
    var connectionState = BLEConnectionState.none {
        didSet {
            notifyStateChange()
        }
    }
    
    override init() {
        super.init()
        centralManager = CBCentralManager(delegate: self, queue: nil)
    }
    
    // MARK: - Public Methods
    func connect() {
        guard centralManager.state == .poweredOn else { return }
        stopScanning()
        startScanning()
    }
    
    func disconnect() {
        if let peripheral = connectedPeripheral {
            centralManager.cancelPeripheralConnection(peripheral)
        }
        connectedPeripheral = nil
        connectionState = .none
    }
    
    // MARK: - CBCentralManagerDelegate
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        // Handle state changes
    }
    
    func centralManager(_ central: CBCentralManager, 
                        didDiscover peripheral: CBPeripheral,
                        advertisementData: [String : Any], 
                        rssi RSSI: NSNumber) {
        guard peripheral.name == "YourDeviceName" else { return }
        central.stopScan()
        connectedPeripheral = peripheral
        peripheral.delegate = self
        central.connect(peripheral, options: nil)
    }
    
    func centralManager(_ central: CBCentralManager, 
                        didConnect peripheral: CBPeripheral) {
        peripheral.discoverServices([BLE_SERVICE_UUID])
    }
    
    // MARK: - CBPeripheralDelegate
    func peripheral(_ peripheral: CBPeripheral, 
                    didDiscoverServices error: Error?) {
        guard let services = peripheral.services else { return }
        for service in services {
            if service.uuid == BLE_SERVICE_UUID {
                peripheral.discoverCharacteristics([BLE_DATA_RX_UUID, BLE_DATA_TX_UUID], 
                                                   for: service)
            }
        }
    }
    
    func peripheral(_ peripheral: CBPeripheral,
                    didDiscoverCharacteristicsFor service: CBService,
                    error: Error?) {
        guard let characteristics = service.characteristics else { return }
        
        for characteristic in characteristics {
            switch characteristic.uuid {
            case BLE_DATA_RX_UUID:
                dataRxCharacteristic = characteristic
                peripheral.setNotifyValue(true, for: characteristic)
                
            case BLE_DATA_TX_UUID:
                dataTxCharacteristic = characteristic
                
            default:
                break
            }
        }
        
        connectionState = .connected
    }
    
    func peripheral(_ peripheral: CBPeripheral,
                    didUpdateValueFor characteristic: CBCharacteristic,
                    error: Error?) {
        guard let data = characteristic.value else { return }
        processIncomingData(data)
    }
    
    // MARK: - Data Processing
    private func processIncomingData(_ data: Data) {
        // Implement packet parsing similar to Android version
        guard let header = BLEHeader(data: data) else { return }
        
        let payload = data.subdata(in: 8..<data.count)
        // Handle payload based on header
    }
    
    private func writeData(_ data: Data) {
        guard let characteristic = dataTxCharacteristic else { return }
        
        writeQueue.async { [weak self] in
            self?.semaphore.wait()
            self?.connectedPeripheral?.writeValue(data, 
                                                 for: characteristic,
                                                 type: .withResponse)
            // Need to implement proper semaphore handling
            self?.semaphore.signal()
        }
    }
    
    // MARK: - Private Methods
    private func startScanning() {
        guard !isScanning else { return }
        isScanning = true
        centralManager.scanForPeripherals(withServices: [BLE_SERVICE_UUID], 
                                         options: nil)
    }
    
    private func stopScanning() {
        guard isScanning else { return }
        centralManager.stopScan()
        isScanning = false
    }
    
    private func notifyStateChange() {
        NotificationCenter.default.post(name: .bleConnectionStateChanged, 
                                        object: nil)
    }
}

// MARK: - Enums & Extensions
enum BLEConnectionState {
    case none
    case connecting
    case connected
    case error(String)
}

extension UInt16 {
    var bytes: [UInt8] {
        return [UInt8(self & 0xFF), UInt8(self >> 8)]
    }
}

extension Notification.Name {
    static let bleConnectionStateChanged = Notification.Name("BLEConnectionStateChanged")
}
