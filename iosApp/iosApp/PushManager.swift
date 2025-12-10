import Foundation
import UserNotifications
import UIKit
import AppTrackingTransparency

final class PushManager {

    static let shared = PushManager()
    private init() {}

    func requestPushIfATTGranted(completion: @escaping (Bool) -> Void) {

        let attStatus = ATTrackingManager.trackingAuthorizationStatus
        guard attStatus == .authorized else {
            completion(false)
            return
        }

        let center = UNUserNotificationCenter.current()

        center.getNotificationSettings { settings in

            guard settings.authorizationStatus == .notDetermined else {
                completion(settings.authorizationStatus == .authorized)
                return
            }

            center.requestAuthorization(options: [.alert, .badge, .sound]) { granted, _ in

                if granted {
                    DispatchQueue.main.async {
                        UIApplication.shared.registerForRemoteNotifications()
                    }
                }

                completion(granted)
            }
        }
    }
}