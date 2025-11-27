package com.team.mediguide;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminHelper {
    
    private static final String ADMIN_USER_ID = "ScXFFGFOo1aEYtOYibji6EeHLdg1";
    private static Boolean isAdminCached = null;
    
    public interface AdminCheckCallback {
        void onResult(boolean isAdmin);
    }
    
    /**
     * Check if current user is admin (uses hardcoded admin ID for simplicity)
     */
    public static boolean isAdmin() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return false;
        }
        
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        return ADMIN_USER_ID.equals(currentUserId);
    }
    
    /**
     * Async check if current user is admin using Firestore
     * (Alternative method if you want to use Firestore Admins collection in the future)
     */
    public static void checkAdminStatus(AdminCheckCallback callback) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            callback.onResult(false);
            return;
        }
        
        // Use cached result if available
        if (isAdminCached != null) {
            callback.onResult(isAdminCached);
            return;
        }
        
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        FirebaseFirestore.getInstance()
                .collection("Admins")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    boolean isAdmin = documentSnapshot.exists() && 
                                     Boolean.TRUE.equals(documentSnapshot.getBoolean("isAdmin"));
                    isAdminCached = isAdmin;
                    callback.onResult(isAdmin);
                })
                .addOnFailureListener(e -> {
                    // Fallback to hardcoded check
                    boolean isAdmin = ADMIN_USER_ID.equals(userId);
                    isAdminCached = isAdmin;
                    callback.onResult(isAdmin);
                });
    }
    
    /**
     * Clear cached admin status (call on logout)
     */
    public static void clearCache() {
        isAdminCached = null;
    }
}
