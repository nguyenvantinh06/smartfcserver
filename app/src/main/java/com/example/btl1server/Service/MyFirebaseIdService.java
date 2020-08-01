package com.example.btl1server.Service;

import com.example.btl1server.Common.Common;
import com.example.btl1server.Model.Token;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.FileReader;

public class MyFirebaseIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        updateToServer(refreshedToken);
    }

    private void updateToServer(String refreshedToken) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token = new Token(refreshedToken, true);
        tokens.child(Common.currentUser.getPhone()).setValue(token);
    }
}
