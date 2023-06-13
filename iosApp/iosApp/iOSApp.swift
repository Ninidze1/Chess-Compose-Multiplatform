import SwiftUI
import shared

@main
struct iOSApp: App {
    
    init() {
        StartKoinKt.doInitKoin()
    }
        
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
