import UIKit

// Global variable (consider refactoring into a singleton or view model)
var gUtilitiesMsgList: [String] = []

// Enums
enum BLEConnectionState {
    case none
    case connected
}

enum GUIMessage: String {
    case stateConnection = "STATE_CONNECTION"
    case stateTask = "STATE_TASK"
    case utilityInfo = "UTILITY_INFO"
    case utilityInfoClear = "UTILITY_INFO_CLEAR"
    case utilityProgress = "UTILITY_PROGRESS"
    case utilityProgressMax = "UTILITY_PROGRESS_MAX"
    case utilityProgressShow = "UTILITY_PROGRESS_SHOW"
}

enum BTServiceTask: String {
    case doGetInfo = "DO_GET_INFO"
    case doGetDTC = "DO_GET_DTC"
    case doClearDTC = "DO_CLEAR_DTC"
}

struct ColorList {
    static let BT_BG = UIColor.blue
    static let BT_RIM = UIColor.darkGray
    static let BT_TEXT = UIColor.white
    static let BG_NORMAL = UIColor.white
}

// View Model
class UtilitiesViewModel {
    var connectionState: BLEConnectionState = .none
}

// View Controller
class UtilitiesViewController: UIViewController, UITableViewDataSource {
    // UI Outlets
    @IBOutlet weak var listViewMessage: UITableView!
    @IBOutlet weak var buttonGetInfo: UIButton!
    @IBOutlet weak var buttonGetDTC: UIButton!
    @IBOutlet weak var buttonClearDTC: UIButton!
    @IBOutlet weak var buttonBack: UIButton!
    @IBOutlet weak var progressBarUtilities: UIProgressView!
    
    private let TAG = "UtilitiesViewController"
    private var viewModel = UtilitiesViewModel()
    private var messages: [String] = []
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        configureTableView()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        registerNotifications()
        updateScreenOnSetting()
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        NotificationCenter.default.removeObserver(self)
        UIApplication.shared.isIdleTimerDisabled = false
    }
    
    private func setupUI() {
        configureButtons()
        progressBarUtilities.isHidden = true
        view.backgroundColor = ColorList.BG_NORMAL
    }
    
    private func configureTableView() {
        listViewMessage.dataSource = self
        listViewMessage.backgroundColor = ColorList.BG_NORMAL
        messages = gUtilitiesMsgList
    }
    
    private func configureButtons() {
        let buttons = [buttonGetInfo, buttonGetDTC, buttonClearDTC, buttonBack]
        buttons.forEach {
            $0?.backgroundColor = ColorList.BT_BG
            $0?.layer.borderColor = ColorList.BT_RIM.cgColor
            $0?.layer.borderWidth = 1
            $0?.setTitleColor(ColorList.BT_TEXT, for: .normal)
        }
        
        buttonGetInfo.addTarget(self, action: #selector(getInfoTapped), for: .touchUpInside)
        buttonGetDTC.addTarget(self, action: #selector(getDTCTapped), for: .touchUpInside)
        buttonClearDTC.addTarget(self, action: #selector(clearDTCTapped), for: .touchUpInside)
        buttonClearDTC.addTarget(self, action: #selector(clearDTCLongPressed), for: .touchDownRepeat)
        buttonBack.addTarget(self, action: #selector(backTapped), for: .touchUpInside)
    }
    
    private func registerNotifications() {
        let notificationNames = [
            GUIMessage.stateConnection.rawValue,
            GUIMessage.stateTask.rawValue,
            GUIMessage.utilityInfo.rawValue,
            GUIMessage.utilityInfoClear.rawValue,
            GUIMessage.utilityProgress.rawValue,
            GUIMessage.utilityProgressMax.rawValue,
            GUIMessage.utilityProgressShow.rawValue
        ]
        
        notificationNames.forEach { name in
            NotificationCenter.default.addObserver(
                self,
                selector: #selector(handleNotification(_:)),
                name: Notification.Name(name),
                object: nil
            )
        }
    }
    
    private func updateScreenOnSetting() {
        // Replace with actual setting check
        let keepScreenOn = false
        UIApplication.shared.isIdleTimerDisabled = keepScreenOn
    }
    
    // MARK: - UITableViewDataSource
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return messages.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "MessageCell", for: indexPath)
        cell.textLabel?.text = messages[indexPath.row]
        return cell
    }
    
    // MARK: - Button Actions
    @objc private func getInfoTapped() {
        var ecuString = "Get Info\n---------------"
        if viewModel.connectionState == .connected {
            sendServiceMessage(type: .doGetInfo)
        } else {
            ecuString += "\nNot connected"
        }
        doWriteMessage(message: ecuString)
    }
    
    @objc private func getDTCTapped() {
        handleDTC(clear: false)
    }
    
    @objc private func clearDTCTapped() {
        doWriteMessage(message: "Clear DTC\n---------------\nHold button to clear DTC codes.")
    }
    
    @objc private func clearDTCLongPressed() {
        handleDTC(clear: true)
    }
    
    @objc private func backTapped() {
        navigationController?.popViewController(animated: true)
    }
    
    // MARK: - Notification Handling
    @objc private func handleNotification(_ notification: Notification) {
        guard let messageType = GUIMessage(rawValue: notification.name.rawValue) else { return }
        
        DispatchQueue.main.async {
            switch messageType {
            case .stateConnection:
                if let state = notification.userInfo?[GUIMessage.stateConnection.rawValue] as? BLEConnectionState {
                    self.viewModel.connectionState = state
                }
            case .stateTask:
                self.viewModel.connectionState = .connected
            case .utilityInfo:
                let message = notification.userInfo?[GUIMessage.utilityInfo.rawValue] as? String ?? ""
                self.doWriteMessage(message: message)
            case .utilityInfoClear:
                self.doClearMessages()
            case .utilityProgress:
                let progress = notification.userInfo?[GUIMessage.utilityProgress.rawValue] as? Int ?? 0
                self.setProgressBar(amount: progress)
            case .utilityProgressMax:
                let max = notification.userInfo?[GUIMessage.utilityProgressMax.rawValue] as? Int ?? 0
                self.setProgressBarMax(amount: max)
            case .utilityProgressShow:
                let show = notification.userInfo?[GUIMessage.utilityProgressShow.rawValue] as? Bool ?? false
                self.setProgressBarShow(allow: show)
            }
        }
    }
    
    // MARK: - Helper Methods
    private func handleDTC(clear: Bool) {
        var dtcString = clear ? "Clear DTC\n---------------" : "Get DTC\n---------------"
        if viewModel.connectionState == .connected {
            let task: BTServiceTask = clear ? .doClearDTC : .doGetDTC
            sendServiceMessage(type: task)
        } else {
            dtcString += "\nNot connected."
        }
        doWriteMessage(message: dtcString)
    }
    
    private func doClearMessages() {
        gUtilitiesMsgList = []
        messages = []
        listViewMessage.reloadData()
    }
    
    private func doWriteMessage(message: String) {
        gUtilitiesMsgList.append(message)
        messages = gUtilitiesMsgList
        listViewMessage.reloadData()
        scrollToBottom()
    }
    
    private func scrollToBottom() {
        let indexPath = IndexPath(row: messages.count - 1, section: 0)
        listViewMessage.scrollToRow(at: indexPath, at: .bottom, animated: true)
    }
    
    private func setProgressBar(amount: Int) {
        progressBarUtilities.progress = Float(amount) / Float(progressBarUtilities.tag)
    }
    
    private func setProgressBarMax(amount: Int) {
        progressBarUtilities.tag = amount
    }
    
    private func setProgressBarShow(allow: Bool) {
        progressBarUtilities.isHidden = !allow
    }
    
    private func sendServiceMessage(type: BTServiceTask) {
        // Replace with actual BLE service communication
        NotificationCenter.default.post(
            name: Notification.Name(type.rawValue),
            object: nil
        )
    }
}
