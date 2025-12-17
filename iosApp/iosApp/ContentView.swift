import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(
        _ uiViewController: UIViewController,
        context: Context
    ) {}
}

struct ContentView: View {

    @State private var didRequestATT = false

    var body: some View {
        ComposeView()
            .ignoresSafeArea()
            .onAppear {
                requestATTOnce()
            }
    }

    private func requestATTOnce() {
        guard !didRequestATT else { return }
        didRequestATT = true

        DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
            ATTManager.shared.requestTrackingIfNeeded { granted in
                print("📡 ATT permission granted: \(granted)")
            }
        }
    }
}


