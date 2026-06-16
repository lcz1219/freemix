import UIKit
import Capacitor
import UserNotifications
import WebKit

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate, UNUserNotificationCenterDelegate, WKScriptMessageHandler {

    var window: UIWindow?
    private var isWebViewReady = false
    /// 冷启动时的快捷操作类型
    private var pendingShortcutType: String?

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        // 启用 iOS 右滑返回手势
        DispatchQueue.main.async { [weak self] in
            self?.enableSwipeBackGesture(retry: 3)
        }

        // 注册本地通知
        registerForNotifications(application)

        // 冷启动快捷操作
        if let shortcutItem = launchOptions?[.shortcutItem] as? UIApplicationShortcutItem {
            pendingShortcutType = shortcutItem.type
        }

        return true
    }

    // MARK: - Quick Actions

    func application(_ application: UIApplication, performActionFor shortcutItem: UIApplicationShortcutItem, completionHandler: @escaping (Bool) -> Void) {
        if isWebViewReady {
            notifyJSQuickAction(shortcutItem.type)
        }
        completionHandler(true)
    }

    // MARK: - URL Scheme

    func application(_ app: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey: Any] = [:]) -> Bool {
        return ApplicationDelegateProxy.shared.application(app, open: url, options: options)
    }

    func application(_ application: UIApplication, continue userActivity: NSUserActivity, restorationHandler: @escaping ([UIUserActivityRestoring]?) -> Void) -> Bool {
        return ApplicationDelegateProxy.shared.application(application, continue: userActivity, restorationHandler: restorationHandler)
    }

    // MARK: - 本地通知注册

    private func registerForNotifications(_ application: UIApplication) {
        let center = UNUserNotificationCenter.current()
        center.delegate = self

        // 目标到期提醒：可标记完成 / 查看详情
        let completeAction = UNNotificationAction(identifier: "COMPLETE_GOAL", title: "标记完成", options: [.authenticationRequired])
        let viewAction = UNNotificationAction(identifier: "VIEW_GOAL", title: "查看详情", options: [.foreground])
        let goalCategory = UNNotificationCategory(identifier: "GOAL_REMINDER", actions: [completeAction, viewAction], intentIdentifiers: [], options: [.customDismissAction])

        // 每日概览
        let todayViewAction = UNNotificationAction(identifier: "VIEW_TODAY", title: "查看今日目标", options: [.foreground])
        let dailyCategory = UNNotificationCategory(identifier: "DAILY_SUMMARY", actions: [todayViewAction], intentIdentifiers: [], options: [.customDismissAction])

        // AI 晨报
        let aiViewAction = UNNotificationAction(identifier: "VIEW_AI_MORNING", title: "查看 AI 分析", options: [.foreground])
        let aiCategory = UNNotificationCategory(identifier: "AI_MORNING", actions: [aiViewAction], intentIdentifiers: [], options: [.customDismissAction])

        // JS 调用原生弹出的通知（只读）
        let jsNotifyCategory = UNNotificationCategory(identifier: "JS_NOTIFY", actions: [], intentIdentifiers: [], options: [.customDismissAction])

        center.setNotificationCategories([goalCategory, dailyCategory, aiCategory, jsNotifyCategory])

        center.requestAuthorization(options: [.alert, .sound, .badge]) { granted, error in
            if granted {
                DispatchQueue.main.async {
                    application.registerForRemoteNotifications()
                }
            }
        }
    }

    // MARK: - WKScriptMessageHandler (JS → 原生发送通知)

    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        guard message.name == "freemix",
              let body = message.body as? [String: Any],
              let action = body["action"] as? String,
              action == "showNotification" else { return }

        let title = body["title"] as? String ?? ""
        let notifyBody = body["body"] as? String ?? ""

        let content = UNMutableNotificationContent()
        content.title = title
        content.body = notifyBody
        content.sound = .default
        content.categoryIdentifier = "JS_NOTIFY"

        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: 1, repeats: false)
        let request = UNNotificationRequest(identifier: "js-notify-\(Date().timeIntervalSince1970)", content: content, trigger: trigger)
        UNUserNotificationCenter.current().add(request)
    }

    // MARK: - UNUserNotificationCenterDelegate

    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                willPresent notification: UNNotification,
                                withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        completionHandler([.banner, .sound, .badge])
    }

    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                didReceive response: UNNotificationResponse,
                                withCompletionHandler completionHandler: @escaping () -> Void) {
        guard isWebViewReady else { completionHandler(); return }

        let userInfo = response.notification.request.content.userInfo
        let goalId = userInfo["goalId"] as? String ?? ""
        let goalTitle = userInfo["goalTitle"] as? String ?? ""
        let notifId = response.notification.request.identifier

        switch response.actionIdentifier {
        case "COMPLETE_GOAL":
            notifyJSAction(goalId: goalId, goalTitle: goalTitle, action: "complete")
        case "VIEW_GOAL", UNNotificationDefaultActionIdentifier:
            // 如果是 AI 晨报通知被点击，触发 AI 分析
            if notifId == "ai-morning-report" {
                notifyJSAction(goalId: "", goalTitle: "", action: "ai-morning")
            } else {
                notifyJSAction(goalId: goalId, goalTitle: goalTitle, action: "view")
            }
        case "VIEW_TODAY":
            notifyJSRoute(route: "/home")
        case "VIEW_AI_MORNING":
            notifyJSAction(goalId: "", goalTitle: "", action: "ai-morning")
        default:
            break
        }
        completionHandler()
    }

    // MARK: - 通知调度（支持自定义时间）

    private func scheduleAllNotifications() {
        guard let vc = window?.rootViewController as? CAPBridgeViewController,
              let webView = vc.webView,
              isWebViewReady else { return }

        let js = "(function(){var d=window.freemixGoalsData;return d?JSON.stringify(d):'[]';})()"
        webView.evaluateJavaScript(js) { [weak self] result, error in
            guard let self = self else { return }

            let center = UNUserNotificationCenter.current()
            center.removeAllPendingNotificationRequests()

            let now = Date()
            let isoFormatter = ISO8601DateFormatter()
            isoFormatter.formatOptions = [.withInternetDateTime, .withFractionalSeconds]

            if let jsonStr = result as? String, !jsonStr.isEmpty,
               let data = jsonStr.data(using: .utf8),
               let goals = try? JSONSerialization.jsonObject(with: data) as? [[String: Any]],
               !goals.isEmpty {

                var count = 0
                // 收集通知日志，调度完成后批量保存到 MongoDB
                var notificationLogs: [[String: Any]] = []

                for goal in goals {
                    guard let goalId = goal["_id"] as? String,
                          let title = goal["title"] as? String,
                          let status = goal["status"] as? String,
                          status == "in-progress" || status == "active" else { continue }

                    // 解析 deadline：支持 ISO 8601 字符串和时间戳两种格式
                    var deadline: Date?
                    if let deadlineStr = goal["deadline"] as? String {
                        // 尝试 ISO 8601 格式：2026-03-20T08:00:00.000+00:00
                        deadline = isoFormatter.date(from: deadlineStr)
                        // 如果带小数秒解析失败，去掉小数秒再试
                        if deadline == nil {
                            let cleaned = deadlineStr.replacingOccurrences(of: "\\.\\d+", with: "", options: .regularExpression)
                            deadline = isoFormatter.date(from: cleaned)
                        }
                        // 最后尝试时间戳字符串
                        if deadline == nil, let ms = Double(deadlineStr) {
                            deadline = Date(timeIntervalSince1970: ms / 1000)
                        }
                    } else if let ms = goal["deadline"] as? Double {
                        deadline = Date(timeIntervalSince1970: ms / 1000)
                    }
                    guard let deadlineDate = deadline, deadlineDate > now else { continue }

                    // 计算提醒时间范围：从 deadline 前一天开始，到 deadline 当天截止
                    let calendar = Calendar.current
                    
                    // 提醒时间点：9:25, 12:00, 15:00, 18:00, 21:00
                    let reminderTimes: [(Int, Int)] = [(9, 00), (12, 00), (15, 0), (18, 0), (21, 0)]
                    
                    // 开始日期：deadline 前一天
                    guard let startDate = calendar.date(byAdding: .day, value: -1, to: deadlineDate) else { continue }
                    
                    // 结束日期：deadline 当天
                    let endDate = deadlineDate
                         notificationLogs.append([
                                "title": "⏰ 目标即将到期",
                                "body": "「\(title)」距离截止日期越来越近了，去检查一下完成进度吧",
                                "type": "goal_reminder",
                                "goalId": goalId,
                                "goalTitle": title,
                                
                            ])
                    for (hour, minute) in reminderTimes {
                        // 前一天的时间点
                        if let remindTime1 = calendar.date(bySettingHour: hour, minute: minute, second: 0, of: startDate),
                           remindTime1 > now {
                            scheduleGoalReminder(center: center, goalId: goalId, title: title, at: remindTime1, count: count)
                            // 记录通知日志，调度完成后批量保存到 MongoDB
                           
                            count += 1
                        }
                        
                        // deadline 当天的时间点，但不超过 deadline 本身
                        if let remindTime2 = calendar.date(bySettingHour: hour, minute: minute, second: 0, of: endDate),
                           remindTime2 > now,
                           remindTime2 <= endDate {
                            scheduleGoalReminder(center: center, goalId: goalId, title: title, at: remindTime2, count: count)
                            // 记录通知日志
                            // notificationLogs.append([
                            //     "title": "⏰ 目标即将到期",
                            //     "body": "「\(title)」距离截止日期越来越近了，去检查一下完成进度吧",
                            //     "type": "goal_reminder",
                            //     "goalId": goalId,
                            //     "goalTitle": title,
                            //     "createdAt": Int64(remindTime2.timeIntervalSince1970 * 1000)
                            // ])
                            count += 1
                        }
                        
                        if count >= 64 { break } // 最多 64 个通知（iOS 限制一个 App 最多 64 个待发通知）
                    }
                    if count >= 64 { break }
                }

                // 通知调度完成，批量保存到 MongoDB（通过 JS 桥接调用 saveNotificationLogs）
                if !notificationLogs.isEmpty,
                   let logData = try? JSONSerialization.data(withJSONObject: notificationLogs),
                   let logJson = String(data: logData, encoding: .utf8) {
                    let safeJson = logJson.replacingOccurrences(of: "\\", with: "\\\\")
                                         .replacingOccurrences(of: "'", with: "\\'")
                    let saveJs = "if(window.saveNotificationLogs){window.saveNotificationLogs(\(safeJson))}"
                    webView.evaluateJavaScript(saveJs, completionHandler: nil)
                }
            } else {
                // 无目标时，简单每日提醒
                let daily = UNMutableNotificationContent()
                daily.title = "FreeMix"
                daily.body = "今天有什么新目标吗？打开 App 记录一下吧"
                daily.sound = .default
                daily.categoryIdentifier = "DAILY_SUMMARY"
                var dc = DateComponents(); dc.hour = 9; dc.minute = 0
                let req = UNNotificationRequest(identifier: "daily-reminder", content: daily, trigger: UNCalendarNotificationTrigger(dateMatching: dc, repeats: true))
                center.add(req)
                
                // 也将每日提醒保存到 MongoDB
                let dailyNotification: [[String: Any]] = [[
                    "title": "FreeMix",
                    "body": "今天有什么新目标吗？打开 App 记录一下吧",
                    "type": "daily_summary",
                    "goalId": "",
                    "goalTitle": "",
                    "createdAt": Int64(Date().timeIntervalSince1970 * 1000)
                ]]
                if let logData = try? JSONSerialization.data(withJSONObject: dailyNotification),
                   let logJson = String(data: logData, encoding: .utf8) {
                    let safeJson = logJson.replacingOccurrences(of: "\\", with: "\\\\")
                                         .replacingOccurrences(of: "'", with: "\\'")
                    let saveJs = "if(window.saveNotificationLogs){window.saveNotificationLogs(\(safeJson))}"
                    webView.evaluateJavaScript(saveJs, completionHandler: nil)
                }
            }

            NSLog("[FreeMix] 通知已调度完成")
            center.getPendingNotificationRequests { requests in
                NSLog("[FreeMix] 待发通知: \(requests.count) 条")
            }
        }
    }

    /// 为某个目标在指定时间创建一条到期提醒通知
    private func scheduleGoalReminder(center: UNUserNotificationCenter, goalId: String, title: String, at date: Date, count: Int) {
        let content = UNMutableNotificationContent()
        content.title = "⏰ 目标即将到期"
        content.body = "「\(title)」距离截止日期越来越近了，去检查一下完成进度吧"
        content.sound = .default
        content.badge = NSNumber(value: (count % 10) + 1)
        content.categoryIdentifier = "GOAL_REMINDER"
        content.userInfo = ["goalId": goalId, "goalTitle": title]

        let components = Calendar.current.dateComponents([.year, .month, .day, .hour, .minute], from: date)
        let trigger = UNCalendarNotificationTrigger(dateMatching: components, repeats: false)
        let identifier = "goal-\(goalId)-\(Int(date.timeIntervalSince1970))"
        let req = UNNotificationRequest(identifier: identifier, content: content, trigger: trigger)
        center.add(req)
    }

    private func notifyJSQuickAction(_ type: String) {
        guard isWebViewReady,
              let vc = window?.rootViewController as? CAPBridgeViewController,
              let webView = vc.webView else { return }
        let route: String
        switch type {
        case "create-goal": route = "/add-goal"
        case "today-goals":  route = "/home"
        case "ai-assistant": route = "/AIAssistantWindow"
        case "scan-login":   route = "/mobile/scan"
        default: return
        }
        let js = "window.dispatchEvent(new CustomEvent('quick-action',{detail:{route:'\(route)',type:'\(type)'}}));"
        webView.evaluateJavaScript(js, completionHandler: nil)
    }

    private func notifyJSAction(goalId: String, goalTitle: String, action: String) {
        guard isWebViewReady,
              let vc = window?.rootViewController as? CAPBridgeViewController,
              let webView = vc.webView else { return }
        let safeTitle = goalTitle.replacingOccurrences(of: "'", with: "\\'")
        let js = "window.dispatchEvent(new CustomEvent('notification-action',{detail:{goalId:'\(goalId)',goalTitle:'\(safeTitle)',action:'\(action)'}}));"
        webView.evaluateJavaScript(js, completionHandler: nil)
    }

    private func notifyJSRoute(route: String) {
        guard isWebViewReady,
              let vc = window?.rootViewController as? CAPBridgeViewController,
              let webView = vc.webView else { return }
        let js = "window.dispatchEvent(new CustomEvent('notification-action',{detail:{route:'\(route)',action:'navigate'}}));"
        webView.evaluateJavaScript(js, completionHandler: nil)
    }

    // MARK: - 右滑返回手势 + 冷启动快捷操作

    private func enableSwipeBackGesture(retry: Int) {
        guard let vc = window?.rootViewController as? CAPBridgeViewController,
              let webView = vc.webView else {
            if retry > 0 {
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) { [weak self] in
                    self?.enableSwipeBackGesture(retry: retry - 1)
                }
            }
            return
        }
        webView.allowsBackForwardNavigationGestures = true
        
        // 开启 iOS 16.4+ Web Inspector 调试支持 (允许 Release 包也能调试)
        if #available(iOS 16.4, *) {
            webView.isInspectable = true
        }

        // 注册 WKScriptMessageHandler，供 JS 调用原生发通知
        webView.configuration.userContentController.add(self, name: "freemix")

        isWebViewReady = true

        // 冷启动快捷操作：延时多次发送
        if let type = pendingShortcutType {
            pendingShortcutType = nil
            notifyJSQuickAction(type)
            DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) { [weak self] in
                self?.notifyJSQuickAction(type)
            }
            DispatchQueue.main.asyncAfter(deadline: .now() + 5.0) { [weak self] in
                self?.notifyJSQuickAction(type)
            }
        }
    }

    // MARK: - 应用生命周期

    func applicationWillResignActive(_ application: UIApplication) {}

    func applicationDidEnterBackground(_ application: UIApplication) {
        scheduleAllNotifications()
    }

    func applicationWillEnterForeground(_ application: UIApplication) {
        application.applicationIconBadgeNumber = 0
    }

    func applicationDidBecomeActive(_ application: UIApplication) {
        application.applicationIconBadgeNumber = 0
        // 回到前台时重新调度通知，已完成的目标会自动移除后续提醒
        scheduleAllNotifications()
    }

    func applicationWillTerminate(_ application: UIApplication) {}

}
