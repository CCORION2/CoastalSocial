import SwiftUI
import shared

struct ContentView: View {
    @State private var isLoggedIn = false
    @State private var currentUser: User? = nil
    
    var body: some View {
        NavigationView {
            if isLoggedIn {
                MainTabView(user: currentUser, onLogout: {
                    isLoggedIn = false
                    currentUser = nil
                })
            } else {
                LoginView(onLoginSuccess: { user in
                    currentUser = user
                    isLoggedIn = true
                })
            }
        }
        .accentColor(.blue)
    }
}

struct MainTabView: View {
    let user: User?
    let onLogout: () -> Void
    
    var body: some View {
        TabView {
            FeedView()
                .tabItem {
                    Image(systemName: "house.fill")
                    Text("Feed")
                }
            
            SearchView()
                .tabItem {
                    Image(systemName: "magnifyingglass")
                    Text("Suchen")
                }
            
            NotificationsView()
                .tabItem {
                    Image(systemName: "bell.fill")
                    Text("Mitteilungen")
                }
            
            ProfileView(username: user?.username ?? "", onLogout: onLogout)
                .tabItem {
                    Image(systemName: "person.fill")
                    Text("Profil")
                }
        }
    }
}

struct LoginView: View {
    let onLoginSuccess: (User?) -> Void
    
    @State private var email = ""
    @State private var password = ""
    @State private var isLoading = false
    @State private var errorMessage: String? = nil
    @State private var showRegister = false
    
    var body: some View {
        VStack(spacing: 20) {
            Spacer()
            
            // Logo
            Image(systemName: "wave.3.right.circle.fill")
                .resizable()
                .frame(width: 100, height: 100)
                .foregroundColor(.blue)
            
            Text("CoastalSocial")
                .font(.largeTitle)
                .fontWeight(.bold)
            
            Spacer()
            
            // Login Form
            VStack(spacing: 16) {
                TextField("E-Mail", text: $email)
                    .textFieldStyle(RoundedBorderTextFieldStyle())
                    .keyboardType(.emailAddress)
                    .autocapitalization(.none)
                
                SecureField("Passwort", text: $password)
                    .textFieldStyle(RoundedBorderTextFieldStyle())
                
                if let error = errorMessage {
                    Text(error)
                        .foregroundColor(.red)
                        .font(.caption)
                }
                
                Button(action: login) {
                    if isLoading {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: .white))
                    } else {
                        Text("Anmelden")
                            .fontWeight(.semibold)
                    }
                }
                .frame(maxWidth: .infinity)
                .padding()
                .background(Color.blue)
                .foregroundColor(.white)
                .cornerRadius(10)
                .disabled(isLoading)
            }
            .padding(.horizontal, 32)
            
            Button("Noch kein Konto? Registrieren") {
                showRegister = true
            }
            .foregroundColor(.blue)
            
            Spacer()
        }
        .sheet(isPresented: $showRegister) {
            RegisterView(onRegisterSuccess: onLoginSuccess)
        }
    }
    
    private func login() {
        guard !email.isEmpty, !password.isEmpty else {
            errorMessage = "Bitte alle Felder ausfüllen"
            return
        }
        
        isLoading = true
        errorMessage = nil
        
        // Call shared Kotlin code
        Task {
            // TODO: Implement actual API call using shared module
            // For now, simulate login
            try? await Task.sleep(nanoseconds: 1_000_000_000)
            
            await MainActor.run {
                isLoading = false
                // Simulate successful login
                onLoginSuccess(nil)
            }
        }
    }
}

struct RegisterView: View {
    let onRegisterSuccess: (User?) -> Void
    @Environment(\.dismiss) var dismiss
    
    @State private var username = ""
    @State private var email = ""
    @State private var password = ""
    @State private var confirmPassword = ""
    @State private var isLoading = false
    @State private var errorMessage: String? = nil
    
    var body: some View {
        NavigationView {
            VStack(spacing: 20) {
                VStack(spacing: 16) {
                    TextField("Benutzername", text: $username)
                        .textFieldStyle(RoundedBorderTextFieldStyle())
                        .autocapitalization(.none)
                    
                    TextField("E-Mail", text: $email)
                        .textFieldStyle(RoundedBorderTextFieldStyle())
                        .keyboardType(.emailAddress)
                        .autocapitalization(.none)
                    
                    SecureField("Passwort", text: $password)
                        .textFieldStyle(RoundedBorderTextFieldStyle())
                    
                    SecureField("Passwort bestätigen", text: $confirmPassword)
                        .textFieldStyle(RoundedBorderTextFieldStyle())
                    
                    if let error = errorMessage {
                        Text(error)
                            .foregroundColor(.red)
                            .font(.caption)
                    }
                    
                    Button(action: register) {
                        if isLoading {
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: .white))
                        } else {
                            Text("Registrieren")
                                .fontWeight(.semibold)
                        }
                    }
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.blue)
                    .foregroundColor(.white)
                    .cornerRadius(10)
                    .disabled(isLoading)
                }
                .padding(.horizontal, 32)
                
                Spacer()
            }
            .padding(.top, 32)
            .navigationTitle("Registrieren")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Abbrechen") {
                        dismiss()
                    }
                }
            }
        }
    }
    
    private func register() {
        guard !username.isEmpty, !email.isEmpty, !password.isEmpty else {
            errorMessage = "Bitte alle Felder ausfüllen"
            return
        }
        
        guard password == confirmPassword else {
            errorMessage = "Passwörter stimmen nicht überein"
            return
        }
        
        isLoading = true
        errorMessage = nil
        
        Task {
            try? await Task.sleep(nanoseconds: 1_000_000_000)
            
            await MainActor.run {
                isLoading = false
                onRegisterSuccess(nil)
                dismiss()
            }
        }
    }
}

struct FeedView: View {
    @State private var posts: [PostItem] = []
    @State private var isLoading = true
    
    var body: some View {
        NavigationView {
            ScrollView {
                LazyVStack(spacing: 16) {
                    ForEach(posts) { post in
                        PostCard(post: post)
                    }
                }
                .padding()
            }
            .navigationTitle("Feed")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: {}) {
                        Image(systemName: "plus.circle.fill")
                            .font(.title2)
                    }
                }
            }
            .refreshable {
                await loadPosts()
            }
        }
        .onAppear {
            Task {
                await loadPosts()
            }
        }
    }
    
    private func loadPosts() async {
        // TODO: Load posts from shared module
        isLoading = false
    }
}

struct PostItem: Identifiable {
    let id: Int
    let username: String
    let displayName: String?
    let content: String
    let imageUrl: String?
    let likesCount: Int
    let commentsCount: Int
    let isLiked: Bool
    let createdAt: String
}

struct PostCard: View {
    let post: PostItem
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            // Header
            HStack {
                Circle()
                    .fill(Color.gray.opacity(0.3))
                    .frame(width: 40, height: 40)
                
                VStack(alignment: .leading) {
                    Text(post.displayName ?? post.username)
                        .fontWeight(.semibold)
                    Text("@\(post.username)")
                        .font(.caption)
                        .foregroundColor(.gray)
                }
                
                Spacer()
                
                Button(action: {}) {
                    Image(systemName: "ellipsis")
                        .foregroundColor(.gray)
                }
            }
            
            // Content
            Text(post.content)
            
            // Image (if exists)
            if let _ = post.imageUrl {
                Rectangle()
                    .fill(Color.gray.opacity(0.2))
                    .aspectRatio(16/9, contentMode: .fit)
                    .cornerRadius(12)
            }
            
            // Actions
            HStack(spacing: 24) {
                Button(action: {}) {
                    Label("\(post.likesCount)", systemImage: post.isLiked ? "heart.fill" : "heart")
                        .foregroundColor(post.isLiked ? .red : .gray)
                }
                
                Button(action: {}) {
                    Label("\(post.commentsCount)", systemImage: "bubble.right")
                        .foregroundColor(.gray)
                }
                
                Button(action: {}) {
                    Image(systemName: "square.and.arrow.up")
                        .foregroundColor(.gray)
                }
                
                Spacer()
            }
            .font(.subheadline)
        }
        .padding()
        .background(Color(.systemBackground))
        .cornerRadius(16)
        .shadow(color: .black.opacity(0.1), radius: 5, x: 0, y: 2)
    }
}

struct SearchView: View {
    @State private var searchText = ""
    @State private var searchResults: [UserItem] = []
    
    var body: some View {
        NavigationView {
            VStack {
                if searchResults.isEmpty {
                    ContentUnavailableView(
                        "Suche nach Benutzern",
                        systemImage: "magnifyingglass",
                        description: Text("Finde Freunde und neue Leute")
                    )
                } else {
                    List(searchResults) { user in
                        UserRow(user: user)
                    }
                }
            }
            .navigationTitle("Suchen")
            .searchable(text: $searchText, prompt: "Benutzer suchen...")
        }
    }
}

struct UserItem: Identifiable {
    let id: Int
    let username: String
    let displayName: String?
    let profileImageUrl: String?
}

struct UserRow: View {
    let user: UserItem
    
    var body: some View {
        HStack {
            Circle()
                .fill(Color.gray.opacity(0.3))
                .frame(width: 50, height: 50)
            
            VStack(alignment: .leading) {
                Text(user.displayName ?? user.username)
                    .fontWeight(.semibold)
                Text("@\(user.username)")
                    .font(.caption)
                    .foregroundColor(.gray)
            }
        }
    }
}

struct NotificationsView: View {
    var body: some View {
        NavigationView {
            VStack(spacing: 16) {
                Image(systemName: "bell.slash")
                    .font(.system(size: 60))
                    .foregroundColor(.gray)
                Text("Keine Mitteilungen")
                    .font(.title2)
                    .fontWeight(.semibold)
                Text("Du hast keine neuen Benachrichtigungen")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
            }
            .padding()
            .navigationTitle("Mitteilungen")
        }
    }
}

struct ProfileView: View {
    let username: String
    let onLogout: () -> Void
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 20) {
                    // Profile Header
                    VStack {
                        Circle()
                            .fill(Color.gray.opacity(0.3))
                            .frame(width: 100, height: 100)
                        
                        Text("Benutzer")
                            .font(.title2)
                            .fontWeight(.bold)
                        
                        Text("@\(username.isEmpty ? "username" : username)")
                            .foregroundColor(.gray)
                    }
                    .padding()
                    
                    // Stats
                    HStack(spacing: 40) {
                        StatItem(value: "0", label: "Beiträge")
                        StatItem(value: "0", label: "Follower")
                        StatItem(value: "0", label: "Folge ich")
                    }
                    
                    // Edit Profile Button
                    Button(action: {}) {
                        Text("Profil bearbeiten")
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(Color.gray.opacity(0.2))
                            .cornerRadius(10)
                    }
                    .padding(.horizontal)
                    
                    Divider()
                    
                    // Posts Grid Placeholder
                    Text("Keine Beiträge")
                        .foregroundColor(.gray)
                        .padding(.top, 40)
                }
            }
            .navigationTitle("Profil")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Menu {
                        Button("Abmelden", role: .destructive, action: onLogout)
                    } label: {
                        Image(systemName: "gearshape")
                    }
                }
            }
        }
    }
}

struct StatItem: View {
    let value: String
    let label: String
    
    var body: some View {
        VStack {
            Text(value)
                .font(.title2)
                .fontWeight(.bold)
            Text(label)
                .font(.caption)
                .foregroundColor(.gray)
        }
    }
}

#Preview {
    ContentView()
}
