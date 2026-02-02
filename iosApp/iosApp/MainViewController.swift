import UIKit
import SwiftUI
import shared

class MainViewController: UIHostingController<ContentView> {
    init() {
        super.init(rootView: ContentView())
    }
    
    @MainActor required dynamic init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder, rootView: ContentView())
    }
}
