import SwiftUI
import Shared

@main
struct iOSApp: App {

    init() {
        // Initialize Firebase
        FirebaseInit.shared.initialize()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}