import SwiftUI
import ComposeApp
import StoreKit
import UIKit
import SwiftUI
import StoreKit
import AppsFlyerLib
// import Firebase
// import UserNotifications


@main
struct iOSApp: App {

    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    init() {
        KoinStarter.shared.start()
    }
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

class AppDelegate: NSObject,
                   UIApplicationDelegate,
                   AppsFlyerLibDelegate
    // UNUserNotificationCenterDelegate,
    // MessagingDelegate
{

    var purchaseIntentObserver: PurchaseIntentObserver?

    private let appsFlyerDevKey = "YOUR_DEV_KEY"
    private let appleAppID = "YOUR_APP_ID"

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil
    ) -> Bool {

        SKPaymentQueue.default().add(IOSPromotionBridge.shared)
        purchaseIntentObserver = PurchaseIntentObserver()
        print("⚡ PurchaseIntentObserver initialized")

        // FirebaseApp.configure()

        // let center = UNUserNotificationCenter.current()
        // center.delegate = self
        // Messaging.messaging().delegate = self

        let af = AppsFlyerLib.shared()
        af.appsFlyerDevKey = appsFlyerDevKey
        af.appleAppID = appleAppID
        af.delegate = self
        af.isDebug = false

        DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
            ATTManager.shared.requestTrackingIfNeeded { granted in

                AppsFlyerLib.shared().start()
                print("🎯 AppsFlyer started, ATT granted = \(granted)")

                // if granted {
                //     PushManager.shared.requestPushIfATTGranted { pushGranted in
                //         print("🔔 Push granted = \(pushGranted)")
                //     }
                // } else {
                //     print("📵 ATT not granted — skipping push registration")
                // }
            }
        }

        return true
    }

    func application(
        _ application: UIApplication,
        open url: URL,
        options: [UIApplication.OpenURLOptionsKey : Any] = [:]
    ) -> Bool {
        AppsFlyerLib.shared().handleOpen(url, options: options)
        return true
    }

    func application(
        _ application: UIApplication,
        continue userActivity: NSUserActivity,
        restorationHandler: @escaping ([UIUserActivityRestoring]?) -> Void
    ) -> Bool {
        AppsFlyerLib.shared().continue(userActivity, restorationHandler: nil)
        return true
    }

    func onConversionDataSuccess(_ data: [AnyHashable : Any]) {
        print("🎯 AppsFlyer Conversion Data: \(data)")
    }

    func onConversionDataFail(_ error: Error) {
        print("❌ AppsFlyer error: \(error.localizedDescription)")
    }

    // func application(_ application: UIApplication,
    //                  didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
    //     let token = deviceToken.map { String(format: "%02.2hhx", $0) }.joined()
    //     print("📲 APNs device token: \(token)")
    //     Messaging.messaging().apnsToken = deviceToken
    // }

    // func userNotificationCenter(_ center: UNUserNotificationCenter,
    //                             willPresent notification: UNNotification,
    //                             withCompletionHandler completionHandler:
    //                                 @escaping (UNNotificationPresentationOptions) -> Void) {
    //     print("📬 Push (foreground): \(notification.request.identifier)")
    //     completionHandler([.banner, .sound, .badge])
    // }

    // func userNotificationCenter(_ center: UNUserNotificationCenter,
    //                             didReceive response: UNNotificationResponse,
    //                             withCompletionHandler completionHandler: @escaping () -> Void) {
    //     print("📬 Push tapped: \(response.notification.request.identifier)")
    //     completionHandler()
    // }
}