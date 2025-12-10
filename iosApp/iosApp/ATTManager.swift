import AppTrackingTransparency
import Foundation

final class ATTManager {
    static let shared = ATTManager()
    private init() {}

    func requestTrackingIfNeeded(completion: @escaping (Bool) -> Void) {
        guard #available(iOS 14, *) else {
            completion(false)
            return
        }

        let status = ATTrackingManager.trackingAuthorizationStatus

        guard status == .notDetermined else {
            completion(status == .authorized)
            return
        }

        ATTrackingManager.requestTrackingAuthorization { newStatus in
            completion(newStatus == .authorized)
        }
    }
}